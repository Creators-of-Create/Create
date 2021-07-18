package com.simibubi.create;

import javax.annotation.Nullable;

import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid.PotionFluidAttributes;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.FluidEntry;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class AllFluids {

	private static final CreateRegistrate REGISTRATE = Create.registrate();

	public static final FluidEntry<PotionFluid> POTION =
			REGISTRATE.virtualFluid("potion", PotionFluidAttributes::new, PotionFluid::new)
					.lang(f -> "fluid.create.potion", "Potion")
					.register();

	public static final FluidEntry<VirtualFluid> TEA = REGISTRATE.virtualFluid("tea")
			.lang(f -> "fluid.create.tea", "Builder's Tea")
			.tag(AllTags.forgeFluidTag("tea"))
			.register();

	public static final FluidEntry<VirtualFluid> MILK = REGISTRATE.virtualFluid("milk")
			.lang(f -> "fluid.create.milk", "Milk")
			.tag(Tags.Fluids.MILK)
			.register();

	public static final FluidEntry<ForgeFlowingFluid.Flowing> HONEY =
			REGISTRATE.standardFluid("honey", NoColorFluidAttributes::new)
					.lang(f -> "fluid.create.honey", "Honey")
					.attributes(b -> b.viscosity(500)
							.density(1400))
					.properties(p -> p.levelDecreasePerBlock(2)
							.tickRate(25)
							.slopeFindDistance(3)
							.explosionResistance(100f))
					.tag(AllFluidTags.HONEY.tag)
					.bucket()
					.properties(p -> p.stacksTo(1))
					.build()
					.register();

	public static final FluidEntry<ForgeFlowingFluid.Flowing> CHOCOLATE =
			REGISTRATE.standardFluid("chocolate", NoColorFluidAttributes::new)
					.lang(f -> "fluid.create.chocolate", "Chocolate")
					.tag(AllTags.forgeFluidTag("chocolate"))
					.attributes(b -> b.viscosity(500)
							.density(1400))
					.properties(p -> p.levelDecreasePerBlock(2)
							.tickRate(25)
							.slopeFindDistance(3)
							.explosionResistance(100f))
					.bucket()
			.properties(p -> p.stacksTo(1))
			.build()
			.register();

	// Load this class

	public static void register() {}

	@OnlyIn(Dist.CLIENT)
	public static void assignRenderLayers() {}

	@OnlyIn(Dist.CLIENT)
	private static void makeTranslucent(FluidEntry<?> entry) {
		ForgeFlowingFluid fluid = entry.get();
		RenderTypeLookup.setRenderLayer(fluid, RenderType.translucent());
		RenderTypeLookup.setRenderLayer(fluid.getSource(), RenderType.translucent());
	}

	@Nullable
	public static BlockState getLavaInteraction(FluidState fluidState) {
		Fluid fluid = fluidState.getType();
		if (fluid.isSame(HONEY.get()))
			return fluidState.isSource() ? AllPaletteBlocks.LIMESTONE.getDefaultState()
				: AllPaletteBlocks.LIMESTONE_VARIANTS.registeredBlocks.get(0)
					.getDefaultState();
		if (fluid.isSame(CHOCOLATE.get()))
			return fluidState.isSource() ? AllPaletteBlocks.SCORIA.getDefaultState()
				: AllPaletteBlocks.SCORIA_VARIANTS.registeredBlocks.get(0)
					.getDefaultState();
		return null;
	}

	/**
	 * Removing alpha from tint prevents optifine from forcibly applying biome
	 * colors to modded fluids (Makes translucent fluids disappear)
	 */
	private static class NoColorFluidAttributes extends FluidAttributes {

		protected NoColorFluidAttributes(Builder builder, Fluid fluid) {
			super(builder, fluid);
		}

		@Override
		public int getColor(IBlockDisplayReader world, BlockPos pos) {
			return 0x00ffffff;
		}

	}

}
