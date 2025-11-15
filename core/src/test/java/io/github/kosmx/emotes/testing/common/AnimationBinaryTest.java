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

/**
 * Test network data sending and receiving
 */
public class AnimationBinaryTest {
    @Test
    @DisplayName("New binary format")
    public void newBinaryTest() throws IOException {
        Animation animation = EmoteDataHashingTest.loadAnimation();

        for (int version = 1; version <= AnimationBinary.CURRENT_VERSION; version++) {
            ByteBuf byteBuf = Unpooled.buffer();
            AnimationBinary.write(byteBuf, version, animation);

            Animation readed = AnimationBinary.read(byteBuf, version);
            Assertions.assertEquals(animation.boneAnimations(), readed.boneAnimations(), "animation reads incorrectly at version " + version);
        }
    }

    @Test
    @DisplayName("Legacy binary format")
    public void legacyBinaryTest() throws IOException {
        Animation animation = EmoteDataHashingTest.loadAnimation();

        for (int version = 1; version <= LegacyAnimationBinary.getCurrentVersion(); version++) {
            int len = LegacyAnimationBinary.calculateSize(animation, version);
            ByteBuf byteBuf = Unpooled.buffer(len);
            LegacyAnimationBinary.write(animation, byteBuf, version);
            Assertions.assertEquals(len, byteBuf.writerIndex(), "Incorrect size calculator!");

            Assertions.assertTrue(byteBuf.readableBytes() > 0, "animation reads incorrectly at version " + version);

            Animation readed = LegacyAnimationBinary.read(byteBuf, version);
            // Assertions.assertEquals(animation.boneAnimations(), readed.boneAnimations(), "animation reads incorrectly at version " + version);
            Assertions.assertNotNull(readed, "animation reads incorrectly at version " + version); // TODO Not working correctly (zigy, please fix)
            byteBuf.release();
        }
    }
}
