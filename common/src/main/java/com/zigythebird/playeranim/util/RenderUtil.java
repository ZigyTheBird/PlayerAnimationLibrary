package com.zigythebird.playeranim.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.bones.PivotBone;
import com.zigythebird.playeranim.bones.PlayerAnimBone;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.world.phys.Vec3;

/**
 * Helper class for various methods and functions useful while rendering
 */
public final class RenderUtil {
	public static void translateMatrixToBone(PoseStack poseStack, PlayerAnimBone bone) {
		poseStack.translate(bone.getPosX() / 16f, bone.getPosY() / 16f, bone.getPosZ() / 16f);
	}

	public static void rotateMatrixAroundBone(PoseStack poseStack, PlayerAnimBone bone) {
		if (bone.getRotZ() != 0)
			poseStack.mulPose(Axis.ZP.rotation(bone.getRotZ()));

		if (bone.getRotY() != 0)
			poseStack.mulPose(Axis.YP.rotation(bone.getRotY()));

		if (bone.getRotX() != 0)
			poseStack.mulPose(Axis.XP.rotation(bone.getRotX()));
	}

	public static void scaleMatrixForBone(PoseStack poseStack, PlayerAnimBone bone) {
		poseStack.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
	}

	public static void translateToPivotPoint(PoseStack poseStack, PivotBone bone) {
		Vec3 pivot = bone.getPivot();
		poseStack.translate(pivot.x()/16, pivot.y()/16, pivot.z()/16);
	}

	public static void translateAwayFromPivotPoint(PoseStack poseStack, PivotBone bone) {
		Vec3 pivot = bone.getPivot();
		poseStack.translate(-pivot.x()/16, -pivot.y()/16, -pivot.z()/16);
	}

	public static void translateAndRotateMatrixForBone(PoseStack poseStack, PivotBone bone) {
		translateToPivotPoint(poseStack, bone);
		rotateMatrixAroundBone(poseStack, bone);
	}

	public static void prepMatrixForBone(PoseStack poseStack, PivotBone bone) {
		translateMatrixToBone(poseStack, bone);
		translateToPivotPoint(poseStack, bone);
		rotateMatrixAroundBone(poseStack, bone);
		scaleMatrixForBone(poseStack, bone);
		translateAwayFromPivotPoint(poseStack, bone);
	}

    public static void translatePartToBone(ModelPart part, PlayerAnimBone bone, PartPose initialPose) {
        part.x = bone.getPosX() + initialPose.x();
        part.y = bone.getPosY() + initialPose.y();
        part.z = bone.getPosZ() + initialPose.z();

        part.xRot = bone.getRotX();
        part.yRot = bone.getRotY();
        part.zRot = bone.getRotZ();

        part.xScale = bone.getScaleX();
        part.yScale = bone.getScaleY();
        part.zScale = bone.getScaleZ();
    }
}
