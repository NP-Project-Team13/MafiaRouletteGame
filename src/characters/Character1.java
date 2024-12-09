// 1. 총알의 위치를 확인하는 능력
package characters;

import java.util.List;
import resources.Gun;

public class Character1 extends CharacterTemplate {

    public Character1(String name, String team) {
        super(name, team, "총알 위치 확인 능력");
    }

    @Override
    public String useAbility() {
        StringBuilder result = new StringBuilder();

        if (health <= 0) {
            result.append(name).append("님은 이미 사망했기 때문에 능력을 사용할 수 없습니다.\n");
            return result.toString();
        }
        if (isAbilityUsed) {
            result.append(name).append("님은 이미 이번 라운드에서 능력을 사용했습니다.\n");
            return result.toString();
        }

        // 현재 슬롯의 총알 상태 확인
        List<Boolean> chambers = Gun.getChambers();
        result.append(name).append("님은 현재 슬롯의 총알 상태를 확인했습니다:\n");
        for (int i = 0; i < chambers.size(); i++) {
            result.append("슬롯 ").append(i + 1).append(": ").append(chambers.get(i) ? "총알 있음💥" : "빈 슬롯").append("\n");
        }

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