package com.simibubi.create.compat.crafttweaker.expands;


import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Expansion("crafttweaker.api.tag.MCTag")
public class ExpandMCTag {
    @ZenCodeType.Caster(implicit = true)
    @SuppressWarnings("unused")
    public static WeightedItemStack asWeightedItemStack(MCTag value) {
        return new WeightedItemStack(value.asVanillaIngredient(), 1.0f, true);
    }
}
