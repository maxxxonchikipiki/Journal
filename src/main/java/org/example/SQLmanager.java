package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLmanager {
    private Connection connection;

    public SQLmanager() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:journal.db");
        System.out.println("Подключение к базе данных установлено!");
    }

    public void createStudents() throws SQLException{
        Statement stmt = connection.createStatement();

        String sql = """
                CREATE TABLE IF NOT EXISTS Students (
                studentId INTEGER PRIMARY KEY AUTOINCREMENT,
                stId TEXT NOT NULL,
                studentFullName TEXT NOT NULL
            )
        """;

        stmt.execute(sql);
        stmt.close();

        System.out.println("Таблица Students создана!");
    }

    public void createSubjects() throws SQLException {
        Statement stmt = connection.createStatement();

        String sql = """
                CREATE TABLE IF NOT EXISTS Subjects(
                subjectId INTEGER PRIMARY KEY AUTOINCREMENT,
                subjectName TEXT NOT NULL
                )
        """;

        stmt.execute(sql);
        stmt.close();

        System.out.println("Таблица Subjects создана!");
    }

    public void createTeachers() throws SQLException {
        Statement stmt = connection.createStatement();

        String sql = """
                CREATE TABLE IF NOT EXISTS Teachers (
                teacherId INTEGER PRIMARY KEY AUTOINCREMENT,
                stTeacherId TEXT NOT NULL,
                teacherFullName TEXT NOT NULL,
                subjectId INTEGER,
                FOREIGN KEY (subjectId) REFERENCES Subjects(subjectId)
            )
        """;

        stmt.execute(sql);
        stmt.close();

        System.out.println("Таблица Teachers создана!");
    }

    public void createGrades() throws SQLException{
        Statement stmt = connection.createStatement();
        String sql = """
                CREATE TABLE IF NOT EXISTS Grades(
                gradeId INTEGER PRIMARY KEY AUTOINCREMENT,
                studentId INTEGER,
                teacherId INTEGER,
                subjectId INTEGER, 
                value INTEGER,
                data TEXT NOT NULL,
                FOREIGN KEY(studentId) REFERENCES Students(studentId),
                FOREIGN KEY(teacherId) REFERENCES Teachers(teacherId),
                FOREIGN KEY(subjectId) REFERENCES Subjects(subjectId)
            )
        """;

        stmt.execute(sql);
        stmt.close();
    }

    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) {
        try {
            SQLmanager db = new SQLmanager();
            db.createStudents();
            db.createSubjects();
            db.createTeachers();
            db.createGrades();
            db.disconnect();
            System.out.println("Все таблицы созданы!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
