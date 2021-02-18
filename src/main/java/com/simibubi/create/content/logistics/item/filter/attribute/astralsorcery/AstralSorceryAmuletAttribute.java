package com.simibubi.create.content.logistics.item.filter.attribute.astralsorcery;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.item.filter.ItemAttribute;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

public class AstralSorceryAmuletAttribute implements ItemAttribute {
    String enchName;
    int enchType;

    public AstralSorceryAmuletAttribute(String enchName, int enchType) {
        this.enchName = enchName;
        this.enchType = enchType;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        for (INBT trait : extractTraitList(itemStack)) {
            if(((CompoundNBT) trait).getString("ench").equals(this.enchName)
                    && ((CompoundNBT)trait).getInt("type") == this.enchType)
                return true;
        }
        return false;
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        ListNBT traits = extractTraitList(itemStack);
        List<ItemAttribute> atts = new ArrayList<>();
        for (int i = 0; i < traits.size(); i++) {
            atts.add(new AstralSorceryAmuletAttribute(
                    traits.getCompound(i).getString("ench"),
                    traits.getCompound(i).getInt("type")));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        return "astralsorcery_amulet";
    }

    @Override
    public Object[] getTranslationParameters() {
        String something = "";

        Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryCreate(enchName));
        if(enchant != null) {
            something = new TranslationTextComponent(enchant.getName()).getString();
        }

        if(enchType == 1) something = "existing " + something;

        return new Object[] { something };
    }

    @Override
    public void writeNBT(CompoundNBT nbt) {
        nbt.putString("enchName", this.enchName);
        nbt.putInt("enchType", this.enchType);
    }

    @Override
    public ItemAttribute readNBT(CompoundNBT nbt) {
        return new AstralSorceryAmuletAttribute(nbt.getString("enchName"), nbt.getInt("enchType"));
    }

    private ListNBT extractTraitList(ItemStack stack) {
        return stack.getTag() != null ? stack.getTag().getCompound("astralsorcery").getList("amuletEnchantments", 10) : new ListNBT();
    }
}
