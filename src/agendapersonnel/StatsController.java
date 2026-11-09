
package agendapersonnel;
import java.net.URL;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

public class StatsController implements Initializable {

    // ── FXML INJECTIONS ──────────────────────────────────────────────────────
    @FXML private Label totalLabel;
    @FXML private Label completedLabel;
    @FXML private Label upcomingLabel;
    @FXML private Label highPrioLabel;
    @FXML private VBox  typeChartBox;
    @FXML private VBox  prioChartBox;
    @FXML private VBox  monthChartBox;
    @FXML private Label mostBusyLabel;
    @FXML private Label completionRateLabel;

    private final EventDAO dao = new EventDAO();

    // ── INIT ─────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadStats();
    }

    // ── CHARGER TOUTES LES STATS ─────────────────────────────────────────────
    private void loadStats() {
        List<Event> all = dao.getAllEvents();

        if (all.isEmpty()) {
            totalLabel.setText("0");
            completedLabel.setText("0");
            upcomingLabel.setText("0");
            highPrioLabel.setText("0");
            mostBusyLabel.setText("—");
            completionRateLabel.setText("0%");
            return;
        }

        // ── Compteurs ────────────────────────────────────────────────────────
        long total     = all.size();
        long completed = all.stream().filter(Event::isCompleted).count();
        long upcoming  = all.stream()
            .filter(e -> e.getDate() != null &&
                    !e.getDate().isBefore(java.time.LocalDate.now()))
            .count();
        long highPrio  = all.stream()
            .filter(e -> e.getPriority() == Priority.HAUTE)
            .count();

        totalLabel.setText(String.valueOf(total));
        completedLabel.setText(String.valueOf(completed));
        upcomingLabel.setText(String.valueOf(upcoming));
        highPrioLabel.setText(String.valueOf(highPrio));

        // ── Taux de complétion ───────────────────────────────────────────────
        int rate = (int) ((completed * 100.0) / total);
        completionRateLabel.setText(rate + "%");

        // ── Graphe par type ──────────────────────────────────────────────────
        buildTypeChart(all, total);

        // ── Graphe par priorité ──────────────────────────────────────────────
        buildPrioChart(all, total);

        // ── Graphe par mois ──────────────────────────────────────────────────
        buildMonthChart(all);

        // ── Mois le plus chargé ──────────────────────────────────────────────
        findMostBusyMonth(all);
    }

    // ── GRAPHE PAR TYPE ──────────────────────────────────────────────────────
    private void buildTypeChart(List<Event> all, long total) {
        typeChartBox.getChildren().clear();

        Map<EventType, Long> byType = all.stream()
            .collect(Collectors.groupingBy(Event::getType, Collectors.counting()));

        String[][] typeData = {
            {"Rendez-vous",  "#d4537e", String.valueOf(byType.getOrDefault(EventType.RENDEZ_VOUS,  0L))},
            {"Tâche",        "#1d9e75", String.valueOf(byType.getOrDefault(EventType.TACHE,        0L))},
            {"Réunion",      "#185fa5", String.valueOf(byType.getOrDefault(EventType.REUNION,      0L))},
            {"Anniversaire", "#ef9f27", String.valueOf(byType.getOrDefault(EventType.ANNIVERSAIRE, 0L))},
            {"Rappel",       "#9b59b6", String.valueOf(byType.getOrDefault(EventType.RAPPEL,       0L))},
            {"Autre",        "#888888", String.valueOf(byType.getOrDefault(EventType.AUTRE,        0L))}
        };

        for (String[] row : typeData) {
            long count = Long.parseLong(row[2]);
            if (count == 0) continue;
            double pct = (count * 100.0) / total;
            typeChartBox.getChildren().add(
                buildBarRow(row[0], row[1], count, pct)
            );
        }
    }

    // ── GRAPHE PAR PRIORITÉ ──────────────────────────────────────────────────
    private void buildPrioChart(List<Event> all, long total) {
        prioChartBox.getChildren().clear();

        long haute   = all.stream().filter(e -> e.getPriority() == Priority.HAUTE).count();
        long moyenne = all.stream().filter(e -> e.getPriority() == Priority.MOYENNE).count();
        long basse   = all.stream().filter(e -> e.getPriority() == Priority.BASSE).count();

        if (haute   > 0) prioChartBox.getChildren().add(buildBarRow("🔴 Haute",   "#e24b4a", haute,   (haute*100.0)/total));
        if (moyenne > 0) prioChartBox.getChildren().add(buildBarRow("🟡 Moyenne", "#ef9f27", moyenne, (moyenne*100.0)/total));
        if (basse   > 0) prioChartBox.getChildren().add(buildBarRow("🟢 Basse",   "#97c459", basse,   (basse*100.0)/total));
    }

    // ── GRAPHE PAR MOIS ──────────────────────────────────────────────────────
    private void buildMonthChart(List<Event> all) {
        monthChartBox.getChildren().clear();

        // Grouper par mois
        Map<String, Long> byMonth = all.stream()
            .filter(e -> e.getDate() != null)
            .collect(Collectors.groupingBy(
                e -> e.getDate().getMonth()
                    .getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                    + " " + e.getDate().getYear(),
                Collectors.counting()
            ));

        if (byMonth.isEmpty()) return;

        long maxCount = byMonth.values().stream()
            .mapToLong(Long::longValue).max().orElse(1);

        // Trier par ordre naturel des entrées
        byMonth.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(6)
            .forEach(entry -> {
                double pct = (entry.getValue() * 100.0) / maxCount;
                monthChartBox.getChildren().add(
                    buildBarRow(entry.getKey(), "#d4537e",
                                entry.getValue(), pct)
                );
            });
    }

    // ── TROUVER LE MOIS LE PLUS CHARGÉ ───────────────────────────────────────
    private void findMostBusyMonth(List<Event> all) {
        all.stream()
            .filter(e -> e.getDate() != null)
            .collect(Collectors.groupingBy(
                e -> e.getDate().getMonth()
                    .getDisplayName(TextStyle.FULL, Locale.FRENCH)
                    + " " + e.getDate().getYear(),
                Collectors.counting()
            ))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .ifPresent(entry ->
                mostBusyLabel.setText(
                    entry.getKey() + " (" + entry.getValue() + " événements)"
                )
            );
    }

    // ── CONSTRUIRE UNE BARRE ─────────────────────────────────────────────────
    private HBox buildBarRow(String label, String color, long count, double pct) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(3, 0, 3, 0));

        // Label
        Label lbl = new Label(label);
        lbl.setMinWidth(100);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: -clr-text;");

        // Barre
        StackPane track = new StackPane();
        track.setMinHeight(16);
        track.setPrefWidth(200);
        track.setStyle(
            "-fx-background-color: -clr-bg2;" +
            "-fx-background-radius: 4;"
        );

        Rectangle fill = new Rectangle();
        fill.setHeight(16);
        fill.setWidth((pct / 100.0) * 200);
        fill.setStyle(
            "-fx-fill: " + color + ";" +
            "-fx-arc-width: 4;" +
            "-fx-arc-height: 4;"
        );
        fill.setArcWidth(4);
        fill.setArcHeight(4);

        track.getChildren().add(fill);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);

        HBox.setHgrow(track, javafx.scene.layout.Priority.ALWAYS);

        // Valeur
        Label val = new Label(count + " (" + String.format("%.0f", pct) + "%)");
        val.setMinWidth(70);
        val.setStyle("-fx-font-size: 11px; -fx-text-fill: -clr-text2;");

        row.getChildren().addAll(lbl, track, val);
        return row;
    }

    // ── REFRESH ──────────────────────────────────────────────────────────────
    @FXML
    public void handleRefresh() {
        loadStats();
    }
}