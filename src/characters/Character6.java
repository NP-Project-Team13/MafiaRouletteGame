// 6. 자신의 health -1 소모하여 아군을 health +1
package characters;

public class Character6 extends CharacterTemplate {

    public Character6(String name, String team) {
        super(name, team, "자신의 health -1 소모하여 아군을 health +1");
    }

    @Override
    public String useAbility(CharacterTemplate... targets) {
        StringBuilder result = new StringBuilder();

        if (health <= 0) {
            result.append(name).append("은(는) 이미 사망했기 때문에 능력을 사용할 수 없습니다.\n");
            return result.toString();
        }
        if (isAbilityUsed) {
            result.append(name).append("은(는) 이미 이번 라운드에서 능력을 사용했습니다.\n");
            return result.toString();
        }
        if (targets.length < 1) {
            result.append(name).append("은(는) 치유할 대상을 필요로 합니다.\n");
            return result.toString();
        }
        CharacterTemplate target = targets[0];
        if (health > 1) {
            health--;
            target.health++;
            result.append(name).append("은(는) 자신의 생명을 1 소모하여 ")
                    .append(target.getName()).append("을(를) 치유했습니다.\n");
        } else {
            result.append(name).append("은(는) 치유할 만큼 충분한 체력이 없습니다.\n");
        }
        setAbilityUsed(true);
        return result.toString();
    }

    @Override
    public String resetRound() {
        StringBuilder result = new StringBuilder();
        isAbilityUsed = false;
        result.append(name).append("의 상태가 초기화되었습니다.\n");
        return result.toString();
    }
}
