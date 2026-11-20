package com.example.tracker.service;

import com.example.tracker.model.Category;
import com.example.tracker.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageManager {
    private static class Holder {
        private static final StorageManager INSTANCE = new StorageManager();
    }

    private Connection connection;

    private StorageManager() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:tracker.db");
            initDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static StorageManager getInstance() {
        return Holder.INSTANCE;
    }

    private void initDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, description TEXT, category_id INTEGER, estimated_minutes INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS sessions (id INTEGER PRIMARY KEY AUTOINCREMENT, task_id INTEGER, start_time INTEGER, end_time INTEGER, interruption_reason TEXT)");

            // Добавляем базовые категории, если их еще нет
            stmt.execute("INSERT INTO categories (name) SELECT 'Учеба' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Учеба')");
            stmt.execute("INSERT INTO categories (name) SELECT 'Работа' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Работа')");
            stmt.execute("INSERT INTO categories (name) SELECT 'Отдых' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Отдых')");
            stmt.execute("INSERT INTO categories (name) SELECT 'Спорт' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Спорт')");
        }
    }

    public void saveTask(Task task) {
        String sql = "INSERT INTO tasks(name, description, category_id, estimated_minutes) VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, task.getName());
            pstmt.setString(2, task.getDescription());
            pstmt.setLong(3, task.getCategory() != null ? task.getCategory().getId() : 1);
            pstmt.setInt(4, task.getEstimatedMinutes());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) task.setId(rs.getLong(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Task> loadTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.*, c.name as cat_name, c.id as cat_id FROM tasks t LEFT JOIN categories c ON t.category_id = c.id";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Category cat = new Category(rs.getLong("cat_id"), rs.getString("cat_name"));
                Task task = new Task.Builder(rs.getString("name"))
                        .description(rs.getString("description"))
                        .category(cat)
                        .estimatedMinutes(rs.getInt("estimated_minutes"))
                        .build();
                task.setId(rs.getLong("id"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public List<Category> loadCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(new Category(rs.getLong("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public void saveSession(long taskId, long startTime, long endTime, String reason) {
        String sql = "INSERT INTO sessions(task_id, start_time, end_time, interruption_reason) VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, taskId);
            pstmt.setLong(2, startTime);
            pstmt.setLong(3, endTime);
            pstmt.setString(4, reason);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("ОШИБКА СОХРАНЕНИЯ: " + e.getMessage());
        }
    }

    public Map<String, Integer> getTimeByCategory(ReportFilter filter) {
        Map<String, Integer> result = new HashMap<>();
        String sql = "SELECT c.name, SUM(s.end_time - s.start_time) as total_ms " +
                "FROM sessions s JOIN tasks t ON s.task_id = t.id " +
                "JOIN categories c ON t.category_id = c.id " +
                "WHERE s.start_time >= ? AND s.end_time <= ? " +
                "GROUP BY c.name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, filter.getStartTime());
            pstmt.setLong(2, filter.getEndTime());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int seconds = (int) (rs.getLong("total_ms") / 1000);
                if (seconds > 0) {
                    result.put(rs.getString("name"), seconds);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}