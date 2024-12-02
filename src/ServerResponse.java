import characters.CharacterTemplate;

import java.util.List;

public class ServerResponse {
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
        this.characters = characters;
        this.chambers = chambers;
        this.roundNumber = roundNumber;
        this.currentPlayerIndex = currentPlayerIndex;
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
