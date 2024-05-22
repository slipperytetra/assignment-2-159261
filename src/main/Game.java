package main;

import block.BlockTypes;
import block.decorations.DecorationTypes;
import level.Level;
import level.LevelManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Game extends GameEngine {
    public static int BLOCK_SIZE = 32;
    private boolean gameOver;
    public boolean isPaused;

    public long timeSinceLastFrame;
    public long lastTime;
    public long currentTime;
    public HashMap<String, Image> imageBank;
    public Set<Integer> keysPressed = new HashSet();

    LevelManager lvlManager;
    private Level activeLevel;
    Camera camera;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameMenu::new);
    }

    public void startGame(){
        createGame(this,  60);
    }

    public void init() {
        this.imageBank = new HashMap<>();
        loadDecorationImages();
        loadBlockImages();
        loadCharacterImages();

        this.setWindowSize(1280, 720);
        this.lvlManager = new LevelManager(this);
        setActiveLevel(lvlManager.LEVEL_4);

        //System.out.println("Starting X position: " + this.activeLevel.getPlayer().getLocation().getX());
        //System.out.println("Starting Y position: " + this.activeLevel.getPlayer().getLocation().getY());
    }

    public Level getActiveLevel() {
        return activeLevel;
    }

    public void setActiveLevel(Level level) {
        this.activeLevel = level;
        level.load();
        this.camera = new Camera(this, level.getPlayer());
    }

    public void update(double dt) {
        lastTime = currentTime;
        currentTime = System.currentTimeMillis();
        timeSinceLastFrame = currentTime - lastTime;
        camera.update();


        if (!isPaused) {
            getActiveLevel().update(dt);


        }
    }

    public void paintComponent() {
        this.clearBackground(width(), height());
        if (imageBank.containsKey("background")) {
            this.drawImage(imageBank.get("background"), 0, 0, this.width(), this.height());
        }
        if(getActiveLevel().getPlayer().getHealth() <= 0){
            gameOver = true;
            drawText(100,100,"You died",30);
            getActiveLevel().reset();
        } else {
            camera.draw();
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        this.keysPressed.add(event.getKeyCode());

        if (event.getKeyCode() == 27) { //ESCAPE
            isPaused = !isPaused;
        }

        if(event.getKeyCode() == 81){
            System.exit(0);
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        this.keysPressed.remove(event.getKeyCode());
        if (event.getKeyCode() == 72) {
            camera.debugMode = !camera.debugMode;
            //setActiveLevel(lvlManager.DEMO_2);
        }

        if (event.getKeyCode() == 65 || event.getKeyCode() == 68) {
            activeLevel.getPlayer().setDirectionX(0);
        }

        if (event.getKeyCode() == 83 || event.getKeyCode() == 87) {
            activeLevel.getPlayer().setDirectionY(0);
        }
    }

    public void loadDecorationImages() {
        for (DecorationTypes type : DecorationTypes.values()) {
            imageBank.put(type.toString(),  loadImage(type.getFilePath()));
        }
    }

    public void loadBlockImages() {
        for (BlockTypes type : BlockTypes.values()) {
            if (type == BlockTypes.VOID || type == BlockTypes.BARRIER) {
                continue;
            }

            imageBank.put(type.toString(),  loadImage(type.getFilePath()));
        }
    }

    /*
    *   This is where the image bank is loaded. Basically how it works is it uses a HashMap<String, Image> and it assigns
    *   a string to an image object.
    *
    *   By doing it
    * */
    public void loadCharacterImages() {
        imageBank.put("player_run_0",  loadImage("resources/images/characters/run0.png"));
        imageBank.put("player_run_1",  loadImage("resources/images/characters/run1.png"));
        imageBank.put("player_run_2",  loadImage("resources/images/characters/run2.png"));
        imageBank.put("player_run_3", loadImage("resources/images/characters/run3.png"));
        imageBank.put("player_jump_0",  loadImage("resources/images/characters/jump0.png"));
        imageBank.put("player_jump_1",  loadImage("resources/images/characters/jump1.png"));
        imageBank.put("player_jump_2",  loadImage("resources/images/characters/jump2.png"));
        imageBank.put("player_jump_3",  loadImage("resources/images/characters/jump3.png"));
        imageBank.put("player_attack", Toolkit.getDefaultToolkit().createImage("resources/images/characters/attack.gif"));

        imageBank.put("spot_light",  loadImage("resources/images/blocks/decorations/spot_light.png"));
        imageBank.put("door",  loadImage(EntityType.DOOR.getFilePath()));
        imageBank.put("player", loadImage(EntityType.PLAYER.getFilePath()));
       // imageBank.put("plant_monster", loadImage(EntityType.PLANT_MONSTER.getFilePath()));
        imageBank.put("stone_door",loadImage(EntityType.STONE_DOOR.getFilePath()));
        //imageBank.put(EntityType.KEY.toString().toLowerCase(), loadImage(EntityType.KEY.getFilePath()));
        //imageBank.put(EntityType.PLANT_MONSTER.toString().toLowerCase(),  loadImage(EntityType.PLANT_MONSTER.getFilePath()));
        imageBank.put("key",  Toolkit.getDefaultToolkit().createImage("resources/images/keyy.gif"));
        imageBank.put("plant_monsterAttack", Toolkit.getDefaultToolkit().createImage("resources/images/plantAttack.gif"));
        imageBank.put("plant_monsterAttack_flipped", Toolkit.getDefaultToolkit().createImage("resources/images/plantAttack_flipped.gif"));
        imageBank.put("plant_monster", loadImage("resources/images/characters/plant_monster.png"));
        imageBank.put("heart", Toolkit.getDefaultToolkit().createImage("resources/images/heart.gif"));
        imageBank.put("snow_fx", Toolkit.getDefaultToolkit().createImage("resources/images/idea.gif"));
        imageBank.put("cloud",  loadImage("resources/images/cloud.png"));





    }

    public Image getTexture(String textureName) {
        return imageBank.get(textureName);
    }

    public Image flipImageHorizontal(Image img) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1.0, 1.0);
        tx.translate(-img.getWidth(null), 0.0);
        AffineTransformOp op = new AffineTransformOp(tx, 1);
        return op.filter((BufferedImage) img, null);
    }
}
