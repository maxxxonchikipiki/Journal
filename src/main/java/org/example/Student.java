package org.example;

public class Student {
    private int studentId;
    private int userId;
    private String stId;
    private String studentFullName;

    public Student(String stuId, String name) {
        stId = stuId;
        studentFullName = name;
    }

    public Student(int distinctId, String stuId, String name) {
        studentId = distinctId;
        stId = stuId;
        studentFullName = name;
    }

    public Student(int distinctId, int uId, String stuId, String name){
	studentId = distinctId;
	userId = uId;
	stId = stuId;
	studentFullName = name;
    }

    public int getStudentId() { return studentId; }
    public int getUserId() { return userId; }
    public String getStId() { return stId; }
    public String getStudentFullName() { return studentFullName; }

    public void setUserId(int uId) { userId = uId; }
    public void setStId(String st) { stId = st; }
    public void setStudentFullName(String name) { studentFullName = name; }
}
