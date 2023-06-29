package com.simibubi.create.foundation.damageTypes;

import java.util.concurrent.CompletableFuture;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.simibubi.create.Create;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class DamageTypeTagGen extends DamageTypeTagsProvider {
	private final String namespace;

	public DamageTypeTagGen(String namespace, PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider) {
		super(pOutput, pLookupProvider);
		this.namespace = namespace;
	}

	public DamageTypeTagGen(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider) {
		this(Create.ID, pOutput, pLookupProvider);
	}

	@Override
	protected void addTags(@NotNull Provider provider) {
		Multimap<TagKey<DamageType>, ResourceKey<DamageType>> tagsToTypes = HashMultimap.create();
		DamageTypeData.allInNamespace(namespace).forEach(data -> data.tags.forEach(tag -> tagsToTypes.put(tag, data.key)));
		tagsToTypes.asMap().forEach((tag, keys) -> {
			TagAppender<DamageType> appender = tag(tag);
			keys.forEach(appender::add);
		});
	}
}
