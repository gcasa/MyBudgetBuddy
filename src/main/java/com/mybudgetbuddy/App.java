package com.mybudgetbuddy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.mybudgetbuddy.controller.MainController;
import com.mybudgetbuddy.service.BudgetService;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        BudgetService budgetService = new BudgetService();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mybudgetbuddy/main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setBudgetService(budgetService);
        controller.initialize();

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/com/mybudgetbuddy/styles.css").toExternalForm());

        primaryStage.setTitle("MyBudgetBuddy");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
