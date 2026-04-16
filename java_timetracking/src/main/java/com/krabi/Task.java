package com.krabi;

public record Task(long id, String date, String project, int hours, String task, String username) {

    public Task(String date, String project, int hours, String task, String username) {
        this(0, date, project, hours, task, username);
    }

    public Task withId(long newId) {
        return new Task(newId, date, project, hours, task, username);
    }

    public Task withUsername(String newUsername) {
        return new Task(id, date, project, hours, task, newUsername);
    }

    public Task withIdAndUsername(long newId, String newUsername) {
        return new Task(newId, date, project, hours, task, newUsername);
    }
}