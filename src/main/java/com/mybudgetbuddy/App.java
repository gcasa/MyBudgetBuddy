package com.mybudgetbuddy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.mybudgetbuddy.controller.MainController;
import com.mybudgetbuddy.application.service.TransactionService;
import com.mybudgetbuddy.application.service.impl.TransactionServiceImpl;
import com.mybudgetbuddy.infrastructure.database.DatabaseManager;
import com.mybudgetbuddy.viewmodel.MainViewModel;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create services and repositories
        TransactionService transactionService = new TransactionServiceImpl();
        MainViewModel mainViewModel = new MainViewModel(transactionService);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mybudgetbuddy/main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setTransactionService(transactionService);
        controller.setViewModel(mainViewModel);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/com/mybudgetbuddy/styles.css").toExternalForm());

        primaryStage.setTitle("MyBudgetBuddy");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Handle clean database shutdown when application closes
        primaryStage.setOnCloseRequest(event -> {
            DatabaseManager.getInstance().close();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
