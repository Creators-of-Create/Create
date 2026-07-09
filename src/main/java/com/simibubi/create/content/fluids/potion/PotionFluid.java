package com.simibubi.create.content.fluids.potion;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllFluids.TintedFluidType;
import com.simibubi.create.content.fluids.VirtualFluid;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.api.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.FluidState;

import net.neoforged.neoforge.fluids.FluidStack;

public class PotionFluid extends VirtualFluid {

	public static PotionFluid createSource(Properties properties) {
		return new PotionFluid(properties, true);
	}

	public static PotionFluid createFlowing(Properties properties) {
		return new PotionFluid(properties, false);
	}

	public PotionFluid(Properties properties, boolean source) {
		super(properties, source);
	}

	public static FluidStack of(int amount, PotionContents potionContents, BottleType bottleType) {
		FluidStack fluidStack;
		fluidStack = new FluidStack(AllFluids.POTION.get().getSource(), amount);
		addPotionToFluidStack(fluidStack, potionContents);
		fluidStack.set(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, bottleType);
		return fluidStack;
	}

	public static FluidStack addPotionToFluidStack(FluidStack fs, PotionContents potionContents) {
		if (potionContents == PotionContents.EMPTY) {
			fs.remove(DataComponents.POTION_CONTENTS);
			return fs;
		}
		fs.set(DataComponents.POTION_CONTENTS, potionContents);
		return fs;
	}

	public enum BottleType implements StringRepresentable {
		REGULAR, SPLASH, LINGERING;

		public static final Codec<BottleType> CODEC = StringRepresentable.fromEnum(BottleType::values);
		public static final StreamCodec<ByteBuf, BottleType> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(BottleType.class);

		@Override
		public @NotNull String getSerializedName() {
			return Lang.asId(name());
		}
	}

	public static class PotionFluidType extends TintedFluidType {

		public PotionFluidType(net.neoforged.neoforge.fluids.FluidType.Properties properties) {
			super(properties);
		}

		@Override
		public int getTintColor(FluidStack stack) {
			return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor() | 0xff000000;
		}

		@Override
		public String getDescriptionId(FluidStack stack) {
			PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
			ItemLike itemFromBottleType =
				PotionFluidHandler.itemFromBottleType(stack.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR));
			String suffix = contents.customName()
				.or(() -> contents.potion().map(potion -> potion.value().name()))
				.orElse("empty");
			return itemFromBottleType.asItem().getDescriptionId() + ".effect." + suffix;
		}

		@Override
		public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
			return NO_TINT;
		}

	}

}
