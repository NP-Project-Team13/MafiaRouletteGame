public class Player {
    private String id;
    private int health;

    public Player(String id, int health) {
        this.id = id;
        this.health = health;
    }

    public String getId() {
        return id;
    }

    public int getHealth() {
        return health;
    }

    public void decreaseHealth() {
        if (health > 0) {
            health--;
        }
    }
}
