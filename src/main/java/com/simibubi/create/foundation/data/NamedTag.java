package com.simibubi.create.foundation.data;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NamedTag<T> implements ITag.INamedTag<T> {
	private final ResourceLocation id;
	private final ITag<T> tag;

	public NamedTag(@Nullable ITag<T> tag, ResourceLocation id) {
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
