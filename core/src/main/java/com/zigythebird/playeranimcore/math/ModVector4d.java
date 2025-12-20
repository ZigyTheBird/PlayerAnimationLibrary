package com.zigythebird.playeranimcore.math;

public class ModVector4d {
    public double x;
    public double y;
    public double z;
    public double w;

    public ModVector4d(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public ModVector4d mul(ModMatrix4d mat) {
        int prop = mat.properties();
        if ((prop & 4) != 0) {
            return this;
        } else if ((prop & 8) != 0) {
            return this.mulTranslation(mat);
        } else {
            return (prop & 2) != 0 ? this.mulAffine(mat) : this.mulGeneric(mat);
        }
    }

    public ModVector4d mulAffine(ModMatrix4d mat) {
        double x = this.x;
        double y = this.y;
        double z = this.z;
        double w = this.w;
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w)));
        this.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w)));
        this.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w)));
        this.w = w;
        return this;
    }

    public ModVector4d mulGeneric(ModMatrix4d mat) {
        double x = this.x;
        double y = this.y;
        double z = this.z;
        double w = this.w;
        this.x = Math.fma(mat.m00(), x, Math.fma(mat.m10(), y, Math.fma(mat.m20(), z, mat.m30() * w)));
        this.y = Math.fma(mat.m01(), x, Math.fma(mat.m11(), y, Math.fma(mat.m21(), z, mat.m31() * w)));
        this.z = Math.fma(mat.m02(), x, Math.fma(mat.m12(), y, Math.fma(mat.m22(), z, mat.m32() * w)));
        this.w = Math.fma(mat.m03(), x, Math.fma(mat.m13(), y, Math.fma(mat.m23(), z, mat.m33() * w)));
        return this;
    }

    public ModVector4d mulTranslation(ModMatrix4d mat) {
        this.x = Math.fma(mat.m30(), this.w, this.x);
        this.y = Math.fma(mat.m31(), this.w, this.y);
        this.z = Math.fma(mat.m32(), this.w, this.z);
        return this;
    }
}
