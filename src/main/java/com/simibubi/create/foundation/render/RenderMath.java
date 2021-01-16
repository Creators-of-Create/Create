package com.simibubi.create.foundation.render;

public class RenderMath {
    public static int nextPowerOf2(int a)  {
        int h = Integer.highestOneBit(a);
        return (h == a) ? h : (h << 1);
    }
}
