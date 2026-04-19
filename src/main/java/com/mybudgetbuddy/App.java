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

import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends Application {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

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

        Scene scene = new Scene(root, 1800, 1200);
        scene.getStylesheets().add(getClass().getResource("/com/mybudgetbuddy/styles.css").toExternalForm());

        primaryStage.setTitle("MyBudgetBuddy");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(1400);
        primaryStage.setMinHeight(900);
        primaryStage.setMaximized(false);
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Handle clean database shutdown when application closes
        primaryStage.setOnCloseRequest(event -> {
            try {
                DatabaseManager.getInstance().close();
                LOGGER.info("Application shutdown completed successfully");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during application shutdown", e);
            } finally {
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start application", e);
            System.exit(1);
        }
    }
}
