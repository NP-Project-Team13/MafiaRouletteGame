// 2. 총알을 튕겨내는 방어 능력
package characters;

public class Character2 extends Character0 {

    private boolean deflectReady = false; // 총알 튕겨낼 준비 되었는지

    public Character2(String name, String team) {
        super(name, team, "총알을 튕겨내는 방어 능력");
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
        deflectReady = true;
        System.out.println(name + "은(는) 다음 총알을 튕겨낼 준비가 되었습니다.");
        setAbilityUsed(true);
    }

    @Override
    public void receiveDamage() {
        if (deflectReady) {
            System.out.println(name + "은(는) 총알을 튕겨냈습니다!");
            deflectReady = false;
        } else {
            System.out.println(name + "이(가) 데미지를 받았습니다.");
            decreaseHealth();
        }
    }

    @Override
    public void resetRound(){
        isAbilityUsed = false;
        deflectReady = false;
    }
}

