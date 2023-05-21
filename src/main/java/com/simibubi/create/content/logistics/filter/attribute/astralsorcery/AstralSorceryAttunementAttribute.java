package com.simibubi.create.content.logistics.filter.attribute.astralsorcery;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class AstralSorceryAttunementAttribute implements ItemAttribute {
    String constellationName;

    public AstralSorceryAttunementAttribute(String constellationName) {
        this.constellationName = constellationName;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        CompoundTag nbt = extractAstralNBT(itemStack);
        String constellation = nbt.contains("constellation") ? nbt.getString("constellation") : nbt.getString("constellationName");

        // Special handling for shifting stars
        ResourceLocation itemResource = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        if (itemResource != null && itemResource.toString().contains("shifting_star_")) {
            constellation = itemResource.toString().replace("shifting_star_", "");
        }

        return constellation.equals(constellationName);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        CompoundTag nbt = extractAstralNBT(itemStack);
        String constellation = nbt.contains("constellation") ? nbt.getString("constellation") : nbt.getString("constellationName");

        // Special handling for shifting stars
        ResourceLocation itemResource = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        if (itemResource != null && itemResource.toString().contains("shifting_star_")) {
            constellation = itemResource.toString().replace("shifting_star_", "");
        }

        List<ItemAttribute> atts = new ArrayList<>();
        if(constellation.length() > 0) {
            atts.add(new AstralSorceryAttunementAttribute(constellation));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        return "astralsorcery_constellation";
    }

    @Override
    public Object[] getTranslationParameters() {
        ResourceLocation constResource = new ResourceLocation(constellationName);
        String something = Components.translatable(String.format("%s.constellation.%s", constResource.getNamespace(), constResource.getPath())).getString();
        return new Object[] { something };
    }

    @Override
    public void writeNBT(CompoundTag nbt) {
        nbt.putString("constellation", this.constellationName);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return new AstralSorceryAttunementAttribute(nbt.getString("constellation"));
    }

    private CompoundTag extractAstralNBT(ItemStack stack) {
        return stack.getTag() != null ? stack.getTag().getCompound("astralsorcery") : new CompoundTag();
    }
}
