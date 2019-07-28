package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.modules.curiosities.placementHandgun.BuilderGunUpgradeRecipe;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;

public enum AllRecipes {

	Placement_Handgun_Upgrade(BuilderGunUpgradeRecipe.Serializer::new);

	public IRecipeSerializer<?> serializer;
	public Supplier<IRecipeSerializer<?>> supplier;

	private AllRecipes(Supplier<IRecipeSerializer<?>> supplier) {
		this.supplier = supplier;
	}

	public static void register(RegistryEvent.Register<IRecipeSerializer<?>> event) {
		for (AllRecipes r : AllRecipes.values()) {
			r.serializer = r.supplier.get();
			ResourceLocation location = new ResourceLocation(Create.ID, r.name().toLowerCase());
			event.getRegistry().register(r.serializer.setRegistryName(location));
		}
	}

}
