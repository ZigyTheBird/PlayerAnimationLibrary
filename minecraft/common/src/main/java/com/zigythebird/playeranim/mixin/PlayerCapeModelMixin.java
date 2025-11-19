package com.zigythebird.playeranim.mixin;

import com.zigythebird.playeranim.accessors.IAvatarAnimationState;
import com.zigythebird.playeranim.accessors.ICapeLayer;
import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.math.MathHelper;
import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//Set the priority high cause why not!
@Mixin(value = PlayerCapeModel.class, priority = 2001)
public class PlayerCapeModelMixin implements ICapeLayer {
    @Shadow
    @Final
    private ModelPart cape;

    //Make sure nothing from the previous frame remains.
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("HEAD"))
    private void resetPose(AvatarRenderState renderState, CallbackInfo ci) {
        this.cape.resetPose();
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void setupAnim(AvatarRenderState avatarRenderState, CallbackInfo ci) {
        AvatarAnimManager emote = ((IAvatarAnimationState)avatarRenderState).playerAnimLib$getAnimManager();
        if (emote != null && emote.isActive()) {
            PlayerAnimBone bone = RenderUtil.copyVanillaPart(this.cape, new PlayerAnimBone("cape"));

            bone.rotX -= MathHelper.PI;
            bone.rotZ -= MathHelper.PI;
            bone.rotX *= -1;
            bone.rotY *= -1;
            bone = emote.get3DTransform(bone);
            bone.rotX *= -1;
            bone.rotY *= -1;
            bone.rotX += MathHelper.PI;
            bone.rotZ += MathHelper.PI;

            RenderUtil.translatePartToCape(this.cape, bone, this.cape.getInitialPose());

            this.applyBend(emote, bone.getBend());
        }
        else this.resetBend();
    }
}
