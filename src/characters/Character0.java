package characters;

import java.io.Serializable;

public abstract class Character0 implements Serializable {
    protected int health;
    protected String name;
    protected boolean isAbilityUsed;
    protected String team;
    protected String info;

    public Character0(String name, String team, String info) {
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

    public abstract void useAbility(Character0... targets);

    // 데미지 받기
    public void receiveDamage() {
        System.out.println(name + "이(가) 데미지를 받았습니다.");
        decreaseHealth();
    }

    // 체력 감소
    public void decreaseHealth() {
        health--;
        System.out.println(name + "의 체력이 1 감소했습니다. 남은 체력: " + health);
        if (health <= 0) {
            System.out.println(name + "은(는) 사망했습니다.");
        }
    }

    // 총 쏘기 (이 메서드는 총알이 있을 경우에만 호출된다고 가정)
    public void shoot(Character0 target) {
        if (health <= 0) {
            System.out.println(name + "은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.");
            return;
        }
        System.out.println(name + "이(가) " + target.getName() + "에게 총을 발사했습니다!");
        target.receiveDamage();
    }

    public abstract void resetRound();

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

}
