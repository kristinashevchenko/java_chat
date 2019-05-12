package servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.MultipartConfig;


@MultipartConfig
public class AddServlet extends HttpServlet {
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private List<String> messages;
    private AddThread addThread = null;

    @Override
    public void
    init() throws ServletException {
        super.init();
        try {
            socket = new Socket("127.0.0.1", 8000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            messages = new ArrayList<String>();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    @Override
    public void destroy() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Threads didn't closed!");
        }
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = req.getRequestDispatcher("views/add.jsp");
        requestDispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message = req.getParameter("message");
        if (!checkInvalidInfo(message.split(" "))) {
            addThread = new AddThread();
            addThread.start();
        }
        if (addThread != null) {
            out.println(message);
            if (message.equals("/exit")) {
                addThread.setStop();
            } else {
                messages.add(message);
                ServletContext ctx=getServletContext();
                ctx.setAttribute("messages", messages);
                RequestDispatcher requestDispatcher = req.getRequestDispatcher("views/add.jsp");
                requestDispatcher.forward(req, resp);
            }
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
                    messages.add(str);
                    getServletContext().setAttribute("messages",messages);
                }
            } catch (IOException e) {
                System.err.println("Error in message recieving");

            }
        }
    }
}

