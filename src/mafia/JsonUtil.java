package mafia;
public class JsonUtil {
    // 클라이언트의 행동을 JSON 문자열로 변환
    public static String actionToJson(ClientAction action) {
        return "{ \"action\": \"" + action.getAction() + "\", \"target\": \"" + action.getTarget() + "\" }";
    }

    // JSON 문자열을 `ClientAction` 객체로 변환
    public static ClientAction jsonToAction(String json) {
        String[] parts = json.replace("{", "").replace("}", "").replace("\"", "").split(",");
        String actionType = parts[0].split(":")[1].trim();
        String target = parts[1].split(":")[1].trim();
        return new ClientAction(actionType, target);
    }

    // 서버의 응답을 JSON 문자열로 변환
    public static String responseToJson(ServerResponse response) {
        return "{ \"action\": \"" + response.getAction() + "\", \"initiating_player\": \"" + response.getInitiatingPlayer() + "\", " +
                "\"target_player\": \"" + response.getTargetPlayer() + "\", \"result\": \"" + response.getResult() + "\", " +
                "\"target_health\": " + response.getTargetHealth() + ", \"message\": \"" + response.getMessage() + "\" }";
    }

    // JSON 문자열을 `ServerResponse` 객체로 변환
    public static ServerResponse jsonToResponse(String json) {
        String[] parts = json.replace("{", "").replace("}", "").replace("\"", "").split(",");
        String action = parts[0].split(":")[1].trim();
        String initiatingPlayer = parts[1].split(":")[1].trim();
        String targetPlayer = parts[2].split(":")[1].trim();
        String result = parts[3].split(":")[1].trim();
        int targetHealth = Integer.parseInt(parts[4].split(":")[1].trim());
        String message = parts[5].split(":")[1].trim();
        return new ServerResponse(action, initiatingPlayer, targetPlayer, result, targetHealth, message);
    }
}

