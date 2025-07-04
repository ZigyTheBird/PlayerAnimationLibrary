package com.zigythebird.mcanimcore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zigythebird.mcanimcore.animation.Animation;
import com.zigythebird.mcanimcore.loading.AnimationLoader;
import com.zigythebird.mcanimcore.loading.KeyFrameLoader;
import com.zigythebird.mcanimcore.loading.UniversalAnimLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;

public class MCAnimLib {
    public static Logger LOGGER = LoggerFactory.getLogger("animation_core");

    public static final Type ANIMATIONS_MAP_TYPE = new TypeToken<Map<String, Animation>>() {}.getType();
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Animation.Keyframes.class, new KeyFrameLoader())
            .registerTypeAdapter(Animation.class, new AnimationLoader())
            .registerTypeAdapter(ANIMATIONS_MAP_TYPE, new UniversalAnimLoader())
            .setPrettyPrinting()
            .create();
}
