package com.simibubi.create.foundation.registrate;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.modules.Sections;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.util.NonNullLazyValue;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Properties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class CreateRegistrate extends AbstractRegistrate<CreateRegistrate> {

	public static NonNullLazyValue<CreateRegistrate> lazy(String modid) {
		return new NonNullLazyValue<>(() -> new CreateRegistrate(modid)
				.registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus()));
	}

	protected CreateRegistrate(String modid) {
		super(modid);
	}
	
	/* Section Tracking */

	private static Map<RegistryEntry<?>, Sections> sectionLookup = new IdentityHashMap<>();
	private Sections section;

	public CreateRegistrate startSection(Sections section) {
		this.section = section;
		return this;
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
	
	public <R extends IForgeRegistryEntry<R>> Collection<RegistryEntry<R>> getAll(Sections section, Class<? super R> registryType) {
		return this.<R>getAll(registryType).stream()
				.filter(e -> getSection(e) == section)
				.collect(Collectors.toList());
	}
	
	/* Palettes */
	
	public <T extends Block> BlockBuilder<T, CreateRegistrate> baseBlock(String name,
		NonNullFunction<Properties, T> factory, NonNullSupplier<Block> propertiesFrom) {
		return super.block(name, factory).initialProperties(propertiesFrom)
			.blockstate((c, p) -> {
				final String location = "block/palettes/" + c.getName() + "/plain";
				p.simpleBlock(c.get(), p.models()
					.cubeAll(c.getName(), p.modLoc(location)));
			})
			.simpleItem();
	}

	public static <T extends Block> NonNullUnaryOperator<BlockBuilder<T, CreateRegistrate>> connectedTextures(
		ConnectedTextureBehaviour behavior) {
		return b -> b.onRegister(entry -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> registerModel(entry, behavior)));
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerModel(Block entry, ConnectedTextureBehaviour behavior) {
		CreateClient.getCustomBlockModels()
			.register(entry.delegate, model -> new CTModel(model, behavior));
	}
}
