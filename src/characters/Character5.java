// 5. 총에 맞으면 데미지 2배
package characters;

public class Character5 extends Character0 {

    protected boolean isReceived = false; // 총에 한 번이라도 맞은 경우 true
    protected boolean isReady = false; // 능력 발동 여부

    public Character5(String name, String team) {
        super(name, team,"총에 맞으면 데미지 2배");
    }

    @Override
    public void useAbility(Character0... targets) {
        if (health <= 0) {
            System.out.println(name + "은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.");
            return;
        }
        if(!isReceived) {
            System.out.println("아직 능력을 사용할 수 없습니다.");
        }
        if (isAbilityUsed) {
            System.out.println(name + "은(는) 이미 이번 라운드에서 능력을 사용했습니다.");
            return;
        }
        System.out.println(name + "은(는) 능력을 사용했습니다.");
        setAbilityUsed(true);
        isReady = true;
    }

    @Override
    public void shoot(Character0 target) {
        if (health <= 0) {
            System.out.println(name + "은(는) 이미 사망했기 때문에 총을 쏠 수 없습니다.");
            return;
        }
        System.out.println(name + "이(가) " + target.getName() + "에게 총을 발사했습니다!");
        if(isReady){
            target.decreaseHealth();
            isReady = false;
        }
        target.decreaseHealth();
    }

    @Override
    public void resetRound(){
        // isAbilityUsed 유지
        // isReceived 유지
    }
}