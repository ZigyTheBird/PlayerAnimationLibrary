package com.zigythebird.playeranim.network;

import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.misc.ModCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record BasicPlayerAnimPacket(UUID player, ResourceLocation anim) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BasicPlayerAnimPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModInit.MOD_ID, "basic_player_anim_packet"));
    public static final StreamCodec<ByteBuf, BasicPlayerAnimPacket> STREAM_CODEC;

    static {
        STREAM_CODEC = StreamCodec.composite(
                ModCodecs.UUID_STREAM_CODEC,
                BasicPlayerAnimPacket::player,
                ModCodecs.RESOURCELOCATION_STREAM_CODEC,
                BasicPlayerAnimPacket::anim,
                BasicPlayerAnimPacket::new
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
