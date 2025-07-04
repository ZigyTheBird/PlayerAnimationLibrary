package io.github.kosmx.emotes.testing.common;

import com.zigythebird.mcanimcore.animation.Animation;
import com.zigythebird.mcanimcore.animation.EasingType;
import com.zigythebird.mcanimcore.animation.keyframe.Keyframe;
import com.zigythebird.mcanimcore.loading.UniversalAnimLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class EmoteDataHashingTest {
    @RepeatedTest(10)
    @DisplayName("emoteData hashing test")
    public void hashAndEqualsTest() throws IOException {
        Animation emote1 = EmoteDataHashingTest.loadAnimation();
        Animation emote2 = EmoteDataHashingTest.loadAnimation();

        Assertions.assertEquals(emote1, emote2, "EmoteData should equal with the a perfect copy"); //Object are not the same, but should be equal
        Assertions.assertEquals(emote1.hashCode(), emote2.hashCode(), "The hash should be same");

        emote1.boneAnimations().get("body").positionKeyFrames().xKeyframes().add(new Keyframe(1, Collections.emptyList(), Collections.emptyList(), EasingType.CONSTANT));

        Assertions.assertNotEquals(emote1, emote2, "After any change these should be NOT equals");
        Assertions.assertNotEquals(emote1.hashCode(), emote2.hashCode(), "After any change these should have different hash");
    }

    public static Animation loadAnimation() throws IOException {
        try (InputStream is = EmoteDataHashingTest.class.getResourceAsStream("/bye-bye-bye.json")) {
            return UniversalAnimLoader.loadAnimations(is).values().iterator().next();
        }
    }
}
