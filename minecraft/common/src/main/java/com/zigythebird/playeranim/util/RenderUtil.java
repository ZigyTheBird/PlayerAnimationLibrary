package com.zigythebird.playeranim.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

/**
 * Helper class for various methods and functions useful while rendering
 */
public final class RenderUtil {
	public static void rotateMatrixAroundBone(PoseStack poseStack, PlayerAnimBone bone) {
		if (bone.getRotZ() != 0)
			poseStack.mulPose(Axis.ZP.rotation(bone.getRotZ()));

		if (bone.getRotY() != 0)
			poseStack.mulPose(Axis.YP.rotation(bone.getRotY()));

		if (bone.getRotX() != 0)
			poseStack.mulPose(Axis.XP.rotation(bone.getRotX()));
	}

	public static void translatePartToBone(ModelPart part, PlayerAnimBone bone) {
		part.x = bone.getPosX();
		part.y = -bone.getPosY();
		part.z = bone.getPosZ();

		part.xRot = bone.getRotX();
		part.yRot = bone.getRotY();
		part.zRot = bone.getRotZ();

		part.xScale = bone.getScaleX();
		part.yScale = bone.getScaleY();
		part.zScale = bone.getScaleZ();
	}

	//Initial pose only applied to yRot and position because that's all that's needed for vanilla parts.
    public static void translatePartToBone(ModelPart part, PlayerAnimBone bone, PartPose initialPose) {
        part.x = bone.getPosX() + initialPose.x();
        part.y = -bone.getPosY() + initialPose.y();
        part.z = bone.getPosZ() + initialPose.z();

        part.xRot = bone.getRotX();
        part.yRot = bone.getRotY() + initialPose.yRot();
        part.zRot = bone.getRotZ();

        part.xScale = bone.getScaleX();
        part.yScale = bone.getScaleY();
        part.zScale = bone.getScaleZ();
    }

	public static void translateMatrixToBone(PoseStack poseStack, PlayerAnimBone bone) {
		poseStack.translate(bone.getPosX() / 16, bone.getPosY() / 16, bone.getPosZ() / 16);
		RenderUtil.rotateMatrixAroundBone(poseStack, bone);
		poseStack.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
	}

	public static void copyVanillaPart(ModelPart part, PlayerAnimBone bone) {
		PartPose initialPose = part.getInitialPose();

		bone.setPosX(part.x - initialPose.x());
		bone.setPosY(part.y - initialPose.y());
		bone.setPosZ(part.z - initialPose.z());

		bone.setRotX(part.xRot);
		bone.setRotY(part.yRot);
		bone.setRotZ(part.zRot);

		bone.setScaleX(part.xScale);
		bone.setScaleY(part.yScale);
		bone.setScaleZ(part.zScale);

		bone.setBend(0);
	}
}
