package com.simibubi.create.compat.crafttweaker.expands;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.create.WeightedItemStack")
public class WeightedItemStack {
    private final float weight;
    private final boolean isDefault;

    private final IItemStack stack;
    private final Ingredient ingredient;

    private WeightedItemStack(IItemStack stack, Ingredient ingredient, float weight, boolean isDefault) {
        this.stack = stack;
        this.ingredient = ingredient;
        this.weight = weight;
        this.isDefault = isDefault;
    }

    WeightedItemStack(IItemStack stack, float weight, boolean isDefault) {
        this(stack, stack.asVanillaIngredient(), weight, isDefault);
    }

    WeightedItemStack(Ingredient ingredient, float weight, boolean isDefault) {
        this(null, ingredient, weight, isDefault);
    }

    @ZenCodeType.Constructor
    @SuppressWarnings("unused")
    public WeightedItemStack(IItemStack stack, float weight) {
        this(stack, weight, false);
    }

    @ZenCodeType.Getter("weight")
    public float getWeight() {
        return weight;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getStack() {
        return stack.getInternal();
    }

    public float withDefault(float value) {
        if (isDefault) {
            return value;
        } else {
            return weight;
        }
    }

    @ZenCodeType.Caster(implicit = true)
    @SuppressWarnings("unused")
    public WeightedItemStack[] castToList() {
        return new WeightedItemStack[]{this};
    }
}

