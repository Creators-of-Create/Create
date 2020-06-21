package com.simibubi.create.compat.crafttweaker.recipes;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker.impl.recipes.CTRecipeShaped;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCraftingRecipe;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.create.MechanicalCraftingManager")
public class MechanicalCrafterManager extends RecipeManager {
    public MechanicalCrafterManager() {
        super(AllRecipeTypes.MECHANICAL_CRAFTING.type, null, false);
    }

    @ZenCodeType.Method
    @SuppressWarnings("unused")
    public void addRecipe(String name, IIngredient[][] ingredients, IItemStack results, @ZenCodeType.OptionalString String group) {
        int row = ingredients.length;
        if (row == 0) {
            return;
        }

        int column = ingredients[0].length;
        if (column == 0) {
            return;
        }

        CTRecipeShaped shaped = new CTRecipeShaped(name, results, ingredients, false, null);

        MechanicalCraftingRecipe recipe = new MechanicalCraftingRecipe(new ResourceLocation("crafttweaker", name), group, column, row, shaped.getIngredients(), shaped.getRecipeOutput());
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, ""));
    }
}
