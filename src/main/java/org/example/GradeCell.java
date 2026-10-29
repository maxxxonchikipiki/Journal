package org.example;

public class GradeCell {
    private int studentId;
    private String date;
    private String value;
    private int gradeId;
    private String comment;

    public GradeCell(int studentId, String date, String value, int gradeId, String comment) {
        this.studentId = studentId;
        this.date = date;
        this.value = value;
        this.gradeId = gradeId;
        this.comment = comment;
    }

    public int getStudentId() { return studentId; }
    public String getDate() { return date; }
    public String getValue() { return value; }
    public int getGradeId() { return gradeId; }
    public String getComment() { return comment; }

    public void setValue(String value) { this.value = value; }
    public void setGradeId(int gradeId) { this.gradeId = gradeId; }
    public void setComment(String comment) { this.comment = comment; }
}
