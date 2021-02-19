package com.simibubi.create.foundation.render.backend.gl;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum GlPrimitiveType {
    FLOAT(4, "float", 5126),
    UBYTE(1, "ubyte", 5121),
    BYTE(1, "byte", 5120),
    USHORT(2, "ushort", 5123),
    SHORT(2, "short", 5122),
    UINT(4, "uint", 5125),
    INT(4, "int", 5124);

    private final int size;
    private final String displayName;
    private final int glConstant;

    GlPrimitiveType(int p_i46095_3_, String p_i46095_4_, int p_i46095_5_) {
        this.size = p_i46095_3_;
        this.displayName = p_i46095_4_;
        this.glConstant = p_i46095_5_;
    }

    public int getSize() {
        return this.size;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getGlConstant() {
        return this.glConstant;
    }
}