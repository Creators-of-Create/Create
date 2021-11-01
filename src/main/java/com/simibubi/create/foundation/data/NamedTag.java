package com.simibubi.create.foundation.data;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.tags.Tag;
import net.minecraft.resources.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NamedTag<T> implements Tag.Named<T> {
	private final ResourceLocation id;
	private final Tag<T> tag;

	public NamedTag(@Nullable Tag<T> tag, ResourceLocation id) {
		this.tag = tag;
		this.id = id;
	}

	@Override
	public ResourceLocation getName() {
		return id;
	}

	@Override
	public boolean contains(T p_230235_1_) {
		if (tag == null)
			return false;
		return tag.contains(p_230235_1_);
	}

	@Override
	public List<T> getValues() {
		if (tag == null)
			return Collections.emptyList();
		return tag.getValues();
	}
}
