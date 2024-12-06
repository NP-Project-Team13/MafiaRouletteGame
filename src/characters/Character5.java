// 5. 상대가 총에 맞으면 데미지 2배 주기 (전체 게임에 걸쳐 1회만 사용 가능)
// todo 구현 완료
package characters;

public class Character5 extends CharacterTemplate {

    protected boolean isReady; // 능력 발동 여부

    public Character5(String name, String team) {
        super(name, team, "상대가 총에 맞으면 데미지 2배 주기");
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
        result.append(name).append("은(는) 능력을 사용했습니다.\n");
        setAbilityUsed(true);
        isReady = true;
        return result.toString();
    }

    @Override
    public String shoot(CharacterTemplate target) {
        StringBuilder result = new StringBuilder();
        if (health <= 0) { // 자신
            result.append(name).append("은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.");
            return result.toString();
        }
        if (target.getHealth() <= 0) { // 상대
            result.append(target.getName()).append("은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.");
            return result.toString();
        }
        result.append(name).append("이(가) ").append(target.getName()).append("에게 총을 발사했습니다!\n");
        if (isReady) {
            result.append(target.decreaseHealth());
            result.append(target.decreaseHealth());
            result.append(" 데미지가 2배 적용되었습니다!\n");
            isReady = false;
        }else{
            result.append(target.decreaseHealth());
        }
        return result.toString();
    }

    @Override
    public String resetRound() {
        StringBuilder result = new StringBuilder();
        // 이 캐릭터는 능력 상태가 매 라운드에 걸쳐 유지되어야 하므로 초기화하지 않음.
        result.append(name).append("의 능력 사용 가능 상태는 계속 유지됩니다.\n");
        return result.toString();
    }
}
