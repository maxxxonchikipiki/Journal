package org.example;

public class Teacher{
    private int teacherId;
    private int userId;
    private String stTeacherId;
    private String teacherFullName;
    private int subjectId;

    public Teacher(String stId, String name, int sub){
        stTeacherId = stId;
        teacherFullName = name;
        subjectId = sub;
    }
    public Teacher(int id, String stId, String name, int sub){
        teacherId = id;
        stTeacherId = stId;
        teacherFullName = name;
        subjectId = sub;
    }

    public Teacher(int id, int uId, String stId, String name, int sub) {
        teacherId = id;
        userId = uId;
        stTeacherId = stId;
        teacherFullName = name;
        subjectId = sub;
    }

    public int getTeacherId(){ return teacherId; }
    public int getUserId(){ return userId; }
    public String getStTeacherId(){ return stTeacherId; }
    public String getTeacherFullName(){ return teacherFullName; }
    public int getSubject(){ return subjectId; }

    public void setUserId(int uId){ userId = uId; }
    public void setStTeacherId(String st){ stTeacherId = st; }
    public void setTeacherFullName(String name){ teacherFullName = name; }
    public void setSubject(int sub){ subjectId = sub; }
}
