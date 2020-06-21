package com.simibubi.create.compat.crafttweaker.expands;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStack;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import net.minecraft.item.crafting.Ingredient;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.create.WeightedItemStack")
@SuppressWarnings("unused")
public class WeightedItemStack extends MCItemStack {
    private final float weight;

    private final IIngredient ingredient;

    private WeightedItemStack(IItemStack stack, IIngredient ingredient, float weight) {
        super(stack.getInternal());
        this.ingredient = ingredient;
        this.weight = weight;
    }

    @ZenCodeType.Constructor
    public WeightedItemStack(IItemStack stack, float weight) {
        this(stack, null, weight);
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
        if (ingredient != null) {
            return ingredient.asVanillaIngredient();
        }
        return super.asVanillaIngredient();
    }

    @ZenCodeType.Caster(implicit = true)
    public IItemStack asIItemStack() {
        return this;
    }

    @ZenCodeType.Caster(implicit = true)
    public IIngredient asIIngredient() {
        return this;
    }
}

