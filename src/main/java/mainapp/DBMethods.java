package mainapp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBMethods {

    // Path to the local SQLite file. No server, username, or password needed —
    // SQLite is file-based. System.getProperty("user.dir") resolves to wherever
    // you run the app from (the project root when launched from NetBeans/Maven).
    private static final String DB_PATH = "jdbc:sqlite:" + System.getProperty("user.dir") + "/schedule.db";

    // One shared connection object reused across all method calls.
    private static Connection conn = null;

    // ── Connection ────────────────────────────────────────────────────────────

    // Returns the open connection, or opens a new one if it's null or was closed.
    // Called internally by every method below — JFrames never touch this directly.
    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(DB_PATH);
            // SQLite doesn't enforce foreign keys by default — this PRAGMA turns it on
            // so that ON DELETE CASCADE actually works (deleting a module auto-deletes
            // its sub-modules, tasks, and subtasks).
            conn.createStatement().execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    // Creates all four tables if they don't already exist.
    // Safe to call every startup — IF NOT EXISTS means it skips creation silently
    // when the tables are already there. Call this once in MainMenu before the
    // app window appears.
    public static void initializeDB() {
        // Text block (Java 15+) — triple quotes let us write multi-line SQL cleanly.
        // AUTOINCREMENT means SQLite assigns the id automatically on each INSERT.
        String modules = """
            CREATE TABLE IF NOT EXISTS MODULES (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                name       TEXT    NOT NULL,
                start_date TEXT    NOT NULL,
                end_date   TEXT    NOT NULL
            )""";

        String subModules = """
            CREATE TABLE IF NOT EXISTS SUB_MODULES (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                module_id INTEGER NOT NULL,
                label     TEXT    NOT NULL,
                FOREIGN KEY (module_id) REFERENCES MODULES(id) ON DELETE CASCADE
            )""";

        String tasks = """
            CREATE TABLE IF NOT EXISTS TASKS (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                sub_module_id INTEGER NOT NULL,
                description   TEXT    NOT NULL,
                due_date      TEXT,
                status        TEXT    DEFAULT 'pending',
                created_at    TEXT    DEFAULT (datetime('now')),
                completed_at  TEXT,
                FOREIGN KEY (sub_module_id) REFERENCES SUB_MODULES(id) ON DELETE CASCADE
            )""";

        String subtasks = """
            CREATE TABLE IF NOT EXISTS SUBTASKS (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                task_id     INTEGER NOT NULL,
                description TEXT    NOT NULL,
                status      TEXT    DEFAULT 'pending',
                FOREIGN KEY (task_id) REFERENCES TASKS(id) ON DELETE CASCADE
            )""";

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(modules);
            stmt.execute(subModules);
            stmt.execute(tasks);
            stmt.execute(subtasks);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── MODULES ───────────────────────────────────────────────────────────────

    // Inserts a new module row and returns the auto-generated id.
    // PreparedStatement (?) placeholders prevent SQL injection and handle
    // special characters in user input automatically.
    // RETURN_GENERATED_KEYS tells JDBC to give back the new row's id after insert.
    public static int addModule(String name, String startDate, String endDate) throws SQLException {
        String sql = "INSERT INTO MODULES (name, start_date, end_date) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, startDate);
            ps.setString(3, endDate);
            ps.executeUpdate();
            return ps.getGeneratedKeys().getInt(1); // column 1 = the new id
        }
    }

    public static List<String> getModules() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM MODULES ORDER BY start_date";
        try (ResultSet rs = getConnection().createStatement().executeQuery(sql)) {
            while (rs.next()) list.add(rs.getString("name"));
        }
        return list;
    }

    // Deletes a module by id. Because foreign keys + ON DELETE CASCADE are enabled,
    // this also automatically deletes all its sub-modules, tasks, and subtasks.
    public static void deleteModule(int moduleID) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM MODULES WHERE id = ?")) {
            ps.setInt(1, moduleID);
            ps.executeUpdate();
        }
    }

    // Looks up a module's id by name. Useful when you only have the display name
    // (e.g. from a JComboBox) and need the id to pass to other methods.
    // Returns -1 if no match is found.
    public static int getModuleIdByName(String name) throws SQLException {
        String sql = "SELECT id FROM MODULES WHERE name = ? LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }

    public static String getModuleNameById(int moduleId) throws SQLException {
        String sql = "SELECT name FROM MODULES WHERE id = ? LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("name") : null;
        }
    }

    // Gets the start [0] and end [1] date for modules
    public static String[] getTimePeriodByName(int modID) throws SQLException {
        String sql = "SELECT start_date, end_date FROM MODULES WHERE id = ? LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, modID);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? new String[]{rs.getString("start_date"), rs.getString("end_date")} : null;
        }
    }

    // Updates the name and date range of an existing module. oName -> original module name
    // Updates a module matched by its original name. The id is never touched.
    public static void updateModule(String oName, String name, String startDate, String endDate) throws SQLException {
        String sql = "UPDATE MODULES SET name = ?, start_date = ?, end_date = ? WHERE name = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, startDate);
            ps.setString(3, endDate);
            ps.setString(4, oName);
            ps.executeUpdate();
        }
    }

    // ── SUB_MODULES ───────────────────────────────────────────────────────────

    // Inserts a sub-module under the given module and returns its new id.
    public static int addSubModule(int moduleId, String label) throws SQLException {
        String sql = "INSERT INTO SUB_MODULES (module_id, label) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, moduleId);
            ps.setString(2, label);
            ps.executeUpdate();
            return ps.getGeneratedKeys().getInt(1);
        }
    }

    // Returns just the label strings for all sub-modules belonging to a module.
    // Useful for populating a JComboBox without needing the full row data.
    public static List<String> getSubModuleLabels(int moduleId) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT label FROM SUB_MODULES WHERE module_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString("label"));
        }
        return list;
    }

    // Deletes a single sub-module. Cascades to its tasks and their subtasks.
    public static void deleteSubModule(int subModuleId) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM SUB_MODULES WHERE id = ?")) {
            ps.setInt(1, subModuleId);
            ps.executeUpdate();
        }
    }
    
    // Retrieves the Sub-Module's ID from its name
    public static int getSubModuleIdByName(String name) throws SQLException {
        String sql = "SELECT id FROM SUB_MODULES WHERE label = ? LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }
    
    // Updates the label of the sub-module via its ID
    public static void updateSubModule(int sModID, String name) throws SQLException {
        String sql = "UPDATE SUB_MODULES SET label = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, sModID);
            ps.executeUpdate();
        }
    }

    // ── TASKS ─────────────────────────────────────────────────────────────────

    // Inserts a task under a sub-module. status defaults to 'pending' and
    // created_at defaults to now — both handled by the table definition,
    // so we don't need to pass them here.
    public static int addTask(int subModuleId, String description, String dueDate) throws SQLException {
        String sql = "INSERT INTO TASKS (sub_module_id, description, due_date) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, subModuleId);
            ps.setString(2, description);
            ps.setString(3, dueDate);
            ps.executeUpdate();
            return ps.getGeneratedKeys().getInt(1);
        }
    }

    // Returns task descriptions for a sub-module, sorted by due date.
    public static List<String> getTaskDescriptions(int subModuleId) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT description FROM TASKS WHERE sub_module_id = ? ORDER BY due_date";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, subModuleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString("description"));
        }
        return list;
    }

    // Updates a task's status. If marking as completed, also stamps completed_at
    // with the current time using SQLite's datetime('now') function.
    public static void updateTaskStatus(int taskId, String status) throws SQLException {
        String sql = status.equals("completed")
            ? "UPDATE TASKS SET status = ?, completed_at = datetime('now') WHERE id = ?"
            : "UPDATE TASKS SET status = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, taskId);
            ps.executeUpdate();
        }
    }

    // Deletes a task and cascades to its subtasks.
    public static void deleteTask(int taskId) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM TASKS WHERE id = ?")) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        }
    }

    // ── SUBTASKS ──────────────────────────────────────────────────────────────

    // Inserts a subtask under a task. status defaults to 'pending'.
    public static int addSubtask(int taskId, String description) throws SQLException {
        String sql = "INSERT INTO SUBTASKS (task_id, description) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, taskId);
            ps.setString(2, description);
            ps.executeUpdate();
            return ps.getGeneratedKeys().getInt(1);
        }
    }

    // Returns subtask descriptions for a given task.
    public static List<String> getSubtaskDescriptions(int taskId) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT description FROM SUBTASKS WHERE task_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString("description"));
        }
        return list;
    }

    // Updates a subtask's status ('pending' or 'completed').
    public static void updateSubtaskStatus(int subtaskId, String status) throws SQLException {
        String sql = "UPDATE SUBTASKS SET status = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, subtaskId);
            ps.executeUpdate();
        }
    }
}
