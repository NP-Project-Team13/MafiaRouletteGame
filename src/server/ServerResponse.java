package server;

import characters.CharacterTemplate;

import java.io.*;
import java.util.List;

public class ServerResponse implements Serializable {
    private String action; // 클라이언트에서 수행할 동작
    private String message; // 서버가 전달할 메시지
    private List<CharacterTemplate> characters; // 캐릭터 정보 리스트
    private List<Boolean> chambers; // 총알 슬롯 상태
    private int roundNumber; // 현재 라운드 번호
    private int currentPlayerIndex; // 현재 플레이어 인덱스

    // 총쏘기 등 주요 동작에 대한 응답
    public ServerResponse(String action, String message, List<CharacterTemplate> characters,
                          List<Boolean> chambers, int roundNumber, int currentPlayerIndex) {
        this.action = action;
        this.message = message;
        this.characters = deepCopyCharacters(characters);
        this.chambers = chambers;
        this.roundNumber = roundNumber;
        this.currentPlayerIndex = currentPlayerIndex;
    }

    // 단순 메시지 응답
    public ServerResponse(String message) {
        this.action = "message";
        this.message = message;
    }

    // 메시지와 타겟을 지정한 응답
    public ServerResponse(String action, String target) {
        this.action = action;
        this.message = target;
    }

    // 게임 종료 시 전달하는 응답
    public ServerResponse() {
        this.action = "end";
    }

    // 캐릭터 리스트를 깊은 복사하여 상태 공유 문제 방지
    private List<CharacterTemplate> deepCopyCharacters(List<CharacterTemplate> original) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
            oos.flush();

            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                return (List<CharacterTemplate>) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Character list deep copy failed.", e);
        }
    }

    // Getter 메서드들
    public String getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public List<CharacterTemplate> getCharacters() {
        return characters;
    }

    public List<Boolean> getChambers() {
        return chambers;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
}
