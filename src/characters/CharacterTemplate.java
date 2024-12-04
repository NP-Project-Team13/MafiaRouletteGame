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
        if (health <= 0) {
            result.append(name).append("은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.");
            return result.toString();
        }
        result.append(name).append("이(가) ").append(target.getName()).append("에게 총을 발사했습니다!");
        result.append(target.receiveDamage());
        return result.toString();
    }

    // 데미지 받기 (shoot 당하면 호출되는 메서드)
    public String receiveDamage() {
        StringBuilder result = new StringBuilder();
        result.append(name).append("이(가) 데미지를 받았습니다.");
        result.append(decreaseHealth());
        return result.toString();
    }

    // 체력 감소 (receiveDamage 에서 호출되는 메서드 - 각 캐릭터의 능력에 따라 호출되지 않을 수 있음)
    public String decreaseHealth() {
        StringBuilder result = new StringBuilder();
        health--;
        result.append(name).append("의 체력이 1 감소했습니다. 남은 체력: ").append(health);
        if (health <= 0) {
            result.append(name).append("은(는) 사망했습니다.");
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
