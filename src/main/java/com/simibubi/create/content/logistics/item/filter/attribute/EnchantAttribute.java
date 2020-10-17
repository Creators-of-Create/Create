package com.simibubi.create.content.logistics.item.filter.attribute;

import com.simibubi.create.content.logistics.item.filter.ItemAttribute;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class EnchantAttribute implements ItemAttribute {
    String enchantName;

    public EnchantAttribute(String enchantName) {
        this.enchantName = enchantName;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        ListNBT enchants = extractEnchants(itemStack);
        for (int i = 0; i < enchants.size(); i++) {
            if(enchants.getCompound(i).getString("id").equals(this.enchantName))
                return true;
        }
        return false;
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        ListNBT enchants = extractEnchants(itemStack);
        List<ItemAttribute> atts = new ArrayList<>();
        for (int i = 0; i < enchants.size(); i++) {
            atts.add(new EnchantAttribute(enchants.getCompound(i).getString("id")));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        return "has_enchant";
    }

    @Override
    public Object[] getTranslationParameters() {
        String something = "";
        Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryCreate(enchantName));
        if(enchant != null) {
            something = new TranslationTextComponent(enchant.getName()).getString();
        }
        return new Object[] { something };
    }

    @Override
    public void writeNBT(CompoundNBT nbt) {
        nbt.putString("id", this.enchantName);
    }

    @Override
    public ItemAttribute readNBT(CompoundNBT nbt) {
        return new EnchantAttribute(nbt.getString("id"));
    }

    private ListNBT extractEnchants(ItemStack stack) {
        CompoundNBT tag = stack.getTag() != null ? stack.getTag() : new CompoundNBT();
        return tag.contains("Enchantments") ? stack.getEnchantmentTagList() : tag.getList("StoredEnchantments", 10);
    }
}
