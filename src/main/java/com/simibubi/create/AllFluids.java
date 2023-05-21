package com.simibubi.create;

import static com.simibubi.create.Create.REGISTRATE;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid.PotionFluidType;
import com.tterrag.registrate.util.entry.FluidEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidInteractionRegistry.InteractionInformation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class AllFluids {

	public static final FluidEntry<PotionFluid> POTION =
		REGISTRATE.virtualFluid("potion", PotionFluidType::new, PotionFluid::new)
			.lang("Potion")
			.register();

	public static final FluidEntry<VirtualFluid> TEA = REGISTRATE.virtualFluid("tea")
		.lang("Builder's Tea")
		.tag(AllTags.forgeFluidTag("tea"))
		.register();

	public static final FluidEntry<ForgeFlowingFluid.Flowing> HONEY =
		REGISTRATE.standardFluid("honey", NoColorFluidAttributes::new)
			.lang("Honey")
			.properties(b -> b.viscosity(2000)
				.density(1400))
			.fluidProperties(p -> p.levelDecreasePerBlock(2)
				.tickRate(25)
				.slopeFindDistance(3)
				.explosionResistance(100f))
			.tag(AllFluidTags.HONEY.tag)
			.source(ForgeFlowingFluid.Source::new) // TODO: remove when Registrate fixes FluidBuilder
			.bucket()
			.tag(AllTags.forgeItemTag("buckets/honey"))
			.build()
			.register();

	public static final FluidEntry<ForgeFlowingFluid.Flowing> CHOCOLATE =
		REGISTRATE.standardFluid("chocolate", NoColorFluidAttributes::new)
			.lang("Chocolate")
			.tag(AllTags.forgeFluidTag("chocolate"))
			.properties(b -> b.viscosity(1500)
				.density(1400))
			.fluidProperties(p -> p.levelDecreasePerBlock(2)
				.tickRate(25)
				.slopeFindDistance(3)
				.explosionResistance(100f))
			.register();

	// Load this class

	public static void register() {}

	public static void registerFluidInteractions() {
		FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new InteractionInformation(
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

		FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new InteractionInformation(
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

	public static abstract class TintedFluidType extends FluidType {

		protected static final int NO_TINT = 0xffffffff;
		private ResourceLocation stillTexture;
		private ResourceLocation flowingTexture;

		public TintedFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
			super(properties);
			this.stillTexture = stillTexture;
			this.flowingTexture = flowingTexture;
		}

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

				@Override
				public int getTintColor(FluidStack stack) {
					return TintedFluidType.this.getTintColor(stack);
				}

				@Override
				public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
					return TintedFluidType.this.getTintColor(state, getter, pos);
				}

			});
		}

		protected abstract int getTintColor(FluidStack stack);

		protected abstract int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos);

	}

	/**
	 * Removing alpha from tint prevents optifine from forcibly applying biome
	 * colors to modded fluids (Makes translucent fluids disappear)
	 */
	private static class NoColorFluidAttributes extends TintedFluidType {

		public NoColorFluidAttributes(Properties properties, ResourceLocation stillTexture,
			ResourceLocation flowingTexture) {
			super(properties, stillTexture, flowingTexture);
		}

		@Override
		protected int getTintColor(FluidStack stack) {
			return NO_TINT;
		}

		@Override
		public int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
			return 0x00ffffff;
		}

	}

}
