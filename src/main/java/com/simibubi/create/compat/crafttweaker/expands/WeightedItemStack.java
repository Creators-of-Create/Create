package com.simibubi.create.compat.crafttweaker.expands;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.create.WeightedItemStack")
public class WeightedItemStack extends MCItemStack {
    private final float weight;
    private final boolean isDefault;

    WeightedItemStack(IItemStack stack, float weight, boolean isDefault) {
        super(stack.getInternal());
        this.weight = weight;
        this.isDefault = isDefault;
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

