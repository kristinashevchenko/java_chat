import org.apache.log4j.Logger;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final Logger log = Logger.getLogger(Server.class);
    public static List<Connection> connections =
            Collections.synchronizedList(new ArrayList<Connection>());
    public static List<Connection> connectionAgent =
            Collections.synchronizedList(new ArrayList<Connection>());
    public static List<Connection> connectionClient =
            Collections.synchronizedList(new ArrayList<Connection>());
    public static List<ChatRoom> rooms = Collections.synchronizedList(new ArrayList<ChatRoom>());
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
        Connection con = new Connection(socket);
        synchronized (connections) {
            this.connections.add(con);
        }
        clientExecutor.execute(con);
    }

    public static void main(String[] args) {
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
}
