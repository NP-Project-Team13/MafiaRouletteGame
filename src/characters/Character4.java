// 4. 기관총을 발사하여 적을 공격하는 능력. 총알이 있으면 무조건 적군 전체 데미지, 총알이 없으면 그냥 넘어감 (매 라운드마다 능력 사용 여부 초기화)
package characters;

import server.ClientHandler;
import server.MafiaServer;

public class Character4 extends CharacterTemplate {

    protected boolean isReady; // 능력 발동 여부

    public Character4(String name, String team) {
        super(name, team, "적군 전체에게 데미지 능력");
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
        setAbilityUsed(true);
        isReady = true;
        result.append(name).append("님은 능력을 사용하여 적 전체를 타겟으로 설정했습니다.\n");
        return result.toString();
    }

    @Override
    public String shoot(CharacterTemplate target) {
        StringBuilder result = new StringBuilder();
        if (health <= 0) { // 자신
            result.append(name).append("님은 이미 사망했기 때문에 총을 쏠 수 없습니다.\n");
            return result.toString();
        }
        if (target.getHealth() <= 0) { // 상대
            result.append(target.getName()).append("님은 이미 사망했기 때문에 총을 쏠 수 없습니다.\n");
            return result.toString();
        }
        if (!isReady) {
            result.append(name).append("님이 ").append(target.getName()).append("님에게 총을 발사하여 적중시켰습니다!✅\n");
            result.append(target.receiveDamage());
        } else {
            result.append(name).append("님이 기관총을 발사하여 적 전체를 공격했습니다!\n");
            for (ClientHandler clientHandler : MafiaServer.clients) {
                CharacterTemplate enemyCharacter = clientHandler.getCharacter();

                // 적군인지 확인
                if (!enemyCharacter.getTeam().equals(this.team) && enemyCharacter.isAlive()) {
                    result.append(enemyCharacter.receiveDamage());
                }
            }
            isReady = false;
        }
        return result.toString();
    }

    @Override
    public String resetRound() {
        StringBuilder result = new StringBuilder();
        isAbilityUsed = false;
        isReady = false;
        result.append(name).append("님의 능력 사용 가능 상태가 초기화되었습니다.\n");
        return result.toString();
    }
}