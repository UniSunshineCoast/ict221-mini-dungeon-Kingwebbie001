package dungeon.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * GUI for the Maze Runner Game.
 *
 * NOTE: Do NOT run this class directly in IntelliJ - run 'RunGame' instead.
 */
public class GameGUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = FXMLLoader.load(getClass().getResource("game_gui.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("MiniDungeon Game");
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("Press ESC to exit fullscreen"); // Optional: custom hint
        primaryStage.show();
    }

    /** In IntelliJ, do NOT run this method.  Run 'RunGame.main()' instead. */
    public static void main(String[] args) {
        launch(args);
    }
}
