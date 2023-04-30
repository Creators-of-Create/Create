package com.simibubi.create.foundation.data;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.simibubi.create.content.contraptions.relays.encased.CasingConnectivity;
import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.utility.CreateRegistry;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;

public class CreateRegistrate extends AbstractRegistrate<CreateRegistrate> {
	protected CreateRegistrate(String modid) {
		super(modid);
	}

	public static CreateRegistrate create(String modid) {
		return new CreateRegistrate(modid);
	}

	@Deprecated(forRemoval = true)
	public static NonNullSupplier<CreateRegistrate> lazy(String modid) {
		return NonNullSupplier
			.lazy(() -> new CreateRegistrate(modid).registerEventListeners(FMLJavaModLoadingContext.get()
				.getModEventBus()));
	}

	@Override
	public CreateRegistrate registerEventListeners(IEventBus bus) {
		return super.registerEventListeners(bus);
	}

	/* Section Tracking */

	protected static final Map<RegistryEntry<?>, AllSections> SECTION_LOOKUP = new IdentityHashMap<>();
	protected AllSections currentSection;

	public CreateRegistrate startSection(AllSections section) {
		this.currentSection = section;
		return this;
	}

	public AllSections currentSection() {
		return currentSection;
	}

	@Override
	protected <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> accept(String name,
		ResourceKey<? extends Registry<R>> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator,
		NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {
		RegistryEntry<T> ret = super.accept(name, type, builder, creator, entryFactory);
		SECTION_LOOKUP.put(ret, currentSection());
		return ret;
	}

	public void addToSection(RegistryEntry<?> entry, AllSections section) {
		SECTION_LOOKUP.put(entry, section);
	}

	public AllSections getSection(RegistryEntry<?> entry) {
		return SECTION_LOOKUP.getOrDefault(entry, AllSections.UNASSIGNED);
	}

	public AllSections getSection(IForgeRegistryEntry<?> entry) {
		for (Entry<RegistryEntry<?>, AllSections> mapEntry : SECTION_LOOKUP.entrySet()) {
			if (mapEntry.getKey().get() == entry) {
				return mapEntry.getValue();
			}
		}
		return AllSections.UNASSIGNED;
	}

	public <R extends IForgeRegistryEntry<R>> Collection<RegistryEntry<R>> getAll(AllSections section,
		ResourceKey<? extends Registry<R>> registryType) {
		return this.getAll(registryType)
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
		return (CreateTileEntityBuilder<T, P>) this.entry(name,
			(callback) -> CreateTileEntityBuilder.create(this, parent, name, callback, factory));
	}

	@Override
	public <T extends Entity> CreateEntityBuilder<T, CreateRegistrate> entity(String name,
		EntityType.EntityFactory<T> factory, MobCategory classification) {
		return this.entity(self(), name, factory, classification);
	}

	@Override
	public <T extends Entity, P> CreateEntityBuilder<T, P> entity(P parent, String name,
		EntityType.EntityFactory<T> factory, MobCategory classification) {
		return (CreateEntityBuilder<T, P>) this.entry(name, (callback) -> {
			return CreateEntityBuilder.create(this, parent, name, callback, factory, classification);
		});
	}

	/* Palettes */

	public <T extends Block> BlockBuilder<T, CreateRegistrate> paletteStoneBlock(String name,
		NonNullFunction<Properties, T> factory, NonNullSupplier<Block> propertiesFrom, boolean worldGenStone) {
		BlockBuilder<T, CreateRegistrate> builder = super.block(name, factory).initialProperties(propertiesFrom)
			.transform(pickaxeOnly())
			.blockstate((c, p) -> {
				final String location = "block/palettes/stone_types/" + c.getName();
				p.simpleBlock(c.get(), p.models()
					.cubeAll(c.getName(), p.modLoc(location)));
			})
			.tag(BlockTags.DRIPSTONE_REPLACEABLE)
			.tag(BlockTags.AZALEA_ROOT_REPLACEABLE)
			.tag(BlockTags.MOSS_REPLACEABLE)
			.tag(BlockTags.LUSH_GROUND_REPLACEABLE)
			.item()
			.build();
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

	public <T extends ForgeFlowingFluid> FluidBuilder<T, CreateRegistrate> virtualFluid(String name, ResourceLocation still, ResourceLocation flow,
																						BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory,
																						NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
		return entry(name,
				c -> new VirtualFluidBuilder<>(self(), self(), name, c, still,
						flow, attributesFactory, factory));
	}

	public FluidBuilder<VirtualFluid, CreateRegistrate> virtualFluid(String name) {
		return entry(name,
			c -> new VirtualFluidBuilder<>(self(), self(), name, c, Create.asResource("fluid/" + name + "_still"),
				Create.asResource("fluid/" + name + "_flow"), null, VirtualFluid::new));
	}

	public FluidBuilder<VirtualFluid, CreateRegistrate> virtualFluid(String name, ResourceLocation still, ResourceLocation flow) {
		return entry(name,
				c -> new VirtualFluidBuilder<>(self(), self(), name, c, still,
						flow, null, VirtualFluid::new));
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

	public static <T extends Block> NonNullConsumer<? super T> casingConnectivity(
		BiConsumer<T, CasingConnectivity> consumer) {
		return entry -> onClient(() -> () -> registerCasingConnectivity(entry, consumer));
	}

	public static <T extends Block> NonNullConsumer<? super T> blockModel(
		Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> func) {
		return entry -> onClient(() -> () -> registerBlockModel(entry, func));
	}

	public static <T extends Item> NonNullConsumer<? super T> itemModel(
		Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> func) {
		return entry -> onClient(() -> () -> registerItemModel(entry, func));
	}

	public static <T extends Block> NonNullConsumer<? super T> connectedTextures(
		Supplier<ConnectedTextureBehaviour> behavior) {
		return entry -> onClient(() -> () -> registerCTBehviour(entry, behavior));
	}

	protected static void onClient(Supplier<Runnable> toRun) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, toRun);
	}

	@OnlyIn(Dist.CLIENT)
	private static <T extends Block> void registerCasingConnectivity(T entry,
		BiConsumer<T, CasingConnectivity> consumer) {
		consumer.accept(entry, CreateClient.CASING_CONNECTIVITY);
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerBlockModel(Block entry,
		Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> func) {
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(RegisteredObjects.getKeyOrThrow(entry), func.get());
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerItemModel(Item entry,
		Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> func) {
		CreateClient.MODEL_SWAPPER.getCustomItemModels()
			.register(RegisteredObjects.getKeyOrThrow(entry), func.get());
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerCTBehviour(Block entry, Supplier<ConnectedTextureBehaviour> behaviorSupplier) {
		ConnectedTextureBehaviour behavior = behaviorSupplier.get();
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(RegisteredObjects.getKeyOrThrow(entry), model -> new CTModel(model, behavior));
	}
}
