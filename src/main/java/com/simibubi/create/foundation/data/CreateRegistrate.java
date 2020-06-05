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
import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.NonNullLazyValue;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Properties;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.Item;
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
		NonNullFunction<Properties, T> factory, NonNullSupplier<Block> propertiesFrom, boolean TFworldGen) {
		return super.block(name, factory).initialProperties(propertiesFrom)
			.blockstate((c, p) -> {
				final String location = "block/palettes/" + c.getName() + "/plain";
				p.simpleBlock(c.get(), p.models()
					.cubeAll(c.getName(), p.modLoc(location)));
				// TODO tag with forge:stone; if TFWorldGen == true tag with forge:wg_stone aswell
			})
			.simpleItem();
	}

	public static <T extends Block> NonNullConsumer<? super T> connectedTextures(ConnectedTextureBehaviour behavior) {
		return entry -> onClient(() -> () -> registerCTBehviour(entry, behavior));
	}

	public static <T extends Block> NonNullConsumer<? super T> blockColors(Supplier<Supplier<IBlockColor>> colorFunc) {
		return entry -> onClient(() -> () -> registerBlockColor(entry, colorFunc));
	}

	public static <T extends Block> NonNullConsumer<? super T> blockVertexColors(IBlockVertexColor colorFunc) {
		return entry -> onClient(() -> () -> registerBlockVertexColor(entry, colorFunc));
	}

	public static <T extends Item> NonNullConsumer<? super T> itemModel(
		Supplier<NonNullFunction<IBakedModel, ? extends IBakedModel>> func) {
		return entry -> onClient(() -> () -> registerItemModel(entry, func));
	}

	public static <T extends Item> NonNullConsumer<? super T> itemColors(Supplier<Supplier<IItemColor>> colorFunc) {
		return entry -> onClient(() -> () -> registerItemColor(entry, colorFunc));
	}

	public static <T extends Item, P> NonNullUnaryOperator<ItemBuilder<T, P>> customRenderedItem(
		Supplier<NonNullFunction<IBakedModel, ? extends CustomRenderedItemModel>> func) {
		return b -> b.properties(p -> p.setISTER(() -> () -> func.get()
			.apply(null)
			.createRenderer()))
			.onRegister(entry -> onClient(() -> () -> registerCustomRenderedItem(entry, func)));
	}

	protected static void onClient(Supplier<Runnable> toRun) {
		DistExecutor.runWhenOn(Dist.CLIENT, toRun);
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerCTBehviour(Block entry, ConnectedTextureBehaviour behavior) {
		CreateClient.getCustomBlockModels()
			.register(entry.delegate, model -> new CTModel(model, behavior));
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerItemModel(Item entry,
		Supplier<NonNullFunction<IBakedModel, ? extends IBakedModel>> func) {
		CreateClient.getCustomItemModels()
			.register(entry.delegate, func.get());
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerCustomRenderedItem(Item entry,
		Supplier<NonNullFunction<IBakedModel, ? extends CustomRenderedItemModel>> func) {
		CreateClient.getCustomRenderedItems()
			.register(entry.delegate, func.get());
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
