package dungeon.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate; // For ScoreEntry
import java.time.format.DateTimeFormatter; // For ScoreEntry

import dungeon.engine.things.Bomb;
import dungeon.engine.things.Entry;
import dungeon.engine.things.Gold;
import dungeon.engine.things.HealthPotion;
import dungeon.engine.things.Ladder;
import dungeon.engine.things.MeleeMutant;
import dungeon.engine.things.RangedMutant;
import dungeon.engine.things.Trap;
import dungeon.engine.things.Wall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

/**
 * The central class for the MiniDungeon game engine.
 * Manages the game state, player actions, map interactions, and game progression.
 * Provides a text-based interface for playing the game.
 */
public class GameEngine {
    private DungeonMap currentMap;
    private Player player;
    private int currentLevel;
    private int maxSteps;
    private int stepsTaken;
    private GameStatus gameStatus;
    private Position playerEntryPositionLevel1; // Entry point for Level 1 (where Entry is)
    private Position playerSpawnPositionLevel1; // Where the player *actually* spawns (above the entry)
    private Position playerEntryPositionLevel2; // Entry point for Level 2 (which is Level 1's ladder)
    private List<ScoreEntry> topScores; // In-memory list for top 5 scores
    private List<String> gameStatusMessages; // Messages for the text area
    private Random random;
    private int currentDifficulty; // Stores the difficulty of the current level

    public static final int DEFAULT_MAX_STEPS = 100;
    public static final int INITIAL_PLAYER_HP = 10;
    public static final int INITIAL_SCORE = 0;
    public static final int DEFAULT_DIFFICULTY = 3;
    public static final int MAX_TOP_SCORES = 5;

    /**
     * Constructs a new GameEngine.
     * Initializes game state variables and loads (or initializes) top scores.
     */
    public GameEngine() {
        this.random = new Random();
        this.topScores = new ArrayList<>();
        this.gameStatusMessages = new ArrayList<>();
        // currentDifficulty will be set in startGame()
    }

    /**
     * Starts a new game with the specified difficulty.
     * Resets player stats, generates a new map for Level 1, and places the player.
     *
     * @param difficulty The desired difficulty level (0-10).
     */
    public void startGame(int difficulty) {
        // Ensure difficulty is within bounds
        difficulty = Math.max(0, Math.min(10, difficulty));

        this.currentLevel = 1;
        this.maxSteps = DEFAULT_MAX_STEPS;
        this.stepsTaken = 0;
        this.gameStatus = GameStatus.PLAYING;
        this.gameStatusMessages.clear();
        this.currentDifficulty = difficulty; // Set initial difficulty
        addGameStatusMessage("Welcome to MiniDungeon!");
        addGameStatusMessage("Starting Level 1 with difficulty " + this.currentDifficulty + "...");

        // Create Level 1 map. The map size will now depend on 'difficulty'.
        this.currentMap = new DungeonMap(currentDifficulty, true, null); // level1LadderPosition is null for Level 1

        // Player's fixed spawn point for Level 1: (rows - 2, 0)
        // These positions now depend on the actual size of the 'currentMap'
        this.playerEntryPositionLevel1 = new Position(currentMap.getRows() - 1, 0);
        this.playerSpawnPositionLevel1 = new Position(currentMap.getRows() - 2, 0);

        // Initialize player with the designated spawn position
        this.player = new Player(INITIAL_PLAYER_HP, INITIAL_SCORE, playerSpawnPositionLevel1);

        addGameStatusMessage("Player HP: " + player.getHp() + ", Score: " + player.getScore() + ", Steps: " + stepsTaken);
    }

    /**
     * Attempts to move the player in the specified direction.
     * Handles movement validation, step counting, and interaction with map things.
     *
     * @param direction The direction to move ("u", "d", "l", "r").
     */
    public void movePlayer(String direction) {
        if (gameStatus != GameStatus.PLAYING) {
            addGameStatusMessage("Game is not playing. Start a new game.");
            return;
        }

        int currentRow = player.getPosition().getRow();
        int currentCol = player.getPosition().getCol();
        int newRow = currentRow;
        int newCol = currentCol;

        switch (direction.toLowerCase()) {
            case "u":
                newRow--;
                break;
            case "d":
                newRow++;
                break;
            case "l":
                newCol--;
                break;
            case "r":
                newCol++;
                break;
            default:
                addGameStatusMessage("Invalid move: " + direction + ". Use 'u', 'd', 'l', or 'r'.");
                return;
        }

        // Check if new position is valid (within map bounds)
        if (!currentMap.isValidPosition(newRow, newCol)) {
            addGameStatusMessage("You tried to move " + direction + " one step but it is out of bounds.");
            return;
        }

        Cell targetCell = currentMap.getCell(newRow, newCol);

        // Check if the target cell is traversable (not a wall)
        if (!targetCell.isTraversable()) {
            addGameStatusMessage("You tried to move " + direction + " one step but it is a wall.");
            return;
        }

        // --- Handle Ranged Mutant Attacks BEFORE player moves ---
        // This is a design choice based on "attacks from 2 tiles away".
        // It implies the attack happens as part of the turn, not necessarily by stepping on it.
        handleRangedMutantAttacks();

        // Update player position and steps
        player.setPosition(new Position(newRow, newCol));
        stepsTaken++;
        addGameStatusMessage("You moved " + direction + " one step. Steps taken: " + stepsTaken);

        // Handle interaction with the thing on the new cell
        handleInteraction(newRow, newCol);

        // Check game end conditions after movement and interaction
        checkGameEndConditions();
    }

    /**
     * Handles the interaction when the player moves onto a cell containing a thing.
     * Applies effects based on the type of thing.
     *
     * @param row The row of the cell the player moved to.
     * @param col The column of the cell the player moved to.
     */
    private void handleInteraction(int row, int col) {
        Cell cell = currentMap.getCell(row, col);
        Thing thing = cell.getThing();

        if (thing == null) {
            // Cell is empty, no interaction needed
            return;
        }

        switch (thing.getType()) {
            case GOLD:
                player.addScore(2);
                cell.setThing(null); // Gold is picked up
                addGameStatusMessage("You picked up a gold. Score: " + player.getScore());
                break;
            case HEALTH_POTION:
                player.heal(4);
                cell.setThing(null); // Potion is consumed
                addGameStatusMessage("You consumed a health potion and restored 4 HP. Current HP: " + player.getHp());
                break;
            case TRAP:
                player.takeDamage(2);
                addGameStatusMessage("You fell into a trap and lost 2 HP. Current HP: " + player.getHp());
                // Trap remains active (not removed from cell by default interaction)
                break;
            case MELEE_MUTANT:
                player.takeDamage(2);
                player.addScore(2);
                cell.setThing(null); // Mutant is defeated and removed
                addGameStatusMessage("You attacked a melee mutant and wins. Lost 2 HP, gained 2 score. Current HP: " + player.getHp() + ", Score: " + player.getScore());
                break;
            case RANGED_MUTANT:
                // Ranged mutant is defeated by stepping on it (no HP lost directly)
                player.addScore(2);
                cell.setThing(null); // Mutant is defeated and removed
                addGameStatusMessage("You attacked a ranged mutant and wins. Gained 2 score. Score: " + player.getScore());
                break;
            case BOMB: // Bomb is now picked up, not detonated on step
                player.addBomb(1);
                cell.setThing(null); // Bomb is picked up
                addGameStatusMessage("You picked up a bomb! You now have " + player.getBombCount() + " bomb(s).");
                break;
            case LADDER:
                // --- Score increase for ladder interaction ---
                int ladderScoreBonus = 10 * this.currentDifficulty; // Calculate bonus
                player.addScore(ladderScoreBonus); // Add score
                addGameStatusMessage("You gained " + ladderScoreBonus + " score for finding the ladder!"); // Message
                // --- End score increase ---

                if (currentLevel == 1) {
                    addGameStatusMessage("You found the ladder! Advancing to Level 2...");
                    advanceLevel();
                } else { // currentLevel == 2
                    addGameStatusMessage("You found the ladder and escaped the dungeon!");
                    gameStatus = GameStatus.WON;
                }
                break;
            case ENTRY:
                // Entry point, no specific interaction
                break;
            case WALL:
                // Should not happen as traversable check prevents moving onto walls
                break;
            case PLAYER:
                // Player is conceptually on the cell, not an item to interact with
                break;
        }
    }

    /**
     * Creates an instance of a Thing subclass based on the ThingType.
     * @param type The ThingType to instantiate.
     * @return A new Thing instance corresponding to the type, or null if type is not recognized.
     */
    private Thing createThingInstance(ThingType type) {
        switch (type) {
            case GOLD:
                return new Gold();
            case HEALTH_POTION:
                return new HealthPotion();
            case TRAP:
                return new Trap();
            case MELEE_MUTANT:
                return new MeleeMutant();
            case RANGED_MUTANT:
                return new RangedMutant();
            case BOMB:
                return new Bomb();
            // WALL, ENTRY, LADDER, PLAYER are handled by DungeonMap or not needed here
            default:
                return null;
        }
    }

    /**
     * Attempts to activate a bomb at the player's current position.
     * This method is called when the player chooses to use a bomb from their inventory.
     * @return true if a bomb was successfully activated, false otherwise.
     */
    public boolean activateBomb() {
        if (gameStatus != GameStatus.PLAYING) {
            addGameStatusMessage("Cannot activate bomb: Game is not playing.");
            return false;
        }
        if (player.getBombCount() > 0) {
            if (player.useBomb()) { // Use one bomb from inventory
                addGameStatusMessage("You activated a bomb at your current location!");
                player.addScore(5); // Increase score for detonating bomb
                addGameStatusMessage("Gained 5 score. Current Score: " + player.getScore());
                detonateBomb(player.getPosition().getRow(), player.getPosition().getCol()); // Detonate at player's position
                return true;
            }
        } else {
            addGameStatusMessage("You have no bombs to activate!");
        }
        return false;
    }

    /**
     * Detonates a bomb at the given position, clearing adjacent walls and traps.
     * @param bombRow The row of the bomb.
     * @param bombCol The column of the bomb.
     */
    private void detonateBomb(int bombRow, int bombCol) {
        // Use the getAdjacentThingPositions from DungeonMap
        List<Position> affectedPositions = currentMap.getAdjacentThingPositions(
            new Position(bombRow, bombCol), ThingType.WALL, ThingType.TRAP
        );

        for (Position pos : affectedPositions) {
            Cell cell = currentMap.getCell(pos.getRow(), pos.getCol());
            Thing thing = cell.getThing();
            if (thing != null) {
                cell.setThing(null); // Destroy the wall or trap
                addGameStatusMessage("Bomb destroyed " + thing.getType().getDescription() + " at (" + pos.getRow() + ", " + pos.getCol() + ").");
            }
        }
    }

    /**
     * Handles attacks from Ranged Mutants within 2 tiles of the player.
     * Attacks have a 50% chance to hit.
     */
    private void handleRangedMutantAttacks() {
        Position playerPos = player.getPosition();
        List<Position> mutantsToAttack = new ArrayList<>();

        // Get current positions of ranged mutants
        List<Position> rangedMutantPositions = currentMap.getRangedMutantPositions();

        for (Position mutantPos : rangedMutantPositions) {
            // Calculate Manhattan distance
            int distance = Math.abs(playerPos.getRow() - mutantPos.getRow()) + Math.abs(playerPos.getCol() - mutantPos.getCol());
            if (distance <= 2) {
                mutantsToAttack.add(mutantPos);
            }
        }

        for (Position mutantPos : mutantsToAttack) {
            // 50% chance to hit
            if (random.nextBoolean()) { // true for hit, false for miss
                player.takeDamage(2);
                addGameStatusMessage("A ranged mutant at " + mutantPos + " attacked and you lost 2 HP. Current HP: " + player.getHp());
            } else {
                addGameStatusMessage("A ranged mutant at " + mutantPos + " attacked, but missed.");
            }
        }
    }

    /**
     * Advances the player to the next level.
     * Creates a new map for Level 2 and places the player at the Level 1 ladder's position.
     */
    private void advanceLevel() {
        currentLevel++;
        // Difficulty increases by 2 for Level 2
        int newDifficulty = DEFAULT_DIFFICULTY + (currentLevel - 1) * 2; // e.g., Level 2: 3 + 2 = 5
        this.currentDifficulty = newDifficulty; // Update current difficulty for the new level

        addGameStatusMessage("Entering Level " + currentLevel + " with difficulty " + this.currentDifficulty + "...");

        // The ladder position from Level 1 becomes the entry point for Level 2
        // We need to find the ladder position from the previous map.
        Position oldLadderPos = findThingPosition(currentMap, ThingType.LADDER);
        if (oldLadderPos == null) {
            // Fallback, should not happen if Level 1 was generated correctly
            oldLadderPos = playerEntryPositionLevel1; // Use Level 1 entry as fallback
            addGameStatusMessage("Warning: Could not find Level 1 ladder. Using default entry for Level 2.");
        }
        this.playerEntryPositionLevel2 = oldLadderPos; // Store for potential saving/loading if needed

        this.currentMap = new DungeonMap(currentDifficulty, false, playerEntryPositionLevel2);
        player.setPosition(playerEntryPositionLevel2); // Place player at the new entry point
        addGameStatusMessage("Player HP: " + player.getHp() + ", Score: " + player.getScore() + ", Steps: " + stepsTaken);
    }

    /**
     * Finds the position of a specific ThingType on the given map.
     *
     * @param map The DungeonMap to search.
     * @param type The ThingType to find.
     * @return The Position of the first occurrence of the ThingType, or null if not found.
     */
    private Position findThingPosition(DungeonMap map, ThingType type) {
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getCols(); c++) {
                if (map.getCell(r, c).containsThingType(type)) {
                    return new Position(r, c);
                }
            }
        }
        return null;
    }

    /**
     * Checks the current game state for win or loss conditions.
     * Updates the gameStatus accordingly.
     */
    private void checkGameEndConditions() {
        if (player.getHp() <= 0) {
            gameStatus = GameStatus.LOST;
            player.setScore(-1); // Set score to -1 on loss
            addGameStatusMessage("Your HP dropped to 0! Game Over.");
            addGameStatusMessage("Final Score: " + player.getScore());
        } else if (stepsTaken >= maxSteps) {
            gameStatus = GameStatus.LOST;
            player.setScore(-1); // Set score to -1 on loss
            addGameStatusMessage("You ran out of steps! Game Over.");
            addGameStatusMessage("Final Score: " + player.getScore());
        } else if (gameStatus == GameStatus.WON) { // Already set to WON by ladder interaction
            addGameStatusMessage("Congratulations! You successfully escaped the dungeon!");
            addGameStatusMessage("Final Score: " + player.getScore());
            updateTopScores(player.getScore());
        }
    }

    /**
     * Adds a new score to the top scores list if it qualifies.
     * Maintains only the top 5 scores.
     *
     * @param finalScore The score to potentially add.
     */
    private void updateTopScores(int finalScore) {
        if (finalScore <= 0) { // Only positive scores can be top scores
            return;
        }

        ScoreEntry newScore = new ScoreEntry(finalScore);
        topScores.add(newScore);
        Collections.sort(topScores); // Sorts in descending order of score

        // Keep only the top MAX_TOP_SCORES entries
        while (topScores.size() > MAX_TOP_SCORES) {
            topScores.remove(topScores.size() - 1); // Remove the lowest score
        }

        // Check if the new score made it into the top 5
        if (topScores.contains(newScore)) {
            addGameStatusMessage("🎉 New High Score! You made it into the Top " + MAX_TOP_SCORES + "! 🎉");
        }
    }

    /**
     * Returns a formatted string of the current top scores.
     *
     * @return A string listing the top scores.
     */
    public String getTopScoresDisplay() {
        if (topScores.isEmpty()) {
            return "No top scores yet.";
        }
        StringBuilder sb = new StringBuilder("--- Top Scores ---\n");
        for (int i = 0; i < topScores.size(); i++) {
            ScoreEntry entry = topScores.get(i);
            sb.append("#").append(i + 1).append(" ")
              .append(entry.getScore()).append(" ")
              .append(entry.getFormattedDate()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns the current game status (Playing, Won, Lost).
     *
     * @return The GameStatus enum value.
     */
    public GameStatus getGameStatus() {
        return gameStatus;
    }

    /**
     * Returns a string representation of the player's current status.
     *
     * @return A string showing HP, Score, Steps, and Bombs.
     */
    public String getPlayerStatus() {
        return "HP: " + player.getHp() + " | Score: " + player.getScore() + " | Steps: " + stepsTaken + "/" + maxSteps + " | Bombs: " + player.getBombCount();
    }

    /**
     * Adds a message to the game status messages list.
     *
     * @param message The message to add.
     */
    public void addGameStatusMessage(String message) {
        this.gameStatusMessages.add(message);
        // Keep the message list from growing indefinitely for text UI, maybe last 10 messages
        if (gameStatusMessages.size() > 10) {
            gameStatusMessages.remove(0);
        }
    }

    /**
     * Returns a string containing all accumulated game status messages.
     *
     * @return A multi-line string of game status messages.
     */
    public String getGameStatusMessages() {
        StringBuilder sb = new StringBuilder();
        for (String msg : gameStatusMessages) {
            sb.append(msg).append("\n");
        }
        return sb.toString();
    }

    /**
     * Clears all accumulated game status messages.
     */
    public void clearGameStatusMessages() {
        this.gameStatusMessages.clear();
    }

    /**
     * Returns the current DungeonMap instance.
     * This is crucial for the GUI to render the map.
     * @return The current DungeonMap.
     */
    public DungeonMap getCurrentMap() {
        return currentMap;
    }

    /**
     * Returns the player's current position.
     * @return The player's Position.
     */
    public Position getPlayerPosition() {
        return player.getPosition();
    }

    /**
     * Returns the player's current HP.
     * @return The player's HP.
     */
    public int getPlayerHp() {
        return player.getHp();
    }

    /**
     * Returns the player's current score.
     * @return The player's score.
     */
    public int getPlayerScore() {
        return player.getScore();
    }

    /**
     * Returns the number of steps taken by the player.
     * @return The steps taken.
     */
    public int getStepsTaken() {
        return stepsTaken;
    }

    /**
     * Returns the maximum allowed steps for the game.
     * @return The maximum steps.
     */
    public int getMaxSteps() {
        return maxSteps;
    }

    /**
     * Returns the current level of the game.
     * @return The current level.
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Returns the Player object. This is primarily for testing and internal use.
     * @return The Player object.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Main method for text-based game play.
     * This will be the entry point for running the game in the console.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GameEngine game = new GameEngine();

        System.out.println("Welcome to MiniDungeon!");
        System.out.print("Enter game difficulty (0-10, default 3): ");
        int difficulty = DEFAULT_DIFFICULTY;
        try {
            String input = scanner.nextLine();
            if (!input.trim().isEmpty()) {
                difficulty = Integer.parseInt(input);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input for difficulty. Using default (3).");
        }

        game.startGame(difficulty);

        while (game.getGameStatus() == GameStatus.PLAYING) {
            // Clear console (basic attempt, may not work on all systems)
            // System.out.print("\033[H\033[2J");
            // System.out.flush();

            game.currentMap.printMap(game.player.getPosition()); // Print map with player
            System.out.println(game.getPlayerStatus());
            System.out.println("\n--- Game Log ---");
            System.out.println(game.getGameStatusMessages());
            game.clearGameStatusMessages(); // Clear messages after displaying

            System.out.print("Enter your move (u/d/l/r): ");
            String move = scanner.nextLine();
            game.movePlayer(move);
        }

        // Game ended, display final state and messages
        game.currentMap.printMap(game.player.getPosition());
        System.out.println(game.getPlayerStatus());
        System.out.println("\n--- Final Game Log ---");
        System.out.println(game.getGameStatusMessages());
        System.out.println(game.getTopScoresDisplay());

        scanner.close();
    }

    // --- DTOs for Save/Load (Made STATIC) ---
    public static class GameSaveState {
        public PlayerData player;
        public GameProgressData gameProgress;
        public MapData map;
        public List<ScoreEntry> topScores;

        public GameSaveState() {} // Default constructor for Jackson
        // Getters and setters for all fields (Jackson needs these)
        public PlayerData getPlayer() { return player; }
        public void setPlayer(PlayerData player) { this.player = player; }
        public GameProgressData getGameProgress() { return gameProgress; }
        public void setGameProgress(GameProgressData gameProgress) { this.gameProgress = gameProgress; }
        public MapData getMap() { return map; }
        public void setMap(MapData map) { this.map = map; }
        public List<ScoreEntry> getTopScores() { return topScores; }
        public void setTopScores(List<ScoreEntry> topScores) { this.topScores = topScores; }
    }

    public static class PlayerData {
        public int hp;
        public int score;
        public int row;
        public int col;
        public int bombCount;

        public PlayerData() {}
        public PlayerData(Player player) {
            this.hp = player.getHp();
            this.score = player.getScore();
            this.row = player.getPosition().getRow();
            this.col = player.getPosition().getCol();
            this.bombCount = player.getBombCount();
        }
        // Getters and setters for all fields
        public int getHp() { return hp; }
        public void setHp(int hp) { this.hp = hp; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public int getRow() { return row; }
        public void setRow(int row) { this.row = row; }
        public int getCol() { return col; }
        public void setCol(int col) { this.col = col; }
        public int getBombCount() { return bombCount; }
        public void setBombCount(int bombCount) { this.bombCount = bombCount; }
    }

    public static class GameProgressData {
        public int currentLevel;
        public int stepsTaken;
        public int currentDifficulty;
        public int level1LadderRow;
        public int level1LadderCol;

        public GameProgressData() {}
        public GameProgressData(GameEngine engine, Position level1LadderPos) {
            this.currentLevel = engine.getCurrentLevel();
            this.stepsTaken = engine.getStepsTaken();
            this.currentDifficulty = engine.currentDifficulty;
            if (level1LadderPos != null) {
                this.level1LadderRow = level1LadderPos.getRow();
                this.level1LadderCol = level1LadderPos.getCol();
            } else {
                this.level1LadderRow = -1; // Indicate not set
                this.level1LadderCol = -1;
            }
        }
        // Getters and setters
        public int getCurrentLevel() { return currentLevel; }
        public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }
        public int getStepsTaken() { return stepsTaken; }
        public void setStepsTaken(int stepsTaken) { this.stepsTaken = stepsTaken; }
        public int getCurrentDifficulty() { return currentDifficulty; }
        public void setCurrentDifficulty(int currentDifficulty) { this.currentDifficulty = currentDifficulty; }
        public int getLevel1LadderRow() { return level1LadderRow; }
        public void setLevel1LadderRow(int level1LadderRow) { this.level1LadderRow = level1LadderRow; }
        public int getLevel1LadderCol() { return level1LadderCol; }
        public void setLevel1LadderCol(int level1LadderCol) { this.level1LadderCol = level1LadderCol; }
    }

    public static class MapData {
        public int rows;
        public int cols;
        public String[][] gridSymbols; // Use string symbols for ThingType
        public List<Position> rangedMutantPositions; // Position is already a simple POJO

        public MapData() {}
        public MapData(DungeonMap map) {
            this.rows = map.getRows();
            this.cols = map.getCols();
            this.gridSymbols = new String[rows][cols];
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Cell cell = map.getCell(r, c);
                    this.gridSymbols[r][c] = cell.isEmpty() ? " " : cell.getThing().getSymbol();
                }
            }
            this.rangedMutantPositions = map.getRangedMutantPositions(); // Copies the list
        }
        // Getters and setters
        public int getRows() { return rows; }
        public void setRows(int rows) { this.rows = rows; }
        public int getCols() { return cols; }
        public void setCols(int cols) { this.cols = cols; }
        public String[][] getGridSymbols() { return gridSymbols; }
        public void setGridSymbols(String[][] gridSymbols) { this.gridSymbols = gridSymbols; }
        public List<Position> getRangedMutantPositions() { return rangedMutantPositions; }
        public void setRangedMutantPositions(List<Position> rangedMutantPositions) { this.rangedMutantPositions = rangedMutantPositions; }
    }

    // --- End DTOs ---


    public void saveGame(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print JSON

        // To handle LocalDate in ScoreEntry (if not already handled)
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public void serialize(LocalDate date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        });
        module.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                return LocalDate.parse(jsonParser.getText(), DateTimeFormatter.ISO_LOCAL_DATE);
            }
        });
        mapper.registerModule(module);

        try {
            GameSaveState saveState = new GameSaveState();
            saveState.player = new PlayerData(this.player);
            saveState.gameProgress = new GameProgressData(this, findThingPosition(this.currentMap, ThingType.LADDER)); // Pass current ladder position
            saveState.map = new MapData(this.currentMap);
            saveState.topScores = new ArrayList<>(this.topScores); // Create a copy

            mapper.writeValue(new File(filename), saveState);
            addGameStatusMessage("Game saved to " + filename);
        } catch (IOException e) {
            addGameStatusMessage("Error saving game: " + e.getMessage());
            System.err.println("Error saving game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadGame(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        // To handle LocalDate in ScoreEntry (if not already handled)
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public void serialize(LocalDate date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        });
        module.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                return LocalDate.parse(jsonParser.getText(), DateTimeFormatter.ISO_LOCAL_DATE);
            }
        });
        mapper.registerModule(module);

        try {
            File file = new File(filename);
            if (!file.exists()) {
                addGameStatusMessage("Load failed: Save file not found at " + filename);
                return;
            }

            GameSaveState loadedState = mapper.readValue(file, GameSaveState.class);

            // Reconstruct player
            this.player = new Player(loadedState.player.hp, loadedState.player.score, new Position(loadedState.player.row, loadedState.player.col));
            this.player.addBomb(loadedState.player.bombCount); // Set bomb count explicitly

            // Reconstruct game progress
            this.currentLevel = loadedState.gameProgress.currentLevel;
            this.stepsTaken = loadedState.gameProgress.stepsTaken;
            this.currentDifficulty = loadedState.gameProgress.currentDifficulty;

            // Reconstruct map
            Position loadedLadderPos = null;
            if (loadedState.gameProgress.level1LadderRow != -1 && loadedState.gameProgress.level1LadderCol != -1) {
                loadedLadderPos = new Position(loadedState.gameProgress.level1LadderRow, loadedState.gameProgress.level1LadderCol);
            }
            this.currentMap = new DungeonMap(loadedState.gameProgress.currentDifficulty, loadedState.gameProgress.currentLevel == 1, loadedLadderPos);
            for (int r = 0; r < loadedState.map.rows; r++) {
                for (int c = 0; c < loadedState.map.cols; c++) {
                    String symbol = loadedState.map.gridSymbols[r][c];
                    ThingType type = ThingType.fromSymbol(symbol);

                    // Skip placing WALL, ENTRY, LADDER, PLAYER as they are handled by DungeonMap's constructor
                    // or are temporary (PLAYER)
                    if (type != null && type != ThingType.WALL && type != ThingType.ENTRY && type != ThingType.LADDER && type != ThingType.PLAYER) {
                        // Create an instance of the specific Thing (e.g., Gold, Trap, Mutant)
                        this.currentMap.getCell(r, c).setThing(createThingInstance(type));
                    } else if (type == null || symbol.equals(" ")) { // Explicitly set to empty if it was empty in save
                         this.currentMap.getCell(r,c).setThing(null);
                    }
                }
            }
            this.currentMap.getRangedMutantPositions().clear(); // Clear existing
            if (loadedState.map.rangedMutantPositions != null) {
                for (Position p : loadedState.map.rangedMutantPositions) {
                     if (this.currentMap.isValidPosition(p.getRow(), p.getCol())) {
                        this.currentMap.getCell(p.getRow(), p.getCol()).setThing(new RangedMutant()); // Place mutant
                        this.currentMap.getRangedMutantPositions().add(p); // Add to internal list for game logic
                    }
                }
            }


            // Reconstruct top scores
            this.topScores.clear();
            this.topScores.addAll(loadedState.topScores);
            Collections.sort(this.topScores); // Re-sort to ensure order

            this.gameStatus = GameStatus.PLAYING; // Assume loaded game is playing
            addGameStatusMessage("Game loaded from " + filename);
        } catch (IOException e) {
            addGameStatusMessage("Error loading game: " + e.getMessage());
            System.err.println("Error loading game: " + e.getMessage());
            e.printStackTrace();
        }
    }
}