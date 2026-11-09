package agendapersonnel;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EventFormController implements Initializable {

    // ── FXML INJECTIONS ──────────────────────────────────────────────────────
    @FXML private Label          formTitle;
    @FXML private TextField      titleField;
    @FXML private DatePicker     datePicker;
    @FXML private TextField      timeField;
    @FXML private ComboBox<EventType>  typeCombo;
    @FXML private ComboBox<Priority>   priorityCombo;
    @FXML private TextArea       descArea;
    @FXML private CheckBox       reminderCheck;
    @FXML private ComboBox<String>    reminderCombo;
    @FXML private Button         saveBtn;
    @FXML private Button         cancelBtn;
    @FXML private Label          errorLabel;

    // ── STATE ────────────────────────────────────────────────────────────────
    private final EventDAO    dao          = new EventDAO();
    private       MainController mainCtrl  = null;
    private       Event          eventToEdit = null; // null = mode ajout
    private       LocalDate      selectedDate;

    // ── INIT ─────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Remplir les ComboBox
        typeCombo.getItems().addAll(EventType.values());
        typeCombo.setValue(EventType.RENDEZ_VOUS);

        priorityCombo.getItems().addAll(Priority.values());
        priorityCombo.setValue(Priority.MOYENNE);

        reminderCombo.getItems().addAll(
            "15 minutes", "30 minutes", "1 heure", "1 jour"
        );
        reminderCombo.setValue("30 minutes");
        reminderCombo.setDisable(true);

        // Activer/désactiver le combo rappel selon la checkbox
        reminderCheck.setOnAction(e ->
            reminderCombo.setDisable(!reminderCheck.isSelected())
        );

        // Format heure placeholder
        timeField.setPromptText("HH:mm  (ex: 09:30)");

        // Cacher le message d'erreur
        errorLabel.setVisible(false);
    }

    // ── SETTERS appelés par MainController ───────────────────────────────────
    public void setMainController(MainController ctrl) {
        this.mainCtrl = ctrl;
    }

    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        datePicker.setValue(date);
    }

    // ── CHARGER UN ÉVÉNEMENT EXISTANT (mode modification) ────────────────────
    public void loadEvent(Event e) {
        this.eventToEdit = e;
        formTitle.setText("✏️ Modifier l'événement");
        saveBtn.setText("Mettre à jour");

        // Remplir tous les champs avec les données existantes
        titleField.setText(e.getTitle());
        datePicker.setValue(e.getDate());
        timeField.setText(
            e.getTime() != null ? e.getTime().toString() : ""
        );
        typeCombo.setValue(e.getType());
        priorityCombo.setValue(e.getPriority());
        descArea.setText(
            e.getDescription() != null ? e.getDescription() : ""
        );
    }

    // ── BOUTON ENREGISTRER ───────────────────────────────────────────────────
    @FXML
    public void handleSave() {

        // ── Validation ───────────────────────────────────────────────────────
        if (titleField.getText().trim().isEmpty()) {
            showError("Le titre est obligatoire !");
            titleField.requestFocus();
            return;
        }
        if (datePicker.getValue() == null) {
            showError("La date est obligatoire !");
            return;
        }
        if (typeCombo.getValue() == null) {
            showError("Le type est obligatoire !");
            return;
        }

        // ── Parser l'heure ───────────────────────────────────────────────────
        LocalTime time = null;
        String timeText = timeField.getText().trim();
        if (!timeText.isEmpty()) {
            try {
                // Accepter "9:30" et "09:30"
                if (timeText.length() == 4) timeText = "0" + timeText;
                time = LocalTime.parse(timeText);
            } catch (Exception ex) {
                showError("Format heure invalide ! Utilisez HH:mm (ex: 09:30)");
                timeField.requestFocus();
                return;
            }
        }

        // ── Construire l'objet Event ─────────────────────────────────────────
        Event event = eventToEdit != null ? eventToEdit : new Event();

        event.setTitle      (titleField.getText().trim());
        event.setDate       (datePicker.getValue());
        event.setTime       (time);
        event.setType       (typeCombo.getValue());
        event.setPriority   (priorityCombo.getValue());
        event.setDescription(descArea.getText().trim());

        // ── Sauvegarder en DB ────────────────────────────────────────────────
        boolean success;
        if (eventToEdit == null) {
            // Mode ajout
            success = dao.addEvent(event);
        } else {
            // Mode modification
            success = dao.updateEvent(event);
        }

        if (success) {
            // Rafraîchir la vue principale
            if (mainCtrl != null) {
                mainCtrl.refreshAll();
                mainCtrl.setStatus(
                    eventToEdit == null ?
                    "Événement ajouté avec succès ✓" :
                    "Événement modifié avec succès ✓"
                );
            }
            closeForm();
        } else {
            showError("Erreur lors de la sauvegarde. Vérifiez la connexion Oracle.");
        }
    }

    // ── BOUTON ANNULER ───────────────────────────────────────────────────────
    @FXML
    public void handleCancel() {
        closeForm();
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────
    private void showError(String message) {
        errorLabel.setText("⚠️ " + message);
        errorLabel.setVisible(true);
    }

    private void closeForm() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}