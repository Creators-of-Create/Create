package com.simibubi.create.foundation.render.backend.gl;

import org.lwjgl.opengl.GL11;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum GlPrimitiveType {
    FLOAT(4, "float", GL11.GL_FLOAT),
    UBYTE(1, "ubyte", GL11.GL_UNSIGNED_BYTE),
    BYTE(1, "byte", GL11.GL_BYTE),
    USHORT(2, "ushort", GL11.GL_UNSIGNED_SHORT),
    SHORT(2, "short", GL11.GL_SHORT),
    UINT(4, "uint", GL11.GL_UNSIGNED_INT),
    INT(4, "int", GL11.GL_INT);

    private final int size;
    private final String displayName;
    private final int glConstant;

    GlPrimitiveType(int bytes, String name, int glEnum) {
        this.size = bytes;
        this.displayName = name;
        this.glConstant = glEnum;
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