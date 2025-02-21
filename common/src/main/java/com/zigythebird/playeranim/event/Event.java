package com.zigythebird.playeranim.event;

import java.util.ArrayList;


/**
 * To register a listener, use {@link Event#register(Object)};
 * @param <T>
 */
public class Event<T> {
    final ArrayList<T> listeners = new ArrayList<>();
    final Invoker<T> _invoker;

    public Event(Class<T> clazz, Invoker<T> invoker){
        this._invoker = invoker;
    }

    /**
     * Do EVENT.invoker()./invoke(Objects...)/;
     * Only when firing the event.
     * @return the invoker
     * This shall <strong>only</strong> be used by the API
     */
    public final T invoker(){
        return _invoker.invoker(listeners);
    }

    /**
     * Register a new event listener;
     * See the actual event documentation for return type
     * @param listener the listener.
     */
    public void register(T listener){
        if(listener == null) throw new NullPointerException("listener can not be null");
        listeners.add(listener);
    }

    /**
     * unregister the listener
     * @param listener listener to unregister, or a similar listener if it has <code>equals()</code> function.
     */
    public void unregister(T listener){
        listeners.remove(listener);
    }

    @FunctionalInterface
    public interface Invoker<T>{
        T invoker(Iterable<T> listeners);
    }

}
