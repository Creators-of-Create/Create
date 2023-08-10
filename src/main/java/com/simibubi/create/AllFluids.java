package com.simibubi.create;

import static com.simibubi.create.Create.REGISTRATE;

import javax.annotation.Nullable;

import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid.PotionFluidAttributes;
import com.tterrag.registrate.util.entry.FluidEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class AllFluids {

	public static final FluidEntry<PotionFluid> POTION =
			REGISTRATE.virtualFluid("potion", PotionFluidAttributes::new, PotionFluid::new)
					.lang("Potion")
					.register();

	// TODO: Look for better tag than juice/apple
	public static final FluidEntry<VirtualFluid> APPLE_JUICE = REGISTRATE.virtualFluid("apple_juice")
			.lang("Apple Juice")
			.tag(AllTags.forgeFluidTag("juice/apple"))
			.register();

	public static final FluidEntry<VirtualFluid> TEA = REGISTRATE.virtualFluid("tea")
			.lang("Builder's Tea")
			.tag(AllTags.forgeFluidTag("tea"))
			.register();

	public static final FluidEntry<ForgeFlowingFluid.Flowing> HONEY =
			REGISTRATE.standardFluid("honey", NoColorFluidAttributes::new)
					.lang("Honey")
					.attributes(b -> b.viscosity(2000)
							.density(1400))
					.properties(p -> p.levelDecreasePerBlock(2)
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
					.attributes(b -> b.viscosity(1500)
							.density(1400))
					.properties(p -> p.levelDecreasePerBlock(2)
							.tickRate(25)
							.slopeFindDistance(3)
							.explosionResistance(100f))
					.register();

	// Load this class

	public static void register() {}

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

	/**
	 * Removing alpha from tint prevents optifine from forcibly applying biome
	 * colors to modded fluids (Makes translucent fluids disappear)
	 */
	private static class NoColorFluidAttributes extends FluidAttributes {

		protected NoColorFluidAttributes(Builder builder, Fluid fluid) {
			super(builder, fluid);
		}

		@Override
		public int getColor(BlockAndTintGetter world, BlockPos pos) {
			return 0x00ffffff;
		}

	}

}
