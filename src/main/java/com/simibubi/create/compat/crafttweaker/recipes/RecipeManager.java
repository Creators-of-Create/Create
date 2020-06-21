package com.simibubi.create.compat.crafttweaker.recipes;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.simibubi.create.compat.crafttweaker.item.WeightedItemStack;
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
    public void addRecipe(String name, IIngredient[] ingredients, IItemStack[] results, @ZenCodeType.OptionalInt int processingDuration) {
        List<ProcessingIngredient> input = Arrays.stream(ingredients).map(i -> {
            float chance = 0;
            if (i instanceof WeightedItemStack) {
                WeightedItemStack stack = (WeightedItemStack) i;
                chance = inputHasOutputChance ? stack.getWeight() : 0;
            }

            return new ProcessingIngredient(i.asVanillaIngredient(), chance);
        }).collect(Collectors.toList());
        List<ProcessingOutput> output = Arrays.stream(results).map(r -> {
            float chance = 1.0f;
            if (r instanceof WeightedItemStack) {
                WeightedItemStack stack = (WeightedItemStack) r;
                chance = stack.getWeight();
            }

            return new ProcessingOutput(r.getInternal(), chance);
        }).collect(Collectors.toList());

        ProcessingRecipe<?> recipe = factory.create(new ResourceLocation("crafttweaker", name), "", input, output, processingDuration);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, ""));
    }

    @ZenCodeType.Method
    @SuppressWarnings("unused")
    public void addRecipe(String name, IIngredient ingredient, IItemStack result, @ZenCodeType.OptionalInt int processingDuration) {
        addRecipe(name, new IIngredient[]{ingredient}, new IItemStack[]{result}, processingDuration);
    }

    @Override
    public IRecipeType<?> getRecipeType() {
        return type;
    }
}
