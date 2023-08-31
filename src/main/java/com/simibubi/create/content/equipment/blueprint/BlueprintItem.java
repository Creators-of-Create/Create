package com.simibubi.create.content.equipment.blueprint;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu.WhitelistMode;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.ItemAttribute;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient.ItemValue;
import net.minecraft.world.item.crafting.Ingredient.TagValue;
import net.minecraft.world.item.crafting.Ingredient.Value;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.crafting.MultiItemValue;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Collection;

public class BlueprintItem extends Item {

	public BlueprintItem(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Direction face = ctx.getClickedFace();
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();
		BlockPos pos = ctx.getClickedPos()
				.relative(face);

		if (player != null && !player.mayUseItemAt(pos, face, stack))
			return InteractionResult.FAIL;

		Level world = ctx.getLevel();
		HangingEntity hangingentity = new BlueprintEntity(world, pos, face, face.getAxis()
				.isHorizontal() ? Direction.DOWN : ctx.getHorizontalDirection());
		CompoundTag compoundnbt = stack.getTag();

		if (compoundnbt != null)
			EntityType.updateCustomEntityTag(world, player, hangingentity, compoundnbt);
		if (!hangingentity.survives())
			return InteractionResult.CONSUME;
		if (!world.isClientSide) {
			hangingentity.playPlacementSound();
			world.addFreshEntity(hangingentity);
		}

		stack.shrink(1);
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	protected boolean canPlace(Player p_200127_1_, Direction p_200127_2_, ItemStack p_200127_3_,
							   BlockPos p_200127_4_) {
		return p_200127_1_.mayUseItemAt(p_200127_4_, p_200127_2_, p_200127_3_);
	}

	public static void assignCompleteRecipe(Level level, ItemStackHandler inv, Recipe<?> recipe) {
		NonNullList<Ingredient> ingredients = recipe.getIngredients();

		for (int i = 0; i < 9; i++)
			inv.setStackInSlot(i, ItemStack.EMPTY);
		inv.setStackInSlot(9, recipe.getResultItem(level.registryAccess()));

		if (recipe instanceof IShapedRecipe) {
			IShapedRecipe<?> shapedRecipe = (IShapedRecipe<?>) recipe;
			for (int row = 0; row < shapedRecipe.getRecipeHeight(); row++)
				for (int col = 0; col < shapedRecipe.getRecipeWidth(); col++)
					inv.setStackInSlot(row * 3 + col,
							convertIngredientToFilter(ingredients.get(row * shapedRecipe.getRecipeWidth() + col)));
		} else {
			for (int i = 0; i < ingredients.size(); i++)
				inv.setStackInSlot(i, convertIngredientToFilter(ingredients.get(i)));
		}
	}

	private static ItemStack convertIngredientToFilter(Ingredient ingredient) {
		Ingredient.Value[] acceptedItems = ingredient.values;
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

	private static ItemStack convertIItemListToFilter(Value itemList) {
		Collection<ItemStack> stacks = itemList.getItems();
		if (itemList instanceof ItemValue) {
			for (ItemStack itemStack : stacks)
				return itemStack;
		}

		if (itemList instanceof TagValue) {
			ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(itemList.serialize(), "tag"));
			ItemStack filterItem = AllItems.ATTRIBUTE_FILTER.asStack();
			filterItem.getOrCreateTag()
					.putInt("WhitelistMode", WhitelistMode.WHITELIST_DISJ.ordinal());
			ListTag attributes = new ListTag();
			ItemAttribute at = new ItemAttribute.InTag(ItemTags.create(resourcelocation));
			CompoundTag compoundNBT = new CompoundTag();
			at.serializeNBT(compoundNBT);
			compoundNBT.putBoolean("Inverted", false);
			attributes.add(compoundNBT);
			filterItem.getOrCreateTag()
					.put("MatchedAttributes", attributes);
			return filterItem;
		}

		if (itemList instanceof MultiItemValue) {
			ItemStack result = AllItems.FILTER.asStack();
			ItemStackHandler filterItems = FilterItem.getFilterItems(result);
			int i = 0;
			for (ItemStack itemStack : stacks) {
				if (i >= 18)
					break;
				filterItems.setStackInSlot(i++, itemStack);
			}
			CompoundTag tag = result.getOrCreateTag();
			tag.put("Items", filterItems.serializeNBT());
			tag.putBoolean("RespectNBT", true);
			return result;
		}

		return ItemStack.EMPTY;
	}

}
