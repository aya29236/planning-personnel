
package agendapersonnel;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
public class EventDAO {

    // ── CREATE ──────────────────────────────────────────────────────────────
    public boolean addEvent(Event e) {
        String sql = "INSERT INTO events (title, event_date, event_time, " +
                     "description, type, priority, completed) " +
                     "VALUES (?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getTitle());
            ps.setString(2, e.getDate().toString());
            ps.setString(3, e.getTime() != null ? e.getTime().toString() : null);
            ps.setString(4, e.getDescription());
            ps.setString(5, e.getType().name());
            ps.setString(6, e.getPriority().name());
            ps.setInt   (7, e.isCompleted() ? 1 : 0);

            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException ex) {
            System.err.println("Erreur addEvent: " + ex.getMessage());
            return false;
        }
    }

    // ── READ ALL ─────────────────────────────────────────────────────────────
    public List<Event> getAllEvents() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY event_date, event_time";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt   = conn.createStatement();
             ResultSet rs     = stmt.executeQuery(sql)) {

            // Parcourir chaque ligne du ResultSet
            while (rs.next()) {
                Event e = new Event();

                e.setId         (rs.getInt   ("id"));
                e.setTitle      (rs.getString ("title"));

                // Oracle retourne java.sql.Date → convertir en LocalDate
                java.sql.Date sqlDate = rs.getDate("event_date");
                e.setDate(sqlDate != null ? sqlDate.toLocalDate() : null);

                // Heure stockée en String → convertir en LocalTime
                String timeStr = rs.getString("event_time");
                e.setTime(timeStr != null ? LocalTime.parse(timeStr) : null);

                e.setDescription(rs.getString("description"));
                e.setType       (EventType.valueOf(rs.getString("type")));
                e.setPriority   (Priority.valueOf (rs.getString("priority")));
                e.setCompleted  (rs.getInt("completed") == 1);

                list.add(e);
            }

        } catch (SQLException ex) {
            System.err.println("Erreur getAllEvents: " + ex.getMessage());
        }
        return list;
    }

    // ── READ UPCOMING ────────────────────────────────────────────────────────
    public List<Event> getUpcomingEvents() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE event_date >= TRUNC(SYSDATE) " +
                     "ORDER BY event_date, event_time FETCH FIRST 10 ROWS ONLY";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt   = conn.createStatement();
             ResultSet rs     = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Event e = new Event();
                e.setId         (rs.getInt   ("id"));
                e.setTitle      (rs.getString ("title"));
                java.sql.Date d = rs.getDate  ("event_date");
                e.setDate       (d != null ? d.toLocalDate() : null);
                String t = rs.getString("event_time");
                e.setTime       (t != null ? LocalTime.parse(t) : null);
                e.setDescription(rs.getString ("description"));
                e.setType       (EventType.valueOf(rs.getString("type")));
                e.setPriority   (Priority.valueOf (rs.getString("priority")));
                e.setCompleted  (rs.getInt("completed") == 1);
                list.add(e);
            }

        } catch (SQLException ex) {
            System.err.println("Erreur getUpcomingEvents: " + ex.getMessage());
        }
        return list;
    }

    // ── SEARCH BY TITLE ──────────────────────────────────────────────────────
    public List<Event> searchByTitle(String keyword) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE LOWER(title) LIKE ? ORDER BY event_date";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();  // ResultSet explicite

            while (rs.next()) {
                Event e = new Event();
                e.setId         (rs.getInt   ("id"));
                e.setTitle      (rs.getString ("title"));
                java.sql.Date d = rs.getDate  ("event_date");
                e.setDate       (d != null ? d.toLocalDate() : null);
                String t = rs.getString("event_time");
                e.setTime       (t != null ? LocalTime.parse(t) : null);
                e.setDescription(rs.getString ("description"));
                e.setType       (EventType.valueOf(rs.getString("type")));
                e.setPriority   (Priority.valueOf (rs.getString("priority")));
                e.setCompleted  (rs.getInt("completed") == 1);
                list.add(e);
            }
            rs.close(); // fermer explicitement

        } catch (SQLException ex) {
            System.err.println("Erreur searchByTitle: " + ex.getMessage());
        }
        return list;
    }

    // ── SEARCH BY DATE ───────────────────────────────────────────────────────
    public List<Event> searchByDate(LocalDate date) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE event_date = TO_DATE(?, 'YYYY-MM-DD') " +
                     "ORDER BY event_time";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Event e = new Event();
                e.setId         (rs.getInt   ("id"));
                e.setTitle      (rs.getString ("title"));
                java.sql.Date d = rs.getDate  ("event_date");
                e.setDate       (d != null ? d.toLocalDate() : null);
                String t = rs.getString("event_time");
                e.setTime       (t != null ? LocalTime.parse(t) : null);
                e.setDescription(rs.getString ("description"));
                e.setType       (EventType.valueOf(rs.getString("type")));
                e.setPriority   (Priority.valueOf (rs.getString("priority")));
                e.setCompleted  (rs.getInt("completed") == 1);
                list.add(e);
            }
            rs.close();

        } catch (SQLException ex) {
            System.err.println("Erreur searchByDate: " + ex.getMessage());
        }
        return list;
    }

    // ── SEARCH BY TYPE ───────────────────────────────────────────────────────
    public List<Event> searchByType(EventType type) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE type = ? ORDER BY event_date";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Event e = new Event();
                e.setId         (rs.getInt   ("id"));
                e.setTitle      (rs.getString ("title"));
                java.sql.Date d = rs.getDate  ("event_date");
                e.setDate       (d != null ? d.toLocalDate() : null);
                String t = rs.getString("event_time");
                e.setTime       (t != null ? LocalTime.parse(t) : null);
                e.setDescription(rs.getString ("description"));
                e.setType       (EventType.valueOf(rs.getString("type")));
                e.setPriority   (Priority.valueOf (rs.getString("priority")));
                e.setCompleted  (rs.getInt("completed") == 1);
                list.add(e);
            }
            rs.close();

        } catch (SQLException ex) {
            System.err.println("Erreur searchByType: " + ex.getMessage());
        }
        return list;
    }

    // ── SEARCH BY PRIORITY ───────────────────────────────────────────────────
    public List<Event> searchByPriority(Priority priority) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE priority = ? ORDER BY event_date";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, priority.name());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Event e = new Event();
                e.setId         (rs.getInt   ("id"));
                e.setTitle      (rs.getString ("title"));
                java.sql.Date d = rs.getDate  ("event_date");
                e.setDate       (d != null ? d.toLocalDate() : null);
                String t = rs.getString("event_time");
                e.setTime       (t != null ? LocalTime.parse(t) : null);
                e.setDescription(rs.getString ("description"));
                e.setType       (EventType.valueOf(rs.getString("type")));
                e.setPriority   (Priority.valueOf (rs.getString("priority")));
                e.setCompleted  (rs.getInt("completed") == 1);
                list.add(e);
            }
            rs.close();

        } catch (SQLException ex) {
            System.err.println("Erreur searchByPriority: " + ex.getMessage());
        }
        return list;
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateEvent(Event e) {
        String sql = "UPDATE events SET title=?, event_date=TO_DATE(?,'YYYY-MM-DD'), " +
                     "event_time=?, description=?, type=?, priority=?, completed=? " +
                     "WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getTitle());
            ps.setString(2, e.getDate().toString());
            ps.setString(3, e.getTime() != null ? e.getTime().toString() : null);
            ps.setString(4, e.getDescription());
            ps.setString(5, e.getType().name());
            ps.setString(6, e.getPriority().name());
            ps.setInt   (7, e.isCompleted() ? 1 : 0);
            ps.setInt   (8, e.getId());

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException ex) {
            System.err.println("Erreur updateEvent: " + ex.getMessage());
            return false;
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteEvent(int id) {
        String sql = "DELETE FROM events WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rowsDeleted = ps.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException ex) {
            System.err.println("Erreur deleteEvent: " + ex.getMessage());
            return false;
        }
    }

    // ── TOGGLE COMPLETED ─────────────────────────────────────────────────────
    public boolean toggleCompleted(int id, boolean completed) {
        String sql = "UPDATE events SET completed = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, completed ? 1 : 0);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException ex) {
            System.err.println("Erreur toggleCompleted: " + ex.getMessage());
            return false;
        }
    }
}