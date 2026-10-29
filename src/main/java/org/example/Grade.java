package org.example;

public class Grade {
    private int gradeId;
    private int studentId;
    private int teacherId;
    private int subjectId;
    private String value;
    private String date;
    private String comment;

    public Grade(int stId, int teachId, int subId, String grade, String data, String comment) {
        studentId = stId;
        teacherId = teachId;
        subjectId = subId;
        value = grade;
        date = data;
        this.comment = comment;
    }

    public Grade(int gId, int stId, int teachId, int subId, String grade, String data, String comment) {
        gradeId = gId;
        studentId = stId;
        teacherId = teachId;
        subjectId = subId;
        value = grade;
        date = data;
        this.comment = comment;
    }

    public int getGradeId() { return gradeId; }
    public int getStudentId() { return studentId; }
    public int getTeacherId() { return teacherId; }
    public int getSubjectId() { return subjectId; }
    public String getValue() { return value; }
    public String getDate() { return date; }
    public String getComment() { return comment; }

    public void setGradeId(int gradeId) { this.gradeId = gradeId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public void setValue(String val) { value = val; }
    public void setDate(String data) { date = data; }
    public void setComment(String comment) { this.comment = comment; }
}
