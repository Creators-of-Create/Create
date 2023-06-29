package com.simibubi.create.foundation.damageTypes;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.Create;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;

public class DamageTypeTagGen extends TagsProvider<DamageType> {
	private final String namespace;

	public DamageTypeTagGen(String namespace, PackOutput pOutput,
		CompletableFuture<HolderLookup.Provider> pLookupProvider, ExistingFileHelper existingFileHelper) {
		super(pOutput, Registries.DAMAGE_TYPE, pLookupProvider, namespace, existingFileHelper);
		this.namespace = namespace;
	}

	public DamageTypeTagGen(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider,
		ExistingFileHelper existingFileHelper) {
		this(Create.ID, pOutput, pLookupProvider, existingFileHelper);
	}

	@Override
	protected void addTags(@NotNull Provider provider) {
		Multimap<TagKey<DamageType>, ResourceKey<DamageType>> tagsToTypes = HashMultimap.create();
		DamageTypeData.allInNamespace(namespace)
			.forEach(data -> data.tags.forEach(tag -> tagsToTypes.put(tag, data.key)));
		tagsToTypes.asMap()
			.forEach((tag, keys) -> {
				TagAppender<DamageType> appender = tag(tag);
				keys.forEach(key -> appender.addOptional(key.location())); // FIXME: 1.20 usind add() results in datagen failure, is this approach correct?
			});
	}
}
