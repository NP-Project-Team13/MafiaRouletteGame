package resources;

import javax.sound.sampled.*;
import java.io.IOException;

public class SoundPlayer {
    public static void playSound(String soundFile) {
        try {
            // 클래스패스에서 음향 파일 로드
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(SoundPlayer.class.getResource(soundFile));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
