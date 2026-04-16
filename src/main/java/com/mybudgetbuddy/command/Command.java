package com.mybudgetbuddy.command;

public interface Command {
    void execute();
    boolean canExecute();
}