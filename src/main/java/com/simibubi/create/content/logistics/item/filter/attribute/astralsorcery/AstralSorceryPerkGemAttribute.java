package com.simibubi.create.content.logistics.item.filter.attribute.astralsorcery;

import com.simibubi.create.content.logistics.item.filter.ItemAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class AstralSorceryPerkGemAttribute implements ItemAttribute {
    String traitName;

    public AstralSorceryPerkGemAttribute(String traitName) {
        this.traitName = traitName;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        for (INBT trait : extractTraitList(itemStack)) {
            if(((CompoundNBT) trait).getString("type").equals(this.traitName))
                return true;
        }
        return false;
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        ListNBT traits = extractTraitList(itemStack);
        List<ItemAttribute> atts = new ArrayList<>();
        for (int i = 0; i < traits.size(); i++) {
            atts.add(new AstralSorceryPerkGemAttribute(traits.getCompound(i).getString("type")));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        return "astralsorcery_perk_gem";
    }

    @Override
    public Object[] getTranslationParameters() {
        ResourceLocation traitResource = new ResourceLocation(traitName);
        String something = new TranslationTextComponent(String.format("perk.attribute.%s.%s.name", traitResource.getNamespace(), traitResource.getPath())).getString();
        return new Object[] { something };
    }

    @Override
    public void writeNBT(CompoundNBT nbt) {
        nbt.putString("type", this.traitName);
    }

    @Override
    public ItemAttribute readNBT(CompoundNBT nbt) {
        return new AstralSorceryPerkGemAttribute(nbt.getString("type"));
    }

    private ListNBT extractTraitList(ItemStack stack) {
        return stack.getTag() != null ? stack.getTag().getCompound("astralsorcery").getList("attribute_modifiers", 10) : new ListNBT();
    }
}
