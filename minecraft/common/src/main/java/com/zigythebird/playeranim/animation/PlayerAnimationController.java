package com.zigythebird.playeranim.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.animation.*;
import com.zigythebird.playeranimcore.bones.AdvancedPlayerAnimBone;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.math.MathHelper;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class PlayerAnimationController extends AnimationController {
    //Bone pivot point positions used to apply custom pivot point translations.
    public static final Map<String, Vec3f> BONE_POSITIONS = Map.of(
            "right_arm", new Vec3f(5, 22, 0),
            "left_arm", new Vec3f(-5, 22, 0),
            "left_leg", new Vec3f(-2f, 12, 0f),
            "right_leg", new Vec3f(2f, 12, 0f),
            "torso", new Vec3f(0, 24, 0),
            "head", new Vec3f(0, 24, 0),
            "body", new Vec3f(0, 12, 0)
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
        this.top_bones.add(this.registerPlayerAnimBone("right_arm"));
        this.top_bones.add(this.registerPlayerAnimBone("left_arm"));
        this.registerPlayerAnimBone("right_leg");
        this.registerPlayerAnimBone("left_leg");
        this.top_bones.add(this.registerPlayerAnimBone("head"));
        this.registerPlayerAnimBone("torso");
        this.registerPlayerAnimBone("right_item");
        this.registerPlayerAnimBone("left_item");
        this.top_bones.add(this.registerPlayerAnimBone("cape"));
        this.top_bones.add(this.registerPlayerAnimBone("elytra"));
    }

    @Override
    protected Queue<AnimationProcessor.QueuedAnimation> getQueuedAnimations(RawAnimation rawAnimation) {
        if (player == null) return null;
        return this.player.playerAnimLib$getAnimProcessor().buildAnimationQueue(rawAnimation);
    }

    @Override
    protected void applyCustomPivotPoints() {
        float bend = bones.get("torso").getBend();
        if (Math.abs(bend) > 0.001 && (this.currentAnimation != null && this.currentAnimation.animation().data().getNullable(ExtraAnimationData.APPLY_BEND_TO_OTHER_BONES) == Boolean.TRUE)) {
            float s = (float) Math.sin(bend);
            float offset = EasingType.circle(Math.abs(s)) * 6;
            for (AdvancedPlayerAnimBone bone : top_bones) {
                this.activeBones.put(bone.getName(), bone);
                bone.rotX += bend;
                bone.positionY -= offset;
                bone.positionZ += offset;
                if (s > 0)
                    bone.positionZ *= -1;
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
