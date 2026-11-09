package agendapersonnel;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class SearchController implements Initializable {

    // ── FXML INJECTIONS ──────────────────────────────────────────────────────
    @FXML private TextField              searchField;
    @FXML private ComboBox<String>       typeFilter;
    @FXML private ComboBox<String>       priorityFilter;
    @FXML private DatePicker             dateFrom;
    @FXML private DatePicker             dateTo;
    @FXML private ComboBox<String>       sortCombo;
    @FXML private VBox                   resultsBox;
    @FXML private Label                  resultsCount;
    @FXML private Button                 searchBtn;
    @FXML private Button                 clearBtn;

    // ── STATE ────────────────────────────────────────────────────────────────
    private final EventDAO       dao      = new EventDAO();
    private       MainController mainCtrl = null;

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm");

    // ── INIT ─────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Filtre par type
        typeFilter.getItems().addAll(
            "Tous les types",
            "Rendez-vous", "Tâche", "Anniversaire",
            "Réunion", "Rappel", "Autre"
        );
        typeFilter.setValue("Tous les types");

        // Filtre par priorité
        priorityFilter.getItems().addAll(
            "Toutes les priorités",
            "🔴 Haute", "🟡 Moyenne", "🟢 Basse"
        );
        priorityFilter.setValue("Toutes les priorités");

        // Tri
        sortCombo.getItems().addAll(
            "Trier par date ↑",
            "Trier par date ↓",
            "Trier par titre A→Z",
            "Trier par priorité"
        );
        sortCombo.setValue("Trier par date ↑");

        // Recherche en temps réel à chaque frappe
        searchField.textProperty().addListener(
            (obs, oldVal, newVal) -> handleSearch()
        );

        // Charger tous les événements au départ
        handleSearch();
    }

    public void setMainController(MainController ctrl) {
        this.mainCtrl = ctrl;
    }

    // ── RECHERCHE PRINCIPALE ─────────────────────────────────────────────────
    @FXML
    public void handleSearch() {
        resultsBox.getChildren().clear();

        String keyword = searchField.getText().trim();
        List<Event> results;

        // ── Chercher selon le mot-clé ────────────────────────────────────────
        if (keyword.isEmpty()) {
            results = dao.getAllEvents();
        } else {
            results = dao.searchByTitle(keyword);
        }

        // ── Filtrer par type ─────────────────────────────────────────────────
        String typeVal = typeFilter.getValue();
        if (typeVal != null && !typeVal.equals("Tous les types")) {
            EventType selectedType = mapStringToType(typeVal);
            if (selectedType != null) {
                results.removeIf(e -> e.getType() != selectedType);
            }
        }

        // ── Filtrer par priorité ─────────────────────────────────────────────
        String prioVal = priorityFilter.getValue();
        if (prioVal != null && !prioVal.equals("Toutes les priorités")) {
            Priority selectedPrio = mapStringToPriority(prioVal);
            if (selectedPrio != null) {
                results.removeIf(e -> e.getPriority() != selectedPrio);
            }
        }

        // ── Filtrer par période ──────────────────────────────────────────────
        LocalDate from = dateFrom.getValue();
        LocalDate to   = dateTo.getValue();

        if (from != null) {
            results.removeIf(e -> e.getDate() != null && e.getDate().isBefore(from));
        }
        if (to != null) {
            results.removeIf(e -> e.getDate() != null && e.getDate().isAfter(to));
        }

        // ── Trier les résultats ──────────────────────────────────────────────
        String sort = sortCombo.getValue();
        if (sort != null) {
            switch (sort) {
                case "Trier par date ↑" ->
                    results.sort((a, b) -> {
                        if (a.getDate() == null) return 1;
                        if (b.getDate() == null) return -1;
                        return a.getDate().compareTo(b.getDate());
                    });
                case "Trier par date ↓" ->
                    results.sort((a, b) -> {
                        if (a.getDate() == null) return 1;
                        if (b.getDate() == null) return -1;
                        return b.getDate().compareTo(a.getDate());
                    });
                case "Trier par titre A→Z" ->
                    results.sort((a, b) ->
                        a.getTitle().compareToIgnoreCase(b.getTitle()));
                case "Trier par priorité" ->
                    results.sort((a, b) ->
                        a.getPriority().ordinal() - b.getPriority().ordinal());
            }
        }

        // ── Afficher le nombre de résultats ──────────────────────────────────
        resultsCount.setText(results.size() + " résultat(s) trouvé(s)");

        // ── Afficher les résultats ───────────────────────────────────────────
        if (results.isEmpty()) {
            Label empty = new Label("Aucun résultat trouvé 🔍");
            empty.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #993556;" +
                "-fx-padding: 20;"
            );
            resultsBox.getChildren().add(empty);
            return;
        }

        for (Event e : results) {
            resultsBox.getChildren().add(buildResultCard(e));
        }
    }

    // ── EFFACER LES FILTRES ──────────────────────────────────────────────────
    @FXML
    public void handleClear() {
        searchField.clear();
        typeFilter.setValue("Tous les types");
        priorityFilter.setValue("Toutes les priorités");
        dateFrom.setValue(null);
        dateTo.setValue(null);
        sortCombo.setValue("Trier par date ↑");
        handleSearch();
    }

    // ── CONSTRUIRE UNE CARTE RÉSULTAT ────────────────────────────────────────
    private HBox buildResultCard(Event e) {
        HBox card = new HBox(10);
        card.getStyleClass().add("event-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 12, 8, 12));

        // Point coloré
        Label dot = new Label("●");
        dot.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: " + typeColor(e.getType()) + ";"
        );

        // Infos principales
        VBox info = new VBox(2);
        Label title = new Label(e.getTitle());
        title.getStyleClass().add("event-title");

        // Date + heure
        String dateTxt = e.getDate() != null ?
            e.getDate().format(DATE_FMT) : "—";
        String timeTxt = e.getTime() != null ?
            " — " + e.getTime().format(TIME_FMT) : "";
        Label meta = new Label(dateTxt + timeTxt);
        meta.getStyleClass().add("event-meta");

        // Description courte
        if (e.getDescription() != null && !e.getDescription().isEmpty()) {
            String desc = e.getDescription();
            if (desc.length() > 50) desc = desc.substring(0, 50) + "...";
            Label descL = new Label(desc);
            descL.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");
            info.getChildren().addAll(title, meta, descL);
        } else {
            info.getChildren().addAll(title, meta);
        }

        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        // Badge type
        Label typeBadge = new Label(e.getType().toString());
        typeBadge.getStyleClass().addAll(
            "event-badge", badgeStyle(e.getType())
        );

        // Badge priorité
        Label prioBadge = new Label(e.getPriority().toString());
        prioBadge.setStyle("-fx-font-size: 11px;");

        // Bouton aller au jour
        Button goBtn = new Button("📅");
        goBtn.getStyleClass().add("icon-btn");
        goBtn.setTooltip(new Tooltip("Voir dans le calendrier"));
        goBtn.setOnAction(ev -> {
            if (mainCtrl != null && e.getDate() != null) {
                mainCtrl.goToDate(e.getDate());
            }
            // Fermer la fenêtre de recherche
            goBtn.getScene().getWindow().hide();
        });

        card.getChildren().addAll(dot, info, typeBadge, prioBadge, goBtn);
        return card;
    }

    // ── HELPERS MAPPING ──────────────────────────────────────────────────────
    private EventType mapStringToType(String val) {
        return switch (val) {
            case "Rendez-vous"  -> EventType.RENDEZ_VOUS;
            case "Tâche"        -> EventType.TACHE;
            case "Anniversaire" -> EventType.ANNIVERSAIRE;
            case "Réunion"      -> EventType.REUNION;
            case "Rappel"       -> EventType.RAPPEL;
            case "Autre"        -> EventType.AUTRE;
            default             -> null;
        };
    }

    private Priority mapStringToPriority(String val) {
        return switch (val) {
            case "🔴 Haute"   -> Priority.HAUTE;
            case "🟡 Moyenne" -> Priority.MOYENNE;
            case "🟢 Basse"   -> Priority.BASSE;
            default           -> null;
        };
    }

    private String typeColor(EventType type) {
        return switch (type) {
            case RENDEZ_VOUS  -> "#d4537e";
            case TACHE        -> "#1d9e75";
            case ANNIVERSAIRE -> "#ef9f27";
            case REUNION      -> "#185fa5";
            case RAPPEL       -> "#9b59b6";
            default           -> "#666666";
        };
    }

    private String badgeStyle(EventType type) {
        return switch (type) {
            case RENDEZ_VOUS  -> "badge-rdv";
            case TACHE        -> "badge-task";
            case ANNIVERSAIRE -> "badge-fete";
            case REUNION      -> "badge-meet";
            default           -> "badge-rdv";
        };
    }
}