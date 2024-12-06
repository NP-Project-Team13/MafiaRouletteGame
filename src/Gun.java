import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Gun {
    private static int bullets; // 현재 총알 개수
    private static List<Boolean> chambers; // 총의 실린더 상태 (8개의 슬롯)

    public Gun() {
        bullets = 4; // 처음엔 총알 1개
        initializeChambers();
    }

    // 실린더 초기화
    private void initializeChambers() {
        chambers = new ArrayList<>(Collections.nCopies(8, false)); // 8개의 슬롯에 false로 초기화
        for (int i = 0; i < bullets; i++) {
            chambers.set(i, true); // 총알 개수만큼 true로 설정
        }
        Collections.shuffle(chambers); // 슬롯을 랜덤하게 섞음
    }

    // 총 쏘기
    public boolean fire() {
        if (chambers.isEmpty()) {
            increaseBullets();
            initializeChambers(); // 총알이 없다면 실린더를 다시 초기화
        }

        // 실린더의 첫 번째 슬롯을 검사하고 제거
        boolean isHit = chambers.remove(0);
        return isHit;
    }

    // 다음 라운드에서 총알 증가
    public void increaseBullets() {
        if (bullets < 8) {
            bullets++;
            initializeChambers(); // 실린더 재설정
        }
    }

    public int getBullets() {
        return bullets;
    }
    public List<Boolean> getChambers() {
        return chambers;
    }
}
