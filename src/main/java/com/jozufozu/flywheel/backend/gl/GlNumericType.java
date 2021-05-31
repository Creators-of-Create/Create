package com.jozufozu.flywheel.backend.gl;

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

	private final int size;
	private final String displayName;
	private final int glEnum;

	GlNumericType(int bytes, String name, int glEnum) {
		this.size = bytes;
		this.displayName = name;
		this.glEnum = glEnum;
	}

	public int getSize() {
		return this.size;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public int getGlEnum() {
		return this.glEnum;
	}

	@Nullable
	public static GlNumericType byName(String name) {
		return name == null ? null : NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT));
	}
}
