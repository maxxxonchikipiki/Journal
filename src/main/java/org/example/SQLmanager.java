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
	connection.createStatement().execute("PRAGMA foreign_keys = ON;");
        System.out.println("Подключение к базе данных установлено!");
    }

    public void createStudents() throws SQLException{
        Statement stmt = connection.createStatement();

        String sql = """
                CREATE TABLE IF NOT EXISTS Students (
                studentId INTEGER PRIMARY KEY AUTOINCREMENT,
		userId INTEGER,
                stId TEXT NOT NULL,
                studentFullName TEXT NOT NULL,
		FOREIGN KEY (userId) REFERENCES Users(UserId) ON DELETE CASCADE
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
		userId INTEGER,
                stTeacherId TEXT NOT NULL,
                teacherFullName TEXT NOT NULL,
                subjectId INTEGER,
		FOREIGN KEY (userId) REFERENCES Users(UserId) ON DELETE CASCADE,
                FOREIGN KEY (subjectId) REFERENCES Subjects(subjectId) ON DELETE CASCADE
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
                date TEXT NOT NULL,
                FOREIGN KEY(studentId) REFERENCES Students(studentId) ON DELETE CASCADE,
                FOREIGN KEY(teacherId) REFERENCES Teachers(teacherId) ON DELETE CASCADE,
                FOREIGN KEY(subjectId) REFERENCES Subjects(subjectId) ON DELETE CASCADE
            )
        """;

        stmt.execute(sql);
        stmt.close();
    }

    public void createUsers() throws SQLException {
	Statement stmt = connection.createStatement();
	String sql = """
		CREATE TABLE IF NOT EXISTS Users (
		userId INTEGER PRIMARY KEY AUTOINCREMENT,
		login TEXT UNIQUE NOT NULL,
		password TEXT NOT NULL,
		role TEXT NOT NULL
            )
        """;

	stmt.execute(sql);
	stmt.close();
	System.out.println("Таблица Users создана!");
    }

    public void addStudent(Student student) throws SQLException{
        String sql = "INSERT INTO Students (userId, stId, studentFullName) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

	pstmt.setInt(1, student.getUserId());
        pstmt.setString(2, student.getStId());
        pstmt.setString(3, student.getStudentFullName());

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
        String sql = "INSERT INTO Teachers (userId, stTeacherId, teacherFullName, subjectId) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

	pstmt.setInt(1, teacher.getUserId());
        pstmt.setString(2, teacher.getStTeacherId());
        pstmt.setString(3, teacher.getTeacherFullName());
        pstmt.setInt(4, teacher.getSubject());

        pstmt.executeUpdate();
        pstmt.close();

        System.out.println("Преподаватель добавлен: " + teacher.getTeacherFullName());
    }

    public void addGrade(Grade grade) throws SQLException{
        String sql = "INSERT INTO Grades (studentId, teacherId, subjectId, value, date) VALUES (?, ?, ?, ?, ?)";
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

    public User registerUser(String login, String password, String role) throws SQLException {
	String sql = "INSERT INTO Users (login, password, role) VALUES (?, ?, ?)";
	PreparedStatement pstmt = connection.prepareStatement(sql);

	pstmt.setString(1, login);
	pstmt.setString(2, password);
	pstmt.setString(3, role);

	pstmt.executeUpdate();
	pstmt.close();

	return getUserByLogin(login);
    }

    public User login(String login, String password) throws SQLException {
	String sql = "SELECT userId, login, password, role FROM Users WHERE login = ? AND password = ?";
	PreparedStatement pstmt = connection.prepareStatement(sql);

	pstmt.setString(1, login);
	pstmt.setString(2, password);

	ResultSet res = pstmt.executeQuery();
	User user = null;
	if (res.next()) {
	    user = new User(
		res.getInt("userId"),
		res.getString("login"),
		res.getString("password"),
		res.getString("role")
		);
	}

	res.close();
	pstmt.close();

	return user;
    }

    public User getUserByLogin(String login) throws SQLException {
	String sql = "SELECT userId, login, password, role FROM Users WHERE login = ?";
	PreparedStatement pstmt = connection.prepareStatement(sql);

	pstmt.setString(1, login);

	ResultSet res = pstmt.executeQuery();
	User user = null;
	if (res.next()) {
	    user = new User(
		res.getInt("userId"),
		res.getString("login"),
		res.getString("password"),
		res.getString("role")
	    );
	}

	res.close();
	pstmt.close();

	return user;
    }

    public List<Student> getAllStudents() throws SQLException{
        List<Student> students = new ArrayList<>();

        String sql = "SELECT studentId, userId, stId, studentFullName FROM Students";

        Statement stmt = connection.createStatement();

        ResultSet res = stmt.executeQuery(sql);

        while(res.next()){
            int id =  res.getInt("studentId");
	    int uId = res.getInt("userId");
            String stId = res.getString("stId");
            String name = res.getString("studentFullName");

            Student student = new Student(id, uId, stId, name);

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

        String sql = "SELECT teacherId, userId, stTeacherId, teacherFullName, subjectId FROM Teachers";

        Statement stmt = connection.createStatement();

        ResultSet res = stmt.executeQuery(sql);

        while(res.next()){
            int id =  res.getInt("teacherId");
	    int uId = res.getInt("userId");
            String stId = res.getString("stTeacherId");
            String name = res.getString("teacherFullName");
            int sub = res.getInt("subjectId");

            Teacher teacher = new Teacher(id, uId, stId, name, sub);

            teachers.add(teacher);
        }

        stmt.close();
        res.close();

        return teachers;
    }

    public List<Grade> getAllGrades() throws SQLException{
        List<Grade> grades = new ArrayList<>();

        String sql = "SELECT gradeId, studentId, teacherId, subjectId, value, date FROM Grades";

        Statement stmt = connection.createStatement();

        ResultSet res = stmt.executeQuery(sql);

        while(res.next()){
            int gradeId =  res.getInt("gradeId");
            int studentId =  res.getInt("studentId");
            int teacherId =  res.getInt("teacherId");
            int subjectId =  res.getInt("subjectId");
            int value =  res.getInt("value");
            String data =  res.getString("date");

            Grade grade = new Grade(gradeId, studentId, teacherId, subjectId, value, data);

            grades.add(grade);
        }

        stmt.close();
        res.close();

        return grades;
    }

    public List<User> getAllUsers() throws SQLException {
    	List<User> users = new ArrayList<>();

    	String sql = "SELECT userId, login, password, role FROM Users";

    	Statement stmt = connection.createStatement();

	ResultSet res = stmt.executeQuery(sql);

    	while (res.next()) {
            User user = new User(
            	res.getInt("userId"),
            	res.getString("login"),
            	res.getString("password"),
            	res.getString("role")
            );
            users.add(user);
    	}

    	res.close();
    	stmt.close();

    	return users;
    }

    public Student getStudentById(int studentId) throws SQLException{
        String sql = "SELECT studentId, userId, stId, studentFullName FROM Students WHERE studentId = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setInt(1, studentId);

        ResultSet res = pstmt.executeQuery();

        Student student = null;
        if (res.next()){
            int id = res.getInt("studentId");
	    int uId = res.getInt("userId");
            String stId = res.getString("stId");
            String name = res.getString("studentFullName");

            student = new Student(id, uId, stId, name);
        }

        res.close();
        pstmt.close();

        return student;
    }

    public Subject getSubjectById(int subjectId) throws SQLException{
        String sql = "SELECT subjectId, subjectName FROM Subjects WHERE subjectId = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setInt(1, subjectId);

        ResultSet res = pstmt.executeQuery();

        Subject subject = null;
        if (res.next()){
            int id = res.getInt("subjectId");
            String name = res.getString("subjectName");

            subject = new Subject(id, name);
        }

        res.close();
        pstmt.close();

        return subject;
    }

    public Teacher getTeacherById(int teacherId) throws SQLException{
        String sql = "SELECT teacherId, userId, stTeacherId, teacherFullName, subjectId FROM Teachers WHERE teacherId = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setInt(1, teacherId);

        ResultSet res = pstmt.executeQuery();

        Teacher teacher = null;
        if (res.next()){
            int id = res.getInt("teacherId");
	    int uId = res.getInt("userId");
            String stId = res.getString("stTeacherId");
            String name = res.getString("teacherFullName");
            int subId = res.getInt("subjectId");

            teacher = new Teacher(id, uId, stId, name, subId);
        }

        res.close();
        pstmt.close();

        return teacher;
    }

    public Grade getGradeById(int gradeId) throws SQLException{
        String sql = "SELECT gradeId, studentId, teacherId, subjectId, value, date FROM Grades WHERE gradeId = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setInt(1, gradeId);

        ResultSet res = pstmt.executeQuery();

        Grade grade = null;
        if (res.next()){
            int degreeId = res.getInt("gradeId");
            int student = res.getInt("studentId");
            int teacher = res.getInt("teacherId");
            int subject = res.getInt("subjectId");
            int val = res.getInt("value");
            String data = res.getString("date");


            grade = new Grade(degreeId, student, teacher, subject, val, data);
        }

        res.close();
        pstmt.close();

        return grade;
    }

    public User getUserById(int userId) throws SQLException {
	String sql = "SELECT userId, login, password, role FROM Users WHERE userId = ?";
	PreparedStatement pstmt = connection.prepareStatement(sql);

	pstmt.setInt(1, userId);

	ResultSet res = pstmt.executeQuery();
	User user = null;
	if (res.next()) {
	    user = new User(
		res.getInt("userId"),
		res.getString("login"),
		res.getString("password"),
		res.getString("role")
	    );
	}

	res.close();
	pstmt.close();

	return user;
    }

    public Student getStudentByUserId(int userId) throws SQLException {
	String sql = "SELECT studentId, userId, stId, studentFullName FROM Students WHERE userId = ?";
	PreparedStatement pstmt = connection.prepareStatement(sql);

	pstmt.setInt(1, userId);

	ResultSet res = pstmt.executeQuery();
	Student student = null;
	if (res.next()) {
	    student = new Student(
		res.getInt("studentId"),
		res.getInt("userId"),
		res.getString("stId"),
		res.getString("studentFullName")
	    );
	}

	res.close();
	pstmt.close();

	return student;
    }

    public Teacher getTeacherByUserId(int userId) throws SQLException {
	String sql = "SELECT teacherId, userId, stTeacherId, teacherFullName, subjectId FROM Teachers WHERE userId = ?";
	PreparedStatement pstmt = connection.prepareStatement(sql);

	pstmt.setInt(1, userId);

	ResultSet res = pstmt.executeQuery();
	Teacher teacher = null;
	if (res.next()) {
 	    teacher = new Teacher(
 		res.getInt("teacherId"),
      		res.getInt("userId"),
       		res.getString("stTeacherId"),
           	res.getString("teacherFullName"),
            	res.getInt("subjectId")
            );
    	}

   	res.close();
    	pstmt.close();

    	return teacher;
    }

    public void updateStudent(Student student) throws SQLException{
        String sql = "UPDATE Students SET stId = ?, studentFullName = ? WHERE studentId = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, student.getStId());
        pstmt.setString(2, student.getStudentFullName());
        pstmt.setInt(3, student.getStudentId());

        pstmt.executeUpdate();
        pstmt.close();

        System.out.println("Студент обновлён: " + student.getStudentFullName());
    }

    public void updateSubject(Subject subject) throws SQLException{
        String sql = "UPDATE Subjects SET subjectName = ? WHERE subjectId = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, subject.getSubjectName());
        pstmt.setInt(2, subject.getSubjectId());

        pstmt.executeUpdate();
        pstmt.close();

        System.out.println("Предмет обновлён: " + subject.getSubjectName());
    }

    public void updateTeacher(Teacher teacher) throws SQLException{
        String sql = "UPDATE Teachers SET stTeacherId = ?, teacherFullName = ?, subjectId = ? WHERE teacherId = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setString(1, teacher.getStTeacherId());
        pstmt.setString(2, teacher.getTeacherFullName());
        pstmt.setInt(3, teacher.getSubject());
        pstmt.setInt(4, teacher.getTeacherId());

        pstmt.executeUpdate();
        pstmt.close();

        System.out.println("Преподаватель обновлён: " + teacher.getTeacherFullName());
    }

    public void updateGrade(Grade grade) throws SQLException{
        String sql = "UPDATE Grades SET studentId = ?, teacherId = ?, subjectId = ?, value = ?, date = ? WHERE gradeId = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setInt(1, grade.getStudentId());
        pstmt.setInt(2, grade.getTeacherId());
        pstmt.setInt(3, grade.getSubjectId());
        pstmt.setInt(4, grade.getValue());
        pstmt.setString(5, grade.getDate());
        pstmt.setInt(6, grade.getGradeId());

        pstmt.executeUpdate();
        pstmt.close();

        System.out.println("Оценка обновлена: " + grade.getValue());
    }

    public void deleteStudent(int studentId) throws SQLException{
        String sql = "DELETE FROM Students WHERE studentId = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setInt(1, studentId);

        pstmt.executeUpdate();

        pstmt.close();

        System.out.println("Студент с ID " + studentId + " удалён");
    }

    public void deleteSubject(int subjectId) throws SQLException{
        String sql = "DELETE FROM Subjects WHERE subjectId = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setInt(1, subjectId);

        pstmt.executeUpdate();

        pstmt.close();

        System.out.println("Предмет с ID " + subjectId + " удалён");
    }

    public void deleteTeacher(int teacherId) throws SQLException{
        String sql = "DELETE FROM Teachers WHERE teacherId = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setInt(1, teacherId);

        pstmt.executeUpdate();

        pstmt.close();

        System.out.println("Преподаватель с ID " + teacherId + " удалён");
    }

    public void deleteGrade(int gradeId) throws SQLException{
        String sql = "DELETE FROM Grades WHERE gradeId = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        pstmt.setInt(1, gradeId);

        pstmt.executeUpdate();

        pstmt.close();

        System.out.println("Оценка с ID " + gradeId + " удалена");
    }

    public void deleteUser(int userId) throws SQLException {
    	String sql = "DELETE FROM Users WHERE userId = ?";
    	PreparedStatement pstmt = connection.prepareStatement(sql);

	pstmt.setInt(1, userId);

    	pstmt.executeUpdate();
    	pstmt.close();

    	System.out.println("Пользователь с ID " + userId + " удалён");
    }

    public boolean hasUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users";
        Statement stmt = connection.createStatement();
        ResultSet res = stmt.executeQuery(sql);
        int count = 0;
        if (res.next()) {
            count = res.getInt(1);
        }
        res.close();
        stmt.close();
        return count > 0;
    }

    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private String generateTeacherLogin() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Teachers";
        Statement stmt = connection.createStatement();
        ResultSet res = stmt.executeQuery(sql);
        int count = 0;
        if (res.next()) {
            count = res.getInt(1);
        }
        res.close();
        stmt.close();
        return String.format("t%03d", count + 1);
    }

    private String generateStudentLogin() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Students";
        Statement stmt = connection.createStatement();
        ResultSet res = stmt.executeQuery(sql);
        int count = 0;
        if (res.next()) {
            count = res.getInt(1);
        }
        res.close();
        stmt.close();
        return String.format("st%03d", count + 1);
    }

    public User addTeacherWithAutoLogin(String teacherFullName, int subjectId) throws SQLException {
        String login = generateTeacherLogin();
        String password = generatePassword();
        String stTeacherId = login;

        User user = registerUser(login, password, "TEACHER");

        Teacher teacher = new Teacher(0, user.getUserId(), stTeacherId, teacherFullName, subjectId);
        addTeacher(teacher);

        System.out.println("Учитель добавлен: " + teacherFullName);
        System.out.println("Логин: " + login + ", Пароль: " + password);

        return user;
    }

    public User addStudentWithAutoLogin(String studentFullName) throws SQLException {
        String login = generateStudentLogin();
        String password = generatePassword();
        String stId = login;

        User user = registerUser(login, password, "STUDENT");

        Student student = new Student(0, user.getUserId(), stId, studentFullName);
        addStudent(student);

        System.out.println("Ученик добавлен: " + studentFullName);
        System.out.println("Логин: " + login + ", Пароль: " + password);

        return user;
    }

    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) {
        try {
            SQLmanager db = new SQLmanager();

	    db.createUsers();
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
