package com.simibubi.create;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid.PotionFluidType;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.tterrag.registrate.builders.FluidBuilder.FluidTypeFactory;
import com.tterrag.registrate.util.entry.FluidEntry;

import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry.InteractionInformation;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class AllFluids {
	private static final CreateRegistrate REGISTRATE = Create.registrate();

	static {
		REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB);
	}

	private static final Identifier POTION_STILL_TEXTURE = Identifier.fromNamespaceAndPath(Create.ID, "fluid/potion_still");
	private static final Identifier POTION_FLOW_TEXTURE = Identifier.fromNamespaceAndPath(Create.ID, "fluid/potion_flow");

	public static final FluidEntry<PotionFluid> POTION =
		REGISTRATE.virtualFluid("potion", POTION_STILL_TEXTURE, POTION_FLOW_TEXTURE,
				PotionFluidType::new,
				PotionFluid::createSource, PotionFluid::createFlowing)
			.lang("Potion")
			.register();

	public static final FluidEntry<VirtualFluid> TEA = REGISTRATE.virtualFluid("tea")
		.lang("Builder's Tea")
		.tag(AllFluidTags.TEA.tag)
		.register();

	public static final FluidEntry<BaseFlowingFluid.Flowing> HONEY =
		REGISTRATE.standardFluid("honey",
				SolidRenderedPlaceableFluidType.create(0xEAAE2F,
					() -> 1f / 8f * AllConfigs.client().honeyTransparencyMultiplier.getF()))
			.lang("Honey")
			.properties(b -> b.viscosity(2000)
				.density(1400))
			.fluidProperties(p -> p.levelDecreasePerBlock(2)
				.tickRate(25)
				.slopeFindDistance(3)
				.explosionResistance(100f))
			.tag(Tags.Fluids.HONEY)
			.source(BaseFlowingFluid.Source::new) // TODO: remove when Registrate fixes FluidBuilder
			.block()
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
			.build()
			.bucket()
			.onRegister(AllFluids::registerFluidDispenseBehavior)
			.tag(Tags.Items.BUCKETS, AllItemTags.HONEY_BUCKETS.tag)
			.build()
			.register();

	public static final FluidEntry<BaseFlowingFluid.Flowing> CHOCOLATE =
		REGISTRATE.standardFluid("chocolate",
				SolidRenderedPlaceableFluidType.create(0x622020,
					() -> 1f / 32f * AllConfigs.client().chocolateTransparencyMultiplier.getF()))
			.lang("Chocolate")
			.tag(AllFluidTags.CHOCOLATE.tag)
			.properties(b -> b.viscosity(1500)
				.density(1400))
			.fluidProperties(p -> p.levelDecreasePerBlock(2)
				.tickRate(25)
				.slopeFindDistance(3)
				.explosionResistance(100f))
			.source(BaseFlowingFluid.Source::new)
			.block()
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
			.build()
			.bucket()
			.onRegister(AllFluids::registerFluidDispenseBehavior)
			.tag(Tags.Items.BUCKETS, AllItemTags.CHOCOLATE_BUCKETS.tag)
			.build()
			.register();

	// Load this class

	public static void register() {
	}

	public static void registerFluidInteractions() {
		FluidInteractionRegistry.addInteraction(NeoForgeMod.LAVA_TYPE.value(), new InteractionInformation(
			HONEY.get().getFluidType(),
			fluidState -> {
				if (fluidState.isSource()) {
					return Blocks.OBSIDIAN.defaultBlockState();
				} else {
					return AllPaletteStoneTypes.LIMESTONE.getBaseBlock()
						.get()
						.defaultBlockState();
				}
			}
		));

		FluidInteractionRegistry.addInteraction(NeoForgeMod.LAVA_TYPE.value(), new InteractionInformation(
			CHOCOLATE.get().getFluidType(),
			fluidState -> {
				if (fluidState.isSource()) {
					return Blocks.OBSIDIAN.defaultBlockState();
				} else {
					return AllPaletteStoneTypes.SCORIA.getBaseBlock()
						.get()
						.defaultBlockState();
				}
			}
		));
	}

	@Nullable
	public static BlockState getLavaInteraction(FluidState fluidState) {
		Fluid fluid = fluidState.getType();
		if (fluid.isSame(HONEY.get()))
			return AllPaletteStoneTypes.LIMESTONE.getBaseBlock()
				.get()
				.defaultBlockState();
		if (fluid.isSame(CHOCOLATE.get()))
			return AllPaletteStoneTypes.SCORIA.getBaseBlock()
				.get()
				.defaultBlockState();
		return null;
	}

	private static final DispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();
	private static final DispenseItemBehavior DISPENSE_FLUID = new DefaultDispenseItemBehavior(){
			@Override
			protected ItemStack execute(BlockSource pSource, ItemStack pStack) {
				DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem) pStack.getItem();
				BlockPos pos = pSource.pos().relative(pSource.state().getValue(DispenserBlock.FACING));
				Level level = pSource.level();
				if (dispensibleContainerItem.emptyContents(null, level, pos, null, pStack)) {
					return new ItemStack(Items.BUCKET);
				}
				return DEFAULT.dispense(pSource, pStack);
			}
		};

	private static void registerFluidDispenseBehavior(BucketItem bucket) {
		DispenserBlock.registerBehavior(bucket, DISPENSE_FLUID);
	}

	public static abstract class TintedFluidType extends FluidType {

		protected static final int NO_TINT = 0xffffffff;

		public TintedFluidType(Properties properties) {
			super(properties);
		}

		public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
			consumer.accept(new IClientFluidTypeExtensions() {

				@Override
				public void modifyFogColor(Camera camera, float partialTick, ClientLevel level,
										   int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
					Vector3f customFogColor = TintedFluidType.this.getCustomFogColor();
					if (customFogColor != null)
						fluidFogColor.set(customFogColor.x(), customFogColor.y(), customFogColor.z(), fluidFogColor.w());
				}

				@Override
				public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance,
											float partialTick, FogData fogData) {
					float modifier = TintedFluidType.this.getFogDistanceModifier();
					float baseWaterFog = 96.0f;
					if (modifier != 1f) {
						fogData.environmentalStart = -8;
						fogData.environmentalEnd = baseWaterFog * modifier;
						fogData.skyEnd = fogData.environmentalEnd;
						fogData.cloudEnd = fogData.environmentalEnd;
					}
				}

			});
		}

		public abstract int getTintColor(FluidStack stack);

		public abstract int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos);

		protected Vector3f getCustomFogColor() {
			return null;
		}

		protected float getFogDistanceModifier() {
			return 1f;
		}

	}

	private static class SolidRenderedPlaceableFluidType extends TintedFluidType {

		private Vector3f fogColor;
		private Supplier<Float> fogDistance;

		public static FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
			return properties -> {
				SolidRenderedPlaceableFluidType fluidType = new SolidRenderedPlaceableFluidType(properties);
				fluidType.fogColor = new Color(fogColor, false).asVectorF();
				fluidType.fogDistance = fogDistance;
				return fluidType;
			};
		}

		private SolidRenderedPlaceableFluidType(Properties properties) {
			super(properties);
		}

		@Override
		public int getTintColor(FluidStack stack) {
			return NO_TINT;
		}

		/*
		 * Removing alpha from tint prevents optifine from forcibly applying biome
		 * colors to modded fluids (this workaround only works for fluids in the solid
		 * render layer)
		 */
		@Override
		public int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
			return 0x00ffffff;
		}

		@Override
		protected Vector3f getCustomFogColor() {
			return fogColor;
		}

		@Override
		protected float getFogDistanceModifier() {
			return fogDistance.get();
		}

	}

}
