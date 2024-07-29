import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;  // Port number for the server
    private static Set<ClientThread> clientThreads = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();  // Accept a new client connection
                ClientThread clientThread = new ClientThread(clientSocket);
                clientThreads.add(clientThread);  // Add the client thread to the list
                new Thread(clientThread).start();  // Start a new thread for the client
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast a message to all clients except the sender
    public static void broadcastMessage(String message, ClientThread excludeClient) {
        for (ClientThread client : clientThreads) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    // Inner class to handle client threads
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

                // Read nickname as the first message from the client
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
                    socket.close();  // Close the socket when done
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientThreads.remove(this);  // Remove the client thread from the list
                broadcastMessage(nickname + " has left the chat.", null);
                System.out.println(nickname + " has disconnected.");
            }
        }

        // Send a message to the client
        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
