package com.simibubi.create.compat.crafttweaker.item;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.actions.items.ActionSetFood;
import com.blamejared.crafttweaker.impl.data.MapData;
import com.blamejared.crafttweaker.impl.food.MCFood;
import com.blamejared.crafttweaker.impl.item.MCItemStack;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.StringTextComponent;
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
        ItemStack st = stack.getInternal().copy();
        st.setDisplayName(new StringTextComponent(name));
        return new WeightedItemStack(stack, ingredient, weight);
    }

    @Override
    public IItemStack setAmount(int amount) {
        ItemStack st = stack.getInternal().copy();
        st.setCount(amount);
        return new WeightedItemStack(stack, ingredient, weight);
    }

    @Override
    public IItemStack withDamage(int damage) {
        ItemStack st = stack.getInternal().copy();
        st.setDamage(damage);
        return new WeightedItemStack(stack, ingredient, weight);
    }

    @Override
    public IItemStack withTag(IData tag) {
        ItemStack copy = stack.getInternal().copy();
        if (!(tag instanceof MapData)) {
            tag = new MapData(tag.asMap());
        }

        copy.setTag(((MapData) tag).getInternal());
        return new MCItemStack(copy);
    }

    @Override
    public MCFood getFood() {
        return new MCFood(stack.getInternal().getItem().getFood());
    }

    @Override
    public void setFood(MCFood food) {
        CraftTweakerAPI.apply(new ActionSetFood(this, food));
    }

    @Override
    public ItemStack getInternal() {
        return stack.getInternal();
    }
}

