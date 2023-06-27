package com.simibubi.create.content.logistics.filter.attribute;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class EnchantAttribute implements ItemAttribute {
    public static final EnchantAttribute EMPTY = new EnchantAttribute(null);

    private final Enchantment enchantment;

    public EnchantAttribute(@Nullable Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        return EnchantmentHelper.getEnchantments(itemStack).containsKey(enchantment);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        return EnchantmentHelper.getEnchantments(itemStack).keySet().stream().map(EnchantAttribute::new).collect(Collectors.toList());
    }

    @Override
    public String getTranslationKey() {
        return "has_enchant";
    }

    @Override
    public Object[] getTranslationParameters() {
        String parameter = "";
        if(enchantment != null)
            parameter = Components.translatable(enchantment.getDescriptionId()).getString();
        return new Object[] { parameter };
    }

    @Override
    public void writeNBT(CompoundTag nbt) {
        if (enchantment == null)
            return;
        ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        if (id == null)
            return;
        nbt.putString("id", id.toString());
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return nbt.contains("id") ? new EnchantAttribute(ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryParse(nbt.getString("id")))) : EMPTY;
    }
}
