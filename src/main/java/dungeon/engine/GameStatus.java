package dungeon.engine;

/**
 * An enum representing the possible states of the MiniDungeon game.
 */
public enum GameStatus {
    PLAYING, // The game is currently in progress
    WON,     // The player has successfully completed the game
    LOST     // The player has lost the game (HP dropped to 0 or max steps reached)
}