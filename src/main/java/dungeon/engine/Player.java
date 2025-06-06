package dungeon.engine;

/**
 * Represents the player character in the MiniDungeon game.
 * Manages player's health points (HP), score, current position on the map,
 * and inventory of activatable items like bombs.
 */
public class Player {
    private int hp;
    private int score;
    private Position position; // Player's current (row, col) coordinates
    private int bombCount; // Tracks the number of bombs the player has

    public static final int MAX_HP = 10; // Maximum HP for the player

    /**
     * Constructs a new Player with initial HP, score, and position.
     *
     * @param initialHp      The starting health points for the player.
     * @param initialScore   The starting score for the player.
     * @param initialPosition The player's starting position on the map.
     */
    public Player(int initialHp, int initialScore, Position initialPosition) {
        this.hp = initialHp;
        this.score = initialScore;
        this.position = initialPosition;
        this.bombCount = 0; // Initialize bomb count to 0
    }

    /**
     * Returns the player's current health points.
     *
     * @return The current HP.
     */
    public int getHp() {
        return hp;
    }

    /**
     * Sets the player's health points. Ensures HP does not exceed MAX_HP.
     *
     * @param hp The new HP value.
     */
    public void setHp(int hp) {
        this.hp = Math.min(hp, MAX_HP); // Cap HP at MAX_HP
    }

    /**
     * Decreases the player's HP by a specified amount.
     *
     * @param damage The amount of HP to decrease.
     */
    public void takeDamage(int damage) {
        this.hp -= damage;
    }

    /**
     * Increases the player's HP by a specified amount, up to MAX_HP.
     *
     * @param amount The amount of HP to heal.
     */
    public void heal(int amount) {
        this.hp = Math.min(this.hp + amount, MAX_HP);
    }

    /**
     * Returns the player's current score.
     *
     * @return The current score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the player's score.
     *
     * @param score The new score value.
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Increases the player's score by a specified amount.
     *
     * @param points The amount of points to add to the score.
     */
    public void addScore(int points) {
        this.score += points;
    }

    /**
     * Returns the player's current position on the map.
     *
     * @return The current Position object.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets the player's new position on the map.
     *
     * @param newPosition The new Position object.
     */
    public void setPosition(Position newPosition) {
        this.position = newPosition;
    }

    /**
     * Returns the current number of bombs the player possesses.
     * @return The bomb count.
     */
    public int getBombCount() {
        return bombCount;
    }

    /**
     * Adds a bomb to the player's inventory.
     * @param count The number of bombs to add.
     */
    public void addBomb(int count) {
        this.bombCount += count;
    }

    /**
     * Attempts to use one bomb.
     * @return true if a bomb was successfully used, false if no bombs are available.
     */
    public boolean useBomb() {
        if (bombCount > 0) {
            bombCount--;
            return true;
        }
        return false;
    }
}