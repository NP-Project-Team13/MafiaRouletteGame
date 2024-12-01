
public class ServerResponse {
    private String action;
    private String initiatingPlayer;
    private String targetPlayer;
    private String result;
    private int targetHealth;
    private String message;

    public ServerResponse(String action, String initiatingPlayer, String targetPlayer, String result, int targetHealth, String message) {
        this.action = action;
        this.initiatingPlayer = initiatingPlayer;
        this.targetPlayer = targetPlayer;
        this.result = result;
        this.targetHealth = targetHealth;
        this.message = message;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getInitiatingPlayer() {
        return initiatingPlayer;
    }

    public void setInitiatingPlayer(String initiatingPlayer) {
        this.initiatingPlayer = initiatingPlayer;
    }

    public String getTargetPlayer() {
        return targetPlayer;
    }

    public void setTargetPlayer(String targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getTargetHealth() {
        return targetHealth;
    }

    public void setTargetHealth(int targetHealth) {
        this.targetHealth = targetHealth;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
