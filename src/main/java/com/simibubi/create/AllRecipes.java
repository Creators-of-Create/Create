package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.components.crusher.CrushingRecipe;
import com.simibubi.create.modules.contraptions.components.fan.SplashingRecipe;
import com.simibubi.create.modules.contraptions.components.mixer.MixingRecipe;
import com.simibubi.create.modules.contraptions.components.press.PressingRecipe;
import com.simibubi.create.modules.contraptions.components.saw.CuttingRecipe;
import com.simibubi.create.modules.contraptions.processing.ProcessingRecipeSerializer;
import com.simibubi.create.modules.curiosities.placementHandgun.BuilderGunUpgradeRecipe;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;

public enum AllRecipes {

	BLOCKZAPPER_UPGRADE(BuilderGunUpgradeRecipe.Serializer::new, IRecipeType.CRAFTING),
	CRUSHING(() -> new ProcessingRecipeSerializer<>(CrushingRecipe::new), Types.CRUSHING),
	SPLASHING(() -> new ProcessingRecipeSerializer<>(SplashingRecipe::new), Types.SPLASHING),
	PRESSING(() -> new ProcessingRecipeSerializer<>(PressingRecipe::new), Types.PRESSING),
	CUTTING(() -> new ProcessingRecipeSerializer<>(CuttingRecipe::new), Types.CUTTING),
	MIXING(() -> new ProcessingRecipeSerializer<>(MixingRecipe::new), Types.MIXING),

	;

	public static class Types {
		public static IRecipeType<CrushingRecipe> CRUSHING = register("crushing");
		public static IRecipeType<SplashingRecipe> SPLASHING = register("splashing");
		public static IRecipeType<PressingRecipe> PRESSING = register("pressing");
		public static IRecipeType<CuttingRecipe> CUTTING = register("cutting");
		public static IRecipeType<MixingRecipe> MIXING = register("mixing");

		static <T extends IRecipe<?>> IRecipeType<T> register(final String key) {
			return Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(key), new IRecipeType<T>() {
				public String toString() {
					return key;
				}
			});
		}
	}

	public IRecipeSerializer<?> serializer;
	public Supplier<IRecipeSerializer<?>> supplier;
	public IRecipeType<? extends IRecipe<? extends IInventory>> type;

	private AllRecipes(Supplier<IRecipeSerializer<?>> supplier,
			IRecipeType<? extends IRecipe<? extends IInventory>> type) {
		this.supplier = supplier;
		this.type = type;
	}

	public static void register(RegistryEvent.Register<IRecipeSerializer<?>> event) {
		for (AllRecipes r : AllRecipes.values()) {
			r.serializer = r.supplier.get();
			ResourceLocation location = new ResourceLocation(Create.ID, Lang.asId(r.name()));
			event.getRegistry().register(r.serializer.setRegistryName(location));
		}
	}

}
