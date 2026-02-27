package com.zigythebird.playeranimcore.util;

import com.zigythebird.playeranimcore.bones.CustomBone;
import com.zigythebird.playeranimcore.bones.IPivotBone;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.math.Vec3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.Function;

/**
 * Does NOT divide pos 16.
 * Used for applying custom pivot bones to player bones
 */
public class MatrixUtil {
    public static void translateMatrixForBone(Matrix4f matrix, PlayerAnimBone bone) {
        matrix.translate(-bone.position.x, bone.position.y, -bone.position.z);
    }

    public static void rotateMatrixAroundBone(Matrix4f matrix, PlayerAnimBone bone) {
        if (bone.rotation.z != 0 || bone.rotation.y != 0 || bone.rotation.x != 0)
            matrix.rotateZYX(bone.rotation);
    }

    public static void scaleMatrixForBone(Matrix4f matrix, PlayerAnimBone bone) {
        matrix.scale(bone.scale.x, bone.scale.y, bone.scale.z);
    }

    public static void translateToPivotPoint(Matrix4f matrix, Vec3f pivot) {
        matrix.translate(pivot.x(), pivot.y(), pivot.z());
    }

    public static void translateAwayFromPivotPoint(Matrix4f matrix, Vec3f pivot) {
        matrix.translate(-pivot.x(), -pivot.y(), -pivot.z());
    }

    public static void prepMatrixForBone(Matrix4f matrix, PlayerAnimBone bone, Vec3f pivot) {
        translateToPivotPoint(matrix, pivot);
        translateMatrixForBone(matrix, bone);
        rotateMatrixAroundBone(matrix, bone);
        scaleMatrixForBone(matrix, bone);
        translateAwayFromPivotPoint(matrix, pivot);
    }

    public static void applyParentsToChild(PlayerAnimBone child, Iterable<? extends PlayerAnimBone> parents, Function<String, Vec3f> positions) {
        Matrix4f matrix = new Matrix4f();

        for (PlayerAnimBone parent : parents) {
            Vec3f pivot = parent instanceof IPivotBone pivotBone ? pivotBone.getPivot() : positions.apply(parent.getName());
            MatrixUtil.prepMatrixForBone(matrix, parent, pivot);
        }

        Vec3f defaultPos = positions.apply(child.getName());
        matrix.translate(defaultPos.x(), defaultPos.y(), defaultPos.z());
        MatrixUtil.rotateMatrixAroundBone(matrix, child);

        child.position.add(-matrix.m30() + defaultPos.x(), matrix.m31() - defaultPos.y(), -matrix.m32() - defaultPos.z());
        child.rotation.set(matrix.getEulerAnglesZYX(new Vector3f()));
        child.scale.mul(matrix.getScale(new Vector3f()));
    }
}
