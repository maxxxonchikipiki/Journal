package org.example;

public class Student {
    private int studentId;
    private int userId;
    private String stId;
    private String studentFullName;
    private String className;

    public Student(String stuId, String name, String nameClass) {
        stId = stuId;
        studentFullName = name;
        className = nameClass;
    }

    public Student(int distinctId, String stuId, String name, String nameClass) {
        studentId = distinctId;
        stId = stuId;
        studentFullName = name;
        className = nameClass;
    }

    public Student(int distinctId, int uId, String stuId, String name, String nameClass){
        studentId = distinctId;
        userId = uId;
        stId = stuId;
        studentFullName = name;
        className = nameClass;
    }

    public int getStudentId() { return studentId; }
    public int getUserId() { return userId; }
    public String getStId() { return stId; }
    public String getStudentFullName() { return studentFullName; }
    public String getClassName() { return className; }

    public void setUserId(int uId) { userId = uId; }
    public void setStId(String st) { stId = st; }
    public void setStudentFullName(String name) { studentFullName = name; }
    public void setClassName(String name) { className = name; }

    @Override
    public String toString() {
        return studentFullName + " (" + className + ")";
    }
}
