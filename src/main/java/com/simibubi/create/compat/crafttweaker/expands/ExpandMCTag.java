package com.simibubi.create.compat.crafttweaker.expands;


import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Expansion("crafttweaker.api.tag.MCTag")
@SuppressWarnings("unused")
public class ExpandMCTag {
    @ZenCodeType.Caster(implicit = true)
    public static WeightedItemStack asWeightedItemStack(MCTag value) {
        return new WeightedItemStack(value, 1.0f);
    }
}
