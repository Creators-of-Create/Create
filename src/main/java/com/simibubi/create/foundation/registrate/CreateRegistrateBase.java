package com.simibubi.create.foundation.registrate;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.simibubi.create.modules.Sections;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class CreateRegistrateBase<C extends AbstractRegistrate<C>> extends AbstractRegistrate<C> {

	protected CreateRegistrateBase(String modid, NonNullSupplier<ItemGroup> creativeTab) {
		super(modid);
		registerEventListeners(FMLJavaModLoadingContext.get()
			.getModEventBus());
		itemGroup(creativeTab);
	}

	private static Map<RegistryEntry<?>, Sections> sectionLookup = new IdentityHashMap<>();
	private Sections section;

	public void startSection(Sections section) {
		this.section = section;
	}

	public Sections currentSection() {
		return section;
	}

	@Override
	protected <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> accept(String name,
		Class<? super R> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator) {
		RegistryEntry<T> ret = super.accept(name, type, builder, creator);
		sectionLookup.put(ret, currentSection());
		return ret;
	}

	@SuppressWarnings("unchecked")
	public <T extends Block, P> CreateBlockBuilder<T, P> createBlock(String name,
		NonNullFunction<Block.Properties, T> factory) {
		return (CreateBlockBuilder<T, P>) super.block(name, factory);
	}

	@Override
	public <T extends Block, P> CreateBlockBuilder<T, P> block(P parent, String name,
		NonNullFunction<Block.Properties, T> factory, Material material) {
		return CreateBlockBuilder.create(this, parent, name, this::accept, factory, material);
	}

	public void addToSection(RegistryEntry<?> entry, Sections section) {
		sectionLookup.put(entry, section);
	}

	public Sections getSection(RegistryEntry<?> entry) {
		return sectionLookup.getOrDefault(entry, Sections.UNASSIGNED);
	}

	public Sections getSection(IForgeRegistryEntry<?> entry) {
		return sectionLookup.entrySet()
			.stream()
			.filter(e -> e.getKey()
				.get() == entry)
			.map(Entry::getValue)
			.findFirst()
			.orElse(Sections.UNASSIGNED);
	}

}
