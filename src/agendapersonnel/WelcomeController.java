package agendapersonnel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class WelcomeController implements Initializable {

    @FXML private ImageView welcomeImage;
    @FXML private Label     quoteLabel;
    @FXML private TextField prenomField;
    @FXML private Button    startBtn;
    @FXML private VBox      pinkCard;
    @FXML private VBox      blueCard;

    // Theme sélectionné par défaut
    private String selectedTheme = "girly";

    // Quotes motivantes
    private final String[] QUOTES = {
        "✨ Une journée bien organisée est une journée bien vécue !",
        "🌸 Chaque rendez-vous est une opportunité de briller !",
        "🚀 Les grands projets commencent par une bonne planification !",
        "💪 Organise ton temps, libère ton potentiel !",
        "🌟 Ton agenda, ton royaume — règne avec style !",
        "🎯 Un objectif sans plan n'est qu'un souhait. Planifie !",
        "🌈 La réussite appartient à ceux qui s'organisent !"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Charger une quote aléatoire
        int index = (int)(Math.random() * QUOTES.length);
        quoteLabel.setText(QUOTES[index]);

        // Charger l'image agenda
        try {
            Image img = new Image(
                getClass().getResourceAsStream("222222.jpg")
            );
            welcomeImage.setImage(img);
        } catch (Exception e) {
            System.out.println("Image non trouvée, on continue sans.");
        }

        // Charger les préférences sauvegardées
        loadPreferences();

        // Animation fade-in au démarrage
        FadeTransition ft = new FadeTransition(Duration.millis(800),
                                               startBtn.getScene() != null ?
                                               startBtn.getScene().getRoot() : startBtn);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    // ── SÉLECTION THÈME GIRLY ────────────────────────────────────────────────
    @FXML
    public void selectGirly() {
        selectedTheme = "girly";

        // Highlight carte rose
        pinkCard.setStyle(
            "-fx-background-color: #fbeaf0;" +
            "-fx-border-color: #d4537e;" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );
        blueCard.setStyle(
            "-fx-background-color: #e6f1fb;" +
            "-fx-border-color: #c0d8f0;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );

        // Changer le bouton start
        startBtn.setText("Commencer 🌸");
        startBtn.setStyle(
            "-fx-background-color: #d4537e;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 10 40 10 40;"
        );
    }

    // ── SÉLECTION THÈME BOYS ─────────────────────────────────────────────────
    @FXML
    public void selectBoys() {
        selectedTheme = "boys";

        // Highlight carte bleue
        blueCard.setStyle(
            "-fx-background-color: #e6f1fb;" +
            "-fx-border-color: #185fa5;" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );
        pinkCard.setStyle(
            "-fx-background-color: #fbeaf0;" +
            "-fx-border-color: #f4c0d1;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );

        // Changer le bouton start
        startBtn.setText("Commencer 🚀");
        startBtn.setStyle(
            "-fx-background-color: #185fa5;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 10 40 10 40;"
        );
    }

    // ── BOUTON COMMENCER ─────────────────────────────────────────────────────
    @FXML
    public void handleStart() {
        try {
            // Sauvegarder les préférences
            savePreferences();

            // Charger la fenêtre principale
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("MainView.fxml")
            );
            Parent root = loader.load();

            // Passer le prénom et thème au MainController
            MainController mainCtrl = loader.getController();
            String prenom = prenomField.getText().trim();
            mainCtrl.initData(prenom.isEmpty() ? null : prenom, selectedTheme);

            // Créer la scène principale
            Scene scene = new Scene(root, 900, 620);

            // Appliquer le CSS du thème choisi
            ThemeManager.applyTheme(scene, selectedTheme);

            // Récupérer le Stage et changer la scène
            Stage stage = (Stage) startBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Mon Agenda Personnel 📅");
            stage.centerOnScreen();

            // Animation de transition
            FadeTransition ft = new FadeTransition(Duration.millis(600), root);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();

        } catch (IOException e) {
            System.err.println("Erreur chargement MainView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── SAUVEGARDER LES PRÉFÉRENCES ──────────────────────────────────────────
    private void savePreferences() {
        Properties props = new Properties();
        props.setProperty("theme",  selectedTheme);
        props.setProperty("prenom", prenomField.getText().trim());
        props.setProperty("firstLaunch", "false");

        try (FileOutputStream fos = new FileOutputStream("agenda_prefs.properties")) {
            props.store(fos, "Agenda Personnel Preferences");
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde préférences: " + e.getMessage());
        }
    }

    // ── CHARGER LES PRÉFÉRENCES ──────────────────────────────────────────────
    private void loadPreferences() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("agenda_prefs.properties")) {
            props.load(fis);
            String savedTheme  = props.getProperty("theme",  "girly");
            String savedPrenom = props.getProperty("prenom", "");

            prenomField.setText(savedPrenom);

            if ("boys".equals(savedTheme)) {
                selectBoys();
            } else {
                selectGirly();
            }
        } catch (IOException e) {
            // Premier lancement — pas de fichier encore, c'est normal
            selectGirly(); // thème par défaut
        }
    }
}