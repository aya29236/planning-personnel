package agendapersonnel;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainController implements Initializable {

    // ── FXML INJECTIONS ──────────────────────────────────────────────────────
    @FXML private Label     topDeco;
    @FXML private Label     greetLabel;
    @FXML private Button    themeGirlyBtn;
    @FXML private Button    themeBoysBtn;
    @FXML private StackPane contentArea;
    @FXML private VBox      calendarPane;
    @FXML private GridPane  calendarGrid;
    @FXML private Label     monthYearLabel;
    @FXML private Label     selectedDayLabel;
    @FXML private VBox      eventListBox;
    @FXML private VBox      upcomingBox;
    @FXML private Label     statusLabel;
    @FXML private Label     dateTimeLabel;

    // ── STATE ────────────────────────────────────────────────────────────────
    private final EventDAO    dao          = new EventDAO();
    private       LocalDate   currentMonth = LocalDate.now().withDayOfMonth(1);
    private       LocalDate   selectedDay  = LocalDate.now();
    private       String      currentTheme = "girly";
    private       String      prenom       = null;

    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] DAY_HEADERS =
        {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};

    // ── INIT ─────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        startClock();
        buildCalendar();
        loadUpcoming();
        loadEventsForDay(selectedDay);
    }

    // Appelé par WelcomeController après le chargement
    public void initData(String prenom, String theme) {
        this.prenom       = prenom;
        this.currentTheme = theme;

        // Message de bienvenue personnalisé
        if (prenom != null && !prenom.isEmpty()) {
            greetLabel.setText("Bonjour " + prenom + " 👋");
        }

        // Décoration selon le thème
        if ("boys".equals(theme)) {
            topDeco.setText("🚀");
            themeBoysBtn.getStyleClass().add("active-theme");
            themeGirlyBtn.getStyleClass().remove("active-theme");
        } else {
            topDeco.setText("🌸");
            themeGirlyBtn.getStyleClass().add("active-theme");
            themeBoysBtn.getStyleClass().remove("active-theme");
        }
    }

    // ── HORLOGE EN TEMPS RÉEL ────────────────────────────────────────────────
    private void startClock() {
        Thread clockThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        DateTimeFormatter fmt =
                            DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy — HH:mm:ss",
                                                        Locale.FRENCH);
                        dateTimeLabel.setText(LocalTime.now()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                            "  |  " +
                            LocalDate.now().format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.start();
    }

    // ── CONSTRUCTION DU CALENDRIER ───────────────────────────────────────────
    private void buildCalendar() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // Colonnes égales
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            calendarGrid.getColumnConstraints().add(cc);
        }

        // Titre du mois
        String mois = currentMonth.getMonth()
            .getDisplayName(TextStyle.FULL, Locale.FRENCH);
        monthYearLabel.setText(
            mois.substring(0,1).toUpperCase() + mois.substring(1)
            + " " + currentMonth.getYear()
        );

        // En-têtes des jours
        for (int col = 0; col < 7; col++) {
            Label header = new Label(DAY_HEADERS[col]);
            header.getStyleClass().add("cal-day-header");
            header.setMaxWidth(Double.MAX_VALUE);
            header.setAlignment(Pos.CENTER);
            GridPane.setColumnIndex(header, col);
            GridPane.setRowIndex(header, 0);
            calendarGrid.getChildren().add(header);
        }

        // Récupérer les événements du mois pour marquer les jours
        List<Event> allEvents = dao.getAllEvents();

        // Premier jour du mois (0=Lundi, 6=Dimanche)
        int firstDayOfWeek = currentMonth.getDayOfWeek().getValue() - 1;
        int daysInMonth    = currentMonth.lengthOfMonth();
        int row = 1, col = firstDayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            final LocalDate date = currentMonth.withDayOfMonth(day);
            final int dayNum     = day;

            Button dayBtn = new Button(String.valueOf(dayNum));
            dayBtn.getStyleClass().add("cal-day-btn");
            dayBtn.setMaxWidth(Double.MAX_VALUE);
            dayBtn.setMaxHeight(Double.MAX_VALUE);

            // Marquer aujourd'hui
            if (date.equals(LocalDate.now())) {
                dayBtn.getStyleClass().add("cal-day-today");
            }

            // Marquer le jour sélectionné
            if (date.equals(selectedDay)) {
                dayBtn.getStyleClass().add("cal-day-selected");
            }

            // Marquer s'il y a des événements ce jour
            boolean hasEvent = allEvents.stream()
                .anyMatch(e -> e.getDate() != null && e.getDate().equals(date));
            if (hasEvent) {
                dayBtn.getStyleClass().add("cal-day-has-event");
            }

            // Clic sur un jour
            dayBtn.setOnAction(ev -> {
                selectedDay = date;
                buildCalendar(); // Reconstruire pour update la sélection
                loadEventsForDay(date);
            });

            GridPane.setColumnIndex(dayBtn, col);
            GridPane.setRowIndex   (dayBtn, row);
            calendarGrid.getChildren().add(dayBtn);

            col++;
            if (col == 7) { col = 0; row++; }
        }
    }

    // ── CHARGER LES ÉVÉNEMENTS D'UN JOUR ────────────────────────────────────
    private void loadEventsForDay(LocalDate date) {
        eventListBox.getChildren().clear();

        String jourFmt = date.format(
            DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH));
        selectedDayLabel.setText("Événements du " + jourFmt);

        List<Event> events = dao.searchByDate(date);

        if (events.isEmpty()) {
            Label empty = new Label("Aucun événement ce jour 🌸");
            empty.setStyle("-fx-text-fill: #993556; -fx-font-size: 13px; -fx-padding: 16;");
            eventListBox.getChildren().add(empty);
            return;
        }

        for (Event e : events) {
            eventListBox.getChildren().add(buildEventCard(e));
        }

        // Animation
        FadeTransition ft = new FadeTransition(Duration.millis(400), eventListBox);
        ft.setFromValue(0.5);
        ft.setToValue(1.0);
        ft.play();
    }

    // ── CONSTRUIRE UNE CARTE ÉVÉNEMENT ───────────────────────────────────────
    private HBox buildEventCard(Event e) {
        HBox card = new HBox(10);
        card.getStyleClass().add("event-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 10, 8, 10));

        // Point coloré selon le type
        Label dot = new Label("●");
        dot.setStyle("-fx-font-size: 14px; -fx-text-fill: " + typeColor(e.getType()) + ";");

        // Infos
        VBox info = new VBox(2);
        Label title = new Label(e.getTitle());
        title.getStyleClass().add("event-title");
        if (e.isCompleted()) {
            title.setStyle("-fx-strikethrough: true; -fx-text-fill: #aaa;");
        }

        String timeTxt = e.getTime() != null ?
            e.getTime().format(TIME_FMT) + " — " : "";
        String descTxt = e.getDescription() != null &&
            !e.getDescription().isEmpty() ?
            e.getDescription() : "";
        Label meta = new Label(timeTxt + descTxt);
        meta.getStyleClass().add("event-meta");

        info.getChildren().addAll(title, meta);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        // Badge type
        Label badge = new Label(e.getType().toString());
        badge.getStyleClass().addAll("event-badge", badgeStyle(e.getType()));

        // Badge priorité
        Label prio = new Label(e.getPriority().toString());
        prio.setStyle("-fx-font-size: 11px;");

        // Bouton Modifier
        Button editBtn = new Button("✏️");
        editBtn.getStyleClass().add("icon-btn");
        editBtn.setTooltip(new Tooltip("Modifier"));
        editBtn.setOnAction(ev -> openEditEvent(e));

        // Bouton Supprimer
        Button delBtn = new Button("🗑");
        delBtn.getStyleClass().add("icon-btn");
        delBtn.setTooltip(new Tooltip("Supprimer"));
        delBtn.setOnAction(ev -> deleteEvent(e));

        // Bouton Compléter
        Button doneBtn = new Button(e.isCompleted() ? "↩️" : "✅");
        doneBtn.getStyleClass().add("icon-btn");
        doneBtn.setTooltip(new Tooltip(e.isCompleted() ? "Marquer non fait" : "Marquer fait"));
        doneBtn.setOnAction(ev -> {
            dao.toggleCompleted(e.getId(), !e.isCompleted());
            loadEventsForDay(selectedDay);
        });

        card.getChildren().addAll(dot, info, badge, prio, doneBtn, editBtn, delBtn);
        return card;
    }

    // ── CHARGER LES PROCHAINS ÉVÉNEMENTS ────────────────────────────────────
    private void loadUpcoming() {
        upcomingBox.getChildren().clear();
        List<Event> upcoming = dao.getUpcomingEvents();

        if (upcoming.isEmpty()) {
            Label lbl = new Label("Aucun événement à venir");
            lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #993556;");
            upcomingBox.getChildren().add(lbl);
            return;
        }

        for (Event e : upcoming) {
            VBox card = new VBox(2);
            card.getStyleClass().add("upcoming-card");
            card.setPadding(new Insets(6, 8, 6, 8));

            String dateFmt = e.getDate().format(
                DateTimeFormatter.ofPattern("dd MMM", Locale.FRENCH));
            String timeFmt = e.getTime() != null ?
                " — " + e.getTime().format(TIME_FMT) : "";

            Label dateL = new Label(dateFmt + timeFmt);
            dateL.getStyleClass().add("upcoming-date");

            Label nameL = new Label(e.getTitle());
            nameL.getStyleClass().add("upcoming-name");

            card.getChildren().addAll(dateL, nameL);

            // Clic → aller au jour de cet événement
            card.setOnMouseClicked(ev -> {
                selectedDay  = e.getDate();
                currentMonth = selectedDay.withDayOfMonth(1);
                buildCalendar();
                loadEventsForDay(selectedDay);
            });

            upcomingBox.getChildren().add(card);
        }
    }

    // ── NAVIGATION MOIS ──────────────────────────────────────────────────────
    @FXML public void prevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        buildCalendar();
    }

    @FXML public void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        buildCalendar();
    }

    @FXML public void goToday() {
        selectedDay  = LocalDate.now();
        currentMonth = selectedDay.withDayOfMonth(1);
        buildCalendar();
        loadEventsForDay(selectedDay);
    }

    // ── OUVRIR FORMULAIRE AJOUT ──────────────────────────────────────────────
    @FXML public void openAddEvent() {
        openEventForm(null);
    }

    // ── OUVRIR FORMULAIRE MODIFICATION ───────────────────────────────────────
    private void openEditEvent(Event event) {
        openEventForm(event);
    }

    private void openEventForm(Event eventToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("EventForm.fxml")
            );
            Parent root = loader.load();

            EventFormController ctrl = loader.getController();
            ctrl.setMainController(this);
            ctrl.setSelectedDate(selectedDay);
            if (eventToEdit != null) {
                ctrl.loadEvent(eventToEdit);
            }

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(eventToEdit == null ?
                "Ajouter un événement" : "Modifier l'événement");
            dialog.setResizable(false);

            Scene scene = new Scene(root, 480, 500);
            ThemeManager.applyTheme(scene, currentTheme);
            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur ouverture formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── SUPPRIMER UN ÉVÉNEMENT ───────────────────────────────────────────────
    private void deleteEvent(Event event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer \"" + event.getTitle() + "\" ?");
        confirm.setContentText("Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                dao.deleteEvent(event.getId());
                statusLabel.setText("Événement supprimé ✓");
                refreshAll();
            }
        });
    }

    // ── RAFRAÎCHIR TOUT ──────────────────────────────────────────────────────
    public void refreshAll() {
        buildCalendar();
        loadEventsForDay(selectedDay);
        loadUpcoming();
    }

    // ── NAVIGATION SIDEBAR ───────────────────────────────────────────────────
    @FXML public void showCalendar()   {
        contentArea.getChildren().stream()
            .forEach(n -> n.setVisible(n == calendarPane));
        statusLabel.setText("Vue Calendrier");
    }

    @FXML public void showEvents()     { statusLabel.setText("Vue Événements — bientôt !"); }
    @FXML public void showSearch()     { openSearchWindow(); }
    @FXML public void showImportant()  { loadImportantEvents(); }
    @FXML public void showStats()      { 
    try {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("StatsView.fxml")
            );
            Parent root = loader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("📊 Statistiques");

        Scene scene = new Scene(root, 600, 560);
        ThemeManager.applyTheme(scene, currentTheme);
        stage.setScene(scene);
        stage.show();

    } catch (IOException e) {
        System.err.println("Erreur stats: " + e.getMessage());
        e.printStackTrace();
    }
} 
    @FXML public void showSettings()   { statusLabel.setText("Paramètres — bientôt !"); }

    // ── ÉVÉNEMENTS IMPORTANTS (Priorité HAUTE) ───────────────────────────────
    private void loadImportantEvents() {
        eventListBox.getChildren().clear();
        selectedDayLabel.setText("⭐ Événements importants (Priorité Haute)");

        List<Event> important = dao.searchByPriority(Priority.HAUTE);
        if (important.isEmpty()) {
            Label lbl = new Label("Aucun événement important 🎉");
            lbl.setStyle("-fx-font-size: 13px; -fx-padding: 16; -fx-text-fill: #993556;");
            eventListBox.getChildren().add(lbl);
        } else {
            for (Event e : important) {
                eventListBox.getChildren().add(buildEventCard(e));
            }
        }
        statusLabel.setText("Événements importants affichés");
    }
    // Appelé par SearchController pour naviguer vers une date
public void goToDate(LocalDate date) {
    selectedDay  = date;
    currentMonth = date.withDayOfMonth(1);
    buildCalendar();
    loadEventsForDay(date);
}

// Appelé par EventFormController pour mettre à jour le status
public void setStatus(String message) {
    statusLabel.setText(message);
}

    // ── FENÊTRE RECHERCHE ────────────────────────────────────────────────────
    private void openSearchWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("SearchView.fxml")
            );
            Parent root = loader.load();

            SearchController ctrl = loader.getController();
            ctrl.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("🔍 Rechercher un événement");

            Scene scene = new Scene(root, 600, 500);
            ThemeManager.applyTheme(scene, currentTheme);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur ouverture recherche: " + e.getMessage());
        }
    }

    // ── SWITCH THEMES ────────────────────────────────────────────────────────
    @FXML public void switchGirly() {
        currentTheme = "girly";
        ThemeManager.applyTheme(
            themeGirlyBtn.getScene(), "girly"
        );
        topDeco.setText("🌸");
        themeGirlyBtn.getStyleClass().add("active-theme");
        themeBoysBtn.getStyleClass().remove("active-theme");
        statusLabel.setText("Thème Girly Rose activé 🌸");
    }

    @FXML public void switchBoys() {
        currentTheme = "boys";
        ThemeManager.applyTheme(
            themeBoysBtn.getScene(), "boys"
        );
        topDeco.setText("🚀");
        themeBoysBtn.getStyleClass().add("active-theme");
        themeGirlyBtn.getStyleClass().remove("active-theme");
        statusLabel.setText("Thème Boys Bleu activé 🚀");
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────
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