package io.github.kosmx.emotes.testing.common;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.network.LegacyAnimationBinary;
import dev.kosmx.playerAnim.core.data.AnimationBinary;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationJson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.List;

public class BinaryBackwardsTest {
    @Test
    @DisplayName("Binary backwards test (to playeranimator)")
    public void this2playeranimator() throws IOException {
        Animation animation = EmoteDataHashingTest.loadAnimation();

        for (int version = 1; version <= LegacyAnimationBinary.getCurrentVersion(); version++) {
            int len = LegacyAnimationBinary.calculateSize(animation, version);
            ByteBuffer byteBuf = ByteBuffer.allocate(len);
            LegacyAnimationBinary.write(animation, byteBuf, version);
            Assertions.assertEquals(len, byteBuf.position(), "Incorrect size calculator!");
            byteBuf.flip();

            Assertions.assertTrue(byteBuf.hasRemaining(), "animation reads incorrectly at version " + version);

            KeyframeAnimation keyframe = AnimationBinary.read(byteBuf, version);
            Assertions.assertNotNull(keyframe, "animation reads incorrectly at version " + version);
        }
    }

    @Test
    @DisplayName("Binary backwards test (from playeranimator)")
    public void playeranimator2this() throws IOException {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/bye-bye-bye.json"))) {
            List<KeyframeAnimation> keyframes = AnimationJson.GSON.fromJson(reader, AnimationJson.getListedTypeToken());

            for (int version = 1; version <= AnimationBinary.getCurrentVersion(); version++) {
                int len = AnimationBinary.calculateSize(keyframes.getFirst(), version);
                ByteBuffer byteBuf = ByteBuffer.allocate(len);
                AnimationBinary.write(keyframes.getFirst(), byteBuf, version);
                Assertions.assertEquals(len, byteBuf.position(), "Incorrect size calculator!");
                byteBuf.flip();

                Animation readed = LegacyAnimationBinary.read(byteBuf, version);
                Assertions.assertNotNull(readed, "animation reads incorrectly at version " + version);
            }
        }
    }
}
