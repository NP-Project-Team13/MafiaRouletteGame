// 7. 무작위로 생존 여부가 결정되는 "대박 아니면 쪽박" 능력 (나 아니면 너 OUT → 한명을 지목, 50:50 확률로 둘 중 한 명은 사망)
package characters;

import java.util.Random;

public class Character7 extends Character0 {

    protected boolean isReady = false; // 능력 발동 여부

    public Character7(String name, String team) {
        super(name, team,"나 아니면 너 OUT");
    }

    @Override
    public void shoot(Character0 target) {
        if (health <= 0) {
            System.out.println(name + "은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.");
            return;
        }
        if (isReady){
            System.out.println(name + "이(가) " + target.getName() + "에게 총을 발사했습니다!");
            target.receiveDamage();
            System.out.println(name + "은(는) " + target.getName() + "과(와) 함께 대박 아니면 쪽박 능력을 사용합니다.");

            if (new Random().nextBoolean()) {
                System.out.println(name + "은(는) 죽음의 도박에서 살아남았습니다. " + target.getName() + "은(는) 사망하였습니다.");
                target.health = 0;
            } else {
                System.out.println(target.getName() + "은(는) 죽음의 도박에서 살아남았습니다. " + name + "은(는) 사망하였습니다.");
                this.health = 0;
            }
        }else{
            System.out.println(name + "이(가) " + target.getName() + "에게 총을 발사했습니다!");
            target.receiveDamage();
        }
    }


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
        isReady = true;
        setAbilityUsed(true);
    }

    @Override
    public void resetRound(){
        isAbilityUsed = false;
        isReady = false;
    }
}
