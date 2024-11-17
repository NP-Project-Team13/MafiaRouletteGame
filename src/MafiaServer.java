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
    static final int MAX_PLAYERS = 4;
    static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    public static Map<String, Player> players = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for players...");

            while (clients.size() < MAX_PLAYERS) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
                System.out.println("New player connected.");
            }

            startGame();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startGame() {
        System.out.println("All players connected. Starting the game...");
        broadcast("Game Started!\n");

        for (int round = 0; round < 4; round++) { // 4번의 라운드
            for (ClientHandler client : clients) {
                try {
                    System.out.println(client.getPlayerId() + "'s turn. Selecting the target...");
                    client.sendMessage("Your Turn, choose target player (e.g., Player2):");
                    // 클라이언트로부터 JSON 요청 수신
                    String message = null;
                    while((message = client.receiveMessage()) != null) {
                        if(message.startsWith("TE")){
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        broadcast("Game End\n");
    }

    // 모든 클라이언트에 메시지 브로드캐스트
    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
