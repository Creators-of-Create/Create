package com.simibubi.create.foundation.data;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.decoration.encasing.CasingConnectivity;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;

public class CreateRegistrate extends AbstractRegistrate<CreateRegistrate> {
	@Nullable
	protected Function<Item, TooltipModifier> currentTooltipModifierFactory;

	protected CreateRegistrate(String modid) {
		super(modid);
	}

	public static CreateRegistrate create(String modid) {
		return new CreateRegistrate(modid);
	}

	public CreateRegistrate setTooltipModifierFactory(@Nullable Function<Item, TooltipModifier> factory) {
		currentTooltipModifierFactory = factory;
		return self();
	}

	@Nullable
	public Function<Item, TooltipModifier> getTooltipModifierFactory() {
		return currentTooltipModifierFactory;
	}

	@Override
	public CreateRegistrate registerEventListeners(IEventBus bus) {
		return super.registerEventListeners(bus);
	}
	
	private static Map<RegistryEntry<?>, RegistryObject<CreativeModeTab>> tabLookup = new IdentityHashMap<>();
	private RegistryObject<CreativeModeTab> currentTab;

	public CreateRegistrate useCreativeTab(RegistryObject<CreativeModeTab> tab) {
		this.currentTab = tab;
		return this;
	}
	
	public boolean isInCreativeTab(RegistryEntry<?> entry, RegistryObject<CreativeModeTab> tab) {
		return tabLookup.get(entry) == tab;
	}

	@Override
	protected <R, T extends R> RegistryEntry<T> accept(String name, ResourceKey<? extends Registry<R>> type,
		Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator,
		NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {
		RegistryEntry<T> entry = super.accept(name, type, builder, creator, entryFactory);
		if (type.equals(Registries.ITEM)) {
			if (currentTooltipModifierFactory != null) {
				TooltipModifier.REGISTRY.registerDeferred(entry.getId(), currentTooltipModifierFactory);
			}
		}
		if (currentTab != null)
			tabLookup.put(entry, currentTab);
		return entry;
	}

	@Override
	public <T extends BlockEntity> CreateBlockEntityBuilder<T, CreateRegistrate> blockEntity(String name,
		BlockEntityFactory<T> factory) {
		return blockEntity(self(), name, factory);
	}

	@Override
	public <T extends BlockEntity, P> CreateBlockEntityBuilder<T, P> blockEntity(P parent, String name,
		BlockEntityFactory<T> factory) {
		return (CreateBlockEntityBuilder<T, P>) entry(name,
			(callback) -> CreateBlockEntityBuilder.create(this, parent, name, callback, factory));
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
		NonNullFunction<Properties, T> factory, NonNullSupplier<Block> propertiesFrom, boolean worldGenStone,
		boolean hasNaturalVariants) {
		BlockBuilder<T, CreateRegistrate> builder = super.block(name, factory).initialProperties(propertiesFrom)
			.transform(pickaxeOnly())
			.blockstate(hasNaturalVariants ? BlockStateGen.naturalStoneTypeBlock(name) : (c, p) -> {
				final String location = "block/palettes/stone_types/" + c.getName();
				p.simpleBlock(c.get(), p.models()
					.cubeAll(c.getName(), p.modLoc(location)));
			})
			.tag(BlockTags.DRIPSTONE_REPLACEABLE)
			.tag(BlockTags.AZALEA_ROOT_REPLACEABLE)
			.tag(BlockTags.MOSS_REPLACEABLE)
			.tag(BlockTags.LUSH_GROUND_REPLACEABLE)
			.item()
			.model((c, p) -> p.cubeAll(c.getName(),
				p.modLoc(hasNaturalVariants ? "block/palettes/stone_types/natural/" + name + "_1"
					: "block/palettes/stone_types/" + c.getName())))
			.build();
		return builder;
	}

	public BlockBuilder<Block, CreateRegistrate> paletteStoneBlock(String name, NonNullSupplier<Block> propertiesFrom,
		boolean worldGenStone, boolean hasNaturalVariants) {
		return paletteStoneBlock(name, Block::new, propertiesFrom, worldGenStone, hasNaturalVariants);
	}

	/* Fluids */

	public <T extends ForgeFlowingFluid> FluidBuilder<T, CreateRegistrate> virtualFluid(String name,
		FluidBuilder.FluidTypeFactory typeFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
		return entry(name,
			c -> new VirtualFluidBuilder<>(self(), self(), name, c, Create.asResource("fluid/" + name + "_still"),
				Create.asResource("fluid/" + name + "_flow"), typeFactory, factory));
	}

	public <T extends ForgeFlowingFluid> FluidBuilder<T, CreateRegistrate> virtualFluid(String name,
		ResourceLocation still, ResourceLocation flow, FluidBuilder.FluidTypeFactory typeFactory,
		NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
		return entry(name, c -> new VirtualFluidBuilder<>(self(), self(), name, c, still, flow, typeFactory, factory));
	}

	public FluidBuilder<VirtualFluid, CreateRegistrate> virtualFluid(String name) {
		return entry(name,
			c -> new VirtualFluidBuilder<VirtualFluid, CreateRegistrate>(self(), self(), name, c,
				Create.asResource("fluid/" + name + "_still"), Create.asResource("fluid/" + name + "_flow"),
				CreateRegistrate::defaultFluidType, VirtualFluid::new));
	}

	public FluidBuilder<VirtualFluid, CreateRegistrate> virtualFluid(String name, ResourceLocation still,
		ResourceLocation flow) {
		return entry(name, c -> new VirtualFluidBuilder<>(self(), self(), name, c, still, flow,
			CreateRegistrate::defaultFluidType, VirtualFluid::new));
	}

	public FluidBuilder<ForgeFlowingFluid.Flowing, CreateRegistrate> standardFluid(String name) {
		return fluid(name, Create.asResource("fluid/" + name + "_still"), Create.asResource("fluid/" + name + "_flow"));
	}

	public FluidBuilder<ForgeFlowingFluid.Flowing, CreateRegistrate> standardFluid(String name,
		FluidBuilder.FluidTypeFactory typeFactory) {
		return fluid(name, Create.asResource("fluid/" + name + "_still"), Create.asResource("fluid/" + name + "_flow"),
			typeFactory);
	}

	public static FluidType defaultFluidType(FluidType.Properties properties, ResourceLocation stillTexture,
		ResourceLocation flowingTexture) {
		return new FluidType(properties) {
			@Override
			public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
				consumer.accept(new IClientFluidTypeExtensions() {
					@Override
					public ResourceLocation getStillTexture() {
						return stillTexture;
					}

					@Override
					public ResourceLocation getFlowingTexture() {
						return flowingTexture;
					}
				});
			}
		};
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
			.register(CatnipServices.REGISTRIES.getKeyOrThrow(entry), func.get());
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerItemModel(Item entry,
		Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> func) {
		CreateClient.MODEL_SWAPPER.getCustomItemModels()
			.register(CatnipServices.REGISTRIES.getKeyOrThrow(entry), func.get());
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerCTBehviour(Block entry, Supplier<ConnectedTextureBehaviour> behaviorSupplier) {
		ConnectedTextureBehaviour behavior = behaviorSupplier.get();
		CreateClient.MODEL_SWAPPER.getCustomBlockModels()
			.register(CatnipServices.REGISTRIES.getKeyOrThrow(entry), model -> new CTModel(model, behavior));
	}
}
