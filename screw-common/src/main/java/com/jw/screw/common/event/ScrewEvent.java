package com.jw.screw.common.event;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * screw的事件
 * @author jiangw
 * @date 2021/4/16 14:59
 * @since 1.0
 */
public abstract class ScrewEvent implements Observable {

    private final CopyOnWriteArraySet<Observer> observers;

    public ScrewEvent() {
        observers = new CopyOnWriteArraySet<>();
    }

    public void attach(Observer observer) {
        if (observer == null) {
            return;
        }
        observers.add(observer);
    }

    public boolean detach(Observer observer) {
        if (observer == null) {
            return false;
        }
        return observers.remove(observer);
    }

    protected void notifyObservers(Object... args) {
        for (Observer observer : observers) {
            observer.update(this, args);
        }
    }

    @Override
    public String toString() {
        return "ScrewEvent{" +
                "observers=" + observers +
                '}';
    }
}
