// 3. 순서 변경 능력
package characters;


public class Character3 extends Character0 {

    public Character3(String name, String team) {
        super(name, team,"순서 변경 능력");
    }

    @Override
    public void useAbility(Character0... targets) { // 외부에서 순서 교체 로직 필요
        if (health <= 0) {
            System.out.println(name + "은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.");
            return;
        }
        if (isAbilityUsed) {
            System.out.println(name + "은(는) 이미 이번 라운드에서 능력을 사용했습니다.");
            return;
        }
        if (targets.length < 1) {
            System.out.println(name + "은(는) 순서를 변경할 대상을 필요로 합니다.");
            return;
        }
        Character0 target = targets[0];
        System.out.println(name + "은(는) " + target.getName() + "과(와) 순서를 교체했습니다.");
        setAbilityUsed(true);
    }

    @Override
    public void resetRound(){
        isAbilityUsed = false;
    }
}