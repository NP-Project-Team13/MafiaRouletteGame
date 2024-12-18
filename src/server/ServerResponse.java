package server;

import characters.CharacterTemplate;

import java.io.*;
import java.util.List;

public class ServerResponse implements Serializable {
    private String action;
    private String message;
    private List<CharacterTemplate> characters; // 플레이어 정보 리스트
    private List<Boolean> chambers; // 총알 슬롯 상태
    private int roundNumber; // 현재 라운드 번호
    private int currentPlayerIndex; // 현재 플레이어 인덱스


    // shoot 관련 Response
    public ServerResponse(String action, String message, List<CharacterTemplate> characters,
                          List<Boolean> chambers, int roundNumber, int currentPlayerIndex) {
        this.action = action;
        this.message = message;
        this.characters = deepCopyCharacters(characters);
        this.chambers = chambers;
        this.roundNumber = roundNumber;
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public ServerResponse(String message) {
        this.action = "message";
        this.message = message;
    }

    public ServerResponse(String message, String target){
        this.action = message;
        this.message = target;
    }

    public ServerResponse() { // 게임 종료 시 전달하는 response 형태
        this.action = "end";
    }

    public ServerResponse(String assignCharacter, String s, CharacterTemplate character) {
    }

    private List<CharacterTemplate> deepCopyCharacters(List<CharacterTemplate> original) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(original);
            oos.flush();

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (List<CharacterTemplate>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Character list deep copy failed.", e);
        }
    }
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
