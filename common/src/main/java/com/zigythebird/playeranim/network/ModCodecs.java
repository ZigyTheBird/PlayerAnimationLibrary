package com.zigythebird.playeranim.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class ModCodecs {
    public static final StreamCodec<ByteBuf, UUID> UUID_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public UUID decode(ByteBuf object) {
            return UUID.fromString(Utf8String.read(object, 32767));
        }

        @Override
        public void encode(ByteBuf object, UUID object2) {
            Utf8String.write(object, object2.toString(), 32767);
        }
    };

    public static final StreamCodec<ByteBuf, ResourceLocation> RESOURCELOCATION_STREAM_CODEC = new StreamCodec<>() {
        public ResourceLocation decode(ByteBuf byteBuf) {
            return ResourceLocation.parse(Utf8String.read(byteBuf, 32767));
        }

        public void encode(ByteBuf byteBuf, ResourceLocation buf) {
            Utf8String.write(byteBuf, buf.toString(), 32767);
        }
    };
}
