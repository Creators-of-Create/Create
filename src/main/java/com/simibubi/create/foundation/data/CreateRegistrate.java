package com.simibubi.create.foundation.data;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.foundation.block.IBlockVertexColor;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.NonNullLazyValue;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Properties;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.BlockItem;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class CreateRegistrate extends AbstractRegistrate<CreateRegistrate> {

	protected CreateRegistrate(String modid) {
		super(modid);
	}

	public static NonNullLazyValue<CreateRegistrate> lazy(String modid) {
		return new NonNullLazyValue<>(
			() -> new CreateRegistrate(modid).registerEventListeners(FMLJavaModLoadingContext.get()
				.getModEventBus()));
	}

	/* Section Tracking */

	private static Map<RegistryEntry<?>, AllSections> sectionLookup = new IdentityHashMap<>();
	private AllSections section;

	public CreateRegistrate startSection(AllSections section) {
		this.section = section;
		return this;
	}

	public AllSections currentSection() {
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

	public void addToSection(RegistryEntry<?> entry, AllSections section) {
		sectionLookup.put(entry, section);
	}

	public AllSections getSection(RegistryEntry<?> entry) {
		return sectionLookup.getOrDefault(entry, AllSections.UNASSIGNED);
	}

	public AllSections getSection(IForgeRegistryEntry<?> entry) {
		return sectionLookup.entrySet()
			.stream()
			.filter(e -> e.getKey()
				.get() == entry)
			.map(Entry::getValue)
			.findFirst()
			.orElse(AllSections.UNASSIGNED);
	}

	public <R extends IForgeRegistryEntry<R>> Collection<RegistryEntry<R>> getAll(AllSections section,
		Class<? super R> registryType) {
		return this.<R>getAll(registryType)
			.stream()
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
		return b -> b.onRegister(entry -> onClient(() -> () -> registerModel(entry, behavior)));
	}

	public static <T extends Block> NonNullUnaryOperator<BlockBuilder<T, CreateRegistrate>> blockColors(
		Supplier<Supplier<IBlockColor>> colorFunc) {
		return b -> b.onRegister(entry -> onClient(() -> () -> registerBlockColor(entry, colorFunc)));
	}

	public static <T extends Block> NonNullUnaryOperator<BlockBuilder<T, CreateRegistrate>> blockVertexColors(
		IBlockVertexColor colorFunc) {
		return b -> b.onRegister(entry -> onClient(() -> () -> registerBlockVertexColor(entry, colorFunc)));
	}

	public static <I extends BlockItem, P> NonNullFunction<ItemBuilder<I, P>, P> itemColors(
		Supplier<Supplier<IItemColor>> colorFunc) {
		return b -> b.onRegister(entry -> onClient(() -> () -> registerItemColor(entry, colorFunc)))
			.build();
	}

	protected static void onClient(Supplier<Runnable> toRun) {
		DistExecutor.runWhenOn(Dist.CLIENT, toRun);
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerModel(Block entry, ConnectedTextureBehaviour behavior) {
		CreateClient.getCustomBlockModels()
			.register(entry.delegate, model -> new CTModel(model, behavior));
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerBlockColor(Block entry, Supplier<Supplier<IBlockColor>> colorFunc) {
		CreateClient.getColorHandler()
			.register(entry, colorFunc.get()
				.get());
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerBlockVertexColor(Block entry, IBlockVertexColor colorFunc) {
		CreateClient.getColorHandler()
			.register(entry, colorFunc);
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerItemColor(IItemProvider entry, Supplier<Supplier<IItemColor>> colorFunc) {
		CreateClient.getColorHandler()
			.register(entry, colorFunc.get()
				.get());
	}

}
