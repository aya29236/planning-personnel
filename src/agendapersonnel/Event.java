
package agendapersonnel;
import java.time.LocalDate;
import java.time.LocalTime;
public class Event {
    private int id;
    private String title;
    private LocalDate date;
    private LocalTime time;
    private String description;
    private EventType type;
    private Priority priority;
    private boolean completed;

    public Event() {}

    public Event(String title, LocalDate date, LocalTime time,
                 String description, EventType type, Priority priority) {
        this.title       = title;
        this.date        = date;
        this.time        = time;
        this.description = description;
        this.type        = type;
        this.priority    = priority;
        this.completed   = false;
    }

    // Getters & Setters
    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }

    public String getTitle()            { return title; }
    public void setTitle(String t)      { this.title = t; }

    public LocalDate getDate()          { return date; }
    public void setDate(LocalDate d)    { this.date = d; }

    public LocalTime getTime()          { return time; }
    public void setTime(LocalTime t)    { this.time = t; }

    public String getDescription()       { return description; }
    public void setDescription(String d) { this.description = d; }

    public EventType getType()           { return type; }
    public void setType(EventType t)     { this.type = t; }

    public Priority getPriority()        { return priority; }
    public void setPriority(Priority p)  { this.priority = p; }

    public boolean isCompleted()          { return completed; }
    public void setCompleted(boolean c)   { this.completed = c; }

    @Override
    public String toString() {
        return String.format("[%s] %s — %s à %s",
                type, title, date, time != null ? time.toString() : "—");
    }
}
    

