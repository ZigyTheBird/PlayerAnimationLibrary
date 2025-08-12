package com.zigythebird.playeranim.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.animation.*;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.bones.AdvancedPlayerAnimBone;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class PlayerAnimationController extends AnimationController {
    //Bone pivot point positions used to apply custom pivot point translations.
    private static final Map<String, Vec3f> BONE_POSITIONS = Map.of(
            "right_arm", new Vec3f(5, 22, 0),
            "left_arm", new Vec3f(-5, 22, 0),
            "left_leg", new Vec3f(-2f, 12, 0f),
            "right_leg", new Vec3f(2f, 12, 0f),
            "torso", new Vec3f(0, 24, 0),
            "head", new Vec3f(0, 24, 0),
            "body", new Vec3f(0, 12, 0),
            "cape", new Vec3f(0, 24, 2)
    );

    //Used for applying torso bend to bones like the head.
    protected List<AdvancedPlayerAnimBone> top_bones;

    protected final AbstractClientPlayer player;

    /**
     * Instantiates a new {@code AnimationController}
     *
     * @param player           The object that will be animated by this controller
     * @param animationHandler The {@link AnimationStateHandler} animation state handler responsible for deciding which animations to play
     */
    public PlayerAnimationController(AbstractClientPlayer player, AnimationStateHandler animationHandler) {
        super(animationHandler);
        this.player = player;
    }

    public AbstractClientPlayer getPlayer() {
        return this.player;
    }

    public boolean triggerAnimation(ResourceLocation newAnimation, float startAnimFrom) {
        if (PlayerAnimResources.hasAnimation(newAnimation)) {
            triggerAnimation(PlayerAnimResources.getAnimation(newAnimation), startAnimFrom);
            return true;
        }
        return false;
    }

    public boolean triggerAnimation(ResourceLocation newAnimation) {
        return triggerAnimation(newAnimation, 0);
    }

    public boolean replaceAnimationWithFade(@NotNull AbstractFadeModifier fadeModifier, @Nullable ResourceLocation newAnimation, boolean fadeFromNothing) {
        if (PlayerAnimResources.hasAnimation(newAnimation)) {
            replaceAnimationWithFade(fadeModifier, PlayerAnimResources.getAnimation(newAnimation), fadeFromNothing);
            return true;
        }
        return false;
    }

    public boolean replaceAnimationWithFade(@NotNull AbstractFadeModifier fadeModifier, @Nullable ResourceLocation newAnimation) {
        return replaceAnimationWithFade(fadeModifier, newAnimation, true);
    }

    /**
     * Get the position of a bone in the world in the form of a PoseStack.
     */
    public @Nullable PoseStack getBoneWorldPositionPoseStack(String name, float tickDelta, Vec3 cameraPos) {
        if (!this.activeBones.containsKey(name)) return null;
        PoseStack poseStack = new PoseStack();
        Vec3f pivot = getBonePosition(name);
        Vec3 position = player.getPosition(tickDelta).subtract(cameraPos).add(pivot.x(), pivot.y(), pivot.z());
        poseStack.translate(position.x(), position.y(), position.z());
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - Mth.lerp(tickDelta, player.yBodyRotO, player.yBodyRot)));
        RenderUtil.translateMatrixToBone(poseStack, this.activeBones.get(name));
        return poseStack;
    }

    @Override
    public void registerBones() {
        this.top_bones = new ArrayList<>();

        this.registerPlayerAnimBone("body");
        this.registerTopPlayerAnimBone("right_arm");
        this.registerTopPlayerAnimBone("left_arm");
        this.registerPlayerAnimBone("right_leg");
        this.registerPlayerAnimBone("left_leg");
        this.registerTopPlayerAnimBone("head");
        this.registerPlayerAnimBone("torso");
        this.registerPlayerAnimBone("right_item");
        this.registerPlayerAnimBone("left_item");
        this.registerTopPlayerAnimBone("cape");
        this.registerTopPlayerAnimBone("elytra");
    }

    public void registerTopPlayerAnimBone(String name) {
        this.top_bones.add(this.registerPlayerAnimBone(name));
    }

    @Override
    protected Queue<AnimationProcessor.QueuedAnimation> getQueuedAnimations(RawAnimation rawAnimation) {
        if (player == null) return null;
        return this.player.playerAnimLib$getAnimProcessor().buildAnimationQueue(rawAnimation);
    }

    @Override
    protected void applyCustomPivotPoints() {
        float bend = bones.get("torso").getBend();
        float absBend = Mth.abs(bend);
        if (absBend > 0.001 && (this.currentAnimation != null && this.currentAnimation.animation().data().getNullable(ExtraAnimationData.APPLY_BEND_TO_OTHER_BONES_KEY) == Boolean.TRUE)) {
            float h = -(1 - Mth.cos(absBend));
            float i = 1 - Mth.sin(absBend);
            int sign = Mth.sign(bend);
            for (AdvancedPlayerAnimBone bone : top_bones) {
                float offset = getBonePosition(bone.getName()).y() - 18;
                this.activeBones.put(bone.getName(), bone);
                bone.rotX += bend;
                bone.positionZ += (offset * i - offset) * sign;
                bone.positionY += offset * h;
                bone.rotXEnabled = true;
                bone.positionYEnabled = true;
                bone.positionZEnabled = true;
            }
        }
        super.applyCustomPivotPoints();
    }

    @Override
    public Vec3f getBonePosition(String name) {
        if (BONE_POSITIONS.containsKey(name)) return BONE_POSITIONS.get(name);
        if (pivotBones.containsKey(name)) return pivotBones.get(name).getPivot();
        return Vec3f.ZERO;
    }
}
