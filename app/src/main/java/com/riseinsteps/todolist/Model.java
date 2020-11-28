package com.riseinsteps.todolist;

public class Model {
    private String task, descripton, id, date;


    public Model() {
    }

    public Model(String task, String descripton, String id, String date) {
        this.task = task;
        this.descripton = descripton;
        this.id = id;
        this.date = date;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDescripton() {
        return descripton;
    }

    public void setDescripton(String descripton) {
        this.descripton = descripton;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
