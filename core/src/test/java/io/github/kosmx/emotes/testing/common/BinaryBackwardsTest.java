package io.github.kosmx.emotes.testing.common;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.network.LegacyAnimationBinary;
import dev.kosmx.playerAnim.core.data.AnimationBinary;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"UnstableApiUsage","deprecation"})
public class BinaryBackwardsTest {
    @Test
    @DisplayName("Binary backwards test (to playeranimator)")
    public void this2playeranimator() throws IOException {
        Animation animation = EmoteDataHashingTest.loadAnimation();

        for (int version = 1; version <= LegacyAnimationBinary.getCurrentVersion(); version++) {
            int len = LegacyAnimationBinary.calculateSize(animation, version);
            ByteBuf byteBuf = Unpooled.buffer(len);
            LegacyAnimationBinary.write(animation, byteBuf, version);
            Assertions.assertEquals(len, byteBuf.writerIndex(), "Incorrect size calculator!");

            Assertions.assertTrue(byteBuf.readableBytes() > 0, "animation reads incorrectly at version " + version);

            KeyframeAnimation keyframe = AnimationBinary.read(byteBuf.nioBuffer(), version);
            Assertions.assertNotNull(keyframe, "animation reads incorrectly at version " + version);
            byteBuf.release();
        }
    }

    @Test
    @DisplayName("Binary backwards test (from playeranimator)")
    public void playeranimator2this() throws IOException {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/bye-bye-bye.json")))) {
            List<KeyframeAnimation> keyframes = AnimationJson.GSON.fromJson(reader, AnimationJson.getListedTypeToken());

            for (int version = 1; version <= AnimationBinary.getCurrentVersion(); version++) {
                int len = AnimationBinary.calculateSize(keyframes.getFirst(), version);
                ByteBuffer byteBuf = ByteBuffer.allocate(len);
                AnimationBinary.write(keyframes.getFirst(), byteBuf, version);
                Assertions.assertEquals(len, byteBuf.position(), "Incorrect size calculator!");
                byteBuf.flip();

                ByteBuf netty = Unpooled.wrappedBuffer(byteBuf);
                Animation readed = LegacyAnimationBinary.read(netty, version);
                Assertions.assertNotNull(readed, "animation reads incorrectly at version " + version);
                netty.release();
            }
        }
    }
}
