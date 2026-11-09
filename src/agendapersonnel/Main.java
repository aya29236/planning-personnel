package agendapersonnel;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // ── 1. Initialiser la base de données Oracle ─────────────────────────
        DatabaseManager.initDatabase();

        // ── 2. Vérifier si c'est le premier lancement ────────────────────────
        Properties prefs = loadPreferences();
        boolean firstLaunch = Boolean.parseBoolean(
            prefs.getProperty("firstLaunch", "true")
        );

        Parent root;
        Scene  scene;

        if (firstLaunch) {
            // ── Premier lancement → Welcome Screen ───────────────────────────
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("Welcome.fxml")
            );
            root  = loader.load();
            scene = new Scene(root, 600, 520);

            // Appliquer le thème par défaut (girly)
            ThemeManager.applyTheme(scene, "girly");

        } else {
            // ── Lancement suivant → Aller directement au MainView ────────────
            String savedTheme  = prefs.getProperty("theme",  "girly");
            String savedPrenom = prefs.getProperty("prenom", "");

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("MainView.fxml")
            );
            root  = loader.load();
            scene = new Scene(root, 900, 620);

            ThemeManager.applyTheme(scene, savedTheme);

            // Passer les données sauvegardées au MainController
            agendapersonnel.MainController ctrl = loader.getController();
            ctrl.initData(
                savedPrenom.isEmpty() ? null : savedPrenom,
                savedTheme
            );
        }

        // ── 3. Configurer la fenêtre principale ──────────────────────────────
        primaryStage.setTitle("Mon Agenda Personnel 📅");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(500);
        primaryStage.centerOnScreen();

        // ── 4. Animation de démarrage ────────────────────────────────────────
        FadeTransition ft = new FadeTransition(Duration.millis(800), root);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();

        // ── 5. Fermer la DB proprement à la fermeture ────────────────────────
        primaryStage.setOnCloseRequest(e -> {
            DatabaseManager.closeConnection();
            System.out.println("👋 Connexion Oracle fermée. Au revoir !");
        });

        primaryStage.show();
    }

    // ── Charger les préférences sauvegardées ─────────────────────────────────
    private Properties loadPreferences() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("agenda_prefs.properties")) {
            props.load(fis);
        } catch (IOException e) {
            // Pas encore de fichier → premier lancement
            props.setProperty("firstLaunch", "true");
        }
        return props;
    }

    // ── Point d'entrée ───────────────────────────────────────────────────────
    public static void main(String[] args) {
        launch(args);
    }
}