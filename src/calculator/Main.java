package calculator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("mainWindow.fxml"));
        primaryStage.setTitle("My Calculator");
        primaryStage.setScene(new Scene(root,  280, 330));
        primaryStage.getIcons().setAll(new Image("File:/Users/ngyen/IdeaProjects/Calculator/calculator.png"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
