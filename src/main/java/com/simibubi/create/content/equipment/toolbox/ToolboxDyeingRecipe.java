package com.simibubi.create.content.equipment.toolbox;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

public class ToolboxDyeingRecipe extends CustomRecipe {
	public static final MapCodec<ToolboxDyeingRecipe> CODEC =
		MapCodec.unit(() -> new ToolboxDyeingRecipe(CraftingBookCategory.MISC));
	public static final StreamCodec<RegistryFriendlyByteBuf, ToolboxDyeingRecipe> STREAM_CODEC =
		StreamCodec.unit(new ToolboxDyeingRecipe(CraftingBookCategory.MISC));
	public static final RecipeSerializer<ToolboxDyeingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	public ToolboxDyeingRecipe(CraftingBookCategory category) {
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		int toolboxes = 0;
		int dyes = 0;

		for (int i = 0; i < input.size(); ++i) {
			ItemStack stack = input.getItem(i);
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
	public ItemStack assemble(CraftingInput input) {
		ItemStack toolbox = ItemStack.EMPTY;
		DyeColor color = DyeColor.BROWN;

		for (int i = 0; i < input.size(); ++i) {
			ItemStack stack = input.getItem(i);
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
		if (!toolbox.isComponentsPatchEmpty()) {
			dyedToolbox.applyComponents(toolbox.getComponentsPatch());
		}

		return dyedToolbox;
	}

	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<ToolboxDyeingRecipe> getSerializer() {
		return SERIALIZER;
	}

}
