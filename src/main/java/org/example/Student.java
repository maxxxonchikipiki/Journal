package org.example;

public class Student {
    private int studentId;
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

    public int getStudentId() { return studentId; }
    public String getStId() { return stId; }
    public String getStudentFullName() { return studentFullName; }

    public void setStId(String st) { stId = st; }
    public void setStudentFullName(String name) { studentFullName = name; }
}
