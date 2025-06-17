package com.zigythebird.playeranimcore.util;

import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.math.Vec3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * DOES NOT DIVIDE POS BY 16!
 * Used for applying custom pivot bones to player bones
 */
public class MatrixUtil {
    public static void translateMatrixToBone(Matrix4f matrix, PlayerAnimBone bone) {
        matrix.translate(bone.getPosX(), bone.getPosY(), bone.getPosZ());
    }

    public static void rotateMatrixAroundBone(Matrix4f matrix, PlayerAnimBone bone) {
        if (bone.getRotZ() != 0 || bone.getRotY() != 0 || bone.getRotX() != 0)
            matrix.rotate(new Quaternionf().rotateZYX(bone.getRotZ(), bone.getRotY(), bone.getRotX()));
    }

    public static void scaleMatrixForBone(Matrix4f matrix, PlayerAnimBone bone) {
        matrix.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
    }

    public static void translateToPivotPoint(Matrix4f matrix, Vec3f pivot) {
        matrix.translate(pivot.x, pivot.y, pivot.z);
    }

    public static void translateAwayFromPivotPoint(Matrix4f matrix, Vec3f pivot) {
        matrix.translate(-pivot.x, -pivot.y, -pivot.z);
    }

    public static void prepMatrixForBone(Matrix4f matrix, PlayerAnimBone bone, Vec3f pivot) {
        translateMatrixToBone(matrix, bone);
        translateToPivotPoint(matrix, pivot);
        rotateMatrixAroundBone(matrix, bone);
        scaleMatrixForBone(matrix, bone);
        translateAwayFromPivotPoint(matrix, pivot);
    }
}
