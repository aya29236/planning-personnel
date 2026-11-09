# planning-personnel 

A desktop application for managing personal events, built with **JavaFX**, **Java**, and **Oracle Database**.

Developed as a university project for *Programmation Orientée Objet Avancée* at **Institut Supérieur de Gestion de Tunis (ISG Tunis)**.

**Author:**  Aya Fayachi

## ✨ Features

- **Event management:** Create, edit, delete, and view events.
- **Monthly calendar:** Navigate between months and select days to view events.
- **Search and filters:** Search by title and filter by type, priority, or date.
- **Sorting:** Organize events by date, title, or priority.
- **Important events:** Quickly access high-priority events.
- **Task completion:** Mark events as completed.
- **Statistics:** View event totals, completion rates, and statistics by type, priority, and month.
- **Two visual themes:** Choose between Girly Rose 🌸 and Boys Bleu 🚀.
- **Welcome screen:** Choose your name and preferred theme on first launch.
- **Live clock:** Display the current time in the application.
- **Automatic database setup:** Create the required database table, sequence, and trigger on first launch.

## 🛠️ Technologies

| Component             | Technology             |
| --------------------- | ---------------------- |
| Language              | Java 21+               |
| Interface             | JavaFX, FXML, CSS      |
| Database              | Oracle Database 21c XE |
| Database connectivity | JDBC (ojdbc11)         |
| Architecture          | MVC + DAO              |
| IDE                   | Apache NetBeans        |
| Build system          | Apache Ant             |

## 📋 Requirements

Before running the application, install:

- **JDK 21 or newer**
- **JavaFX SDK 21 or newer**
- **Oracle Database XE**
- **Oracle JDBC Driver** (`ojdbc11.jar`)
- **Apache NetBeans** (with Ant support)

## 🚀 Installation & Setup

### 1. Get the project

Clone the repository:

```bash
git clone https://github.com/<your-username>/<your-repository>.git
```

**Important:** The current project configuration uses an absolute Windows path. For the application to run correctly, place the project in:

```text
C:\
```

### 2. Add the required libraries

Create a `lib` folder if it does not already exist.

Copy the following files into `C:\AgendaPersonnel\lib`:

- `javafx.base.jar`
- `javafx.controls.jar`
- `javafx.fxml.jar`
- `javafx.graphics.jar`
- `javafx.media.jar`
- `ojdbc11.jar`

The JavaFX libraries are available in the JavaFX SDK, while the JDBC driver can be downloaded from Oracle.

### 3. Configure Oracle Database

Create your database configuration file:

```bat
cd C:\
copy db.properties.example db.properties
```

Edit `db.properties` with your Oracle credentials:

```properties
db.url=jdbc:oracle:thin:@localhost:1521:XE
db.user=your_username
db.password=your_password
```

You can also configure the connection using environment variables:

- `AGENDA_DB_URL`
- `AGENDA_DB_USER`
- `AGENDA_DB_PASSWORD`

Environment variables take priority over `db.properties`.

**Note:** Oracle Database must be running before launching the application. The configured database user must have permission to create tables, sequences, and triggers.

The application automatically initializes the required database objects on first launch.

### 4. Run the application

**Using NetBeans:**

1. Open NetBeans.
2. Select *File → Open Project*.
3. Open `C:\`planning-personnel
4. Select *Run → Clean and Build Project*.
5. Click *Run Project* (F6).

**Using the terminal:**

```bat
cd C:\planning-personnel
ant run
```

Make sure to launch the application from the project directory so it can find its configuration files.

## 📁 Project Structure

```text
AgendaPersonnel/
├── src/
│   └── agendapersonnel/
│       ├── Main.java
│       ├── Event.java
│       ├── EventType.java
│       ├── Priority.java
│       ├── EventDAO.java
│       ├── DatabaseManager.java
│       ├── ThemeManager.java
│       ├── Controllers/
│       ├── FXML/
│       └── CSS/
│           ├── girly-theme.css
│           └── boys-theme.css
├── lib/
├── nbproject/
├── build.xml
└── db.properties.example
```

## 🗄️ Database Structure

The application uses an Oracle table named `events`.

| Column      | Description                |
| ----------- | -------------------------- |
| id          | Unique event identifier    |
| title       | Event title                |
| event_date  | Event date                 |
| event_time  | Event time                 |
| description | Event description          |
| type        | Event category             |
| priority    | Event priority             |
| completed   | Completion status (0 or 1) |

The application also uses an Oracle sequence (`events_seq`) and trigger (`events_bir`) to generate event IDs automatically.

## 🔮 Future Improvements

The following features are planned for future versions:

- **Database error notifications:** Display a user-friendly popup when Oracle Database is unavailable.
- **Event reminders:** Notify users before scheduled events.
- **All Events view:** Add a dedicated screen displaying the complete event list.
- **Settings screen:** Provide a dedicated interface for application preferences.
- **Theme persistence:** Save theme changes made from the main window.
- **Unit testing:** Add JUnit tests for event logic and time parsing.
- **Flexible installation:** Remove the hardcoded project path to support different installation directories.
- **Modern build system:** Migrate from Ant to Maven or Gradle.

## 📄 License

This project is developed for educational purposes at ISG Tunis.

A license can be added in a future release.

