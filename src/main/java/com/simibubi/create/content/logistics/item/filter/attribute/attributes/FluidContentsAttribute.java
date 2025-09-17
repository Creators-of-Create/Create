package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public record FluidContentsAttribute(@Nullable Fluid fluid) implements ItemAttribute {
	public static final MapCodec<FluidContentsAttribute> CODEC = BuiltInRegistries.FLUID.byNameCodec()
			.xmap(FluidContentsAttribute::new, FluidContentsAttribute::fluid)
			.fieldOf("value");

	public static final StreamCodec<RegistryFriendlyByteBuf, FluidContentsAttribute> STREAM_CODEC = CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.FLUID)
		.map(FluidContentsAttribute::new, FluidContentsAttribute::fluid);

	private static List<Fluid> extractFluids(ItemStack stack) {
		List<Fluid> fluids = new ArrayList<>();

		IFluidHandlerItem capability = stack.getCapability(Capabilities.FluidHandler.ITEM);

		if (capability != null) {
			for (int i = 0; i < capability.getTanks(); i++) {
				fluids.add(capability.getFluidInTank(i).getFluid());
			}
		}

		return fluids;
	}

	@Override
	public boolean appliesTo(ItemStack itemStack, Level level) {
		return extractFluids(itemStack).contains(fluid);
	}

	@Override
	public String getTranslationKey() {
		return "has_fluid";
	}

	@Override
	public Object[] getTranslationParameters() {
		String parameter = "";
		if (fluid != null)
			parameter = fluid.getFluidType().getDescription().getString();
		return new Object[]{parameter};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.HAS_FLUID;
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new FluidContentsAttribute(null);
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			List<ItemAttribute> list = new ArrayList<>();

			for (Fluid fluid : extractFluids(stack)) {
				list.add(new FluidContentsAttribute(fluid));
			}

			return list;
		}

		@Override
		public MapCodec<? extends ItemAttribute> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
