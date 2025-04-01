package com.simibubi.create;

import java.util.Collection;
import java.util.function.Consumer;

import org.jetbrains.annotations.Unmodifiable;

import com.simibubi.create.api.fluids.transfer.ItemEmptying;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import net.minecraftforge.fluids.FluidStack;

public class AllItemEmptyings {
	static void registerDefaults() {
		ItemEmptying.REGISTRY.add(Items.POTION, new PotionEmptying(Items.POTION));
		ItemEmptying.REGISTRY.add(Items.SPLASH_POTION, new PotionEmptying(Items.SPLASH_POTION));
		ItemEmptying.REGISTRY.add(Items.LINGERING_POTION, new PotionEmptying(Items.LINGERING_POTION));
	}

	record PotionEmptying(Item potion) implements ItemEmptying {
		@Override
		public Pair<FluidStack, ItemStack> emptyItem(Level world, ItemStack stack, boolean simulate) {
			return PotionFluidHandler.emptyPotion(stack, simulate);
		}

		@Override
		public void provideRecipes(Consumer<EmptyingRecipe> consumer, @Unmodifiable Collection<ItemStack> itemIngredients, @Unmodifiable Collection<FluidStack> fluidIngredients) {
			itemIngredients
				.stream()
				.filter(stack -> stack.is(this.potion))
				.map(stack -> {
					Potion potion = PotionUtils.getPotion(stack);
					ResourceLocation potionId = CatnipServices.REGISTRIES.getKeyOrThrow(potion);
					ResourceLocation id = new ResourceLocation(
						potionId.getNamespace(),
						"emptying/" +
						CatnipServices.REGISTRIES.getKeyOrThrow(stack.getItem()).getPath() +
						"_of_" +
						potionId.getPath()
					);
					return new ProcessingRecipeBuilder<>(EmptyingRecipe::new, id)
						.require(Ingredient.of(stack))
						.output(PotionFluidHandler.getFluidFromPotionItem(stack))
						.output(Items.GLASS_BOTTLE)
						.build();
				})
				.forEach(consumer);
		}
	}
}
