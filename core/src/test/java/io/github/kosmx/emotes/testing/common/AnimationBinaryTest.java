package io.github.kosmx.emotes.testing.common;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import com.zigythebird.playeranimcore.network.AnimationBinary;
import com.zigythebird.playeranimcore.network.LegacyAnimationBinary;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Test network data sending and receiving
 */
public class AnimationBinaryTest {
    @Test
    @DisplayName("New binary format")
    public void newBinaryTest() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/waving.json")) {
            Animation animation = UniversalAnimLoader.loadPlayerAnim(is).values().iterator().next();

            for (int version = 1; version <= AnimationBinary.CURRENT_VERSION; version++) {
                ByteBuf byteBuf = Unpooled.buffer();
                AnimationBinary.write(byteBuf, version, animation);

                Animation readed = AnimationBinary.read(byteBuf, version);
                Assertions.assertEquals(animation, readed, "animation reads incorrectly at version " + version);
            }
        }
    }

    @Test
    @DisplayName("Legacy binary format")
    public void legacyBinaryTest() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/waving.json")) {
            Animation animation = UniversalAnimLoader.loadPlayerAnim(is).values().iterator().next();

            for (int version = 1; version <= LegacyAnimationBinary.getCurrentVersion(); version++) {
                ByteBuffer byteBuf = ByteBuffer.allocate(LegacyAnimationBinary.calculateSize(animation, version));
                LegacyAnimationBinary.write(animation, byteBuf, version);
                byteBuf.flip();

                Assertions.assertTrue(byteBuf.hasRemaining(), "animation reads incorrectly at version " + version);

                Animation readed = LegacyAnimationBinary.read(byteBuf, version);
                Assertions.assertNotNull(readed, "animation reads incorrectly at version " + version);
            }
        }
    }
}
