package com.simibubi.create.compat.crafttweaker.item;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.food.MCFood;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.create.WeightedItemStack")
@SuppressWarnings("unused")
public class WeightedItemStack implements IItemStack {
    private final float weight;

    private final IItemStack stack;
    private final IIngredient ingredient;

    private WeightedItemStack(IItemStack stack, IIngredient ingredient, float weight) {
        this.stack = stack;
        this.ingredient = ingredient;
        this.weight = weight;
    }

    @ZenCodeType.Constructor
    public WeightedItemStack(IItemStack stack, float weight) {
        this(stack, stack, weight);
    }

    @ZenCodeType.Constructor
    public WeightedItemStack(MCTag tag, float weight) {
        this(tag.getFirstItem(), tag, weight);
    }

    @ZenCodeType.Getter("weight")
    public float getWeight() {
        return weight;
    }

    @Override
    public Ingredient asVanillaIngredient() {
        return ingredient.asVanillaIngredient();
    }

    @Override
    public String getCommandString() {
        return stack.getCommandString();
    }

    @Override
    public IItemStack[] getItems() {
        return stack.getItems();
    }

    @Override
    public IItemStack copy() {
        return new WeightedItemStack(stack.copy(), ingredient, weight);
    }

    @Override
    public IItemStack setDisplayName(String name) {
        return new WeightedItemStack(stack.setDisplayName(name), ingredient, weight);
    }

    @Override
    public IItemStack setAmount(int amount) {
        return new WeightedItemStack(stack.setAmount(amount), ingredient, weight);
    }

    @Override
    public IItemStack withDamage(int damage) {
        return new WeightedItemStack(stack.withDamage(damage), ingredient, weight);
    }

    @Override
    public IItemStack withTag(IData tag) {
        return new WeightedItemStack(stack.withTag(tag), ingredient, weight);
    }

    @Override
    public MCFood getFood() {
        return stack.getFood();
    }

    @Override
    public void setFood(MCFood food) {
        stack.setFood(food);
    }

    @Override
    public ItemStack getInternal() {
        return stack.getInternal();
    }
}

