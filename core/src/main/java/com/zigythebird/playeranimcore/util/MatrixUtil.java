package com.zigythebird.playeranimcore.util;

import com.zigythebird.playeranimcore.bones.PivotBone;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.math.ModMatrix4f;
import com.zigythebird.playeranimcore.math.Vec3f;

import java.util.function.Function;

/**
 * Does NOT divide pos 16.
 * Used for applying custom pivot bones to player bones
 */
public class MatrixUtil {
    public static void rotateMatrixAroundBone(ModMatrix4f matrix, PlayerAnimBone bone) {
        if (bone.getRotZ() != 0 || bone.getRotY() != 0 || bone.getRotX() != 0)
            matrix.rotateZ(bone.getRotZ()).rotateY(bone.getRotY()).rotateX(bone.getRotX());
    }

    public static void rotateMatrixAroundBoneXYZ(ModMatrix4f matrix, PlayerAnimBone bone) {
        if (bone.getRotZ() != 0 || bone.getRotY() != 0 || bone.getRotX() != 0)
            matrix.rotateX(bone.getRotX()).rotateY(bone.getRotY()).rotateZ(bone.getRotZ());
    }

    public static void scaleMatrixForBone(ModMatrix4f matrix, PlayerAnimBone bone) {
        matrix.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
    }

    public static void translateToPivotPoint(ModMatrix4f matrix, Vec3f pivot) {
        matrix.translate(pivot.x(), pivot.y(), pivot.z());
    }

    public static void translateAwayFromPivotPoint(ModMatrix4f matrix, Vec3f pivot) {
        matrix.translate(-pivot.x(), -pivot.y(), -pivot.z());
    }

    public static void prepMatrixForBone(ModMatrix4f matrix, PlayerAnimBone bone, Vec3f pivot) {
        translateToPivotPoint(matrix, pivot);
        rotateMatrixAroundBone(matrix, bone);
        scaleMatrixForBone(matrix, bone);
        translateAwayFromPivotPoint(matrix, pivot);
    }

    public static void prepMatrixForBoneXYZ(ModMatrix4f matrix, PlayerAnimBone bone, Vec3f pivot) {
        translateToPivotPoint(matrix, pivot);
        rotateMatrixAroundBoneXYZ(matrix, bone);
        scaleMatrixForBone(matrix, bone);
        translateAwayFromPivotPoint(matrix, pivot);
    }

    public static void applyParentsToChild(PlayerAnimBone child, Iterable<? extends PlayerAnimBone> parents, Function<String, Vec3f> positions, boolean isXYZ) {
        ModMatrix4f matrix = new ModMatrix4f();

        for (PlayerAnimBone parent : parents) {
            Vec3f pivot = parent instanceof PivotBone pivotBone ? pivotBone.getPivot() : positions.apply(parent.getName());
            if (isXYZ)
                prepMatrixForBoneXYZ(matrix, parent, pivot);
            else MatrixUtil.prepMatrixForBone(matrix, parent, pivot);
            child.addPos(parent.getPosX(), parent.getPosY(), parent.getPosZ());
        }

        Vec3f defaultPos = positions.apply(child.getName());
        matrix.translate(defaultPos.x(), defaultPos.y(), defaultPos.z());
        if (isXYZ) {
            MatrixUtil.rotateMatrixAroundBoneXYZ(matrix, child);
        }
        else MatrixUtil.rotateMatrixAroundBone(matrix, child);
        child.setPosX(-matrix.m30() + defaultPos.x() + child.getPosX());
        child.setPosY(matrix.m31() - defaultPos.y() + child.getPosY());
        child.setPosZ(-matrix.m32() - defaultPos.z() + child.getPosZ());

        Vec3f rotation = isXYZ ? matrix.getEulerRotationXYZ() : matrix.getEulerRotationZYX();
        child.updateRotation(rotation.x(), rotation.y(), rotation.z());

        child.mulScale(matrix.getColumnScale(0), matrix.getColumnScale(1), matrix.getColumnScale(2));
    }
}
