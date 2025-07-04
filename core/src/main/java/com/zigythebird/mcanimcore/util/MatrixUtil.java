package com.zigythebird.mcanimcore.util;

import com.zigythebird.mcanimcore.bones.AnimBone;
import com.zigythebird.mcanimcore.math.ModMatrix4f;
import com.zigythebird.mcanimcore.math.Vec3f;

/**
 * Does NOT divide pos 16.
 * Used for applying custom pivot bones to bones
 */
public class MatrixUtil {
    public static void translateMatrixToBone(ModMatrix4f matrix, AnimBone bone) {
        matrix.translate(bone.getPosX(), bone.getPosY(), bone.getPosZ());
    }

    public static void rotateMatrixAroundBone(ModMatrix4f matrix, AnimBone bone) {
        if (bone.getRotZ() != 0 || bone.getRotY() != 0 || bone.getRotX() != 0)
            matrix.rotateZ(bone.getRotZ()).rotateY(bone.getRotY()).rotateX(bone.getRotX());
    }

    public static void scaleMatrixForBone(ModMatrix4f matrix, AnimBone bone) {
        matrix.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
    }

    public static void translateToPivotPoint(ModMatrix4f matrix, Vec3f pivot) {
        matrix.translate(pivot.x(), pivot.y(), pivot.z());
    }

    public static void translateAwayFromPivotPoint(ModMatrix4f matrix, Vec3f pivot) {
        matrix.translate(-pivot.x(), -pivot.y(), -pivot.z());
    }

    public static void prepMatrixForBone(ModMatrix4f matrix, AnimBone bone, Vec3f pivot) {
        translateMatrixToBone(matrix, bone);
        translateToPivotPoint(matrix, pivot);
        rotateMatrixAroundBone(matrix, bone);
        scaleMatrixForBone(matrix, bone);
        translateAwayFromPivotPoint(matrix, pivot);
    }
}
