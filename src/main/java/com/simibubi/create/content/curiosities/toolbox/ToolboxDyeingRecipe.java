package com.simibubi.create.content.curiosities.toolbox;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;

import net.minecraft.block.Block;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

public class ToolboxDyeingRecipe extends SpecialRecipe {

	public ToolboxDyeingRecipe(ResourceLocation rl) {
		super(rl);
	}

	@Override
	public boolean matches(CraftingInventory inventory, World world) {
		int toolboxes = 0;
		int dyes = 0;

		for (int i = 0; i < inventory.getContainerSize(); ++i) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty()) {
				if (Block.byItem(stack.getItem()) instanceof ToolboxBlock) {
					++toolboxes;
				} else {
					if (!stack.getItem().is(Tags.Items.DYES)) {
						return false;
					}

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
	public ItemStack assemble(CraftingInventory inventory) {
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

		ItemStack dyedToolbox = AllBlocks.TOOLBOXES.get(color).asStack();
		if (toolbox.hasTag()) {
			dyedToolbox.setTag(toolbox.getTag().copy());
		}

		return dyedToolbox;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return AllRecipeTypes.TOOLBOX_DYEING.getSerializer();
	}

}
