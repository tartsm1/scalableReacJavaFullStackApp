package com.krabi;

public class Task {
    private long id;
    private String date;
    private String project;
    private int hours;
    private String task;
    private String username;

    public Task() {}
    public Task(String date, String project, int hours, String task, String username) {
   
        this.date = date;
        this.project = project;
        this.hours = hours;
        this.task = task;
        this.username = username;
    }

    public Task(long id, String date, String project, int hours, String task, String username) {
        this.id = id;
        this.date = date;
        this.project = project;
        this.hours = hours;
        this.task = task;
        this.username = username;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }
    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
} 