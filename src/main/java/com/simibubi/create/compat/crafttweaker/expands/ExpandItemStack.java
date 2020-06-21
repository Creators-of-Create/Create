package com.simibubi.create.compat.crafttweaker.expands;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Expansion("crafttweaker.api.item.IItemStack")
@SuppressWarnings("unused")
public class ExpandItemStack {
    @ZenCodeType.Caster(implicit = true)
    public static WeightedItemStack asWeightedItemStack(IItemStack value) {
        return new WeightedItemStack(value, 1.0f);
    }
}
