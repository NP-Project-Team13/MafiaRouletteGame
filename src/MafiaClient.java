import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MafiaClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println(in.readLine()); // 서버 환영 메시지 출력

            while (true) {
                System.out.print("Enter target player (e.g., Player2): ");
                String target = scanner.nextLine();

                // JSON 요청 생성
                ClientAction action = new ClientAction("shoot", target);
                String actionJson = JsonUtil.actionToJson(action);
                out.println(actionJson);

                // 서버로부터 JSON 응답 수신
                String response = in.readLine();
                ServerResponse serverResponse = JsonUtil.jsonToResponse(response);
                System.out.println("Server: " + serverResponse.message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
