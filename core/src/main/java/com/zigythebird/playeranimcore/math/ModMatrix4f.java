package com.zigythebird.playeranimcore.math;

import team.unnamed.mocha.runtime.standard.MochaMath;

public class ModMatrix4f {
    int properties;
    float m00;
    float m01;
    float m02;
    float m03;
    float m10;
    float m11;
    float m12;
    float m13;
    float m20;
    float m21;
    float m22;
    float m23;
    float m30;
    float m31;
    float m32;
    float m33;

    public ModMatrix4f() {
        this._m00(1.0F)._m11(1.0F)._m22(1.0F)._m33(1.0F)._properties(30);
    }

    public float m00() {
        return this.m00;
    }

    public float m01() {
        return this.m01;
    }

    public float m02() {
        return this.m02;
    }

    public float m03() {
        return this.m03;
    }

    public float m10() {
        return this.m10;
    }

    public float m11() {
        return this.m11;
    }

    public float m12() {
        return this.m12;
    }

    public float m13() {
        return this.m13;
    }

    public float m20() {
        return this.m20;
    }

    public float m21() {
        return this.m21;
    }

    public float m22() {
        return this.m22;
    }

    public float m23() {
        return this.m23;
    }

    public float m30() {
        return this.m30;
    }

    public float m31() {
        return this.m31;
    }

    public float m32() {
        return this.m32;
    }

    public float m33() {
        return this.m33;
    }

    public int properties() {
        return properties;
    }

    ModMatrix4f _m00(float m00) {
        this.m00 = m00;
        return this;
    }

    ModMatrix4f _m01(float m01) {
        this.m01 = m01;
        return this;
    }

    ModMatrix4f _m02(float m02) {
        this.m02 = m02;
        return this;
    }

    ModMatrix4f _m03(float m03) {
        this.m03 = m03;
        return this;
    }

    ModMatrix4f _m10(float m10) {
        this.m10 = m10;
        return this;
    }

    ModMatrix4f _m11(float m11) {
        this.m11 = m11;
        return this;
    }

    ModMatrix4f _m12(float m12) {
        this.m12 = m12;
        return this;
    }

    ModMatrix4f _m13(float m13) {
        this.m13 = m13;
        return this;
    }

    ModMatrix4f _m20(float m20) {
        this.m20 = m20;
        return this;
    }

    ModMatrix4f _m21(float m21) {
        this.m21 = m21;
        return this;
    }

    ModMatrix4f _m22(float m22) {
        this.m22 = m22;
        return this;
    }

    ModMatrix4f _m23(float m23) {
        this.m23 = m23;
        return this;
    }

    ModMatrix4f _m30(float m30) {
        this.m30 = m30;
        return this;
    }

    ModMatrix4f _m31(float m31) {
        this.m31 = m31;
        return this;
    }

    ModMatrix4f _m32(float m32) {
        this.m32 = m32;
        return this;
    }

    ModMatrix4f _m33(float m33) {
        this.m33 = m33;
        return this;
    }

    ModMatrix4f _properties(int properties) {
        this.properties = properties;
        return this;
    }

    public void rotateX(float ang, ModMatrix4f dest) {
        if ((this.properties & 4) != 0) {
            dest.rotationX(ang);
        } else if ((this.properties & 8) != 0) {
            float x = this.m30();
            float y = this.m31();
            float z = this.m32();
            dest.rotationX(ang).setTranslation(x, y, z);
        } else {
            this.rotateXInternal(ang, dest);
        }
    }

    private void rotateXInternal(float ang, ModMatrix4f dest) {
        float sin = (float) Math.sin(ang);
        float cos = MathHelper.cosFromSin(sin, ang);
        float lm10 = this.m10();
        float lm11 = this.m11();
        float lm12 = this.m12();
        float lm13 = this.m13();
        float lm20 = this.m20();
        float lm21 = this.m21();
        float lm22 = this.m22();
        float lm23 = this.m23();
        dest._m20(Math.fma(lm10, -sin, lm20 * cos))._m21(Math.fma(lm11, -sin, lm21 * cos))._m22(Math.fma(lm12, -sin, lm22 * cos))._m23(Math.fma(lm13, -sin, lm23 * cos))._m10(Math.fma(lm10, cos, lm20 * sin))._m11(Math.fma(lm11, cos, lm21 * sin))._m12(Math.fma(lm12, cos, lm22 * sin))._m13(Math.fma(lm13, cos, lm23 * sin))._m00(this.m00())._m01(this.m01())._m02(this.m02())._m03(this.m03())._m30(this.m30())._m31(this.m31())._m32(this.m32())._m33(this.m33())._properties(this.properties & -14);
    }

    public void rotateX(float ang) {
        this.rotateX(ang, this);
    }

    public ModMatrix4f rotateY(float ang, ModMatrix4f dest) {
        if ((this.properties & 4) != 0) {
            return dest.rotationY(ang);
        } else if ((this.properties & 8) != 0) {
            float x = this.m30();
            float y = this.m31();
            float z = this.m32();
            return dest.rotationY(ang).setTranslation(x, y, z);
        } else {
            return this.rotateYInternal(ang, dest);
        }
    }

    private ModMatrix4f rotateYInternal(float ang, ModMatrix4f dest) {
        float sin = (float) Math.sin(ang);
        float cos = MathHelper.cosFromSin(sin, ang);
        float nm00 = Math.fma(this.m00(), cos, this.m20() * -sin);
        float nm01 = Math.fma(this.m01(), cos, this.m21() * -sin);
        float nm02 = Math.fma(this.m02(), cos, this.m22() * -sin);
        float nm03 = Math.fma(this.m03(), cos, this.m23() * -sin);
        return dest._m20(Math.fma(this.m00(), sin, this.m20() * cos))._m21(Math.fma(this.m01(), sin, this.m21() * cos))._m22(Math.fma(this.m02(), sin, this.m22() * cos))._m23(Math.fma(this.m03(), sin, this.m23() * cos))._m00(nm00)._m01(nm01)._m02(nm02)._m03(nm03)._m10(this.m10())._m11(this.m11())._m12(this.m12())._m13(this.m13())._m30(this.m30())._m31(this.m31())._m32(this.m32())._m33(this.m33())._properties(this.properties & -14);
    }

    public ModMatrix4f rotateY(float ang) {
        return this.rotateY(ang, this);
    }

    public ModMatrix4f rotateZ(float ang, ModMatrix4f dest) {
        if ((this.properties & 4) != 0) {
            return dest.rotationZ(ang);
        } else if ((this.properties & 8) != 0) {
            float x = this.m30();
            float y = this.m31();
            float z = this.m32();
            return dest.rotationZ(ang).setTranslation(x, y, z);
        } else {
            return this.rotateZInternal(ang, dest);
        }
    }

    private ModMatrix4f rotateZInternal(float ang, ModMatrix4f dest) {
        float sin = (float) Math.sin(ang);
        float cos = MathHelper.cosFromSin(sin, ang);
        return this.rotateTowardsXY(sin, cos, dest);
    }

    public ModMatrix4f rotateZ(float ang) {
        return this.rotateZ(ang, this);
    }

    public ModMatrix4f rotationX(float ang) {
        float sin = (float) Math.sin(ang);
        float cos = MathHelper.cosFromSin(sin, ang);
        if ((this.properties & 4) == 0) {
            identity();
        }

        this._m11(cos)._m12(sin)._m21(-sin)._m22(cos)._properties(18);
        return this;
    }

    public ModMatrix4f rotationY(float ang) {
        float sin = (float) Math.sin(ang);
        float cos = MathHelper.cosFromSin(sin, ang);
        if ((this.properties & 4) == 0) {
            identity();
        }

        this._m00(cos)._m02(-sin)._m20(sin)._m22(cos)._properties(18);
        return this;
    }

    public ModMatrix4f rotationZ(float ang) {
        float sin = (float) Math.sin(ang);
        float cos = MathHelper.cosFromSin(sin, ang);
        if ((this.properties & 4) == 0) {
            identity();
        }

        return this._m00(cos)._m01(sin)._m10(-sin)._m11(cos)._properties(18);
    }

    public void identity() {
        this._m00(1.0F)._m01(0.0F)._m02(0.0F)._m03(0.0F)._m10(0.0F)._m11(1.0F)._m12(0.0F)._m13(0.0F)._m20(0.0F)._m21(0.0F)._m22(1.0F)._m23(0.0F)._m30(0.0F)._m31(0.0F)._m32(0.0F)._m33(1.0F);
    }

    public ModMatrix4f rotateTowardsXY(float dirX, float dirY, ModMatrix4f dest) {
        if ((this.properties & 4) != 0) {
            return dest.rotationTowardsXY(dirX, dirY);
        } else {
            float nm00 = Math.fma(this.m00(), dirY, this.m10() * dirX);
            float nm01 = Math.fma(this.m01(), dirY, this.m11() * dirX);
            float nm02 = Math.fma(this.m02(), dirY, this.m12() * dirX);
            float nm03 = Math.fma(this.m03(), dirY, this.m13() * dirX);
            return dest._m10(Math.fma(this.m00(), -dirX, this.m10() * dirY))._m11(Math.fma(this.m01(), -dirX, this.m11() * dirY))._m12(Math.fma(this.m02(), -dirX, this.m12() * dirY))._m13(Math.fma(this.m03(), -dirX, this.m13() * dirY))._m00(nm00)._m01(nm01)._m02(nm02)._m03(nm03)._m20(this.m20())._m21(this.m21())._m22(this.m22())._m23(this.m23())._m30(this.m30())._m31(this.m31())._m32(this.m32())._m33(this.m33())._properties(this.properties & -14);
        }
    }

    public ModMatrix4f rotationTowardsXY(float dirX, float dirY) {
        if ((this.properties & 4) == 0) {
            identity();
        }

        return this._m00(dirY)._m01(dirX)._m10(-dirX)._m11(dirY)._properties(18);
    }

    public ModMatrix4f setTranslation(float x, float y, float z) {
        return this._m30(x)._m31(y)._m32(z)._properties(this.properties & -6);
    }

    public ModMatrix4f translate(float x, float y, float z) {
        return (this.properties & 4) != 0 ? this.translation(x, y, z) : this.translateGeneric(x, y, z);
    }

    private ModMatrix4f translateGeneric(float x, float y, float z) {
        return this._m30(Math.fma(this.m00(), x, Math.fma(this.m10(), y, Math.fma(this.m20(), z, this.m30()))))._m31(Math.fma(this.m01(), x, Math.fma(this.m11(), y, Math.fma(this.m21(), z, this.m31()))))._m32(Math.fma(this.m02(), x, Math.fma(this.m12(), y, Math.fma(this.m22(), z, this.m32()))))._m33(Math.fma(this.m03(), x, Math.fma(this.m13(), y, Math.fma(this.m23(), z, this.m33()))))._properties(this.properties & -6);
    }

    public ModMatrix4f translation(float x, float y, float z) {
        if ((this.properties & 4) == 0) {
            identity();
        }

        return this._m30(x)._m31(y)._m32(z)._properties(26);
    }

    public ModMatrix4f scale(float x, float y, float z, ModMatrix4f dest) {
        return (this.properties & 4) != 0 ? dest.scaling(x, y, z) : this.scaleGeneric(x, y, z, dest);
    }

    private ModMatrix4f scaleGeneric(float x, float y, float z, ModMatrix4f dest) {
        boolean one = MathHelper.absEqualsOne(x) && MathHelper.absEqualsOne(y) && MathHelper.absEqualsOne(z);
        return dest._m00(this.m00() * x)._m01(this.m01() * x)._m02(this.m02() * x)._m03(this.m03() * x)._m10(this.m10() * y)._m11(this.m11() * y)._m12(this.m12() * y)._m13(this.m13() * y)._m20(this.m20() * z)._m21(this.m21() * z)._m22(this.m22() * z)._m23(this.m23() * z)._m30(this.m30())._m31(this.m31())._m32(this.m32())._m33(this.m33())._properties(this.properties & ~(13 | (one ? 0 : 16)));
    }

    public ModMatrix4f scale(float x, float y, float z) {
        return this.scale(x, y, z, this);
    }

    public ModMatrix4f scaling(float x, float y, float z) {
        if ((this.properties & 4) == 0) {
            identity();
        }

        boolean one = MathHelper.absEqualsOne(x) && MathHelper.absEqualsOne(y) && MathHelper.absEqualsOne(z);
        return this._m00(x)._m11(y)._m22(z)._properties(2 | (one ? 16 : 0));
    }

    public Vec3f getEulerRotation() {
        float x;
        float y;
        float z;

        if (m02 < 1) {
            if (m02 > -1) {
                y = (float) Math.asin(-m02);
                z = (float) Math.atan2(m01, m00);
                x = (float) Math.atan2(m12, m22);
            }
            else {
                y = (float) (Math.PI/2);
                z = (float) -Math.atan2(-m21, m11);
                x = 0;
            }
        }
        else {
            y = -(float) (Math.PI/2);
            z = (float) Math.atan2(-m21, m11);
            x = 0;
        }

        return new Vec3f(x, y, z);
    }

    public float getColumnScale(int i) {
        switch (i) {
            case 0 -> {
                return MathHelper.length(m00, m01, m02, m03);
            }
            case 1 -> {
                return MathHelper.length(m10, m11, m12, m13);
            }
            case 2 -> {
                return MathHelper.length(m20, m21, m22, m23);
            }
            case 3 -> {
                return MathHelper.length(m30, m31, m32, m33);
            }
        }
        return 1;
    }
}
