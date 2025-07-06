package com.zigythebird.playeranim.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

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

    public @Nullable PoseStack getBoneWorldPositionPoseStack(String name, float tickDelta) {
        if (!this.activeBones.containsKey(name)) return null;
        PoseStack poseStack = new PoseStack();
        Vec3 position = player.getPosition(tickDelta);
        poseStack.translate(position.x(), position.y(), position.z());
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - Mth.lerp(tickDelta, player.yBodyRotO, player.yBodyRot)));
        RenderUtil.translateMatrixToBone(poseStack, this.activeBones.get(name));
        return poseStack;
    }

    @Override
    public void registerBones() {
        this.registerPlayerAnimBone("body");
        this.registerPlayerAnimBone("right_arm");
        this.registerPlayerAnimBone("left_arm");
        this.registerPlayerAnimBone("right_leg");
        this.registerPlayerAnimBone("left_leg");
        this.registerPlayerAnimBone("head");
        this.registerPlayerAnimBone("torso");
        this.registerPlayerAnimBone("right_item");
        this.registerPlayerAnimBone("left_item");
        this.registerPlayerAnimBone("cape");
        this.registerPlayerAnimBone("elytra");
    }

    @Override
    protected Queue<AnimationProcessor.QueuedAnimation> getQueuedAnimations(RawAnimation rawAnimation) {
        if (player == null) return null;
        return this.player.playerAnimLib$getAnimProcessor().buildAnimationQueue(rawAnimation);
    }

    @Override
    protected void internalSetupAnim(AnimationData state) {
        if (state instanceof PlayerAnimationData playerAnimationData) {
            this.isJustStarting = playerAnimationData.getPlayerAnimManager().isFirstTick();
            this.process(state, playerAnimationData.getPlayer().playerAnimLib$getAnimProcessor().animTime);
        }
        super.internalSetupAnim(state);
    }

    @Override
    public Vec3f getBonePosition(String name) {
        return BONE_POSITIONS.getOrDefault(name, Vec3f.ZERO);
    }
}
