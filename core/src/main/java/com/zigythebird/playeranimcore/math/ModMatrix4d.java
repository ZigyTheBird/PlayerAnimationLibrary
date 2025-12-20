package com.zigythebird.playeranimcore.math;

public class ModMatrix4d {
    int properties;
    double m00;
    double m01;
    double m02;
    double m03;
    double m10;
    double m11;
    double m12;
    double m13;
    double m20;
    double m21;
    double m22;
    double m23;
    double m30;
    double m31;
    double m32;
    double m33;

    public ModMatrix4d() {
        this._m00(1.0F)._m11(1.0F)._m22(1.0F)._m33(1.0F)._properties(30);
    }

    public double m00() {
        return this.m00;
    }

    public double m01() {
        return this.m01;
    }

    public double m02() {
        return this.m02;
    }

    public double m03() {
        return this.m03;
    }

    public double m10() {
        return this.m10;
    }

    public double m11() {
        return this.m11;
    }

    public double m12() {
        return this.m12;
    }

    public double m13() {
        return this.m13;
    }

    public double m20() {
        return this.m20;
    }

    public double m21() {
        return this.m21;
    }

    public double m22() {
        return this.m22;
    }

    public double m23() {
        return this.m23;
    }

    public double m30() {
        return this.m30;
    }

    public double m31() {
        return this.m31;
    }

    public double m32() {
        return this.m32;
    }

    public double m33() {
        return this.m33;
    }

    public int properties() {
        return properties;
    }

    ModMatrix4d _m00(double m00) {
        this.m00 = m00;
        return this;
    }

    ModMatrix4d _m01(double m01) {
        this.m01 = m01;
        return this;
    }

    ModMatrix4d _m02(double m02) {
        this.m02 = m02;
        return this;
    }

    ModMatrix4d _m03(double m03) {
        this.m03 = m03;
        return this;
    }

    ModMatrix4d _m10(double m10) {
        this.m10 = m10;
        return this;
    }

    ModMatrix4d _m11(double m11) {
        this.m11 = m11;
        return this;
    }

    ModMatrix4d _m12(double m12) {
        this.m12 = m12;
        return this;
    }

    ModMatrix4d _m13(double m13) {
        this.m13 = m13;
        return this;
    }

    ModMatrix4d _m20(double m20) {
        this.m20 = m20;
        return this;
    }

    ModMatrix4d _m21(double m21) {
        this.m21 = m21;
        return this;
    }

    ModMatrix4d _m22(double m22) {
        this.m22 = m22;
        return this;
    }

    ModMatrix4d _m23(double m23) {
        this.m23 = m23;
        return this;
    }

    ModMatrix4d _m30(double m30) {
        this.m30 = m30;
        return this;
    }

    ModMatrix4d _m31(double m31) {
        this.m31 = m31;
        return this;
    }

    ModMatrix4d _m32(double m32) {
        this.m32 = m32;
        return this;
    }

    ModMatrix4d _m33(double m33) {
        this.m33 = m33;
        return this;
    }

    ModMatrix4d _properties(int properties) {
        this.properties = properties;
        return this;
    }

    public void rotateX(double ang, ModMatrix4d dest) {
        if ((this.properties & 4) != 0) {
            dest.rotationX(ang);
        } else if ((this.properties & 8) != 0) {
            double x = this.m30();
            double y = this.m31();
            double z = this.m32();
            dest.rotationX(ang).setTranslation(x, y, z);
        } else {
            this.rotateXInternal(ang, dest);
        }
    }

    private void rotateXInternal(double ang, ModMatrix4d dest) {
        double sin = Math.sin(ang);
        double cos = MathHelper.cosFromSin(sin, ang);
        double lm10 = this.m10();
        double lm11 = this.m11();
        double lm12 = this.m12();
        double lm13 = this.m13();
        double lm20 = this.m20();
        double lm21 = this.m21();
        double lm22 = this.m22();
        double lm23 = this.m23();
        dest._m20(Math.fma(lm10, -sin, lm20 * cos))._m21(Math.fma(lm11, -sin, lm21 * cos))._m22(Math.fma(lm12, -sin, lm22 * cos))._m23(Math.fma(lm13, -sin, lm23 * cos))._m10(Math.fma(lm10, cos, lm20 * sin))._m11(Math.fma(lm11, cos, lm21 * sin))._m12(Math.fma(lm12, cos, lm22 * sin))._m13(Math.fma(lm13, cos, lm23 * sin))._m00(this.m00())._m01(this.m01())._m02(this.m02())._m03(this.m03())._m30(this.m30())._m31(this.m31())._m32(this.m32())._m33(this.m33())._properties(this.properties & -14);
    }

    public void rotateX(double ang) {
        this.rotateX(ang, this);
    }

    public ModMatrix4d rotateY(double ang, ModMatrix4d dest) {
        if ((this.properties & 4) != 0) {
            return dest.rotationY(ang);
        } else if ((this.properties & 8) != 0) {
            double x = this.m30();
            double y = this.m31();
            double z = this.m32();
            return dest.rotationY(ang).setTranslation(x, y, z);
        } else {
            return this.rotateYInternal(ang, dest);
        }
    }

    private ModMatrix4d rotateYInternal(double ang, ModMatrix4d dest) {
        double sin = Math.sin(ang);
        double cos = MathHelper.cosFromSin(sin, ang);
        double nm00 = Math.fma(this.m00(), cos, this.m20() * -sin);
        double nm01 = Math.fma(this.m01(), cos, this.m21() * -sin);
        double nm02 = Math.fma(this.m02(), cos, this.m22() * -sin);
        double nm03 = Math.fma(this.m03(), cos, this.m23() * -sin);
        return dest._m20(Math.fma(this.m00(), sin, this.m20() * cos))._m21(Math.fma(this.m01(), sin, this.m21() * cos))._m22(Math.fma(this.m02(), sin, this.m22() * cos))._m23(Math.fma(this.m03(), sin, this.m23() * cos))._m00(nm00)._m01(nm01)._m02(nm02)._m03(nm03)._m10(this.m10())._m11(this.m11())._m12(this.m12())._m13(this.m13())._m30(this.m30())._m31(this.m31())._m32(this.m32())._m33(this.m33())._properties(this.properties & -14);
    }

    public ModMatrix4d rotateY(double ang) {
        return this.rotateY(ang, this);
    }

    public ModMatrix4d rotateZ(double ang, ModMatrix4d dest) {
        if ((this.properties & 4) != 0) {
            return dest.rotationZ(ang);
        } else if ((this.properties & 8) != 0) {
            double x = this.m30();
            double y = this.m31();
            double z = this.m32();
            return dest.rotationZ(ang).setTranslation(x, y, z);
        } else {
            return this.rotateZInternal(ang, dest);
        }
    }

    private ModMatrix4d rotateZInternal(double ang, ModMatrix4d dest) {
        double sin = Math.sin(ang);
        double cos = MathHelper.cosFromSin(sin, ang);
        return this.rotateTowardsXY(sin, cos, dest);
    }

    public ModMatrix4d rotateZ(double ang) {
        return this.rotateZ(ang, this);
    }

    public ModMatrix4d rotationX(double ang) {
        double sin = Math.sin(ang);
        double cos = MathHelper.cosFromSin(sin, ang);
        if ((this.properties & 4) == 0) {
            identity();
        }

        this._m11(cos)._m12(sin)._m21(-sin)._m22(cos)._properties(18);
        return this;
    }

    public ModMatrix4d rotationY(double ang) {
        double sin = Math.sin(ang);
        double cos = MathHelper.cosFromSin(sin, ang);
        if ((this.properties & 4) == 0) {
            identity();
        }

        this._m00(cos)._m02(-sin)._m20(sin)._m22(cos)._properties(18);
        return this;
    }

    public ModMatrix4d rotationZ(double ang) {
        double sin = Math.sin(ang);
        double cos = MathHelper.cosFromSin(sin, ang);
        if ((this.properties & 4) == 0) {
            identity();
        }

        return this._m00(cos)._m01(sin)._m10(-sin)._m11(cos)._properties(18);
    }

    public void identity() {
        this._m00(1.0F)._m01(0.0F)._m02(0.0F)._m03(0.0F)._m10(0.0F)._m11(1.0F)._m12(0.0F)._m13(0.0F)._m20(0.0F)._m21(0.0F)._m22(1.0F)._m23(0.0F)._m30(0.0F)._m31(0.0F)._m32(0.0F)._m33(1.0F);
    }

    public ModMatrix4d rotateTowardsXY(double dirX, double dirY, ModMatrix4d dest) {
        if ((this.properties & 4) != 0) {
            return dest.rotationTowardsXY(dirX, dirY);
        } else {
            double nm00 = Math.fma(this.m00(), dirY, this.m10() * dirX);
            double nm01 = Math.fma(this.m01(), dirY, this.m11() * dirX);
            double nm02 = Math.fma(this.m02(), dirY, this.m12() * dirX);
            double nm03 = Math.fma(this.m03(), dirY, this.m13() * dirX);
            return dest._m10(Math.fma(this.m00(), -dirX, this.m10() * dirY))._m11(Math.fma(this.m01(), -dirX, this.m11() * dirY))._m12(Math.fma(this.m02(), -dirX, this.m12() * dirY))._m13(Math.fma(this.m03(), -dirX, this.m13() * dirY))._m00(nm00)._m01(nm01)._m02(nm02)._m03(nm03)._m20(this.m20())._m21(this.m21())._m22(this.m22())._m23(this.m23())._m30(this.m30())._m31(this.m31())._m32(this.m32())._m33(this.m33())._properties(this.properties & -14);
        }
    }

    public ModMatrix4d rotationTowardsXY(double dirX, double dirY) {
        if ((this.properties & 4) == 0) {
            identity();
        }

        return this._m00(dirY)._m01(dirX)._m10(-dirX)._m11(dirY)._properties(18);
    }

    public ModMatrix4d setTranslation(double x, double y, double z) {
        return this._m30(x)._m31(y)._m32(z)._properties(this.properties & -6);
    }

    public ModMatrix4d translate(double x, double y, double z) {
        return (this.properties & 4) != 0 ? this.translation(x, y, z) : this.translateGeneric(x, y, z);
    }

    private ModMatrix4d translateGeneric(double x, double y, double z) {
        return this._m30(Math.fma(this.m00(), x, Math.fma(this.m10(), y, Math.fma(this.m20(), z, this.m30()))))._m31(Math.fma(this.m01(), x, Math.fma(this.m11(), y, Math.fma(this.m21(), z, this.m31()))))._m32(Math.fma(this.m02(), x, Math.fma(this.m12(), y, Math.fma(this.m22(), z, this.m32()))))._m33(Math.fma(this.m03(), x, Math.fma(this.m13(), y, Math.fma(this.m23(), z, this.m33()))))._properties(this.properties & -6);
    }

    public ModMatrix4d translation(double x, double y, double z) {
        if ((this.properties & 4) == 0) {
            identity();
        }

        return this._m30(x)._m31(y)._m32(z)._properties(26);
    }

    public ModMatrix4d scale(double x, double y, double z, ModMatrix4d dest) {
        return (this.properties & 4) != 0 ? dest.scaling(x, y, z) : this.scaleGeneric(x, y, z, dest);
    }

    private ModMatrix4d scaleGeneric(double x, double y, double z, ModMatrix4d dest) {
        boolean one = MathHelper.absEqualsOne(x) && MathHelper.absEqualsOne(y) && MathHelper.absEqualsOne(z);
        return dest._m00(this.m00() * x)._m01(this.m01() * x)._m02(this.m02() * x)._m03(this.m03() * x)._m10(this.m10() * y)._m11(this.m11() * y)._m12(this.m12() * y)._m13(this.m13() * y)._m20(this.m20() * z)._m21(this.m21() * z)._m22(this.m22() * z)._m23(this.m23() * z)._m30(this.m30())._m31(this.m31())._m32(this.m32())._m33(this.m33())._properties(this.properties & ~(13 | (one ? 0 : 16)));
    }

    public ModMatrix4d scale(double x, double y, double z) {
        return this.scale(x, y, z, this);
    }

    public ModMatrix4d scaling(double x, double y, double z) {
        if ((this.properties & 4) == 0) {
            identity();
        }

        boolean one = MathHelper.absEqualsOne(x) && MathHelper.absEqualsOne(y) && MathHelper.absEqualsOne(z);
        return this._m00(x)._m11(y)._m22(z)._properties(2 | (one ? 16 : 0));
    }

    public Vec3f getEulerRotation() {
        double x;
        double y;
        double z;

        if (m02 < 1) {
            if (m02 > -1) {
                y = Math.asin(-m02);
                z = Math.atan2(m01, m00);
                x = Math.atan2(m12, m22);
            }
            else {
                y = (Math.PI/2);
                z = -Math.atan2(-m21, m11);
                x = 0;
            }
        }
        else {
            y = -(Math.PI/2);
            z = Math.atan2(-m21, m11);
            x = 0;
        }

        return new Vec3f((float) x, (float) y, (float) z);
    }

    public double getColumnScale(int i) {
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
