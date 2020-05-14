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

import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class CreateRegistrateBase<S extends CreateRegistrateBase<S>> extends AbstractRegistrate<S> {

	protected CreateRegistrateBase(String modid, NonNullSupplier<ItemGroup> creativeTab) {
		super(modid);
		registerEventListeners(FMLJavaModLoadingContext.get()
			.getModEventBus());
		itemGroup(creativeTab);
	}
	
	/* Section Tracking */

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
			Class<? super R> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator,
			NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {
		RegistryEntry<T> ret = super.accept(name, type, builder, creator, entryFactory);
		sectionLookup.put(ret, currentSection());
		return ret;
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
