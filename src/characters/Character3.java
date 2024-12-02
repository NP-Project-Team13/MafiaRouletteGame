// 3. 순서 변경 능력 (후순위 기능으루 미룸, 구현 필요 X)
package characters;

public class Character3 extends CharacterTemplate {

    public Character3(String name, String team) {
        super(name, team, "순서 변경 능력");
    }

    @Override
    public String useAbility(CharacterTemplate... targets) { // 외부에서 순서 교체 로직 필요
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
            result.append(name).append("은(는) 순서를 변경할 대상을 필요로 합니다.\n");
            return result.toString();
        }
        CharacterTemplate target = targets[0];
        result.append(name).append("은(는) ").append(target.getName()).append("과(와) 순서를 교체했습니다.\n");
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
