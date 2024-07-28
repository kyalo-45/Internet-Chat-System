import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;

    public ChatClient(String nickname) {
        this.nickname = nickname;
    }

    public void start() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(new IncomingMessageHandler()).start();
            out.println(nickname);

            Scanner scanner = new Scanner(System.in);
            String message;
            while (!(message = scanner.nextLine()).equalsIgnoreCase("exit")) {
                out.println(message);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class IncomingMessageHandler implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your nickname: ");
        String nickname = scanner.nextLine();
        ChatClient client = new ChatClient(nickname);
        client.start();
    }
}
