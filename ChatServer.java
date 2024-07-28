import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientThread> clientThreads = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(clientSocket);
                clientThreads.add(clientThread);
                new Thread(clientThread).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastMessage(String message, ClientThread excludeClient) {
        for (ClientThread client : clientThreads) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    static class ClientThread implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String nickname;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request nickname
                out.println("Enter your nickname:");
                nickname = in.readLine();
                System.out.println(nickname + " has joined the chat.");
                broadcastMessage(nickname + " has joined the chat.", this);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(nickname + ": " + message);
                    broadcastMessage(nickname + ": " + message, this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientThreads.remove(this);
                broadcastMessage(nickname + " has left the chat.", null);
                System.out.println(nickname + " has disconnected.");
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
