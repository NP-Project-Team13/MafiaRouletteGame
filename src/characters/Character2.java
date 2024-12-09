// 2. 총알을 튕겨내는 방어 능력 (매 라운드마다 능력 사용 여부 초기화)
package characters;

public class Character2 extends CharacterTemplate {

    public boolean isReady; // 총알 튕겨낼 준비 되었는지

    public Character2(String name, String team) {
        super(name, team, "총알 튕겨내는 방어 능력");
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
        isReady = true;
        result.append(name).append("님은 다음 총알을 튕겨낼 준비가 되었습니다.\n");
        setAbilityUsed(true);
        return result.toString();
    }

    @Override
    public String receiveDamage() {
        StringBuilder result = new StringBuilder();

        if (isReady) {
            result.append(name).append("님은 총알을 튕겨냈습니다!\n");
            isReady = false;
        } else {
            result.append(name).append("님이 데미지를 받았습니다.\n");
            decreaseHealth();
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
