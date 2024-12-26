package Entity;
import static Utilz.Constants.PlayerConstants.*;
import static Utilz.HelpMethods.*;
import static Utilz.HelpMethods.CanMoveHere;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import Gamestate.Playing;
import Main.Game;
import Utilz.LoadAndSave;

public class Player extends Entity {

    private BufferedImage[][] animations;
    private int aniTick, aniIndex, aniSpeed = 25;

    private int playerAction = IDLE; /* Karakterin varsayılan animasyonu */
    private boolean isMoving = false, isAttacking = false;

    private boolean left, right, jump;
    private float playerSpeed = 1.0f * Game.SCALE;

    private int[][] levelData;
    private float xDrawOffset = 125 * Game.SCALE;
    private float yDrawOffset = 97 * Game.SCALE;

    // Yer Çekimi ve Zıplama
    private float airSpeed = 0f;
    private float gravity = 0.04f * Game.SCALE;
    private float jumpSpeed = -2.25f * Game.SCALE;
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;
    private boolean inAir = false;

    //Kullanıcı durumu gösterge barları, user interface.
    private BufferedImage statusBarImage;

    private int statusBarWidth = (int) (192 * Game.SCALE);
    private int statusBarHeight = (int) (58 * Game.SCALE);
    private int statusBarX = (int) (10 * Game.SCALE);
    private int statusBarY = (int) (10 * Game.SCALE);

    private int healthBarWidth = (int) (150 * Game.SCALE);
    private int healthBarHeight = (int) (4 * Game.SCALE);
    private int healthBarXStart = (int) (34 * Game.SCALE);
    private int healthBarYStart = (int) (14 * Game.SCALE);

    private int maxHealth = 100;
    private int currentHealth = maxHealth;
    private int healthBarRectangleWidth = healthBarWidth;

    // Saldırı hitboxu
    private Rectangle2D.Float attackHitBox;

    private int flipX = 0;
    private int flipW = 1;

    private boolean attackChecked;
    private Playing playing;

    public Player(float x, float y, int width, int height, Playing playing) {
        /* Taking the parameters x, y and then passing them to the superclass to be stored. */
        super(x, y, width, height);
        this.playing = playing;
        loadAnimations();
        initHitbox(x, y, (int) (31 * Game.SCALE), (int) (29 * Game.SCALE));
        initAttackBox();
    }

    public void setSpawn(Point spawn) {
        this.x = spawn.x;
        this.y = spawn.y;
        hitbox.x = x;
        hitbox.y = y;
    }

    private void initAttackBox() {
        attackHitBox = new Rectangle2D.Float(x, y, (int) (85 * Game.SCALE), (int) (50 * Game.SCALE));
    }

    public void update() {
        updateHealthBarRectangle();

        if(currentHealth <= 0) {
            if(playerAction != DEAD) {
                playerAction = DEAD;
                aniTick = 0;
                aniIndex = 0;
                playing.setPlayerDying(true);
            } else if(aniIndex == getSpriteAmount(DEAD) - 1 && aniTick >= aniSpeed - 1) {
                playing.setGameOver(true);
            } else
                updateAnimationTick();
            return;
        }

        updateAttackHitBox();

        updatePosition();
        if(isAttacking)
            checkAttack();
        updateAnimationTick();
        setAnimation();
    }

    private void checkAttack() {
        if(attackChecked || aniIndex != 2)
            return;
        attackChecked = true;
        playing.checkEnemyHit(attackHitBox);
    }

    private void updateAttackHitBox() {
        int verticalOffset = (int) (-20 * Game.SCALE);

        if (right) {
            attackHitBox.x = hitbox.x + hitbox.width;
            attackHitBox.width = (int) (40 * Game.SCALE);
        } else if (left) {
            attackHitBox.x = hitbox.x - (int) (20 * Game.SCALE);
            attackHitBox.width = (int) (40 * Game.SCALE);
        }
        attackHitBox.y = hitbox.y + verticalOffset;
    }

    private void updateHealthBarRectangle() {
        healthBarRectangleWidth = (int) ((currentHealth / (float) maxHealth) * healthBarWidth);
    }

    public void render(Graphics g, int lvlOffset) {
        g.drawImage(animations[playerAction][aniIndex],
                (int) (hitbox.x - xDrawOffset) - lvlOffset + flipX,
                (int) (hitbox.y - yDrawOffset), width * flipW, height, null);
//      drawHitbox(g, lvlOffset);
//      drawAttackBox(g, lvlOffset);

        drawUI(g);
    }

    private void drawAttackHitBox(Graphics g, int lvlOffsetX) {
        g.setColor(Color.red);
        g.drawRect((int) attackHitBox.x - lvlOffsetX, (int) attackHitBox.y, (int) attackHitBox.width, (int) attackHitBox.height);
    }

    private void drawUI(Graphics g) {
        g.drawImage(statusBarImage, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);
        g.setColor(Color.red);
        g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthBarRectangleWidth, healthBarHeight);
    }

    private void updateAnimationTick() {
        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= getSpriteAmount(playerAction)) {
                aniIndex = 0;
                isAttacking = false;
                attackChecked = false;
            }

        }

    }

    private void setAnimation() {
        int startAni = playerAction;

        if (isMoving)
            playerAction = RUNNING;
        else
            playerAction = IDLE;

        if(inAir)
            playerAction = JUMP;

        if (isAttacking) {
            playerAction = ATTACK;
            if(startAni != ATTACK) {
                aniIndex = 2; // for a faster attack.
                aniTick = 0;
                return;
            }

        }

        if (startAni != playerAction)
            resetAniTick();
    }

    private void resetAniTick() {
        aniTick = 0;
        aniIndex = 0;
    }

    private void updatePosition() {

        isMoving = false;
        /* Unless one of the conditions below is true, the player character will not be moving. */
        /* This will also allow the character to move diagonally. */

        if(jump) {
            jump();
        }
        if(!inAir) {
            if ((!left && !right) || (left && right)) {
                /* We don't have to control up and down here as we have the gravity variable for
                that. */
                /* This if statement is checking out whether the character is moving on the x-plane or the character is moving
                on the y-plane (which means we check if the character is in the middle of a jump or falling. */
                return;
            }
        }

        float xSpeed = 0;

        if (left){
            xSpeed -= playerSpeed;
            flipX = width;
            flipW = -1;
        }

        if (right){
            xSpeed += playerSpeed;
            flipX = 0;
            flipW = 1;
        }

        if(!inAir) {
            /* Karakter havada değilse - yani yerle temas ediyorsa- sadece x eksenindeki çarpışmaları
            kontrol etmemiz yeterli olacak. */
            if (!IsEntityOnFloor(hitbox, levelData)) {
                /* Karakterin zeminle temas edip etmediği kontrol edilir. Eğer karakter zeminle temas
                etmiyorsa karakter havada duruyor olmalıdır. 'inTheAir' boolean değişkeninin değerine
                true atanır. */
                inAir = true;
            }
        }


        if(inAir) {

            /* Karakter havadaysa hem x hem y eksenindeki çarpışmaları kontrol etmemiz gerekecek. */

            if(CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, levelData)) {
                hitbox.y += airSpeed;
                airSpeed += gravity;
                /* Karakter zıplıyorsa hitbox'ın üst kenarının y eksenindeki koordinat değerini azaltıyor. */
                updateXPosition(xSpeed);
            }
            else {
                /* Karakter zıplarken  */
                hitbox.y = GetEntityYPositionUnderRoofOrAboveFloor(hitbox, airSpeed);

                if(airSpeed > 0) {
                    resetInAir();
                }
                else {
                    airSpeed = fallSpeedAfterCollision;
                }
                updateXPosition(xSpeed);
            }
        }
        else {
            updateXPosition(xSpeed);
        }

        isMoving = true;
    }

    private void jump() {
        if(inAir)
            return;
        inAir = true;
        airSpeed = jumpSpeed;
    }

    private void resetInAir() {
        inAir = false;
        airSpeed = 0;
    }

    private void updateXPosition(float xSpeed) {
        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, levelData)) {
            hitbox.x += xSpeed;
        }else {
            hitbox.x = GetEntityXPositionNextToWall(hitbox, xSpeed);
        }
    }

    public void changeHealth(int value) {
        currentHealth += value;

        if (currentHealth <= 0) {
            currentHealth = 0;
        }else if (currentHealth >= maxHealth)
            currentHealth = maxHealth;
    }

    private void loadAnimations() {
        BufferedImage img = LoadAndSave.getSpriteAtlas(LoadAndSave.PLAYER_SPRITE_SHEET);

        animations = new BufferedImage[12][9];
        for (int j = 0; j < animations.length; j++)
            for (int i = 0; i < animations[j].length; i++)
                animations[j][i] = img.getSubimage(i * 288, j * 128, 288, 128);

        statusBarImage = LoadAndSave.getSpriteAtlas(LoadAndSave.STATUS_BAR);

    }

    public void loadLevelData(int[][] levelData) {
        this.levelData = levelData;
        if(!IsEntityOnFloor(hitbox, levelData))
            inAir = true;
    }

    public void resetDirectionBooleans() {
        left = false;
        right = false;
    }

    public void setAttacking(boolean attacking) {
        this.isAttacking = attacking;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public void resetAll() {
        resetDirectionBooleans();
        inAir = false;
        isAttacking = false;
        isMoving = false;
        playerAction = IDLE;
        currentHealth = maxHealth;

        hitbox.x = x;
        hitbox.y = y;

        if(!IsEntityOnFloor(hitbox, levelData))
            inAir = true;
    }

}