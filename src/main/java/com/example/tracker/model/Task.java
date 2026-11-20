package com.example.tracker.model;

public class Task {
    private long id;
    private String name;
    private String description;
    private Category category;
    private int estimatedMinutes;

    private Task(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.category = builder.category;
        this.estimatedMinutes = builder.estimatedMinutes;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public int getEstimatedMinutes() { return estimatedMinutes; }

    public static class Builder {
        private final String name;
        private String description = "";
        private Category category;
        private int estimatedMinutes = 0;

        public Builder(String name) {
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Builder estimatedMinutes(int estimatedMinutes) {
            this.estimatedMinutes = estimatedMinutes;
            return this;
        }

        public Task build() {
            return new Task(this);
        }
    }

    @Override
    public String toString() { return name; }
}
