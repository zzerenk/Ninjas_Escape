package Entity;

import Main.Game;

import java.awt.geom.Rectangle2D;

import static Utilz.Constants.EnemyConstants.*;
import static Utilz.HelpMethods.*;
import static Utilz.Constants.Directions.*;

public abstract class Enemy extends Entity{

    protected int animationIndex, enemyState, enemyType;
    protected int animationPulse, animationSpeed = 25;
    protected boolean firstUpdate = true;
    protected boolean inAir = false;
    protected float fallSpeed;
    protected float gravity = 0.04f + Game.SCALE;
    protected float runningSpeed = 0.40f * Game.SCALE;
    protected int runningDirection = LEFT;
    protected int tileY;
    protected float attackDistance = Game.TILES_SIZE;
    protected int maxHealth;
    protected int currentHealth;
    protected boolean active = true;
    protected boolean attackChecked;

    public Enemy(float x, float y, int width, int height, int enemyType) {
        super(x, y, width, height);
        this.enemyType = enemyType;
        initHitbox(x, y, width, height);
        maxHealth = getMaxHealth(enemyType);
        currentHealth = maxHealth;
    }

    protected void updateAnimationPulse() {

        animationPulse++;
        if(animationPulse >= animationSpeed) {
            animationPulse = 0;
            animationIndex++;
            if(animationIndex >= GetSpriteAmount(enemyType, enemyState)) {
                animationIndex = 0;

                if(enemyState == ATTACK)
                    enemyState = IDLE;
                else if(enemyState == HIT)
                    enemyState = IDLE;
                else if(enemyState == DEAD)
                    active = false;
            }
        }
    }

    public int getAnimationIndex() {
        return animationIndex;
    }

    public int getEnemyState() {
        return enemyState;
    }

    protected void firstUpdateCheck(int[][] lvlData) {
        if (!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;
        firstUpdate = false;
    }

    protected void updateInAir(int[][] lvlData) {
        if (CanMoveHere(hitbox.x, hitbox.y + fallSpeed, hitbox.width, hitbox.height, lvlData)) {
            hitbox.y += fallSpeed;
            fallSpeed += gravity;
        } else {
            inAir = false;
            hitbox.y = GetEntityYPositionUnderRoofOrAboveFloor(hitbox, fallSpeed);
            tileY = (int) (hitbox.y / Game.TILES_SIZE);
        }
    }

    protected void move(int[][] lvlData) {

        float xSpeed = 0;

        if(runningDirection == LEFT)
            xSpeed = -runningSpeed;
        else
            xSpeed = runningSpeed;

        if(CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
            if(IsFloor(hitbox, xSpeed, lvlData)) {
                hitbox.x += xSpeed;
                return;
            }
        changeRunningDirection();
    }

    protected boolean canSeePlayer(int[][] lvlData, Player player) {

        /* Oyuncunun bulunduğu mevcut karenin y eksenindeki indeksi hesaplanır. */
        int playerCurrentTileYIndex = (int) (player.getHitbox().y / Game.TILES_SIZE);

        /* Oyuncu ve düşman karakterin aynı yükseklikte olup olmadığı elde edilen kare indeksi değerleri
        ile kontrol edilir. Eğer oyuncu ve düşman karakter aynı yükseklikte değilse, düşman karakter
        oyuncuyu görmeyecektir. Eğer aynı yüksekliktelerse diğer kontroller yapılır.*/
        if(playerCurrentTileYIndex == tileY) {

            /* Oyuncunun, düşman karakterin oyuncuyu görebileceği mesafe aralığında olup olmadığı kontrol
            edilir. */
            if (isPlayerInSightRange(player)) {

                /* Oyuncu ile düşman karakter arasındda görüşü engelleyen bir nesne olup olmadığı kontrol
                edilir. */
                if (IsSightClear(lvlData, hitbox, player.hitbox, tileY)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean isPlayerInSightRange(Player player) {
        /* absoluteValue değerine parametreleri olan Math.abs() -mutlak değer metodu- metodunun döndürdüğü
        değeri atanır. Bu şekilde oyuncu ve düşman karakter arasındaki mesafe yönden bağımsız olarak, sadece
        uzaklık cinsinden elde edilmiş olur.*/
        int absoluteValue = (int) Math.abs(player.hitbox.x - hitbox.x);
        return absoluteValue <= attackDistance * 5;
    }

    protected boolean isPlayerInAttackRange(Player player) {
        int absoluteValue = (int) Math.abs(player.hitbox.x - hitbox.x);
        return absoluteValue <= attackDistance;
    }

    protected void turnTowardsPlayer(Player player) {
        if(player.hitbox.x > hitbox.x)
            runningDirection = RIGHT;
        else
            runningDirection = LEFT;
    }

    protected void newState(int enemyState) {
        this.enemyState = enemyState;
        animationPulse = 0;
        animationIndex = 0;
    }

    public void hurt(int amount) {
        currentHealth -= amount;
        if(currentHealth <= 0)
            newState(DEAD);
        else
            newState(HIT);
    }

    protected void checkEnemyHit(Rectangle2D.Float attackBox, Player player) {
        if(attackBox.intersects(player.hitbox))
            player.changeHealth(-getEnemyDmg(enemyType));
        attackChecked = true;
    }

    public void resetEnemy() {
        hitbox.x = x;
        hitbox.y = y;
        firstUpdate = true;
        currentHealth = maxHealth;
        newState(IDLE);
        active = true;
        fallSpeed = 0;
    }


    public boolean isActive() {
        return active;
    }

    protected void changeRunningDirection() {
        if(runningDirection == LEFT)
            runningDirection = RIGHT;
        else
            runningDirection = LEFT;
    }

}