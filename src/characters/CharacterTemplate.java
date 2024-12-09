package characters;

import java.io.Serializable;

public abstract class CharacterTemplate implements Serializable {
    protected int health;
    protected String name;
    protected boolean isAbilityUsed;
    protected String team;
    protected String info;

    public CharacterTemplate(String name, String team, String info) {
        this.health = 3;
        this.name = name;
        this.team = team;
        this.info = info;
        this.isAbilityUsed = false;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void resetAbilityUsage() {
        this.isAbilityUsed = false;
    }

    public abstract String useAbility();


    // 총 쏘기 (이 메서드는 총알이 있을 경우에만 호출된다고 가정)
    public String shoot(CharacterTemplate target) {
        StringBuilder result = new StringBuilder();
        if (health <= 0) { // 자신
            result.append(name).append("님은 이미 사망했기 때문에 총을 쏠 수 없습니다.\n");
            return result.toString();
        }
        if (target.getHealth() <= 0) { // 상대
            result.append(target.getName()).append("님은 이미 사망했기 때문에 총을 쏠 수 없습니다.\n");
            return result.toString();
        }
        result.append(name).append("님이 ").append(target.getName()).append("님에게 총을 발사하여 적중시켰습니다!✅\n");
        result.append(target.receiveDamage());
        return result.toString();
    }

    // 데미지 받기 (shoot 당하면 호출되는 메서드)
    public String receiveDamage() {
        StringBuilder result = new StringBuilder();
        result.append(name).append("님이 데미지를 받았습니다.\n");
        result.append(decreaseHealth());
        return result.toString();
    }

    // 체력 감소 (receiveDamage 에서 호출되는 메서드 - 각 캐릭터의 능력에 따라 호출되지 않을 수 있음)
    public String decreaseHealth() {
        StringBuilder result = new StringBuilder();
        if(this.health > 0){
            health--;
            result.append("🩸").append(name).append("님의 체력 1 감소 → 남은 체력: ").append(health).append("\n");
        }
        if (health <= 0) {
            result.append(name).append("님은 사망했습니다☠️.\n");
        }
        return result.toString();
    }



    public abstract String resetRound();

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAbilityUsed() {
        return isAbilityUsed;
    }

    public void setAbilityUsed(boolean abilityUsed) {
        isAbilityUsed = abilityUsed;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return name;
    }
}
