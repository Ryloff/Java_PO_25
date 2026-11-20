package com.example.tracker.model;

public class Category {
    private long id;
    private String name;

    public Category(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(String name) {
        this.name = name;
    }

    public long getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() { return name; }
}