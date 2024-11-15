public class ServerResponse {
    String action;
    String initiatingPlayer;
    String targetPlayer;
    String result;
    int targetHealth;
    String message;

    public ServerResponse(String action, String initiatingPlayer, String targetPlayer, String result, int targetHealth, String message) {
        this.action = action;
        this.initiatingPlayer = initiatingPlayer;
        this.targetPlayer = targetPlayer;
        this.result = result;
        this.targetHealth = targetHealth;
        this.message = message;
    }

}
