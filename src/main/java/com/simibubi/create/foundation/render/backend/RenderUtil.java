package com.simibubi.create.foundation.render.backend;

import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;

import java.nio.ByteBuffer;

public class RenderUtil {
    public static int nextPowerOf2(int a)  {
        int h = Integer.highestOneBit(a);
        return (h == a) ? h : (h << 1);
    }

    public static boolean isPowerOf2(int n) {
        int b = n & (n - 1);
        return b == 0 && n != 0;
    }

    // GPUs want matrices in column major order.

    public static void writeMat3(ByteBuffer buf, Matrix3f mat) {
        buf.putFloat(mat.a00);
        buf.putFloat(mat.a10);
        buf.putFloat(mat.a20);
        buf.putFloat(mat.a01);
        buf.putFloat(mat.a11);
        buf.putFloat(mat.a21);
        buf.putFloat(mat.a02);
        buf.putFloat(mat.a12);
        buf.putFloat(mat.a22);
    }

    public static void writeMat4(ByteBuffer buf, Matrix4f mat) {
        buf.putFloat(mat.a00);
        buf.putFloat(mat.a10);
        buf.putFloat(mat.a20);
        buf.putFloat(mat.a30);
        buf.putFloat(mat.a01);
        buf.putFloat(mat.a11);
        buf.putFloat(mat.a21);
        buf.putFloat(mat.a31);
        buf.putFloat(mat.a02);
        buf.putFloat(mat.a12);
        buf.putFloat(mat.a22);
        buf.putFloat(mat.a32);
        buf.putFloat(mat.a03);
        buf.putFloat(mat.a13);
        buf.putFloat(mat.a23);
        buf.putFloat(mat.a33);




    }
}
