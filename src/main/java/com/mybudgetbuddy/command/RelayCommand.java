package com.mybudgetbuddy.command;

import java.util.function.Supplier;

public class RelayCommand implements Command {
    private final Runnable executeAction;
    private final Supplier<Boolean> canExecuteFunction;
    
    public RelayCommand(Runnable executeAction) {
        this(executeAction, () -> true);
    }
    
    public RelayCommand(Runnable executeAction, Supplier<Boolean> canExecuteFunction) {
        this.executeAction = executeAction;
        this.canExecuteFunction = canExecuteFunction;
    }
    
    @Override
    public void execute() {
        if (canExecute()) {
            executeAction.run();
        }
    }
    
    @Override
    public boolean canExecute() {
        return canExecuteFunction.get();
    }
}