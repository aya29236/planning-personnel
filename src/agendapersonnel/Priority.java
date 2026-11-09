
package agendapersonnel;

public enum Priority {
    
    HAUTE("🔴 Haute"),
    MOYENNE("🟡 Moyenne"),
    BASSE("🟢 Basse");

    private final String label;

    Priority(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
    

