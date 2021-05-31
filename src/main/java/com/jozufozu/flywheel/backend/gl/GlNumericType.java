package com.jozufozu.flywheel.backend.gl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum GlNumericType {
	FLOAT(4, "float", GL11.GL_FLOAT),
	UBYTE(1, "ubyte", GL11.GL_UNSIGNED_BYTE),
	BYTE(1, "byte", GL11.GL_BYTE),
	USHORT(2, "ushort", GL11.GL_UNSIGNED_SHORT),
	SHORT(2, "short", GL11.GL_SHORT),
	UINT(4, "uint", GL11.GL_UNSIGNED_INT),
	INT(4, "int", GL11.GL_INT),
	;

	private static final GlNumericType[] VALUES = values();
	private static final Map<String, GlNumericType> NAME_LOOKUP = Arrays.stream(VALUES)
			.collect(Collectors.toMap(GlNumericType::getDisplayName, type -> type));

	private final int byteWidth;
	private final String displayName;
	private final int glEnum;

	GlNumericType(int bytes, String name, int glEnum) {
		this.byteWidth = bytes;
		this.displayName = name;
		this.glEnum = glEnum;
	}

	public int getByteWidth() {
		return this.byteWidth;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public int getGlEnum() {
		return this.glEnum;
	}

	public void castAndBuffer(ByteBuffer buf, int val) {
		if (this == UBYTE || this == BYTE) {
			buf.put((byte) val);
		} else if (this == USHORT || this == SHORT) {
			buf.putShort((short) val);
		} else if (this == UINT || this == INT) {
			buf.putInt(val);
		}
	}

	@Nullable
	public static GlNumericType byName(String name) {
		return name == null ? null : NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT));
	}
}
