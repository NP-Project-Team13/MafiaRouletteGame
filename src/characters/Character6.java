// 6. 자신의 health -1 소모하여 아군을 health +1
package characters;

public class Character6 extends CharacterTemplate {

    private boolean isReady; // 힐 능력 사용 준비 여부 (true 가 되면 shoot 메서드는 공격이 아닌 힐 역할을 수행)

    public Character6(String name, String team) {
        super(name, team, "힐러 능력");
    }

    @Override
    public String useAbility() {
        StringBuilder result = new StringBuilder();

        if (health <= 0) {
            result.append(name).append("은(는) 이미 사망했기 때문에 능력을 사용할 수 없습니다.\n");
            return result.toString();
        }
        if (isAbilityUsed) {
            result.append(name).append("은(는) 이미 이번 라운드에서 능력을 사용했습니다.\n");
            return result.toString();
        }
        result.append(name).append("이(가) 자신의 체력을 소모하여 아군의 체력을 치유할 준비를 했습니다.\n");
        isReady = true;
        setAbilityUsed(true);
        return result.toString();
    }

    @Override
    public String shoot(CharacterTemplate target) {
        StringBuilder result = new StringBuilder();
        if (health <= 0) { // 자신
            result.append(name).append("은(는) 이미 사망했기 때문에 ").append(isReady ? "살릴 수 없습니다.\n" : "총을 쏠 수 없습니다.\n");
            return result.toString();
        }
        if (target.getHealth() <= 0) { // 상대
            result.append(target.getName()).append("은(는) 이미 사망했기 때문에 ").append(isReady ? "살릴 수 없습니다.\n" : "총을 쏠 수 없습니다.\n");
            return result.toString();
        }

        if (!isReady) { // 공격 역할
            result.append(name).append("이(가) ").append(target.getName()).append("에게 총을 발사하여 적중시켰습니다!✅\n")
                    .append(target.receiveDamage());
        } else { // 힐 역할
            if (health < 2) {
                result.append(name).append("은(는) 치유할 만큼 충분한 체력이 없습니다. (최소 2 이상의 체력 필요)\n");
            } else if (target.getHealth() < 3) {
                health--;
                target.health++;
                result.append(name).append("은(는) 자신의 체력을 1 소모하여 ").append(target.getName()).append("을(를) 1 치유했습니다.\n");
            } else {
                result.append(name).append("은(는) 이미 체력이 최대이기 때문에 치유할 수 없습니다.\n");
            }
            isReady = false;
        }

        return result.toString();
    }

    public boolean isReady() {
        return isReady;
    }

    @Override
    public String resetRound() {
        StringBuilder result = new StringBuilder();
        isAbilityUsed = false;
        isReady = false;
        result.append(name).append("의 능력 사용 가능 상태가 초기화되었습니다.\n");
        return result.toString();
    }
}

