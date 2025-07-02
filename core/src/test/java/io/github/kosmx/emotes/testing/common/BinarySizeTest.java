package io.github.kosmx.emotes.testing.common;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.network.AnimationBinary;
import com.zigythebird.playeranimcore.network.LegacyAnimationBinary;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

public class BinarySizeTest {
    /**
     * from emotecraft
     * 1MB
     */
    public static final int MAX_PACKET_SIZE = 1048576;

    @Test
    @DisplayName("New binary size")
    public void newBinarySizeTest() throws IOException {
        Animation animation = EmoteDataHashingTest.loadAnimation();

        for (int version = 1; version <= AnimationBinary.CURRENT_VERSION; version++) {
            ByteBuf byteBuf = Unpooled.buffer();
            AnimationBinary.write(byteBuf, version, animation);

            int size = byteBuf.readableBytes();
            System.out.println("[NEW] in version " + version + " size " + size);
            Assertions.assertTrue(size < MAX_PACKET_SIZE, "size exceeds");
        }
    }

    @Test
    @DisplayName("Legacy binary size")
    public void legacyBinarySizeTest() throws IOException {
        Animation animation = EmoteDataHashingTest.loadAnimation();

        for (int version = 1; version <= LegacyAnimationBinary.getCurrentVersion(); version++) {
            ByteBuffer byteBuf = ByteBuffer.allocate(LegacyAnimationBinary.calculateSize(animation, version));
            LegacyAnimationBinary.write(animation, byteBuf, version);
            byteBuf.flip();

            int size = byteBuf.remaining();
            System.out.println("[LEGACY] in version " + version + " size " + size);
            Assertions.assertTrue(size < MAX_PACKET_SIZE, "size exceeds");
        }
    }
}
