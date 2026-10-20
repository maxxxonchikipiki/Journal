package org.example;

public class Grade {
    private int gradeId;
    private int studentId;
    private int teacherId;
    private int subjectId;
    private int value;
    private String date;

    public Grade(int stId, int teachId, int subId, int grade, String data) {
        studentId = stId;
        teacherId = teachId;
        subjectId = subId;
        value = grade;
        date = data;
    }
    
    public Grade(int gId, int stId, int teachId, int subId, int grade, String data) {
        gradeId = gId;
        studentId = stId;
        teacherId = teachId;
        subjectId = subId;
        value = grade;
        date = data;
    }

    public int getGradeId() { return gradeId; }
    public int getStudentId() { return studentId; }
    public int getTeacherId() { return teacherId; }
    public int getSubjectId() { return subjectId; }
    public int getValue() { return value; }
    public String getDate() { return date; }

    public void setValue(int val) { value = val; }
    public void setDate(String data) { date = data; }
}
