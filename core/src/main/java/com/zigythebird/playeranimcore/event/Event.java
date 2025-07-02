/*
 * MIT License
 *
 * Copyright (c) 2022 KosmX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.zigythebird.playeranimcore.event;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * To register a listener, use {@link Event#register(Object)};
 * @param <T>
 */
public class Event<T> {
    private final List<T> listeners = new ObjectArrayList<>();
    private final Invoker<T> invoker;

    public Event(Invoker<T> invoker) {
        this.invoker = invoker;
    }

    /**
     * Do EVENT.invoker()./invoke(Objects...)/;
     * Only when firing the event.
     * @return the invoker
     * This shall <strong>only</strong> be used by the API
     */
    public final T invoker() {
        return invoker.invoker(listeners);
    }

    /**
     * Register a new event listener;
     * See the actual event documentation for return type
     * @param listener the listener.
     */
    public void register(T listener) {
        if (listener == null) throw new NullPointerException("listener can not be null");
        listeners.add(listener);
    }

    /**
     * unregister the listener
     * @param listener listener to unregister, or a similar listener if it has <code>equals()</code> function.
     */
    public void unregister(T listener) {
        listeners.remove(listener);
    }

    @FunctionalInterface
    public interface Invoker<T>{
        T invoker(Iterable<T> listeners);
    }
}
