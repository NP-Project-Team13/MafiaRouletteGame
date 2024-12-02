// 1. 총알 위치를 확인하는 능력 (매 라운드마다 능력 사용 여부 초기화)
// todo 수정 필요
package characters;

public class Character1 extends CharacterTemplate {

    public Character1(String name, String team) {
        super(name, team, "총알 위치를 확인하는 능력");
    }

    // todo 외부에서 총알 위치 확인 로직 구현 필요
    @Override
    public String useAbility() {
        StringBuilder result = new StringBuilder();

        if (health <= 0) {
            result.append(name).append("은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.\n");
            return result.toString();
        }
        if (isAbilityUsed) {
            result.append(name).append("은(는) 이미 이번 라운드에서 능력을 사용했습니다.\n");
            return result.toString();
        }
        result.append(name).append("은(는) 총알 위치를 확인했습니다.\n");
        setAbilityUsed(true);
        return result.toString();
    }

    @Override
    public String resetRound() {
        StringBuilder result = new StringBuilder();
        isAbilityUsed = false;
        result.append(name).append("의 능력 사용 가능 상태가 초기화되었습니다.\n");
        return result.toString();
    }
}
