/*
 * MIT License
 *
 * Copyright (c) 2024 GeckoLib
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

package com.zigythebird.playeranimcore.data;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.Objects;

/**
 * Ticket object to define a typed data object
 */
public class DataTicket<D> {
    static final Map<Pair<Class<?>, String>, DataTicket<?>> IDENTITY_CACHE = new Object2ObjectOpenHashMap<>();

    private final String id;
    private final Class<? extends D> objectType;

    /**
     * @see #create(String, Class)
     */
    DataTicket(String id, Class<? extends D> objectType) {
        this.id = id;
        this.objectType = objectType;
    }

    /**
     * Create a new DataTicket for a given ID and object type
     * Please include a namespace in your ID like namespace mod_id:name to avoid conflicts with other mods.
     * <p>
     * This DataTicket should then be stored statically somewhere and re-used.
     */
    public static <D> DataTicket<D> create(String id, Class<? extends D> objectType) {
        return (DataTicket<D>)IDENTITY_CACHE.computeIfAbsent(Pair.of(objectType, id), pair -> new DataTicket<>(id, objectType));
    }

    public String id() {
        return this.id;
    }

    public Class<? extends D> objectType() {
        return this.objectType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.objectType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof DataTicket<?> other))
            return false;

        return this.objectType == other.objectType && this.id.equals(other.id);
    }

    @Override
    public String toString() {
        return "DataTicket{" + this.id + ": " + this.objectType.getName() + "}";
    }
}