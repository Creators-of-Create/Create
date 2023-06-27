package com.simibubi.create.content.logistics.filter.attribute.astralsorcery;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
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
        for (Tag trait : extractTraitList(itemStack)) {
            if(((CompoundTag) trait).getString("ench").equals(this.enchName)
                    && ((CompoundTag)trait).getInt("type") == this.enchType)
                return true;
        }
        return false;
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        ListTag traits = extractTraitList(itemStack);
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

        Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryParse(enchName));
        if(enchant != null) {
            something = Components.translatable(enchant.getDescriptionId()).getString();
        }

        if(enchType == 1) something = "existing " + something;

        return new Object[] { something };
    }

    @Override
    public void writeNBT(CompoundTag nbt) {
        nbt.putString("enchName", this.enchName);
        nbt.putInt("enchType", this.enchType);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return new AstralSorceryAmuletAttribute(nbt.getString("enchName"), nbt.getInt("enchType"));
    }

    private ListTag extractTraitList(ItemStack stack) {
        return stack.getTag() != null ? stack.getTag().getCompound("astralsorcery").getList("amuletEnchantments", 10) : new ListTag();
    }
}
