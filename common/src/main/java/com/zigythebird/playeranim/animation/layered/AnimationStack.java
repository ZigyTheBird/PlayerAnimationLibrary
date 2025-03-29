package com.zigythebird.playeranim.animation.layered;

import com.zigythebird.playeranim.animation.AnimationData;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranim.bones.PlayerAnimBone;
import com.zigythebird.playeranim.math.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Player animation stack, can contain multiple active or passive layers, will always be evaluated from the lowest index.
 * Highest index = it can override everything else
 */
public class AnimationStack implements IAnimation {
    protected final ArrayList<Pair<Integer, IAnimation>> layers = new ArrayList<>();

    @Override
    public boolean isActive() {
        for (Pair<Integer, IAnimation> layer : layers) {
            if (layer.getRight().isActive()) return true;
        }
        return false;
    }

    @Override
    public void tick(AnimationData state) {
        for (Pair<Integer, IAnimation> layer : layers) {
            if (layer.getRight().isActive()) {
                layer.getRight().tick(state);
            }
        }
    }

    @Override
    public void get3DTransform(@NotNull PlayerAnimBone bone) {
        for (Pair<Integer, IAnimation> layer : layers) {
            if (layer.getRight().isActive() && (!FirstPersonMode.isFirstPersonPass() || layer.getRight().getFirstPersonMode().isEnabled())) {
                layer.getRight().get3DTransform(bone);
            }
        }
    }

    @Override
    public void setupAnim(AnimationData state) {
        for (Pair<Integer, IAnimation> layer : layers) {
            layer.getRight().setupAnim(state);
        }
    }

    /**
     * Add an animation layer.
     * If there are multiple layers with the same priority, the one added first will have more priority
     * @param priority priority
     * @param layer    animation layer
     */
    public void addAnimLayer(int priority, IAnimation layer) {
        int search = 0;
        //Insert the layer into the correct slot
        while (layers.size() > search && layers.get(search).getLeft() < priority) {
            search++;
        }
        layers.add(search, new Pair<>(priority, layer));
    }

    /**
     * Remove an animation layer
     * @param layer needle
     * @return true if any elements were removed.
     */
    public boolean removeLayer(IAnimation layer) {
        return layers.removeIf(integerIAnimationPair -> integerIAnimationPair.getRight() == layer);
    }

    /**
     * Remove EVERY layer with priority
     * @param layerLevel search and destroy
     * @return true if any elements were removed.
     */
    public boolean removeLayer(int layerLevel) {
        return layers.removeIf(integerIAnimationPair -> integerIAnimationPair.getLeft() == layerLevel);
    }

    @Override
    public @NotNull FirstPersonMode getFirstPersonMode() {
        for (int i = layers.size(); i > 0;) {
            Pair<Integer, IAnimation> layer = layers.get(--i);
            if (layer.getRight().isActive()) { // layer.right.requestFirstPersonMode(tickDelta).takeIf{ it != NONE }?.let{ return@requestFirstPersonMode it }
                FirstPersonMode mode = layer.getRight().getFirstPersonMode();
                if (mode != FirstPersonMode.NONE) return mode;
            }
        }
        return FirstPersonMode.NONE;
    }

    @Override
    public @NotNull FirstPersonConfiguration getFirstPersonConfiguration() {
        for (int i = layers.size(); i > 0;) {
            Pair<Integer, IAnimation> layer = layers.get(--i);
            if (layer.getRight().isActive()) { // layer.right.requestFirstPersonMode(tickDelta).takeIf{ it != NONE }?.let{ return@requestFirstPersonMode it }
                FirstPersonMode mode = layer.getRight().getFirstPersonMode();
                if (mode != FirstPersonMode.NONE) return layer.getRight().getFirstPersonConfiguration();
            }
        }
        return IAnimation.super.getFirstPersonConfiguration();
    }

    public int getPriority() {
        int priority = 0;
        for (int i=layers.size()-1; i>=0; i--) {
            Pair<Integer, IAnimation> layer = layers.get(i);
            if (layer.getRight().isActive()) {
                priority = layer.getLeft();
                break;
            }
        }
        return priority;
    }
}
