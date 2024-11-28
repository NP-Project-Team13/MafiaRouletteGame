// 4. 기관총을 발사하여 적을 공격하는 능력. 발사 후 아군 전체 또는 적군 전체에게 데미지(health -1)
package characters;

public class Character4 extends Character0 {

    protected boolean isReady = false; // 능력 발동 여부
    Character0[] abilityTargetCharacters;

    public Character4(String name, String team) {
        super(name, team,"발사 후 아군 전체 또는 적군 전체에게 데미지");
    }

    // 파라미터는 아군 또는 적군 전체 캐릭터들에게 데미지
    @Override
    public void useAbility(Character0... targets) {
        if (health <= 0) {
            System.out.println(name + "은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.");
            return;
        }
        if (isAbilityUsed) {
            System.out.println(name + "은(는) 이미 이번 라운드에서 능력을 사용했습니다.");
            return;
        }
        setAbilityUsed(true);
        isReady = true;
        abilityTargetCharacters = targets;
    }

    @Override
    public void shoot(Character0 target) {
        if (health <= 0) {
            System.out.println(name + "은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.");
            return;
        }
        if (!isReady){
            System.out.println(name + "이(가) " + target.getName() + "에게 총을 발사했습니다!");
            target.receiveDamage();
        }else{
            for (Character0 abilityTargetCharacter : abilityTargetCharacters) {
                abilityTargetCharacter.receiveDamage();
            }
        }
    }


    @Override
    public void resetRound(){
        isAbilityUsed = false;
        isReady = false;
    }
}