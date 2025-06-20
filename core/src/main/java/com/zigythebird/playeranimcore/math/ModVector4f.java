package com.zigythebird.playeranimcore.math;

public class ModVector4f {
    public float x;
    public float y;
    public float z;
    public float w;

    public ModVector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public ModVector4f mul(ModMatrix4f mat) {
        int prop = mat.properties();
        if ((prop & 4) != 0) {
            return this;
        } else if ((prop & 8) != 0) {
            return this.mulTranslation(mat);
        } else {
            return (prop & 2) != 0 ? this.mulAffine(mat) : this.mulGeneric(mat);
        }
    }

    public ModVector4f mulAffine(ModMatrix4f mat) {
        float x = this.x;
        float y = this.y;
        float z = this.z;
        float w = this.w;
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w)));
        this.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w)));
        this.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w)));
        this.w = w;
        return this;
    }

    public ModVector4f mulGeneric(ModMatrix4f mat) {
        float x = this.x;
        float y = this.y;
        float z = this.z;
        float w = this.w;
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w)));
        this.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w)));
        this.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w)));
        this.w = Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33() * w)));
        return this;
    }

    public ModVector4f mulTranslation(ModMatrix4f mat) {
        this.x = Math.fma(mat.m30(), this.w, this.x);
        this.y = Math.fma(mat.m31(), this.w, this.y);
        this.z = Math.fma(mat.m32(), this.w, this.z);
        return this;
    }
}
