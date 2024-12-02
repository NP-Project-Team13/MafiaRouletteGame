// 7. 무작위로 생존 여부가 결정되는 "대박 아니면 쪽박" 능력 (나 아니면 너 OUT → 한명을 지목, 50:50 확률로 둘 중 한 명은 사망)
// todo 구현 완료
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
            result.append(name).append("은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.");
            return result.toString();
        }
        if(!isReady){
            result.append(name).append("이(가) ").append(target.getName()).append("에게 총을 발사했습니다!\n");
            result.append(target.receiveDamage());
        } else {
            result.append("\n").append(name).append("은(는) ").append(target.getName())
                    .append("과(와) 함께 대박 아니면 쪽박 능력이 적용됩니다.\n");
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
    public String useAbility() {
        StringBuilder result = new StringBuilder();
        setAbilityUsed(true);
        result.append(name).append("은(는) 대박 아니면 쪽박 능력을 사용할 준비가 되었습니다.\n");
        return result.toString();
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
