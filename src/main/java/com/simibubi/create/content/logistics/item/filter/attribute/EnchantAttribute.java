package com.simibubi.create.content.logistics.item.filter.attribute;

import com.simibubi.create.content.logistics.item.filter.ItemAttribute;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

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
            parameter = new TranslationTextComponent(enchantment.getName()).getString();
        return new Object[] { parameter };
    }

    @Override
    public void writeNBT(CompoundNBT nbt) {
        if (enchantment == null)
            return;
        ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        if (id == null)
            return;
        nbt.putString("id", id.toString());
    }

    @Override
    public ItemAttribute readNBT(CompoundNBT nbt) {
        return nbt.contains("id") ? new EnchantAttribute(ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryCreate(nbt.getString("id")))) : EMPTY;
    }
}
