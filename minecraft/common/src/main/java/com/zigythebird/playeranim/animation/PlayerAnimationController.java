package com.zigythebird.playeranim.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
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

public class PlayerAnimationController extends AnimationController {
    // Bone pivot point positions used to apply custom pivot point translations.
    public static final Map<String, Vec3f> BONE_POSITIONS = Map.of(
            "right_arm", new Vec3f(5, 22, 0),
            "left_arm", new Vec3f(-5, 22, 0),
            "left_leg", new Vec3f(-2f, 12, 0f),
            "right_leg", new Vec3f(2f, 12, 0f),
            "torso", new Vec3f(0, 24, 0),
            "head", new Vec3f(0, 24, 0),
            "body", new Vec3f(0, 12, 0),
            "cape", new Vec3f(0, 24, 2),
            "elytra", new Vec3f(0, 24, 2)
    );

    // Used for applying torso bend to bones like the head.
    protected List<String> top_bones;

    protected final AbstractClientPlayer player;
    private float torsoBend;
    private float torsoBendYPosMultiplier;
    private float torsoBendZPosMultiplier;
    private int torsoBendSign;

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
        this.registerPlayerAnimBone("elytra");
    }

    public void registerTopPlayerAnimBone(String name) {
        this.top_bones.add(name);
        this.registerPlayerAnimBone(name);
    }

    @Override
    public void process(AnimationData state) {
        super.process(state);
        this.torsoBend = bones.get("torso").getBend();
        float absBend = Mth.abs(this.torsoBend);
        if (absBend > 0.001 && (this.currentAnimation != null && this.currentAnimation.animation().data().getNullable(ExtraAnimationData.APPLY_BEND_TO_OTHER_BONES_KEY) == Boolean.TRUE)) {
            this.torsoBendSign = Mth.sign(this.torsoBend);
            this.torsoBendYPosMultiplier = -(1 - Mth.cos(absBend));
            this.torsoBendZPosMultiplier = 1 - Mth.sin(absBend);
        } else this.torsoBendSign = 0;
    }

    @Override
    public PlayerAnimBone get3DTransformRaw(@NotNull PlayerAnimBone bone) {
        bone = super.get3DTransformRaw(bone);
        String name = bone.getName();
        if (this.torsoBendSign != 0 && this.top_bones.contains(name)) {
            float offset = getBonePosition(name).y() - 18;
            bone.rotX += this.torsoBend;
            bone.positionZ += (offset * this.torsoBendZPosMultiplier - offset) * this.torsoBendSign;
            bone.positionY += offset * this.torsoBendYPosMultiplier;
        }
        return bone;
    }

    @Override
    public Vec3f getBonePosition(String name) {
        if (BONE_POSITIONS.containsKey(name)) return BONE_POSITIONS.get(name);
        if (pivotBones.containsKey(name)) return pivotBones.get(name).getPivot();
        return Vec3f.ZERO;
    }
}
