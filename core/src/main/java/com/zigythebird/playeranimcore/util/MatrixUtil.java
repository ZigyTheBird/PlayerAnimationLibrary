package com.zigythebird.playeranimcore.util;

import com.zigythebird.playeranimcore.bones.PivotBone;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MatrixUtil {
    public static void translateMatrixToBone(Matrix4f matrix, PlayerAnimBone bone) {
        matrix.translate(bone.getPosX() / 16f, bone.getPosY() / 16f, bone.getPosZ() / 16f);
    }

    public static void rotateMatrixAroundBone(Matrix4f matrix, PlayerAnimBone bone) {
        if (bone.getRotZ() != 0 || bone.getRotY() != 0 || bone.getRotX() != 0)
            matrix.rotate(new Quaternionf().rotateZYX(bone.getRotZ(), bone.getRotY(), bone.getRotX()));
    }

    public static void scaleMatrixForBone(Matrix4f matrix, PlayerAnimBone bone) {
        matrix.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
    }

    public static void translateToPivotPoint(Matrix4f matrix, PivotBone bone) {
        Vector3f pivot = bone.getPivot();
        matrix.translate(pivot.x()/16, pivot.y()/16, pivot.z()/16);
    }

    public static void translateAwayFromPivotPoint(Matrix4f matrix, PivotBone bone) {
        Vector3f pivot = bone.getPivot();
        matrix.translate(-pivot.x()/16, -pivot.y()/16, -pivot.z()/16);
    }

    public static void translateAndRotateMatrixForBone(Matrix4f matrix, PivotBone bone) {
        translateToPivotPoint(matrix, bone);
        rotateMatrixAroundBone(matrix, bone);
    }

    public static void prepMatrixForBone(Matrix4f matrix, PivotBone bone) {
        translateMatrixToBone(matrix, bone);
        translateToPivotPoint(matrix, bone);
        rotateMatrixAroundBone(matrix, bone);
        scaleMatrixForBone(matrix, bone);
        translateAwayFromPivotPoint(matrix, bone);
    }
}
