package com.zigythebird.playeranimcore.loading;

import com.google.gson.*;
import com.zigythebird.playeranimcore.PlayerAnimLib;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.molang.MolangLoader;
import com.zigythebird.playeranimcore.util.JsonUtil;
import team.unnamed.mocha.parser.ast.Expression;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KeyFrameLoader implements JsonDeserializer<Animation.Keyframes> {
	@Override
	public Animation.Keyframes deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		SoundKeyframeData[] sounds = buildSoundFrameData(obj);
		ParticleKeyframeData[] particles = buildParticleFrameData(obj);
		CustomInstructionKeyframeData[] customInstructions = buildCustomFrameData(obj);

		return new Animation.Keyframes(sounds, particles, customInstructions);
	}

	private static SoundKeyframeData[] buildSoundFrameData(JsonObject rootObj) {
		JsonObject soundsObj = JsonUtil.getAsJsonObject(rootObj, "sound_effects", new JsonObject());
		SoundKeyframeData[] sounds = new SoundKeyframeData[soundsObj.size()];
		int index = 0;

		for (Map.Entry<String, JsonElement> entry : soundsObj.entrySet()) {
			sounds[index] = new SoundKeyframeData(Float.parseFloat(entry.getKey()) * 20f, JsonUtil.getAsString(entry.getValue().getAsJsonObject(), "effect"));
			index++;
		}

		return sounds;
	}

	private static ParticleKeyframeData[] buildParticleFrameData(JsonObject rootObj) {
		JsonObject particlesObj = JsonUtil.getAsJsonObject(rootObj, "particle_effects", new JsonObject());
		ParticleKeyframeData[] particles = new ParticleKeyframeData[particlesObj.size()];
		int index = 0;

		for (Map.Entry<String, JsonElement> entry : particlesObj.entrySet()) {
			JsonObject obj = entry.getValue().getAsJsonObject();
			String effect = JsonUtil.getAsString(obj, "effect", "");
			String locator = JsonUtil.getAsString(obj, "locator", "");
			List<Expression> script = MolangLoader.parseJson(false, obj.get("pre_effect_script"), Collections.emptyList());

			particles[index] = new ParticleKeyframeData(Float.parseFloat(entry.getKey()) * 20f, effect, locator, script);
			index++;
		}

		return particles;
	}

	private static CustomInstructionKeyframeData[] buildCustomFrameData(JsonObject rootObj) {
		JsonObject customInstructionsObj = JsonUtil.getAsJsonObject(rootObj, "timeline", new JsonObject());
		CustomInstructionKeyframeData[] customInstructions = new CustomInstructionKeyframeData[customInstructionsObj.size()];
		int index = 0;

		for (Map.Entry<String, JsonElement> entry : customInstructionsObj.entrySet()) {
			String instructions = "";

			if (entry.getValue() instanceof JsonArray array) {
				instructions = PlayerAnimLib.GSON.fromJson(array, List.class).toString();
			}
			else if (entry.getValue() instanceof JsonPrimitive primitive) {
				instructions = primitive.getAsString();
			}

			customInstructions[index] = new CustomInstructionKeyframeData(Float.parseFloat(entry.getKey()) * 20f, instructions);
			index++;
		}

		return customInstructions;
	}
}
