package main;

import block.Block;
import block.BlockClimbable;
import block.BlockTypes;
import level.Level;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Set;

public class Player extends Entity {
    private JProgressBar healthBar;
    private GameEngine.AudioClip attack;


    private boolean isAttacking;
    private boolean keyObtained;
    private boolean doorTouched;
    private boolean attackRegistered = false;
    private boolean isJumping;
    private boolean hasKey;
    private int attackCounter;

    private Timer runAnimationTimer;
    private int runFrameIndex;

    public Timer jumpAnimationTimer;
    private int jumpFrameIndex;
    private double timeJumping;
    private double maxJumpTime = 0.20; //seconds

    Image gifImage;
    Image plantAttack;
    Image gifImage2;
    Image level1;

    private GameEngine.AudioClip hit;

    private Enemy target;

    public Player(Level level, Location loc) {
        super(EntityType.PLAYER, level, loc);



        setHitboxColor(Color.cyan);
        setMaxHealth(100);
        setHealth(getMaxHealth());
        setDirectionY(1);

        setCollisionBox(new CollisionBox((int)loc.getX(), (int)loc.getY(), getWidth() - 4, getHeight()));
        init();
    }

    public void init() {
        gifImage = Toolkit.getDefaultToolkit().createImage("resources/images/keyy.gif");
        plantAttack = Toolkit.getDefaultToolkit().createImage("resources/images/plantAttack.gif");

        gifImage2 = Toolkit.getDefaultToolkit().createImage("resources/images/keyy.gif");
        level1 = Toolkit.getDefaultToolkit().createImage("resources/images/level1.gif");
        hit = getLevel().getManager().getEngine().loadAudio("resources/sounds/hit.wav");

        this.healthBar = new JProgressBar(0, getMaxHealth());
        this.healthBar.setBounds(100, 25, 100, 10); // Adjust position and size as needed
        this.healthBar.setForeground(Color.RED); // Set the color
        this.healthBar.setValue(getMaxHealth()); // Set initial health
        this.healthBar.setStringPainted(true); // Show health value

        this.runAnimationTimer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runFrameIndex = (runFrameIndex + 1) % 4;
                //System.out.println("Run " + currentFrameIndex);
            }
        });
        this.jumpAnimationTimer = new Timer(200, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //System.out.println("Jump: " + jumpFrameIndex);
                jumpFrameIndex = (jumpFrameIndex + 1) % 4;
            }
        });
    }

    public void update(double dt) {
        super.update(dt);
        animateCharacter();
        if(isAttacking){
            attackCounter++;
            if(attackCounter > 10) {
                isAttacking = false;
            }
            }

    }

    public boolean hasKey() {
        return hasKey;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    @Override
    public void processMovement(double dt) {
        moveX = getDirectionX() * (speed * dt);
        moveY = getDirectionY() * (speed * dt);

        moveX(moveX);
        moveY(moveY);

        if (isJumping()) {
            setDirectionY(-1.5);
            timeJumping += 1 * dt;

            if (timeJumping > maxJumpTime) {
                this.setJumping(false);
                this.setDirectionY(0);
                this.timeJumping = 0;
            }
            return;
        }

        if (isFalling() && !canClimb()) {
            if (fallAccel > 0) {
                fallAccel *= fallSpeedMultiplier;
                setDirectionY(1 * fallAccel);
            }
        } else {
            fallAccel = 1;
            setDirectionY(0);
        }

        /*if (isJumping()) {
            setDirectionY(-1.5);
            timeJumping += 1 * dt;

            if (timeJumping > maxJumpTime) {
                this.setJumping(false);
                this.setDirectionY(0);
                this.timeJumping = 0;
            }
        } else if (!canClimb()) {
            if (isFalling()) {
                if (fallAccel > 0) {
                    fallAccel *= fallSpeedMultiplier;
                    setDirectionY(1 * fallAccel);
                }
            } else {
                fallAccel = 1;
                setDirectionY(0);
            }
        }*/
    }

    public boolean isJumping() {
        return isJumping;
    }

    public void setJumping(boolean isJumping) {
        this.isJumping = isJumping;
    }

    public void render(Camera cam) {
        double playerOffsetX = getLocation().getX() + cam.centerOffsetX;
        double playerOffsetY = getLocation().getY() + cam.centerOffsetY;
        Game game = getLevel().getManager().getEngine();
        if (isAttacking) {
            game.drawImage(getAttackFrame(), playerOffsetX, playerOffsetY, getWidth(), getHeight() );
        } else if (isMovingVertically()) {
            game.drawImage(getFallFrame(), playerOffsetX, playerOffsetY, getWidth(), getHeight() );
        } else if (isMovingHorizontally()) {
            game.drawImage(getRunFrame(), playerOffsetX, playerOffsetY, getWidth(), getHeight());
        } else {
            game.drawImage(getIdleFrame(), playerOffsetX, playerOffsetY, getWidth(), getHeight());
        }

        if (cam.showHitboxes) {
            game.changeColor(Color.magenta);

            double hitBoxOffsetX = getCollisionBox().getLocation().getX() + cam.centerOffsetX;
            double hitBoxOffsetY = getCollisionBox().getLocation().getY() + cam.centerOffsetY;
            game.drawRectangle(getLeftBlockBelowEntity().getLocation().getX() + cam.centerOffsetX, getLeftBlockBelowEntity().getLocation().getY() + cam.centerOffsetY, Game.BLOCK_SIZE, Game.BLOCK_SIZE);
            game.drawRectangle(getRightBlockBelowEntity().getLocation().getX() + cam.centerOffsetX, getRightBlockBelowEntity().getLocation().getY() + cam.centerOffsetY, Game.BLOCK_SIZE, Game.BLOCK_SIZE);

            game.changeColor(getHitboxColor());
            game.drawRectangle(hitBoxOffsetX, hitBoxOffsetY, getCollisionBox().getWidth(), getCollisionBox().getHeight());
        }
    }

    public void jump() {
        this.isJumping = true;
        this.jumpFrameIndex = 0;
        this.jumpAnimationTimer.start();
        this.timeJumping = 0;
    }

    public void playerMovement(Set<Integer> keysPressed) {
        if (keysPressed.contains(32)) {//SPACE
            if (!isJumping() && (isOnGround() || canClimb())) {
                //System.out.println("Jump!");
                jump();
            } else {
                //System.out.println("Not on ground!");
            }
        }
        if (keysPressed.contains(87)) {//W
            if (canClimb()) {
                setDirectionY(-1);
            } else {
                setDirectionY(0);
            }
        }
        if (keysPressed.contains(65)) {//A
            setDirectionX(-calculateHorizontalMovement());
        }
        if (keysPressed.contains(83)) {//S
            if (canClimb()) {
                setDirectionY(1);
            }
        }
        if (keysPressed.contains(68)) {//D
            setDirectionX(calculateHorizontalMovement());
        }
        if (keysPressed.contains(81)){
            Attack();
        }
    }

    public JProgressBar getHealthBar() {
        return healthBar;
    }

    public double calculateHorizontalMovement() {
        if (isMovingVertically()) {
            return 0.75;
        }

        return 1;
    }

    private void animateCharacter() {
        if (isMovingHorizontally() && !isMovingVertically()) {
            if (!this.runAnimationTimer.isRunning()) {
                this.runAnimationTimer.start();
            }
        } else {
            this.runAnimationTimer.stop();
        }
    }

    public Image getRunFrame() {
        if (!isFlipped()) {
            return getLevel().getManager().getEngine().flipImageHorizontal(getLevel().getManager().getEngine().getTexture("player_run_" + runFrameIndex));
        }

        return getLevel().getManager().getEngine().getTexture("player_run_" + runFrameIndex);
    }

    public Image getFallFrame() {
        if (!isFlipped()) {
            return getLevel().getManager().getEngine().flipImageHorizontal(getLevel().getManager().getEngine().getTexture("player_jump_" + runFrameIndex));
        }

        return getLevel().getManager().getEngine().getTexture("player_jump_" + runFrameIndex);
    }

      public Image getAttackFrame(){

        return getLevel().getManager().getEngine().getTexture("player_attack");

     }

     public void Attack(){
         attack = getLevel().getManager().getEngine().loadAudio("resources/sounds/attackSound.wav");
         getLevel().getManager().getEngine().playAudio(attack);
         isAttacking = true;
         attackCounter = 0;


         if(canAttack()){
             System.out.println(getTarget().getHealth());
             getLevel().getManager().getEngine().playAudio(hit);


             getTarget().setHealth(getTarget().getHealth()- 2);
             System.out.println(getTarget().getHealth());
             if(getTarget().getHealth() <= 0){
                 getTarget().setDamage(0);
                 getTarget().destroy();

             }



         }



     }

    public boolean canAttack() {



        return getTarget() != null;
    }

    public Enemy getTarget() {
        for (Entity enemy : getLevel().getEntities()){
            if (enemy instanceof Enemy) {
                if (Location.calculateDistance(getLocation().getX(), getLocation().getY(), enemy.getLocation().getX(), enemy.getLocation().getY()) < 64) {
                    System.out.println("Close");
                    return (Enemy) enemy;
                }
            }
        }

        return null;
    }

    public void setTarget(Enemy p) {
        this.target = p;
    }








    public boolean canClimb() {
        return getBlockAtLocation() instanceof BlockClimbable;
    }

    public boolean isAttacking() {
        return this.isAttacking;
    }

    public void setAttacking(boolean isAttacking) {
        this.isAttacking = isAttacking;
    }

    public boolean hasObtainedKey() {
        return this.keyObtained;
    }

    public void setKeyObtained(boolean keyObtained) {
        this.keyObtained = keyObtained;
    }

    public boolean isTouchingDoor() {
        return this.doorTouched;
    }

    public void setTouchingDoor(boolean doorTouched) {
        this.doorTouched = doorTouched;
    }

    public boolean hasRegisteredAttack() {
        return this.attackRegistered;
    }

    public void setAttackRegistered(boolean attackRegistered) {
        this.attackRegistered = attackRegistered;
    }

    @Override
    public void updateCollisionBox() {
        getCollisionBox().setLocation(getLocation().getX(), getLocation().getY());
        getCollisionBox().setSize(getWidth() - 4, getHeight());
    }
}
