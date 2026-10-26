package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

    public void addStudent(Student student) throws SQLException{
        String sql = "INSERT INTO Students (stId, studentFullName) VALUES (?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, student.getStId());
        pstmt.setString(2, student.getStudentFullName());

        pstmt.executeUpdate();
        pstmt.close();

        System.out.println("Студент добавлен: " + student.getStudentFullName());
    }

    public void addSubject(Subject subject) throws SQLException{
        String sql = "INSERT INTO Subjects (subjectName) VALUES (?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, subject.getSubjectName());

        pstmt.executeUpdate();
        pstmt.close();

        System.out.println("Предмет добавлен: " + subject.getSubjectName());
    }

    public void addTeacher(Teacher teacher) throws SQLException{
        String sql = "INSERT INTO Teachers (stTeacherId, teacherFullName, subjectId) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, teacher.getStTeacherId());
        pstmt.setString(2, teacher.getTeacherFullName());
        pstmt.setInt(3, teacher.getSubject());

        pstmt.executeUpdate();
        pstmt.close();

        System.out.println("Преподаватель добавлен: " + teacher.getTeacherFullName());
    }

    public void addGrade(Grade grade) throws SQLException{
        String sql = "INSERT INTO Grades (studentId, teacherId, subjectId, value, data) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setInt(1, grade.getStudentId());
        pstmt.setInt(2, grade.getTeacherId());
        pstmt.setInt(3, grade.getSubjectId());
        pstmt.setInt(4, grade.getValue());
        pstmt.setString(5, grade.getDate());

        pstmt.executeUpdate();
        pstmt.close();

        System.out.println("Оценка добавлена: " + grade.getValue());
    }

    public List<Student> getAllStudents() throws SQLException{
        List<Student> students = new ArrayList<>();

        String sql = "SELECT studentId, stId, studentFullName FROM Students";

        Statement stmt = connection.createStatement();

        ResultSet res = stmt.executeQuery(sql);

        while(res.next()){
            int id =  res.getInt("studentId");
            String stId = res.getString("stId");
            String name = res.getString("studentFullName");

            Student student = new Student(id, stId, name);

            students.add(student);
        }

        stmt.close();
        res.close();

        return students;
    }

    public List<Subject> getAllSubjects() throws SQLException{
        List<Subject> subjects = new ArrayList<>();

        String sql = "SELECT subjectId, subjectName FROM Subjects";

        Statement stmt = connection.createStatement();

        ResultSet res = stmt.executeQuery(sql);

        while(res.next()){
            int id =  res.getInt("subjectId");
            String name = res.getString("subjectName");

            Subject subject = new Subject(id, name);

            subjects.add(subject);
        }

        stmt.close();
        res.close();

        return subjects;
    }

    public List<Teacher> getAllTeachers() throws SQLException{
        List<Teacher> teachers = new ArrayList<>();

        String sql = "SELECT teacherId, stTeacherId, teacherFullName, subjectId FROM Teachers";

        Statement stmt = connection.createStatement();

        ResultSet res = stmt.executeQuery(sql);

        while(res.next()){
            int id =  res.getInt("teacherId");
            String stId = res.getString("stTeacherId");
            String name = res.getString("teacherFullName");
            int sub = res.getInt("subjectId");

            Teacher teacher = new Teacher(id, stId, name, sub);

            teachers.add(teacher);
        }

        stmt.close();
        res.close();

        return teachers;
    }

    public List<Grade> getAllGrades() throws SQLException{
        List<Grade> grades = new ArrayList<>();

        String sql = "SELECT gradeId, studentId, teacherId, subjectId, value, data FROM Grades";

        Statement stmt = connection.createStatement();

        ResultSet res = stmt.executeQuery(sql);

        while(res.next()){
            int gradeId =  res.getInt("gradeId");
            int studentId =  res.getInt("studentId");
            int teacherId =  res.getInt("teacherId");
            int subjectId =  res.getInt("subjectId");
            int value =  res.getInt("value");
            String data =  res.getString("data");

            Grade grade = new Grade(gradeId, studentId, teacherId, subjectId, value, data);

            grades.add(grade);
        }

        stmt.close();
        res.close();

        return grades;
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
