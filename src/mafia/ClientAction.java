package mafia;

public class ClientAction {
    private String action; // "shoot" 또는 "useAbility"
    private String target; // 타겟 플레이어 닉네임

    public ClientAction(String action, String target) {
        this.action = action;
        this.target = target;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}

