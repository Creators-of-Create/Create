package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidContentsAttribute implements ItemAttribute {
	private @Nullable Fluid fluid;

	public FluidContentsAttribute(@Nullable Fluid fluid) {
		this.fluid = fluid;
	}

	private static List<Fluid> extractFluids(ItemStack stack) {
		List<Fluid> fluids = new ArrayList<>();

		LazyOptional<IFluidHandlerItem> capability =
				stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);

		capability.ifPresent((cap) -> {
			for (int i = 0; i < cap.getTanks(); i++) {
				fluids.add(cap.getFluidInTank(i).getFluid());
			}
		});

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
		return AllItemAttributeTypes.HAS_FLUID.get();
	}

	@Override
	public void save(CompoundTag nbt) {
		if (fluid == null)
			return;
		ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
		if (id == null)
			return;
		nbt.putString("id", id.toString());
	}

	@Override
	public void load(CompoundTag nbt) {
		if (nbt.contains("id")) {
			fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.tryParse(nbt.getString("id")));
		}
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
	}
}
