package com.zigythebird.playeranim.loading;

import com.google.gson.*;
import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.animation.Animation;
import com.zigythebird.playeranim.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranim.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranim.animation.keyframe.event.data.SoundKeyframeData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.GsonHelper;

import java.util.Map;

public class KeyFrameLoader {
	public static Animation.Keyframes deserialize(JsonElement json) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		SoundKeyframeData[] sounds = buildSoundFrameData(obj);
		ParticleKeyframeData[] particles = buildParticleFrameData(obj);
		CustomInstructionKeyframeData[] customInstructions = buildCustomFrameData(obj);

		return new Animation.Keyframes(sounds, particles, customInstructions);
	}

	private static SoundKeyframeData[] buildSoundFrameData(JsonObject rootObj) {
		JsonObject soundsObj = GsonHelper.getAsJsonObject(rootObj, "sound_effects", new JsonObject());
		SoundKeyframeData[] sounds = new SoundKeyframeData[soundsObj.size()];
		int index = 0;

		for (Map.Entry<String, JsonElement> entry : soundsObj.entrySet()) {
			sounds[index] = new SoundKeyframeData(Double.parseDouble(entry.getKey()) * 20d, GsonHelper.getAsString(entry.getValue().getAsJsonObject(), "effect"));
			index++;
		}

		return sounds;
	}

	private static ParticleKeyframeData[] buildParticleFrameData(JsonObject rootObj) {
		JsonObject particlesObj = GsonHelper.getAsJsonObject(rootObj, "particle_effects", new JsonObject());
		ParticleKeyframeData[] particles = new ParticleKeyframeData[particlesObj.size()];
		int index = 0;

		for (Map.Entry<String, JsonElement> entry : particlesObj.entrySet()) {
			JsonObject obj = entry.getValue().getAsJsonObject();
			String effect = GsonHelper.getAsString(obj, "effect", "");
			String locator = GsonHelper.getAsString(obj, "locator", "");
			String script = GsonHelper.getAsString(obj, "pre_effect_script", "");

			particles[index] = new ParticleKeyframeData(Double.parseDouble(entry.getKey()) * 20d, effect, locator, script);
			index++;
		}

		return particles;
	}

	private static CustomInstructionKeyframeData[] buildCustomFrameData(JsonObject rootObj) {
		JsonObject customInstructionsObj = GsonHelper.getAsJsonObject(rootObj, "timeline", new JsonObject());
		CustomInstructionKeyframeData[] customInstructions = new CustomInstructionKeyframeData[customInstructionsObj.size()];
		int index = 0;

		for (Map.Entry<String, JsonElement> entry : customInstructionsObj.entrySet()) {
			String instructions = "";

			if (entry.getValue() instanceof JsonArray array) {
				instructions = ModInit.GSON.fromJson(array, ObjectArrayList.class).toString();
			}
			else if (entry.getValue() instanceof JsonPrimitive primitive) {
				instructions = primitive.getAsString();
			}

			customInstructions[index] = new CustomInstructionKeyframeData(Double.parseDouble(entry.getKey()) * 20d, instructions);
			index++;
		}

		return customInstructions;
	}
}
