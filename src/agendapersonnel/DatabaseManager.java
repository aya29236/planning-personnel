package agendapersonnel;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {

    // Settings are read (in this order) from: environment variables
    // AGENDA_DB_URL / AGENDA_DB_USER / AGENDA_DB_PASSWORD, then db.properties,
    // then the defaults below. No credentials are stored in the source code.
    private static final Properties CONFIG = loadConfig();

    private static final String DB_URL  = get("db.url",      "AGENDA_DB_URL",      "jdbc:oracle:thin:@localhost:1521:XE");
    private static final String DB_USER = get("db.user",     "AGENDA_DB_USER",     "");
    private static final String DB_PASS = get("db.password", "AGENDA_DB_PASSWORD", "");

    private static Connection connection;

    private static Properties loadConfig() {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) {
            p.load(in);
        } catch (IOException ignored) {
            // No file: fall back to environment variables / defaults
        }
        return p;
    }

    private static String get(String propKey, String envKey, String def) {
        String env = System.getenv(envKey);
        if (env != null && !env.isEmpty()) return env;
        return CONFIG.getProperty(propKey, def);
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }
        return connection;
    }

    public static void initDatabase() {
        String createSeq = """
            BEGIN
                EXECUTE IMMEDIATE 'CREATE SEQUENCE events_seq START WITH 1 INCREMENT BY 1';
            EXCEPTION
                WHEN OTHERS THEN
                    IF SQLCODE != -955 THEN RAISE; END IF;
            END;
            """;

        String createTable = """
            BEGIN
                EXECUTE IMMEDIATE '
                    CREATE TABLE events (
                        id          NUMBER        PRIMARY KEY,
                        title       VARCHAR2(200) NOT NULL,
                        event_date  DATE          NOT NULL,
                        event_time  VARCHAR2(10),
                        description VARCHAR2(1000),
                        type        VARCHAR2(50)  NOT NULL,
                        priority    VARCHAR2(20)  DEFAULT ''MOYENNE'' NOT NULL,
                        completed   NUMBER(1)     DEFAULT 0 NOT NULL
                    )
                ';
            EXCEPTION
                WHEN OTHERS THEN
                    IF SQLCODE != -955 THEN RAISE; END IF;
            END;
            """;

        String createTrigger = """
            BEGIN
                EXECUTE IMMEDIATE '
                    CREATE OR REPLACE TRIGGER events_bir
                    BEFORE INSERT ON events
                    FOR EACH ROW
                    BEGIN
                        IF :NEW.id IS NULL THEN
                            SELECT events_seq.NEXTVAL INTO :NEW.id FROM DUAL;
                        END IF;
                    END;
                ';
            EXCEPTION
                WHEN OTHERS THEN NULL;
            END;
            """;

        // NOTE: do NOT put the shared connection in try-with-resources,
        // that would close it after every call.
        try {
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createSeq);
                stmt.execute(createTable);
                stmt.execute(createTrigger);
            }
            System.out.println("Oracle database initialised.");
        } catch (SQLException e) {
            System.err.println("Oracle init error: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Close error: " + e.getMessage());
        }
    }
}