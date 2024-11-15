import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class MafiaServer {
    private static final int PORT = 12345;
    static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    public static Map<String, Player> players = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for players...");

            Socket[] clientSocket = new Socket[4];
            BufferedReader[] inStreams = new BufferedReader[4];
            PrintWriter[] outStreams = new PrintWriter[4];

            // 4명의 플레이어 접속 대기
            for (int i = 0; i < clientSocket.length; i++) {
                clientSocket[i] = serverSocket.accept();
                inStreams[i] = new BufferedReader(new InputStreamReader(clientSocket[i].getInputStream()));
                outStreams[i] = new PrintWriter(clientSocket[i].getOutputStream(), true);
                outStreams[i].println("Welcome Player " + (i + 1) + "!");
                System.out.println("Player " + (i + 1) + " connected.");
                ClientHandler clientHandler = new ClientHandler(clientSocket[i]);
                clients.add(clientHandler);
                clientHandler.start();
            }

//            while (true) {
//                Socket clientSocket = serverSocket.accept();
//                ClientHandler clientHandler = new ClientHandler(clientSocket);
//                clients.add(clientHandler);
//                clientHandler.start();
//                System.out.println("New player connected.");
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 모든 클라이언트에 메시지 브로드캐스트
    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
