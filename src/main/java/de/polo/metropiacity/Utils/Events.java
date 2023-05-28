package de.polo.metropiacity.Utils;

import java.util.ArrayList;
import java.util.List;

public class Events {
    private List<Listener> listeners = new ArrayList<Listener>();

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void fireEvent() {
        for (Listener listener : listeners) {
            listener.on();
        }
    }
}