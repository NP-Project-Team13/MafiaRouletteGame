import characters.Character1;
import characters.CharacterTemplate;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    // 클라이언트의 행동을 JSON 문자열로 변환
    public static String actionToJson(ClientAction action) {
        return "{ \"action\": \"" + action.getAction() + "\", \"target\": \"" + action.getTarget() + "\" }";
    }

    // JSON 문자열을 `ClientAction` 객체로 변환
    public static ClientAction jsonToAction(String json) {
        String[] parts = json.replace("{", "").replace("}", "").replace("\"", "").split(",");
        String actionType = parts[0].split(":")[1].trim();
        String target = parts[1].split(":")[1].trim();
        return new ClientAction(actionType, target);
    }

//    // 서버의 응답을 JSON 문자열로 변환
//    public static String responseToJson(ServerResponse response) {
//        StringBuilder json = new StringBuilder();
//        json.append("{");
//        json.append("\"action\": \"").append(response.getAction()).append("\", ");
//        json.append("\"message\": \"").append(response.getMessage()).append("\", ");
//        json.append("\"roundNumber\": ").append(response.getRoundNumber()).append(", ");
//        json.append("\"currentPlayerIndex\": ").append(response.getCurrentPlayerIndex()).append(", ");
//
//        // 플레이어 정보 리스트를 JSON으로 변환
//        json.append("\"characters\": [");
//        for (int i = 0; i < response.getCharacters().size(); i++) {
//            CharacterTemplate character = response.getCharacters().get(i);
//            json.append("{");
//            json.append("\"name\": \"").append(character.getName()).append("\", ");
//            json.append("\"team\": \"").append(character.getTeam()).append("\", ");
//            json.append("\"health\": ").append(character.getHealth()).append(", ");
//            json.append("\"isAlive\": ").append(character.isAlive());
//            json.append("}");
//            if (i < response.getCharacters().size() - 1) {
//                json.append(", "); // 마지막 요소가 아닐 경우 쉼표 추가
//            }
//        }
//        json.append("], ");
//
//        // 총알 슬롯 상태를 JSON으로 변환
//        json.append("\"chambers\": [");
//        for (int i = 0; i < response.getChambers().size(); i++) {
//            json.append(response.getChambers().get(i)); // true/false 값
//            if (i < response.getChambers().size() - 1) {
//                json.append(", "); // 마지막 요소가 아닐 경우 쉼표 추가
//            }
//        }
//        json.append("]");
//
//        json.append("}");
//        return json.toString();
//    }
//
//    // JSON 문자열을 `ServerResponse` 객체로 변환
//    public static ServerResponse jsonToResponse(String json) {
//        String[] parts = json.replace("{", "").replace("}", "").replace("\"", "").split(",");
//
//        String action = parts[0].split(":")[1].trim();
//        String message = parts[1].split(":")[1].trim();
//        int roundNumber = Integer.parseInt(parts[2].split(":")[1].trim());
//        int currentPlayerIndex = Integer.parseInt(parts[3].split(":")[1].trim());
//
//        // 플레이어 정보 리스트 변환
//        List<CharacterTemplate> characters = new ArrayList<>();
//
//        // JSON 문자열에서 "characters" 부분 찾기
//        String charactersJson = json.substring(json.indexOf("\"characters\": [") + "\"characters\": [".length(),
//                json.indexOf("],", json.indexOf("\"characters\": [")));
//
//        if (!charactersJson.trim().isEmpty()) {
//            String[] characterEntries = charactersJson.split("\\},");
//            for (String entry : characterEntries) {
//                entry += "}"; // 각 캐릭터 JSON 문자열의 끝을 맞추기 위해 추가
//                String name = entry.split(",")[0].split(":")[1].trim(); // 이름
//                String team = entry.split(",")[1].split(":")[1].trim(); // 팀
//                int health = Integer.parseInt(entry.split(",")[2].split(":")[1].trim()); // 체력
//
//                // 새로운 CharacterTemplate 객체 생성 (Character1 등으로 대체해야 할 수 있음)
//                CharacterTemplate character = new Character1(name, team); // 여기에 적절한 캐릭터 클래스 사용
//                character.setHealth(health); // 체력 설정
//                characters.add(character); // 리스트에 추가
//            }
//        }
//
//        // 총알 상태 리스트 변환
//        List<Boolean> chambers = new ArrayList<>();
//
//        // JSON 문자열에서 "chambers" 부분 찾기
//        String chambersJson = json.substring(json.indexOf("\"chambers\": [") + "\"chambers\": [".length(),
//                json.indexOf("]", json.indexOf("\"chambers\": [")));
//
//        if (!chambersJson.trim().isEmpty()) {
//            String[] chamberEntries = chambersJson.split(",");
//            for (String chamber : chamberEntries) {
//                chambers.add(Boolean.parseBoolean(chamber.trim())); // 각 슬롯의 상태를 추가
//            }
//        }
//
//        return new ServerResponse(action, message, characters, chambers, roundNumber, currentPlayerIndex);
//    }

}

