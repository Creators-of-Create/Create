package com.simibubi.create.content.curiosities.tools;

import java.util.Collection;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterContainer.WhitelistMode;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.content.logistics.item.filter.ItemAttribute;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.Ingredient.IItemList;
import net.minecraft.item.crafting.Ingredient.SingleItemList;
import net.minecraft.item.crafting.Ingredient.TagList;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.StackList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.ItemStackHandler;

import net.minecraft.item.Item.Properties;

public class BlueprintItem extends Item {

	public BlueprintItem(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	@Override
	public ActionResultType useOn(ItemUseContext ctx) {
		Direction face = ctx.getClickedFace();
		PlayerEntity player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();
		BlockPos pos = ctx.getClickedPos()
				.relative(face);

		if (player != null && !player.mayUseItemAt(pos, face, stack))
			return ActionResultType.FAIL;

		World world = ctx.getLevel();
		HangingEntity hangingentity = new BlueprintEntity(world, pos, face, face.getAxis()
				.isHorizontal() ? Direction.DOWN : ctx.getHorizontalDirection());
		CompoundNBT compoundnbt = stack.getTag();

		if (compoundnbt != null)
			EntityType.updateCustomEntityTag(world, player, hangingentity, compoundnbt);
		if (!hangingentity.survives())
			return ActionResultType.CONSUME;
		if (!world.isClientSide) {
			hangingentity.playPlacementSound();
			world.addFreshEntity(hangingentity);
		}

		stack.shrink(1);
		return ActionResultType.sidedSuccess(world.isClientSide);
	}

	protected boolean canPlace(PlayerEntity p_200127_1_, Direction p_200127_2_, ItemStack p_200127_3_,
							   BlockPos p_200127_4_) {
		return p_200127_1_.mayUseItemAt(p_200127_4_, p_200127_2_, p_200127_3_);
	}

	public static void assignCompleteRecipe(ItemStackHandler inv, IRecipe<?> recipe) {
		NonNullList<Ingredient> ingredients = recipe.getIngredients();

		for (int i = 0; i < 9; i++)
			inv.setStackInSlot(i, ItemStack.EMPTY);
		inv.setStackInSlot(9, recipe.getResultItem());

		if (recipe instanceof ShapedRecipe) {
			ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
			for (int row = 0; row < shapedRecipe.getHeight(); row++)
				for (int col = 0; col < shapedRecipe.getWidth(); col++)
					inv.setStackInSlot(row * 3 + col,
							convertIngredientToFilter(ingredients.get(row * shapedRecipe.getWidth() + col)));
		} else {
			for (int i = 0; i < ingredients.size(); i++)
				inv.setStackInSlot(i, convertIngredientToFilter(ingredients.get(i)));
		}
	}

	private static ItemStack convertIngredientToFilter(Ingredient ingredient) {
		Ingredient.IItemList[] acceptedItems =
				ObfuscationReflectionHelper.getPrivateValue(Ingredient.class, ingredient, "values"); // values
		if (acceptedItems == null || acceptedItems.length > 18)
			return ItemStack.EMPTY;
		if (acceptedItems.length == 0)
			return ItemStack.EMPTY;
		if (acceptedItems.length == 1)
			return convertIItemListToFilter(acceptedItems[0]);

		ItemStack result = AllItems.FILTER.asStack();
		ItemStackHandler filterItems = FilterItem.getFilterItems(result);
		for (int i = 0; i < acceptedItems.length; i++)
			filterItems.setStackInSlot(i, convertIItemListToFilter(acceptedItems[i]));
		result.getOrCreateTag()
				.put("Items", filterItems.serializeNBT());
		return result;
	}

	private static ItemStack convertIItemListToFilter(IItemList itemList) {
		Collection<ItemStack> stacks = itemList.getItems();
		if (itemList instanceof SingleItemList) {
			for (ItemStack itemStack : stacks)
				return itemStack;
		}

		if (itemList instanceof TagList) {
			ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(itemList.serialize(), "tag"));
			ItemStack filterItem = AllItems.ATTRIBUTE_FILTER.asStack();
			filterItem.getOrCreateTag()
					.putInt("WhitelistMode", WhitelistMode.WHITELIST_DISJ.ordinal());
			ListNBT attributes = new ListNBT();
			ItemAttribute at = new ItemAttribute.InTag(resourcelocation);
			CompoundNBT compoundNBT = new CompoundNBT();
			at.serializeNBT(compoundNBT);
			compoundNBT.putBoolean("Inverted", false);
			attributes.add(compoundNBT);
			filterItem.getOrCreateTag()
					.put("MatchedAttributes", attributes);
			return filterItem;
		}

		if (itemList instanceof StackList) {
			ItemStack result = AllItems.FILTER.asStack();
			ItemStackHandler filterItems = FilterItem.getFilterItems(result);
			int i = 0;
			for (ItemStack itemStack : stacks) {
				if (i >= 18)
					break;
				filterItems.setStackInSlot(i++, itemStack);
			}
			CompoundNBT tag = result.getOrCreateTag();
			tag.put("Items", filterItems.serializeNBT());
			tag.putBoolean("RespectNBT", true);
			return result;
		}

		return ItemStack.EMPTY;
	}

}
