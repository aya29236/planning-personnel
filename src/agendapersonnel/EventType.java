
package agendapersonnel;

public enum EventType {
    RENDEZ_VOUS("Rendez-vous"),
    TACHE("Tâche"),
    ANNIVERSAIRE("Anniversaire"),
    REUNION("Réunion"),
    RAPPEL("Rappel"),
    AUTRE("Autre");

    private final String label;

    EventType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}

