package com.simibubi.create.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.compat.crafttweaker.recipes.MechanicalCrafterManager;
import com.simibubi.create.compat.crafttweaker.recipes.RecipeManager;
import com.simibubi.create.content.contraptions.components.crusher.CrushingRecipe;
import com.simibubi.create.content.contraptions.components.fan.SplashingRecipe;
import com.simibubi.create.content.contraptions.components.millstone.MillingRecipe;
import com.simibubi.create.content.contraptions.components.mixer.MixingRecipe;
import com.simibubi.create.content.contraptions.components.press.PressingRecipe;
import com.simibubi.create.content.contraptions.components.saw.CuttingRecipe;
import com.simibubi.create.content.curiosities.tools.SandPaperPolishingRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@SuppressWarnings("unused")
@ZenCodeType.Name("mods.create.Create")
public class Create {
    @ZenCodeType.Field
    public static final RecipeManager mechanicalSaw = new RecipeManager(AllRecipeTypes.CUTTING.type, CuttingRecipe::new);

    @ZenCodeType.Field
    public static final RecipeManager mechanicalPress = new RecipeManager(AllRecipeTypes.PRESSING.type, PressingRecipe::new);

    @ZenCodeType.Field
    public static final RecipeManager mechanicalMixer = new RecipeManager(AllRecipeTypes.MIXING.type, MixingRecipe::new, true);

    @ZenCodeType.Field
    public static final RecipeManager millstone = new RecipeManager(AllRecipeTypes.MILLING.type, MillingRecipe::new);

    @ZenCodeType.Field
    public static final RecipeManager crushingWheel = new RecipeManager(AllRecipeTypes.CRUSHING.type, CrushingRecipe::new);

    @ZenCodeType.Field
    public static final RecipeManager sandPaperPolish = new RecipeManager(AllRecipeTypes.SANDPAPER_POLISHING.type, SandPaperPolishingRecipe::new);

    @ZenCodeType.Field
    public static final RecipeManager splashing = new RecipeManager(AllRecipeTypes.SPLASHING.type, SplashingRecipe::new);

    @ZenCodeType.Field
    public static final MechanicalCrafterManager mechanicalCrafter = new MechanicalCrafterManager();
}
