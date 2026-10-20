package org.example;

public class Teacher {
    private int teacherId;
    private String stTeacherId;
    private String teacherFullName;
    private int subject;

    public Teacher(String stId, String name, int sub) {
        stTeacherId = stId;
        teacherFullName = name;
        subject = sub;
    }

    public Teacher(int id, String stId, String name, int sub) {
        teacherId = id;
        stTeacherId = stId;
        teacherFullName = name;
        subject = sub;
    }

    public int getTeacherId() { return teacherId; }
    public String getStTeacherId() { return stTeacherId; }
    public String getTeacherFullName() { return teacherFullName; }
    public int getSubject() { return subject; }

    public void setStTeacherId(String st) { stTeacherId = st; }
    public void setTeacherFullName(String name) { teacherFullName = name; }
    public void setSubject(int sub) { subject = sub; }
}
