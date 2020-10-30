package com.simibubi.create.foundation.utility;

import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EmptyNamedTag<T> implements Tags.IOptionalNamedTag<T> {
	private final ResourceLocation id;

	public EmptyNamedTag(ResourceLocation id) {
		this.id = id;
	}

	@Override
	public boolean isDefaulted() {
		return false;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public boolean contains(Object p_230235_1_) {
		return false;
	}

	@Override
	public List<T> values() {
		return Collections.emptyList()	;
	}
}
