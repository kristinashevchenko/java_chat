import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;


    public static void main(String[] args) {
        Client cl = new Client();
    }

    public Client() {
        try {
            socket = new Socket("127.0.0.1", 8000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scan = new Scanner(System.in);

            System.out.println("Enter /register client(agent) name");

            String str1 = scan.nextLine();
            String[] arr = str1.split(" ");
            while (checkInvalidInfo(arr)) {
                str1 = scan.nextLine();
                arr = str1.split(" ");
            }
            out.println(str1);


            AddThread addThread = new AddThread();
            addThread.start();

            String str = "";
            while (!str.equals("/exit")) {
                str = scan.nextLine();
                out.println(str);
            }
            addThread.setStop();

        } catch (Exception e) {
            System.err.println("Connection error.");
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public static boolean checkInvalidInfo(String[] arr) {
        if (arr.length >= 3) {
            if (!arr[0].equals("/register")) return true;
            if (!arr[1].equals("agent") && !arr[1].equals("client")) return true;
            return false;
        }
        return true;
    }

    private void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Threads didn't closed!");
        }
    }

    private class AddThread extends Thread {
        private boolean stoped;

        public void setStop() {
            stoped = true;
        }

        @Override
        public void run() {
            try {
                while (!stoped) {
                    String str = in.readLine();
                    System.out.println(str);
                }
            } catch (IOException e) {
                System.err.println("Error in message recieving");
            }
        }
    }
}