// 6. 자신의 health -1 소모하여 아군을 health +1
package characters;

public class Character6 extends CharacterTemplate {

    public Character6(String name, String team) {
        super(name, team,"자신의 health -1 소모하여 아군을 health +1");
    }

    @Override
    public void useAbility(CharacterTemplate... targets) {
        if (health <= 0) {
            System.out.println(name + "은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.");
            return;
        }
        if (isAbilityUsed) {
            System.out.println(name + "은(는) 이미 이번 라운드에서 능력을 사용했습니다.");
            return;
        }
        if (targets.length < 1) {
            System.out.println(name + "은(는) 치유할 대상을 필요로 합니다.");
            return;
        }
        CharacterTemplate target = targets[0];
        if (health > 1) {
            health--;
            target.health++;
            System.out.println(name + "은(는) 자신의 생명을 1 소모하여 " + target.getName() + "을(를) 치유했습니다.");
        } else {
            System.out.println(name + "은(는) 치유할 만큼 충분한 체력이 없습니다.");
        }
        setAbilityUsed(true);
    }

    @Override
    public void resetRound(){
        isAbilityUsed = false;
    }
}

