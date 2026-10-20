package org.example;

public class Subject {
    private int subjectId;
    private String subjectName;
    
    public Subject(int id, String name) {
        subjectId = id;
        subjectName = name;
    }
    
    public Subject(String name) {
        subjectName = name;
    }
    
    public int getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    
    public void setSubjectName(String name) { subjectName = name; }
}
