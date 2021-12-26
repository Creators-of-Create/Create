package com.simibubi.create.lib.extensions;

import org.jetbrains.annotations.Contract;

public interface Matrix4fExtensions {
	@Contract(mutates = "this")
	void create$fromFloatArray(float[] floats);
}
