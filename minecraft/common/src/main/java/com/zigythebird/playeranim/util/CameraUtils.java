//package com.zigythebird.playeranim.util;
//
//import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
//import com.zigythebird.playeranim.animation.PlayerAnimManager;
//import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
//import net.minecraft.client.Camera;
//import net.minecraft.client.Minecraft;
//import org.jetbrains.annotations.Nullable;
//
//public class CameraUtils {
//    public static @Nullable PlayerAnimBone computeCamera(Camera camera) {
//        PlayerAnimManager manager = ((IAnimatedPlayer)Minecraft.getInstance().player).playerAnimLib$getAnimManager();
//        if (manager != null && manager.isActive()) {
//            PlayerAnimBone bone = ((IAnimatedPlayer)Minecraft.getInstance().player).playerAnimLib$getAnimProcessor().getBone("head");
//            bone.setToInitialPose();
//            bone.setRotX(camera.getXRot() * 0.017453292F);
//            bone.setRotY(camera.getYRot() * 0.017453292F);
//            // manager.get3DCameraTransform(camera, bone); TODO
//            bone.divPos(16);
//            return bone;
//        }
//        return null;
//    }
//}
