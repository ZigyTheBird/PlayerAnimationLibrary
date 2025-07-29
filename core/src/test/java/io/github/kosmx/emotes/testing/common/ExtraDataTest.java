package io.github.kosmx.emotes.testing.common;

import com.zigythebird.playeranimcore.animation.Animation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExtraDataTest {
    @Test
    @DisplayName("Extra data test")
    public void extraDataTest() throws IOException {
        Animation animation = EmoteDataHashingTest.loadAnimation();

        Assertions.assertInstanceOf(String.class, animation.data().getNullable("author"));
        Assertions.assertInstanceOf(String.class, animation.data().getNullable("name"));
        Assertions.assertInstanceOf(String.class, animation.data().getNullable("description"));

        Assertions.assertInstanceOf(List.class, animation.data().get("bages").orElseGet(ArrayList::new));
        List<?> badges = animation.data().getList("bages");
        for (Object badge : badges) {
            Assertions.assertInstanceOf(String.class, badge);
        }
    }
}
