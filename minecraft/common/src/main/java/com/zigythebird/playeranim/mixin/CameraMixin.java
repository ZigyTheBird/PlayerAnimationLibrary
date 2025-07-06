//package com.zigythebird.playeranim.mixin;
//
//import com.zigythebird.playeranim.util.CameraUtils;
//import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
//import net.minecraft.client.Camera;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.level.BlockGetter;
//import net.minecraft.world.phys.Vec3;
//import org.joml.Quaternionf;
//import org.joml.Vector3f;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
 //TODO
//@Mixin(Camera.class)
//public abstract class CameraMixin {
//
//    @Shadow private float xRot;
//
//    @Shadow private float yRot;
//
//    @Shadow @Final private Quaternionf rotation;
//
//    @Shadow @Final private static Vector3f FORWARDS;
//
//    @Shadow @Final private static Vector3f UP;
//
//    @Shadow @Final private static Vector3f LEFT;
//
//    @Shadow @Final private Vector3f left;
//
//    @Shadow @Final private Vector3f up;
//
//    @Shadow @Final private Vector3f forwards;
//
//    @Shadow public abstract Vec3 getPosition();
//
//    @Shadow protected abstract void move(float f, float g, float h);
//
//    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", shift = At.Shift.AFTER))
//    private void computeCameraAngles(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
//        PlayerAnimBone bone = CameraUtils.computeCamera(((Camera)(Object)this));
//        if (bone != null) {
//            playerAnimLib$setRotation(bone.getRotX(), bone.getRotY(), bone.getRotZ());
//            this.move(bone.getPosX(), -bone.getPosY(), -bone.getPosZ());
//        }
//    }
//
//    @Unique
//    protected void playerAnimLib$setRotation(float f, float g, float roll) {
//        this.xRot = f / 0.017453292F;
//        this.yRot = g / 0.017453292F;
//        this.rotation.rotationYXZ(3.1415927F - g, -f, -roll);
//        FORWARDS.rotate(this.rotation, this.forwards);
//        UP.rotate(this.rotation, this.up);
//        LEFT.rotate(this.rotation, this.left);
//    }
//}
