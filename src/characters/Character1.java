// 1. 총알 위치를 확인하는 능력
package characters;

public class Character1 extends Character0 {

    public Character1(String name, String team) {
        super(name, team, "총알 위치를 확인하는 능력");
    }

    // 외부에서 총알 위치 확인 로직 구현 필요
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
        System.out.println(name + "은(는) 총알 위치를 확인했습니다.");
        setAbilityUsed(true);
    }

    @Override
    public void resetRound(){
        isAbilityUsed = false;
    }
}