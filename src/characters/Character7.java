// 7. 무작위로 생존 여부가 결정되는 "대박 아니면 쪽박" 능력 (나 아니면 너 OUT → 한명을 지목, 50:50 확률로 둘 중 한 명은 사망)
package characters;

import java.util.Random;

public class Character7 extends CharacterTemplate {

    protected boolean isReady = false; // 능력 발동 여부

    public Character7(String name, String team) {
        super(name, team, "나 아니면 너 OUT");
    }

    @Override
    public String shoot(CharacterTemplate target) {
        StringBuilder result = new StringBuilder();

        if (health <= 0) {
            result.append(name).append("은(는) 이미 사망했기 때문에 능력을 사용할 수 없습니다.\n");
            return result.toString();
        }
        result.append(name).append("이(가) ").append(target.getName()).append("에게 총을 발사했습니다!\n");
        result.append(target.receiveDamage());

        if (isReady) {
            result.append("\n").append(name).append("은(는) ").append(target.getName())
                    .append("과(와) 함께 대박 아니면 쪽박 능력을 사용합니다.\n");
            if (new Random().nextBoolean()) {
                result.append("\n").append(name).append("은(는) 죽음의 도박에서 살아남았습니다.\n")
                        .append(target.getName()).append("은(는) 사망하였습니다.\n");
                target.health = 0;
            } else {
                result.append("\n").append(target.getName()).append("은(는) 죽음의 도박에서 살아남았습니다.\n")
                        .append(name).append("은(는) 사망하였습니다.\n");
                this.health = 0;
            }
        }
        return result.toString();
    }

    @Override
    public String useAbility() { // 추후 능력에 맞게 코드 수정 필요
        StringBuilder result = new StringBuilder();

        if (health <= 0) {
            result.append(name).append("은(는) 이미 사망했기 때문에 능력을 사용할 수 없습니다.\n");
            return result.toString();
        }
        if (isAbilityUsed) {
            result.append(name).append("은(는) 이미 이번 라운드에서 능력을 사용했습니다.\n");
            return result.toString();
        }
        isReady = true;
        setAbilityUsed(true);
        result.append(name).append("은(는) 대박 아니면 쪽박 능력을 준비했습니다.\n");
        return result.toString();
    }

    @Override
    public String resetRound() {
        StringBuilder result = new StringBuilder();
        isAbilityUsed = false;
        isReady = false;
        result.append(name).append("의 라운드 상태가 초기화되었습니다.\n");
        return result.toString();
    }
}
