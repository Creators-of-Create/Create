package com.simibubi.create.content.logistics.filter.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidContentsAttribute implements ItemAttribute {
	public static final FluidContentsAttribute EMPTY = new FluidContentsAttribute(null);

	private final Fluid fluid;

	public FluidContentsAttribute(@Nullable Fluid fluid) {
		this.fluid = fluid;
	}

	@Override
	public boolean appliesTo(ItemStack itemStack) {
		return extractFluids(itemStack).contains(fluid);
	}

	@Override
	public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
		return extractFluids(itemStack).stream().map(FluidContentsAttribute::new).collect(Collectors.toList());
	}

	@Override
	public String getTranslationKey() {
		return "has_fluid";
	}

	@Override
	public Object[] getTranslationParameters() {
		String parameter = "";
		if (fluid != null)
			parameter = Components.translatable(fluid.getAttributes().getTranslationKey()).getString();
		return new Object[] { parameter };
	}

	@Override
	public void writeNBT(CompoundTag nbt) {
		if (fluid == null)
			return;
		ResourceLocation id = fluid.getRegistryName();
		if (id == null)
			return;
		nbt.putString("id", id.toString());
	}

	@Override
	public ItemAttribute readNBT(CompoundTag nbt) {
		return nbt.contains("id") ? new FluidContentsAttribute(ForgeRegistries.FLUIDS.getValue(ResourceLocation.tryParse(nbt.getString("id")))) : EMPTY;
	}

	private List<Fluid> extractFluids(ItemStack stack) {
		List<Fluid> fluids = new ArrayList<>();

		LazyOptional<IFluidHandlerItem> capability =
				stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);

		capability.ifPresent((cap) -> {
			for(int i = 0; i < cap.getTanks(); i++) {
				fluids.add(cap.getFluidInTank(i).getFluid());
			}
		});

		return fluids;
	}
}
