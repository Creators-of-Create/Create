package com.simibubi.create.modules.curiosities.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.modules.contraptions.processing.ProcessingIngredient;
import com.simibubi.create.modules.contraptions.processing.ProcessingOutput;
import com.simibubi.create.modules.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.modules.curiosities.tools.SandPaperPolishingRecipe.SandPaperInv;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class SandPaperPolishingRecipe extends ProcessingRecipe<SandPaperInv> {

	public static DamageSource CURSED_POLISHING = new DamageSource("create.curse_polish").setExplosion();

	public SandPaperPolishingRecipe(ResourceLocation id, String group, List<ProcessingIngredient> ingredients,
			List<ProcessingOutput> results, int processingDuration) {
		super(AllRecipes.SANDPAPER_POLISHING, id, group, ingredients, results, processingDuration);
	}

	public static boolean canPolish(World world, ItemStack stack) {
		return (stack.isDamageable() && isPolishingEnabled()) || !getMatchingRecipes(world, stack).isEmpty();
	}

	public static Boolean isPolishingEnabled() {
		return AllConfigs.SERVER.curiosities.enableSandPaperToolPolishing.get();
	}

	public static ItemStack applyPolish(World world, Vec3d position, ItemStack stack, ItemStack sandPaperStack) {
		List<IRecipe<SandPaperInv>> matchingRecipes = getMatchingRecipes(world, stack);
		if (!matchingRecipes.isEmpty())
			return matchingRecipes.get(0).getCraftingResult(new SandPaperInv(stack)).copy();
		if (stack.isDamageable() && isPolishingEnabled()) {

			stack.setDamage(stack.getDamage() - (stack.getMaxDamage() - stack.getDamage()) / 2);

			int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, sandPaperStack);
			float chanceToPunish = (float) (1 / Math.pow(2, fortuneLevel + 1));

			if (world.rand.nextFloat() > chanceToPunish)
				return stack;

			if (stack.isEnchanted()) {
				Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
				ArrayList<Enchantment> list = new ArrayList<>(enchantments.keySet());
				Enchantment randomKey = list.get(world.rand.nextInt(list.size()));
				int level = enchantments.get(randomKey);
				if (level <= 1)
					enchantments.remove(randomKey);
				else
					enchantments.put(randomKey, level - 1);
				EnchantmentHelper.setEnchantments(enchantments, stack);
				if (randomKey.isCurse())
					if (!world.isRemote)
						world.createExplosion(null, CURSED_POLISHING, position.x, position.y, position.z, 2, true,
								Mode.DESTROY);
			} else {
				stack = ItemStack.EMPTY;
			}
		}

		return stack;
	}

	@Override
	public boolean matches(SandPaperInv inv, World worldIn) {
		return ingredients.get(0).test(inv.getStackInSlot(0));
	}

	public static List<IRecipe<SandPaperInv>> getMatchingRecipes(World world, ItemStack stack) {
		return world.getRecipeManager().getRecipes(AllRecipes.SANDPAPER_POLISHING.getType(), new SandPaperInv(stack),
				world);
	}

	@Override
	protected int getMaxOutputCount() {
		return 1;
	}

	public static class SandPaperInv extends RecipeWrapper {

		public SandPaperInv(ItemStack stack) {
			super(new ItemStackHandler(1));
			inv.setStackInSlot(0, stack);
		}

	}

}
