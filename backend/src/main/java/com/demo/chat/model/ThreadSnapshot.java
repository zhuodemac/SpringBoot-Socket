package com.demo.chat.model;

public class ThreadSnapshot {

    private String name;
    private String state;
    private boolean alive;
    private boolean daemon;

    public ThreadSnapshot() {
    }

    public ThreadSnapshot(String name, String state, boolean alive, boolean daemon) {
        this.name = name;
        this.state = state;
        this.alive = alive;
        this.daemon = daemon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }
}
