import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class Connection implements Runnable {
    private BufferedReader in;
    public PrintWriter out;
    private Socket socket;
    public ChatRoom room;
    public String name = "";
    private boolean isClient = false;
    private String id;
    private ArrayList<String> messageStr = new ArrayList<String>();

    public Connection(Socket socket) {
        this.socket = socket;
        this.id = Long.toString((new Date()).getTime());
        try {
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    public void run() {
        try {
            String startLine = in.readLine();
            initUser(startLine.split(" "));
            startConversation();

            this.out.println("The end of conversation.");
            if (this.room != null) {
                Server.log.info("The end of conversation " + this.name);
                this.room.printMessage(this, "The end of conversation.");
            }

            if (room != null) {
                Connection partner = room.leaveChatRoom(this);
                if (partner != null) {
                    if (isClient) {
                        editAgent(partner, false);
                    } else {
                        editClient(partner, false);
                    }

                    partner.tryToFindPartner();
                }
            }

            synchronized (Server.connections) {
                Server.connections.remove(this);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (Server.connections.size() == 0) {
                System.exit(0);
            }
            close();
        }
    }

    public void startConversation() throws IOException {
        String str = "";
        while (true) {
            str = in.readLine();
            if (!nextStepConv(str))
                break;

        }
    }

    public boolean nextStepConv(String str) {
        if (str.equals("/exit")) return false;
        if (str.equals("/leave") && isClient) clientLeave();
        if (this.room == null && isClient) {//(!this.pair
            if (Server.connectionAgent.size() != 0) {
                tryToFindPartner();
            } else {
                messageStr.add(str);
            }
        }
        if (this.room != null) {
            Server.log.info("next");
            this.room.printMessage(this, str);
        }

        return true;
    }

    public void clientLeave() {
        Connection partner = this.room.leaveChatRoom(this);
        if (partner != null) {
            editAgent(partner, false);
            editClient(this, false);
            Server.log.info("Client " + this.name + " leaved conversation.");
            synchronized (partner) {
                partner.tryToFindPartner();
            }
        }
    }

    public void initUser(String[] arrStr) {
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < arrStr.length; i++) {
            builder.append(arrStr[i]);
        }
        name = builder.toString();

        if (arrStr[1].equals("client")) {
            editClient(this, false);
            isClient = true;
            Server.log.info("Registration client " + name);
        } else {
            Server.log.info("Registration agent " + name);
            editAgent(this, false);
            if (Server.connectionClient.size() != 0) {
                tryToFindPartner();
            }
        }
    }

    public void editClient(Connection client, boolean isRemove) {
        synchronized (Server.connectionClient) {
            if (isRemove) Server.connectionClient.remove(client);
            else
                Server.connectionClient.add(client);
        }
    }

    public void editAgent(Connection agent, boolean isRemove) {
        synchronized (Server.connectionAgent) {
            if (isRemove) Server.connectionAgent.remove(agent);
            else
                Server.connectionAgent.add(agent);
        }
    }

    public void tryToFindPartner() {
        if (!isClient && Server.connectionClient.size() != 0) {
            Connection client;
            synchronized (Server.connectionClient) {
                client = Server.connectionClient.iterator().next();
                Server.connectionClient.remove(0);
            }
            editAgent(this, true);
            this.room = new ChatRoom(this, client);
            this.room.addToPartners(this);
            Server.rooms.add(room);
            this.out.println("Conversation with client " + client.name);
            this.room.printMessage(this, "Conversation with agent " + this.name);
            if (!client.messageStr.isEmpty()) {
                for (String message : client.messageStr) {
                    this.out.println(client.name + ": " + message);
                }
                client.messageStr.clear();
            }

        } else if (isClient && Server.connectionAgent.size() != 0) {
            Connection agent;
            synchronized (Server.connectionAgent) {
                agent = Server.connectionAgent.iterator().next();
                Server.connectionAgent.remove(0);
            }
            editClient(this, true);
            this.room = new ChatRoom(this, agent);
            this.room.addToPartners(this);
            Server.rooms.add(room);
            this.room.printMessage(this, "Conversation with client " + this.name);
            this.out.println("Conversation with agent " + agent.name);
            if (!messageStr.isEmpty()) {
                for (String message : messageStr) {
                    this.room.printMessage(this, message);
                }
                messageStr.clear();
            }

        }
        Server.log.info("Conversation of " + this.name);
    }

    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Threads didn't closed!");
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this.id.equals(((Connection) obj).id);
    }
}
