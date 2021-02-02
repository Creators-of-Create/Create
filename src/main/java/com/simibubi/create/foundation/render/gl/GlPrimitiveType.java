package com.simibubi.create.foundation.render.gl;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum GlPrimitiveType {
    FLOAT(4, "Float", 5126),
    UBYTE(1, "Unsigned Byte", 5121),
    BYTE(1, "Byte", 5120),
    USHORT(2, "Unsigned Short", 5123),
    SHORT(2, "Short", 5122),
    UINT(4, "Unsigned Int", 5125),
    INT(4, "Int", 5124);

    private final int size;
    private final String displayName;
    private final int glConstant;

    private GlPrimitiveType(int p_i46095_3_, String p_i46095_4_, int p_i46095_5_) {
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