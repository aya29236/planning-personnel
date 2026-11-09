package agendapersonnel;

import javafx.scene.Scene;

public class ThemeManager {

    private static String currentTheme = "girly";

    public static void applyTheme(Scene scene, String theme) {
        currentTheme = theme;

        // Retirer tous les CSS existants
        scene.getStylesheets().clear();

        // Appliquer le bon CSS
        String cssPath;
        if ("boys".equals(theme)) {
            cssPath = ThemeManager.class
                .getResource("boys-theme.css")
                .toExternalForm();
        } else {
            cssPath = ThemeManager.class
                .getResource("girly-theme.css")
                .toExternalForm();
        }
        scene.getStylesheets().add(cssPath);
    }

    public static String getCurrentTheme() {
        return currentTheme;
    }
}