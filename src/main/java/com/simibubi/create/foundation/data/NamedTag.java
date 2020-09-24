package com.simibubi.create.foundation.data;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

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
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public boolean contains(T p_230235_1_) {
		if (tag == null)
			return false;
		return tag.contains(p_230235_1_);
	}

	@Override
	public List<T> values() {
		if (tag == null)
			return Collections.emptyList();
		return tag.values();
	}
}
