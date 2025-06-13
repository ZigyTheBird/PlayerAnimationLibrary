package com.zigythebird.playeranim.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class NetworkUtils {
    public static <K, V> Map<K, V> readMap(ByteBuf buf, Function<ByteBuf, K> keyReader, Function<ByteBuf, V> valueReader) {
        int count = buf.readInt();
        Map<K, V> map = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            K key = keyReader.apply(buf);
            V value = valueReader.apply(buf);
            map.put(key, value);
        }
        return map;
    }

    public static <K, V> void writeMap(ByteBuf buf, Map<K, V> map, BiConsumer<ByteBuf, K> keyWriter, BiConsumer<ByteBuf, V> valueWriter) {
        buf.writeInt(map.size());
        for (var entry : map.entrySet()) {
            keyWriter.accept(buf, entry.getKey());
            valueWriter.accept(buf, entry.getValue());
        }
    }

    public static <T> List<T> readList(ByteBuf buf, Function<ByteBuf, T> reader) {
        int count = buf.readInt();
        List<T> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(reader.apply(buf));
        }
        return list;
    }

    public static <T> void writeList(ByteBuf buf, List<T> list, BiConsumer<ByteBuf, T> writer) {
        buf.writeInt(list.size());
        for (T entry : list) {
            writer.accept(buf, entry);
        }
    }
}
