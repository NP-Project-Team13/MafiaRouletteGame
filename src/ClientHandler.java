import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String playerId;
    private int health = 3;
    private static final int CYLINDER_SIZE = 5;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 플레이어 ID 생성 및 공유 자원에 등록
            playerId = "Player" + (MafiaServer.players.size() + 1);
            Player player = new Player(playerId, health);
            MafiaServer.players.put(playerId, player);

            out.println("Welcome " + playerId + "!");
            if(MafiaServer.clients.size() < MafiaServer.MAX_PLAYERS) {
                out.println("Waiting for other players...");
            }

            // 클라이언트로부터 JSON 요청 수신
            String message;
            while ((message = in.readLine()) != null) {
                if(message.startsWith("{")){
                    ClientAction action = JsonUtil.jsonToAction(message);
                    processAction(action);
                }
            }
        } catch (IOException e) {
            System.out.println("Connection with " + playerId + " closed.");
        } finally {
            try {
                socket.close();
                MafiaServer.players.remove(playerId);
                MafiaServer.clients.remove(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 액션 처리 메서드
    public void processAction(ClientAction action) {
        if ("shoot".equals(action.action)) {
            synchronized (MafiaServer.players) {
                Player targetPlayer = MafiaServer.players.get(action.target);
                if (targetPlayer != null && targetPlayer.getHealth() > 0) {
                    int shootResult = new Random().nextInt(CYLINDER_SIZE);
                    String result = (shootResult == 0) ? "hit" : "miss";
                    if ("hit".equals(result)) {
                        targetPlayer.decreaseHealth();
                    }

                    // 모든 클라이언트에 JSON 응답 전송
                    ServerResponse response = new ServerResponse(
                            "shoot", playerId, action.target, result, targetPlayer.getHealth(),
                            playerId + " shot " + action.target + ". " + action.target + "'s health is now " + targetPlayer.getHealth()
                    );
                    String jsonResponse = JsonUtil.responseToJson(response);
                    MafiaServer.broadcast(jsonResponse);
                }
            }
        }
    }

    // 클라이언트에게 메시지 전송
    public void sendMessage(String message) {
        out.println(message);
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public String getPlayerId() { return playerId; }
}
