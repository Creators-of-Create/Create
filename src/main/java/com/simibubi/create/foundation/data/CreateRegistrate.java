package com.simibubi.create.foundation.data;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.simibubi.create.content.contraptions.relays.encased.CasingConnectivity;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.render.ColoredVertexModel;
import com.simibubi.create.foundation.block.render.IBlockVertexColor;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.NonNullLazyValue;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
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

	public <T extends TileEntity> CreateTileEntityBuilder<T, CreateRegistrate> tileEntity(String name,
		NonNullFunction<TileEntityType<T>, ? extends T> factory) {
		return this.tileEntity(this.self(), name, factory);
	}

	@Override
	public <T extends TileEntity, P> CreateTileEntityBuilder<T, P> tileEntity(P parent, String name,
		NonNullFunction<TileEntityType<T>, ? extends T> factory) {
		return (CreateTileEntityBuilder<T, P>) this.entry(name, (callback) -> {
			return CreateTileEntityBuilder.create(this, parent, name, callback, factory);
		});
	}

	@Override
	public <T extends Entity> CreateEntityBuilder<T, CreateRegistrate> entity(String name,
		EntityType.IFactory<T> factory, EntityClassification classification) {
		return this.entity(self(), name, factory, classification);
	}

	public <T extends Entity, P> CreateEntityBuilder<T, P> entity(P parent, String name, EntityType.IFactory<T> factory,
		EntityClassification classification) {
		return (CreateEntityBuilder<T, P>) this.entry(name, (callback) -> {
			return CreateEntityBuilder.create(this, parent, name, callback, factory, classification);
		});
	}

	/* Palettes */

	public <T extends Block> BlockBuilder<T, CreateRegistrate> paletteStoneBlock(String name,
		NonNullFunction<Properties, T> factory, NonNullSupplier<Block> propertiesFrom, boolean worldGenStone) {
		BlockBuilder<T, CreateRegistrate> builder = super.block(name, factory).initialProperties(propertiesFrom)
			.blockstate((c, p) -> {
				final String location = "block/palettes/" + c.getName() + "/plain";
				p.simpleBlock(c.get(), p.models()
					.cubeAll(c.getName(), p.modLoc(location)));
			})
			.tag(Tags.Blocks.STONE)
			.item()
			.tag(Tags.Items.STONE)
			.build();
		if (worldGenStone) {
			builder.tag(BlockTags.BASE_STONE_OVERWORLD, AllTags.AllBlockTags.WG_STONE.tag);
		}
		return builder;
	}

	public BlockBuilder<Block, CreateRegistrate> paletteStoneBlock(String name, NonNullSupplier<Block> propertiesFrom,
		boolean worldGenStone) {
		return paletteStoneBlock(name, Block::new, propertiesFrom, worldGenStone);
	}

	/* Fluids */

	public <T extends ForgeFlowingFluid> FluidBuilder<T, CreateRegistrate> virtualFluid(String name,
		BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory,
		NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
		return entry(name,
			c -> new VirtualFluidBuilder<>(self(), self(), name, c, Create.asResource("fluid/" + name + "_still"),
				Create.asResource("fluid/" + name + "_flow"), attributesFactory, factory));
	}

	public FluidBuilder<VirtualFluid, CreateRegistrate> virtualFluid(String name) {
		return entry(name,
			c -> new VirtualFluidBuilder<>(self(), self(), name, c, Create.asResource("fluid/" + name + "_still"),
				Create.asResource("fluid/" + name + "_flow"), null, VirtualFluid::new));
	}

	public FluidBuilder<ForgeFlowingFluid.Flowing, CreateRegistrate> standardFluid(String name) {
		return fluid(name, Create.asResource("fluid/" + name + "_still"), Create.asResource("fluid/" + name + "_flow"));
	}

	public FluidBuilder<ForgeFlowingFluid.Flowing, CreateRegistrate> standardFluid(String name,
		NonNullBiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
		return fluid(name, Create.asResource("fluid/" + name + "_still"), Create.asResource("fluid/" + name + "_flow"),
			attributesFactory);
	}

	/* Util */

	public static <T extends Block> NonNullConsumer<? super T> connectedTextures(ConnectedTextureBehaviour behavior) {
		return entry -> onClient(() -> () -> registerCTBehviour(entry, behavior));
	}

	public static <T extends Block> NonNullConsumer<? super T> casingConnectivity(
		BiConsumer<T, CasingConnectivity> consumer) {
		return entry -> onClient(() -> () -> registerCasingConnectivity(entry, consumer));
	}

	public static <T extends Block> NonNullConsumer<? super T> blockVertexColors(IBlockVertexColor colorFunc) {
		return entry -> onClient(() -> () -> registerBlockVertexColor(entry, colorFunc));
	}

	public static <T extends Block> NonNullConsumer<? super T> blockModel(
		Supplier<NonNullFunction<IBakedModel, ? extends IBakedModel>> func) {
		return entry -> onClient(() -> () -> registerBlockModel(entry, func));
	}

	public static <T extends Item> NonNullConsumer<? super T> itemModel(
		Supplier<NonNullFunction<IBakedModel, ? extends IBakedModel>> func) {
		return entry -> onClient(() -> () -> registerItemModel(entry, func));
	}

	public static <T extends Item, P> NonNullUnaryOperator<ItemBuilder<T, P>> customRenderedItem(
		Supplier<Supplier<CustomRenderedItemModelRenderer<?>>> supplier) {
		return b -> {
			onClient(() -> () -> customRenderedItem(b, supplier));
			return b;
		};
	}

	protected static void onClient(Supplier<Runnable> toRun) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, toRun);
	}

	@OnlyIn(Dist.CLIENT)
	private static <T extends Item, P> void customRenderedItem(ItemBuilder<T, P> b,
		Supplier<Supplier<CustomRenderedItemModelRenderer<?>>> supplier) {
		b.properties(p -> p.setISTER(() -> supplier.get()::get))
			.onRegister(entry -> {
				ItemStackTileEntityRenderer ister = entry.getItemStackTileEntityRenderer();
				if (ister instanceof CustomRenderedItemModelRenderer)
					registerCustomRenderedItem(entry, (CustomRenderedItemModelRenderer<?>) ister);
			});
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerCTBehviour(Block entry, ConnectedTextureBehaviour behavior) {
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(entry.delegate, model -> new CTModel(model, behavior));
	}

	@OnlyIn(Dist.CLIENT)
	private static <T extends Block> void registerCasingConnectivity(T entry,
		BiConsumer<T, CasingConnectivity> consumer) {
		consumer.accept(entry, CreateClient.CASING_CONNECTIVITY);
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerBlockVertexColor(Block entry, IBlockVertexColor colorFunc) {
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(entry.delegate, model -> new ColoredVertexModel(model, colorFunc));
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerBlockModel(Block entry,
		Supplier<NonNullFunction<IBakedModel, ? extends IBakedModel>> func) {
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(entry.delegate, func.get());
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerItemModel(Item entry,
		Supplier<NonNullFunction<IBakedModel, ? extends IBakedModel>> func) {
		CreateClient.MODEL_SWAPPER.getCustomItemModels()
			.register(entry.delegate, func.get());
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerCustomRenderedItem(Item entry, CustomRenderedItemModelRenderer<?> renderer) {
		CreateClient.MODEL_SWAPPER.getCustomRenderedItems()
			.register(entry.delegate, renderer::createModel);
	}

}
