package main;

public enum EntityType {

    PLAYER("resources/images/characters/player.png"),
    PLANT_MONSTER("resources/images/characters/plant_monster.png");

    private final String filePath;

    EntityType(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
