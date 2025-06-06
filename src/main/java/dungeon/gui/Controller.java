package dungeon.gui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import dungeon.engine.Cell;
import dungeon.engine.GameEngine;
import dungeon.engine.GameStatus;
import dungeon.engine.Player;
import dungeon.engine.Position;
import dungeon.engine.ThingType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

/**
 * Controller class for the MiniDungeon JavaFX GUI.
 * This class handles user interactions, updates the game state via the GameEngine,
 * and refreshes the graphical display.
 */
public class Controller {

    private GameEngine engine;

    // FXML elements injected from the .fxml file
    @FXML
    private GridPane gridPane; // GridPane to display the game map
    @FXML
    private Label hpLabel; // Label to display the player's health points (HP)
    @FXML
    private Label scoreLabel; // Label to display the player's score
    @FXML
    private Label stepsLabel; // Label to display the number of steps taken by the player
    @FXML
    private TextArea gameLogArea; // TextArea for displaying game logs and messages
    @FXML
    private TextField difficultyInput; // TextField for user to input game difficulty
    @FXML
    private TextArea topScoresArea; // TextArea to display top scores
    @FXML
    private Label bombCountLabel; // Label to display the number of bombs

    // Map to hold images for each ThingType for efficient lookup
    private Map<ThingType, Image> thingImages;
    private Image playerImage; // Separate image for the player

    /**
     * Constructor for the Controller.
     * Initializes the GameEngine and loads all necessary images for the game.
     */
    public Controller() {
        engine = new GameEngine();
        thingImages = new HashMap<>();
        loadThingImages(); // Load images when the controller is created
    }

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method is automatically called by JavaFX.
     * Sets initial text for display labels and game log.
     */
     @FXML
    public void initialize() {
        hpLabel.setText("HP: " + GameEngine.INITIAL_PLAYER_HP);
        scoreLabel.setText("Score: " + GameEngine.INITIAL_SCORE);
        stepsLabel.setText("Steps: 0/" + GameEngine.DEFAULT_MAX_STEPS);
        // Initialize bomb count label to 0, as player object might not be fully initialized yet
        // The actual count will be updated in updatePlayerStatsDisplay() after game starts
        if (bombCountLabel != null) {
            bombCountLabel.setText("Bombs: 0");
        }
        gameLogArea.setText("Welcome to MiniDungeon!\nClick 'Start Game' to begin.");
        topScoresArea.setText(engine.getTopScoresDisplay()); // Display initial top scores
    }

    /**
     * Loads images for each ThingType from the resources folder.
     * It's crucial that these image files exist at the specified paths
     * (e.g., src/main/resources/images/).
     * Handles NullPointerException if images are not found.
     */
    private void loadThingImages() {
        try {
            // Adjust these paths if your image files are located elsewhere in your resources
            thingImages.put(ThingType.ENTRY, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/entry.png"))));
            thingImages.put(ThingType.LADDER, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/ladder.png"))));
            thingImages.put(ThingType.WALL, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/wall.png"))));
            thingImages.put(ThingType.TRAP, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/trap.png"))));
            thingImages.put(ThingType.GOLD, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/gold.png"))));
            thingImages.put(ThingType.MELEE_MUTANT, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/melee_mutant.png"))));
            thingImages.put(ThingType.RANGED_MUTANT, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/ranged_mutant.png"))));
            thingImages.put(ThingType.HEALTH_POTION, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/health_potion.png"))));
            thingImages.put(ThingType.BOMB, new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/bomb.png"))));
            playerImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/player.png"))); // Player image
        } catch (NullPointerException e) {
            System.err.println("Error loading images. Make sure image files exist in src/main/resources/images/ and paths are correct.");
            e.printStackTrace();
            gameLogArea.appendText("ERROR: Could not load game images. Check console for details.\n");
            // Consider adding placeholder images or visual cues if images are missing
        }
    }

    /**
     * Handles the action when the "Start Game" button is clicked.
     * Reads difficulty, starts a new game, and updates the GUI.
     */
    @FXML
    private void startGame(ActionEvent event) {
        int difficulty = GameEngine.DEFAULT_DIFFICULTY;
        try {
            String input = difficultyInput.getText();
            if (!input.trim().isEmpty()) {
                difficulty = Integer.parseInt(input);
            }
        } catch (NumberFormatException e) {
            gameLogArea.appendText("Invalid difficulty input. Using default (3).\n");
        }

        engine.startGame(difficulty); // Call startGame on the engine
        updateGUI(); // Refresh the GUI after starting the game
    }

    /**
     * Handles the action when the "Move Up" button is clicked.
     * Instructs the game engine to move the player up and updates the GUI.
     */
    @FXML
    private void moveUp() {
        engine.movePlayer("u");
        updateGUI();
    }

    /**
     * Handles the action when the "Move Down" button is clicked.
     * Instructs the game engine to move the player down and updates the GUI.
     */
    @FXML
    private void moveDown() {
        engine.movePlayer("d");
        updateGUI();
    }

    /**
     * Handles the action when the "Move Left" button is clicked.
     * Instructs the game engine to move the player left and updates the GUI.
     */
    @FXML
    private void moveLeft() {
        engine.movePlayer("l");
        updateGUI();
    }

    /**
     * Handles the action when the "Move Right" button is clicked.
     * Instructs the game engine to move the player right and updates the GUI.
     */
    @FXML
    private void moveRight() {
        engine.movePlayer("r");
        updateGUI();
    }

    @FXML
    private void activateBomb() {
        engine.activateBomb();
        updateGUI(); // Update GUI to reflect bomb usage and map changes
    }

    /**
     * Refreshes all GUI elements based on the current game state.
     * This method is called after every player action.
     */
    private void updateGUI() {
        updateMapDisplay();
        updatePlayerStatsDisplay();
        updateGameLog();
        updateTopScoresDisplay(); // Update top scores if game ended
    }

    /**
     * Updates the visual representation of the game map in the GridPane.
     * Clears the existing grid and repopulates it with ImageView nodes
     * representing the current state of each cell and the player.
     */
    private void updateMapDisplay() {
        gridPane.getChildren().clear(); // Clear existing nodes from the grid
        if (engine.getCurrentMap() == null) {
            return; // Map not initialized yet (e.g., before game starts)
        }

        int rows = engine.getCurrentMap().getRows();
        int cols = engine.getCurrentMap().getCols();
        Position playerPos = engine.getPlayerPosition();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                StackPane cellPane = new StackPane(); // Use StackPane for each cell
                cellPane.setPrefSize(50, 50); // Set a preferred size for each cell (adjust as needed)
                cellPane.setStyle("-fx-border-color: #333; -fx-border-width: 0.5; -fx-background-color: #222;"); // Basic cell styling

                ImageView imageView = new ImageView();
                imageView.setFitWidth(48); // Adjust image size to fit cell
                imageView.setFitHeight(48);
                imageView.setPreserveRatio(true);

                // Determine what to display in the cell
                if (playerPos != null && r == playerPos.getRow() && c == playerPos.getCol()) {
                    // Display player image if this is the player's current position
                    imageView.setImage(playerImage);
                } else {
                    // Display the thing on the cell
                    Cell cell = engine.getCurrentMap().getCell(r, c);
                    ThingType thingType = cell.isEmpty() ? null : cell.getThing().getType();

                    if (thingType != null && thingImages.containsKey(thingType)) {
                        imageView.setImage(thingImages.get(thingType));
                    } else {
                        // If no specific image, or cell is empty, use a default background or empty image
                        // For empty cells, you might want a specific "floor" image or just a background color
                        imageView.setImage(null); // Clear image if no thing or no image found
                    }
                }
                cellPane.getChildren().add(imageView);
                gridPane.add(cellPane, c, r); // Add the StackPane to the GridPane (col, row)
            }
        }
    }

    /**
     * Updates the player's HP, Score, Steps, and Bomb labels with current game data.
     */
    private void updatePlayerStatsDisplay() {
        hpLabel.setText("HP: " + engine.getPlayerHp());
        scoreLabel.setText("Score: " + engine.getPlayerScore());
        stepsLabel.setText("Steps: " + engine.getStepsTaken() + "/" + engine.getMaxSteps());
        // Ensure player object is not null before trying to get bomb count
        if (engine.getPlayer() != null) {
            bombCountLabel.setText("Bombs: " + engine.getPlayer().getBombCount());
        } else {
            bombCountLabel.setText("Bombs: 0"); // Default if player not initialized
        }
    }

    /**
     * Appends new game log messages from the GameEngine to the TextArea.
     */
    private void updateGameLog() {
        gameLogArea.appendText(engine.getGameStatusMessages());
        engine.clearGameStatusMessages(); // Clear messages from engine after displaying
    }

    /**
     * Updates the top scores display, particularly after a game ends.
     */
    private void updateTopScoresDisplay() {
        if (engine.getGameStatus() != GameStatus.PLAYING) {
            topScoresArea.setText(engine.getTopScoresDisplay());
        }
    }

    /**
     * Displays game instructions when the "Help" button is clicked.
     */
    @FXML
    private void showHelp() {
        gameLogArea.appendText("\n--- HELP ---\nNavigate the dungeon (u/d/l/r). Collect G for gold (+2 score). H for health (+4 HP). Avoid T for traps (-2 HP). M for melee mutants (-2 HP, +2 score). R for ranged mutants (50% chance -2 HP from 2 tiles away, +2 score when defeated). B for bomb (+5 score, destroys adjacent walls and traps). Reach L to advance or escape. Max steps: " + GameEngine.DEFAULT_MAX_STEPS + ", Max HP: " + Player.MAX_HP + ".\n");
    }

    @FXML
    private void saveGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Game");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MiniDungeon Save", "*.json"));
        File file = fileChooser.showSaveDialog(gridPane.getScene().getWindow()); // Use any scene window for parent

        if (file != null) {
            engine.saveGame(file.getAbsolutePath());
            updateGameLog(); // Update log to show save status
        } else {
            gameLogArea.appendText("Save cancelled.\n");
        }
    }

    /**
     * Handles the action when the "Load Game" button is clicked.
     * Opens a file dialog to let the user choose which game file to load.
     */
    @FXML
    private void loadGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Game");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MiniDungeon Save", "*.json"));
        File file = fileChooser.showOpenDialog(gridPane.getScene().getWindow());

        if (file != null) {
            engine.loadGame(file.getAbsolutePath());
            updateGUI(); // Refresh GUI after loading game state
        } else {
            gameLogArea.appendText("Load cancelled.\n");
        }
    }
}
