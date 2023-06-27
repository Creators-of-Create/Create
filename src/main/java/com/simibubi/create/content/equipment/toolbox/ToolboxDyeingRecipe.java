package com.simibubi.create.content.equipment.toolbox;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;

public class ToolboxDyeingRecipe extends CustomRecipe {

	public ToolboxDyeingRecipe(ResourceLocation rl, CraftingBookCategory category) {
		super(rl, category);
	}

	@Override
	public boolean matches(CraftingContainer inventory, Level world) {
		int toolboxes = 0;
		int dyes = 0;

		for (int i = 0; i < inventory.getContainerSize(); ++i) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty()) {
				if (Block.byItem(stack.getItem()) instanceof ToolboxBlock) {
					++toolboxes;
				} else {
					if (!stack.is(Tags.Items.DYES))
						return false;
					++dyes;
				}

				if (dyes > 1 || toolboxes > 1) {
					return false;
				}
			}
		}

		return toolboxes == 1 && dyes == 1;
	}

	@Override
	public ItemStack assemble(CraftingContainer inventory) {
		ItemStack toolbox = ItemStack.EMPTY;
		DyeColor color = DyeColor.BROWN;

		for (int i = 0; i < inventory.getContainerSize(); ++i) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty()) {
				if (Block.byItem(stack.getItem()) instanceof ToolboxBlock) {
					toolbox = stack;
				} else {
					DyeColor color1 = DyeColor.getColor(stack);
					if (color1 != null) {
						color = color1;
					}
				}
			}
		}

		ItemStack dyedToolbox = AllBlocks.TOOLBOXES.get(color)
			.asStack();
		if (toolbox.hasTag()) {
			dyedToolbox.setTag(toolbox.getTag()
				.copy());
		}

		return dyedToolbox;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return AllRecipeTypes.TOOLBOX_DYEING.getSerializer();
	}

}
