// 4. 기관총을 발사하여 적을 공격하는 능력. 총알이 있으면 무조건 적군 전체 데미지, 총알이 없으면 그냥 넘어감 (매 라운드마다 능력 사용 여부 초기화)
// todo 수정 필요
package characters;

import server.ClientHandler;
import server.MafiaServer;

public class Character4 extends CharacterTemplate {

    protected boolean isReady; // 능력 발동 여부
    CharacterTemplate[] abilityTargetCharacters; // todo 사라질 예정인 변수

    public Character4(String name, String team) {
        super(name, team, "발사 후 아군 전체 또는 적군 전체에게 데미지");
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
        setAbilityUsed(true);
        isReady = true;
        result.append(name).append("은(는) 능력을 사용하여 적 전체를 타겟으로 설정했습니다.\n");
        return result.toString();
    }

    @Override
    public String shoot(CharacterTemplate target) {
        StringBuilder result = new StringBuilder();

        if (health <= 0) {
            result.append(name).append("은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.\n");
            return result.toString();
        }

        if (!isReady) {
            result.append(name).append("이(가) ").append(target.getName()).append("에게 총을 발사했습니다!\n");
            result.append(target.receiveDamage());
        } else {
            result.append(name).append("이(가) 기관총을 발사하여 적 전체를 공격했습니다!\n");
            // todo 이 부분에 target 가지고 상대팀 배열 추출하도록 수정 필요
            for (ClientHandler clientHandler : MafiaServer.clients) {
                CharacterTemplate enemyCharacter = clientHandler.getCharacter();

                // 적군인지 확인
                if (!enemyCharacter.getTeam().equals(this.team) && enemyCharacter.isAlive()) {
                    result.append(enemyCharacter.receiveDamage()).append("\n");
                }
            }
        }
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