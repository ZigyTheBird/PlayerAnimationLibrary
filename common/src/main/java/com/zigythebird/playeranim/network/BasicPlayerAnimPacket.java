package com.zigythebird.playeranim.network;

import com.zigythebird.playeranim.ModInit;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record BasicPlayerAnimPacket(UUID player, ResourceLocation anim) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BasicPlayerAnimPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModInit.MOD_ID, "basic_player_anim_packet"));
    public static final StreamCodec<ByteBuf, BasicPlayerAnimPacket> STREAM_CODEC;

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

    static {
        STREAM_CODEC = StreamCodec.composite(
                UUID_STREAM_CODEC,
                BasicPlayerAnimPacket::player,
                RESOURCELOCATION_STREAM_CODEC,
                BasicPlayerAnimPacket::anim,
                BasicPlayerAnimPacket::new
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
