import org.apache.log4j.Logger;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger log = Logger.getLogger(Server.class);
    private List<Connection> connections =
            Collections.synchronizedList(new ArrayList<Connection>());
    private List<Connection> connectionAgent =
            Collections.synchronizedList(new ArrayList<Connection>());
    private List<Connection> connectionClient =
            Collections.synchronizedList(new ArrayList<Connection>());
    private ExecutorService clientExecutor = Executors.newFixedThreadPool(6);

    private ServerSocket serverSocket;

    public Server(ServerSocket s) throws IOException {
        log.info("Server started");
        serverSocket = s;
        synchronized (connections) {
            while (true) {
                Socket socket = serverSocket.accept();
                addConnection(socket);
            }
        }
    }

    public void addConnection(Socket socket) {
        Connection con = new Server.Connection(socket);
        synchronized (connections) {
            this.connections.add(con);
        }
        clientExecutor.execute(con);
    }

    public static void main(String[] args) {
        // Server server1 = new Server();
        try {


            Server server1 = new Server(new ServerSocket(8000));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeAll() {
        try {
            serverSocket.close();
            synchronized (connections) {
                Iterator<Connection> iter = connections.iterator();
                while (iter.hasNext()) {
                    ((Connection) iter.next()).close();
                }
            }
        } catch (Exception e) {
            System.err.println("Threads didn't closed!");
        }
    }

    private class Connection implements Runnable {
        private BufferedReader in;
        private PrintWriter out;
        private Socket socket;

        private String name = "";
        private Connection partner;
        private boolean pair = false;
        private boolean isClient = false;
        private ArrayList<String> messageStr = new ArrayList<String>();

        public Connection(Socket socket) {
            this.socket = socket;
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
                this.out.println("/endThread");
                if (this.partner != null) {
                    log.info("The end of conversation " + this.name + " with " + this.partner.name);

                    partner.out.println("The end of conversation.");
                }

                if (partner != null) {
                    partner.pair = false;
                    if (isClient) {
                        editAgent(partner, false);
                    } else {
                        editClient(partner, false);
                    }
                    partner.tryToFindPartner();
                }

                synchronized (connections) {
                    connections.remove(this);
                }


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connections.size() == 0) {
                    closeAll();
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
            if (!this.pair && isClient) {
                if (connectionAgent.size() != 0) {
                    tryToFindPartner();
                } else {
                    messageStr.add(str);
                }
            }
            if (this.pair)
                partner.out.println(name + ": " + str);
            return true;
        }

        public void clientLeave() {
            editAgent(this.partner, false);
            editClient(this, false);
            log.info("Client " + this.name + " leaved conversation.");
            this.pair = false;
            synchronized (this.partner) {
                this.partner.pair = false;
                this.partner.tryToFindPartner();
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
                log.info("Registration client " + name);
            } else {
                log.info("Registration agent " + name);
                editAgent(this, false);
                if (connectionClient.size() != 0) {
                    tryToFindPartner();
                }
            }
        }

        public void editClient(Connection client, boolean isRemove) {
            synchronized (connectionClient) {
                if (isRemove) connectionClient.remove(client);
                else
                    connectionClient.add(client);
            }
        }

        public void editAgent(Connection agent, boolean isRemove) {
            synchronized (connectionAgent) {
                if (isRemove) connectionAgent.remove(agent);
                else
                    connectionAgent.add(agent);
            }
        }

        public void tryToFindPartner() {
            if (!isClient && connectionClient.size() != 0) {
                Connection client;
                synchronized (connectionClient) {
                    client = connectionClient.iterator().next();
                    connectionClient.remove(0);
                }
                editAgent(this, true);
                partner = client;
                client.partner = this;
                this.pair = true;
                client.pair = true;
                this.out.println("Conversation with client " + client.name);
                client.out.println("Conversation with agent " + this.name);
                if (!client.messageStr.isEmpty()) {
                    for (String message : client.messageStr) {
                        this.out.println(partner.name + ": " + message);
                    }
                    client.messageStr.clear();
                }
            } else if (isClient && connectionAgent.size() != 0) {
                Connection agent;
                synchronized (connectionAgent) {
                    agent = connectionAgent.iterator().next();
                    connectionAgent.remove(0);
                }
                editClient(this, true);
                partner = agent;
                agent.partner = this;
                agent.pair = true;
                this.pair = true;
                agent.out.println("Conversation with client " + this.name);
                this.out.println("Conversation with agent " + agent.name);
                if (!messageStr.isEmpty()) {
                    for (String message : messageStr) {
                        partner.out.println(this.name + ": " + message);
                    }
                    messageStr.clear();
                }
            }
            log.info("Conversation of " + this.name + " with " + this.partner.name);
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
    }
}
