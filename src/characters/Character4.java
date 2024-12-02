// 4. 기관총을 발사하여 적을 공격하는 능력. 총알이 있으면 무조건 적군 전체 데미지, 총알이 없으면 그냥 넘어감 (능력 변경됨, 코드 수정 필요)
package characters;

public class Character4 extends CharacterTemplate {

    protected boolean isReady = false; // 능력 발동 여부
    CharacterTemplate[] abilityTargetCharacters;

    public Character4(String name, String team) {
        super(name, team, "발사 후 아군 전체 또는 적군 전체에게 데미지");
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
        setAbilityUsed(true);
        isReady = true;
//        abilityTargetCharacters = targets;
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
            for (CharacterTemplate abilityTargetCharacter : abilityTargetCharacters) {
                result.append(abilityTargetCharacter.receiveDamage());
            }
        }
        return result.toString();
    }

    @Override
    public String resetRound() {
        StringBuilder result = new StringBuilder();
        isAbilityUsed = false;
        isReady = false;
        result.append(name).append("의 상태가 초기화되었습니다.\n");
        return result.toString();
    }
}