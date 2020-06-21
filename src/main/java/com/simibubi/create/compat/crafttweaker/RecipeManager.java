package com.simibubi.create.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.simibubi.create.compat.crafttweaker.expands.WeightedItemStack;
import com.simibubi.create.content.contraptions.processing.ProcessingIngredient;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ZenRegister
@ZenCodeType.Name("mods.create.RecipeManager")
public class RecipeManager implements IRecipeManager {
    private final IRecipeType<?> type;
    private final ProcessingRecipeSerializer.IRecipeFactory<? extends ProcessingRecipe<?>> factory;
    private final boolean inputHasOutputChance;

    public RecipeManager(IRecipeType<?> type, ProcessingRecipeSerializer.IRecipeFactory<? extends ProcessingRecipe<?>> factory, boolean inputHasOutputChance) {
        this.type = type;
        this.factory = factory;
        this.inputHasOutputChance = inputHasOutputChance;
    }

    public RecipeManager(IRecipeType<?> type, ProcessingRecipeSerializer.IRecipeFactory<? extends ProcessingRecipe<?>> factory) {
        this(type, factory, false);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, WeightedItemStack[] ingredients, WeightedItemStack[] results,
                          int processingDuration, @ZenCodeType.OptionalString String group) {
        List<ProcessingIngredient> input = Arrays.stream(ingredients).map(i -> new ProcessingIngredient(i.asVanillaIngredient(), inputHasOutputChance ? i.withDefault(0) : 0)).collect(Collectors.toList());
        List<ProcessingOutput> output = Arrays.stream(results).map(r -> new ProcessingOutput(r.getInternal(), r.getWeight())).collect(Collectors.toList());

        ProcessingRecipe<?> recipe = factory.create(new ResourceLocation("crafttweaker", name), group, input, output, processingDuration);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, ""));
    }

    @ZenCodeType.Method
    @SuppressWarnings("unused")
    public void addRecipe(String name, WeightedItemStack ingredient, WeightedItemStack result,
                          int processingDuration, @ZenCodeType.OptionalString String group) {
        addRecipe(name, new WeightedItemStack[]{ingredient}, new WeightedItemStack[]{result}, processingDuration, group);
    }

    @Override
    public IRecipeType<?> getRecipeType() {
        return type;
    }
}
