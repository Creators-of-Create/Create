package com.simibubi.create.content.logistics.item.filter.attribute.astralsorcery;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.item.filter.ItemAttribute;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class AstralSorceryAttunementAttribute implements ItemAttribute {
    String constellationName;

    public AstralSorceryAttunementAttribute(String constellationName) {
        this.constellationName = constellationName;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        CompoundNBT nbt = extractAstralNBT(itemStack);
        String constellation = nbt.contains("constellation") ? nbt.getString("constellation") : nbt.getString("constellationName");

        // Special handling for shifting stars
        ResourceLocation itemResource = itemStack.getItem().getRegistryName();
        if(itemResource != null && itemResource.toString().contains("shifting_star_")) {
            constellation = itemResource.toString().replace("shifting_star_", "");
        }

        return constellation.equals(constellationName);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        CompoundNBT nbt = extractAstralNBT(itemStack);
        String constellation = nbt.contains("constellation") ? nbt.getString("constellation") : nbt.getString("constellationName");

        // Special handling for shifting stars
        ResourceLocation itemResource = itemStack.getItem().getRegistryName();
        if(itemResource != null && itemResource.toString().contains("shifting_star_")) {
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
        String something = new TranslationTextComponent(String.format("%s.constellation.%s", constResource.getNamespace(), constResource.getPath())).getString();
        return new Object[] { something };
    }

    @Override
    public void writeNBT(CompoundNBT nbt) {
        nbt.putString("constellation", this.constellationName);
    }

    @Override
    public ItemAttribute readNBT(CompoundNBT nbt) {
        return new AstralSorceryAttunementAttribute(nbt.getString("constellation"));
    }

    private CompoundNBT extractAstralNBT(ItemStack stack) {
        return stack.getTag() != null ? stack.getTag().getCompound("astralsorcery") : new CompoundNBT();
    }
}
