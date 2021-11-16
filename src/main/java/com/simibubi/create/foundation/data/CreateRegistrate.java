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
import com.simibubi.create.lib.data.Tags;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.fabric.EnvExecutor;
import com.tterrag.registrate.fabric.RegistryObject;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.util.NonNullLazyValue;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;

public class CreateRegistrate extends AbstractRegistrate<CreateRegistrate> {

	public interface BlockEntityFactory<T extends BlockEntity> {

		public T create(BlockEntityType<T> type, BlockPos pos, BlockState state);

		default T createWithReversedParams(BlockPos pos, BlockState state, BlockEntityType<T> type) {
			return create(type, pos, state);
		}

	}

	protected CreateRegistrate(String modid) {
		super(modid);
	}

	public static NonNullLazyValue<CreateRegistrate> lazy(String modid) {
		return new NonNullLazyValue<>(
			() -> new CreateRegistrate(modid));
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
	protected <R, T extends R> RegistryEntry<T> accept(String name,
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

	public AllSections getSection(Object entry) {
		return sectionLookup.entrySet()
			.stream()
			.filter(e -> e.getKey()
				.get() == entry)
			.map(Entry::getValue)
			.findFirst()
			.orElse(AllSections.UNASSIGNED);
	}

	public <R> Collection<RegistryEntry<R>> getAll(AllSections section,
		Class<? super R> registryType) {
		return this.<R>getAll(registryType)
			.stream()
			.filter(e -> getSection(e) == section)
			.collect(Collectors.toList());
	}

	public <T extends BlockEntity> CreateTileEntityBuilder<T, CreateRegistrate> tileEntity(String name,
		BlockEntityFactory<T> factory) {
		return this.tileEntity(this.self(), name, factory);
	}

	public <T extends BlockEntity, P> CreateTileEntityBuilder<T, P> tileEntity(P parent, String name,
		BlockEntityFactory<T> factory) {
		return (CreateTileEntityBuilder<T, P>) this.entry(name, (callback) -> {
			return CreateTileEntityBuilder.create(this, parent, name, callback, factory::createWithReversedParams);
		});
	}

	@Override
	public <T extends Entity> CreateEntityBuilder<T, FabricEntityTypeBuilder<T>, CreateRegistrate> entity(String name,
																									   EntityType.EntityFactory<T> factory, MobCategory classification) {
		return this.entity(self(), name, factory, classification);
	}

	public <T extends Entity, P> CreateEntityBuilder<T, FabricEntityTypeBuilder<T>, P> entity(P parent, String name,
		EntityType.EntityFactory<T> factory, MobCategory classification) {
		return (CreateEntityBuilder<T, P>) this.entry(name, (callback) -> {
			return CreateEntityBuilder.create(this, parent, name, callback, factory, classification);
		});
	}

	/* Palettes */

	public <T extends Block> BlockBuilder<T, CreateRegistrate> paletteStoneBlock(String name,
		NonNullFunction<FabricBlockSettings, T> factory, NonNullSupplier<Block> propertiesFrom, boolean worldGenStone) {
		BlockBuilder<T, CreateRegistrate> builder = super.block(name, factory).initialProperties(propertiesFrom)
//			.blockstate((c, p) -> {
//				final String location = "block/palettes/" + c.getName() + "/plain";
//				p.simpleBlock(c.get(), p.models()
//					.cubeAll(c.getName(), p.modLoc(location)));
//			})
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

	public <T extends SimpleFlowableFluid> FluidBuilder<T, CreateRegistrate> virtualFluid(String name,
//		BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory,
		NonNullFunction<SimpleFlowableFluid.Properties, T> factory) {
		return entry(name,
			c -> new VirtualFluidBuilder<>(self(), self(), name, c, Create.asResource("fluid/" + name + "_still"),
				Create.asResource("fluid/" + name + "_flow"), /*attributesFactory, */factory));
	}

	public FluidBuilder<VirtualFluid, CreateRegistrate> virtualFluid(String name) {
		return entry(name,
			c -> new VirtualFluidBuilder<>(self(), self(), name, c, Create.asResource("fluid/" + name + "_still"),
				Create.asResource("fluid/" + name + "_flow"), /*null, */VirtualFluid::new));
	}

	public FluidBuilder<SimpleFlowableFluid.Flowing, CreateRegistrate> standardFluid(String name) {
		return fluid(name, Create.asResource("fluid/" + name + "_still"), Create.asResource("fluid/" + name + "_flow"));
	}
/*
	public FluidBuilder<SimpleFlowableFluid.Flowing, CreateRegistrate> standardFluid(String name,
		NonNullBiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory) {
		return fluid(name, Create.asResource("fluid/" + name + "_still"), Create.asResource("fluid/" + name + "_flow"),
			attributesFactory);
	}
*/
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
		Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> func) {
		return entry -> onClient(() -> () -> registerBlockModel(entry, func));
	}

	public static <T extends Item> NonNullConsumer<? super T> itemModel(
		Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> func) {
		return entry -> onClient(() -> () -> registerItemModel(entry, func));
	}

	protected static void onClient(Supplier<Runnable> toRun) {
		EnvExecutor.runWhenOn(EnvType.CLIENT, toRun);
	}

	@Environment(EnvType.CLIENT)
	private static void registerCTBehviour(Block entry, ConnectedTextureBehaviour behavior) {
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(entry/*.delegate*/, model -> new CTModel(model, behavior));
	}

	@Environment(EnvType.CLIENT)
	private static <T extends Block> void registerCasingConnectivity(T entry,
		BiConsumer<T, CasingConnectivity> consumer) {
		consumer.accept(entry, CreateClient.CASING_CONNECTIVITY);
	}

	@Environment(EnvType.CLIENT)
	private static void registerBlockVertexColor(Block entry, IBlockVertexColor colorFunc) {
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(() -> entry/*.delegate*/, model -> new ColoredVertexModel(model, colorFunc));
	}

	@Environment(EnvType.CLIENT)
	private static void registerBlockModel(Block entry,
		Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> func) {
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(() -> entry/*.delegate*/, func.get());
	}

	@Environment(EnvType.CLIENT)
	private static void registerItemModel(Item entry,
		Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> func) {
		CreateClient.MODEL_SWAPPER.getCustomItemModels()
			.register(() -> entry/*.delegate*/, func.get());
	}

}
