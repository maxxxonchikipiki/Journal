package org.example;

public class Teacher{
    private int teacherId;
    private int userId;
    private String stTeacherId;
    private String teacherFullName;
    private int subjectId;
    private String classNames;

    public Teacher(String stId, String name, int sub, String nameClass){
        stTeacherId = stId;
        teacherFullName = name;
        subjectId = sub;
        classNames = nameClass;
    }
    public Teacher(int id, String stId, String name, int sub, String nameClass){
        teacherId = id;
        stTeacherId = stId;
        teacherFullName = name;
        subjectId = sub;
        classNames = nameClass;
    }

    public Teacher(int id, int uId, String stId, String name, int sub, String nameClass) {
        teacherId = id;
        userId = uId;
        stTeacherId = stId;
        teacherFullName = name;
        subjectId = sub;
        classNames = nameClass;
    }

    public int getTeacherId(){ return teacherId; }
    public int getUserId(){ return userId; }
    public String getStTeacherId(){ return stTeacherId; }
    public String getTeacherFullName(){ return teacherFullName; }
    public int getSubject(){ return subjectId; }
    public String getClassNames(){ return classNames; }

    public void setUserId(int uId){ userId = uId; }
    public void setStTeacherId(String st){ stTeacherId = st; }
    public void setTeacherFullName(String name){ teacherFullName = name; }
    public void setSubject(int sub){ subjectId = sub; }
    public void setClassNames(String name){ classNames = name; }
}
