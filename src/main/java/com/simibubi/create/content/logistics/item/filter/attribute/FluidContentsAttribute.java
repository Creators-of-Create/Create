package com.simibubi.create.content.logistics.item.filter.attribute;

import com.simibubi.create.content.logistics.item.filter.ItemAttribute;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class FluidContentsAttribute implements ItemAttribute {
    String fluidName;

    public FluidContentsAttribute(String fluidName) {
        this.fluidName = fluidName;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        return extractFluidNames(itemStack).contains(fluidName);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        List<String> names = extractFluidNames(itemStack);

        List<ItemAttribute> atts = new ArrayList<>();
        for(String name : names) {
            atts.add(new FluidContentsAttribute(name));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        return "has_fluid";
    }

    @Override
    public Object[] getTranslationParameters() {
        ResourceLocation fluidResource = new ResourceLocation(fluidName);
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidResource);
        String trans = "";
        if(fluid != null)
            trans = new TranslationTextComponent(fluid.getAttributes().getTranslationKey()).getString();
        return new Object[] {trans};
    }

    @Override
    public void writeNBT(CompoundNBT nbt) {
        nbt.putString("fluidName", this.fluidName);
    }

    @Override
    public ItemAttribute readNBT(CompoundNBT nbt) {
        return new FluidContentsAttribute(nbt.getString("fluidName"));
    }

    private List<String> extractFluidNames(ItemStack stack) {
        List<String> fluids = new ArrayList<>();

        LazyOptional<IFluidHandlerItem> capability =
                stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);

        capability.ifPresent((cap) -> {
            for(int i = 0; i < cap.getTanks(); i++) {
                fluids.add(cap.getFluidInTank(i).getFluid().getRegistryName().toString());
            }
        });

        return fluids;
    }
}
