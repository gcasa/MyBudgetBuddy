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
        
        // Add application icon with fallback
        boolean iconLoaded = false;
        try {
            // Try to load custom icon first
            java.io.InputStream iconStream = getClass().getResourceAsStream("/com/mybudgetbuddy/icons/app-icon.png");
            if (iconStream != null) {
                javafx.scene.image.Image appIcon = new javafx.scene.image.Image(iconStream);
                if (!appIcon.isError()) {
                    // Add multiple sizes for better compatibility
                    primaryStage.getIcons().addAll(
                        appIcon, 
                        new javafx.scene.image.Image(iconStream, 16, 16, true, true),
                        new javafx.scene.image.Image(iconStream, 32, 32, true, true),
                        new javafx.scene.image.Image(iconStream, 64, 64, true, true)
                    );
                    iconLoaded = true;
                    System.out.println("✅ Custom app icon loaded successfully (multiple sizes)");
                } else {
                    System.out.println("❌ Custom app icon failed to load: " + appIcon.getException());
                }
                iconStream.close();
            } else {
                System.out.println("❌ App icon resource not found: /com/mybudgetbuddy/icons/app-icon.png");
            }
        } catch (Exception e) {
            System.out.println("❌ Error loading app icon: " + e.getMessage());
        }
        
        // If custom icon failed, create a simple programmatic icon
        if (!iconLoaded) {
            try {
                System.out.println("🔄 Creating fallback programmatic icon...");
                javafx.scene.image.WritableImage fallbackIcon = new javafx.scene.image.WritableImage(64, 64);
                javafx.scene.image.PixelWriter pw = fallbackIcon.getPixelWriter();
                
                // Create a bright green budget icon
                for (int x = 0; x < 64; x++) {
                    for (int y = 0; y < 64; y++) {
                        // Green circular background
                        double centerX = 32, centerY = 32, radius = 28;
                        double distance = Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
                        
                        if (distance <= radius) {
                            pw.setColor(x, y, javafx.scene.paint.Color.web("#4CAF50")); // Bright green
                            
                            // Add dollar sign shape (more visible)
                            boolean isDollarSign = 
                                // Vertical line
                                (x >= 30 && x <= 34 && y >= 12 && y < 52) ||
                                // Top curve
                                (y >= 18 && y <= 22 && x >= 18 && x <= 46) ||
                                // Middle line  
                                (y >= 28 && y <= 32 && x >= 18 && x <= 46) ||
                                // Bottom curve
                                (y >= 38 && y <= 42 && x >= 18 && x <= 46);
                                
                            if (isDollarSign) {
                                pw.setColor(x, y, javafx.scene.paint.Color.WHITE);
                            }
                        }
                    }
                }
                
                primaryStage.getIcons().add(fallbackIcon);
                System.out.println("✅ Enhanced fallback programmatic icon created");
            } catch (Exception e) {
                System.out.println("❌ Could not create fallback icon: " + e.getMessage());
            }
        }
        
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
