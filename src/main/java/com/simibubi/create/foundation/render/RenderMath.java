package com.simibubi.create.foundation.render;

public class RenderMath {
    public static final float SQRT2 = 1.41421356237f;

    public static int timesSqrt2(int i) {
        return (int) Math.floor((float) i * SQRT2 / 4f);
    }

    public static int nextPowerOf2(int a)  {
        int h = Integer.highestOneBit(a);
        return (h == a) ? h : (h << 1);
    }

    public static boolean isPowerOf2(int n) {
        int b = n & (n - 1);
        return b == 0 && n != 0;
    }
}
