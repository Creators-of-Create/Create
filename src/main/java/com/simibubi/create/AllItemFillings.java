package com.simibubi.create;

import java.util.Collection;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Unmodifiable;

import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.api.fluids.transfer.ItemFilling;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;

import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AllItemFillings {
	private static final RegistryObject<Item> FD_MILK_BOTTLE = RegistryObject
		.create(Mods.FD.asResource("milk_bottle"), ForgeRegistries.ITEMS);
	private static final RegistryObject<Item> NEA_MILK_BOTTLE = RegistryObject
		.create(Mods.NEA.asResource("milk_bottle"), ForgeRegistries.ITEMS);
	private static final RegistryObject<Item> AM_LAVA_BOTTLE = RegistryObject
		.create(Mods.AM.asResource("lava_bottle"), ForgeRegistries.ITEMS);

	static void registerDefaults() {
		ItemFilling.registerSimple(
			Create.asResource("filling/honey_bottle"),
			Items.GLASS_BOTTLE,
			FluidIngredient.fromTag(AllFluidTags.HONEY.tag, 250),
			Items.HONEY_BOTTLE.getDefaultInstance()
		);
		ItemFilling.registerSimple(
			Create.asResource("filling/builders_tea"),
			Items.GLASS_BOTTLE,
			FluidIngredient.fromFluid(AllFluids.TEA.get(), 250),
			AllItems.BUILDERS_TEA.asStack()
		);
		AM_LAVA_BOTTLE.ifPresent(item -> ItemFilling.registerSimple(
			Create.asResource("filling/" + Mods.AM.recipeId("lava_bottle")),
			Items.GLASS_BOTTLE,
			FluidIngredient.fromFluid(Fluids.LAVA, 250),
			item.getDefaultInstance()
		));
		if (ForgeMod.MILK.isPresent()) {
			FD_MILK_BOTTLE.ifPresent(item -> ItemFilling.registerSimple(
				Create.asResource("filling/" + Mods.FD.recipeId("milk_bottle")),
				Items.GLASS_BOTTLE,
				FluidIngredient.fromFluid(ForgeMod.MILK.get(), 250),
				item.getDefaultInstance()
			));
			NEA_MILK_BOTTLE.ifPresent(item -> ItemFilling.registerSimple(
				Create.asResource("filling/" + Mods.NEA.recipeId("milk_bottle")),
				Items.GLASS_BOTTLE,
				FluidIngredient.fromFluid(ForgeMod.MILK.get(), 250),
				item.getDefaultInstance()
			));
		}
		ItemFilling.REGISTRY.add(Items.GLASS_BOTTLE, new PotionFilling());
	}

	@Internal
	static class PotionFilling implements ItemFilling {
		@Override
		public int getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
			if (availableFluid.getFluid().isSame(AllFluids.POTION.get()))
				return PotionFluidHandler.getRequiredAmountForFilledBottle(stack, availableFluid);
			return -1;
		}

		@Override
		public ItemStack fillItem(Level world, ItemStack stack, FluidStack availableFluid) {
			return PotionFluidHandler.fillBottle(stack, availableFluid);
		}

		@Override
		public void provideRecipes(Consumer<FillingRecipe> consumer, @Unmodifiable Collection<ItemStack> itemIngredients, @Unmodifiable Collection<FluidStack> fluidIngredients) {
			itemIngredients
				.stream()
				.filter(PotionFluidHandler::isPotionItem)
				.map(stack -> {
					FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(stack);
					Ingredient bottle = Ingredient.of(Items.GLASS_BOTTLE);
					ResourceLocation potionId = CatnipServices.REGISTRIES.getKeyOrThrow(PotionUtils.getPotion(stack));
					ResourceLocation id = new ResourceLocation(
						potionId.getNamespace(),
						"filling/" +
						CatnipServices.REGISTRIES.getKeyOrThrow(stack.getItem()).getPath() +
						"_of_" +
						potionId.getPath()
					);
					return new ProcessingRecipeBuilder<>(FillingRecipe::new, id)
						.require(bottle)
						.require(FluidIngredient.fromFluidStack(fluidFromPotionItem))
						.withSingleItemOutput(stack)
						.build();
				})
				.forEach(consumer);
		}
	}
}
