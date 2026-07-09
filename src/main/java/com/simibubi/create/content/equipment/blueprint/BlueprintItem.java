package com.simibubi.create.content.equipment.blueprint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.filter.AttributeFilterWhitelistMode;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute.ItemAttributeEntry;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.InTagAttribute;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.items.ItemStackHandler;

public class BlueprintItem extends Item {

	public BlueprintItem(Properties properties) {
		super(properties);
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
		TypedEntityData<EntityType<?>> customData = stack.get(DataComponents.ENTITY_DATA);

		if (customData != null)
			EntityType.updateCustomEntityTag(world, player, hangingentity, customData);
		if (!hangingentity.survives())
			return InteractionResult.CONSUME;
		if (!world.isClientSide()) {
			hangingentity.playPlacementSound();
			world.addFreshEntity(hangingentity);
		}

		stack.shrink(1);
		return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
	}

	public static void assignCompleteRecipe(Level level, ItemStackHandler inv, Recipe<?> recipe) {
		List<Ingredient> ingredients = recipe.placementInfo()
			.ingredients();

		for (int i = 0; i < 9; i++)
			inv.setStackInSlot(i, ItemStack.EMPTY);
		inv.setStackInSlot(9, recipe.display()
			.stream()
			.findFirst()
			.map(display -> display.result()
				.resolveForFirstStack(SlotDisplayContext.fromLevel(level)))
			.orElse(ItemStack.EMPTY));

		if (recipe instanceof ShapedRecipe shapedRecipe) {
			List<Optional<Ingredient>> shapedIngredients = shapedRecipe.getIngredients();
			for (int row = 0; row < shapedRecipe.getHeight(); row++)
				for (int col = 0; col < shapedRecipe.getWidth(); col++)
					inv.setStackInSlot(row * 3 + col,
						shapedIngredients.get(row * shapedRecipe.getWidth() + col)
							.map(BlueprintItem::convertIngredientToFilter)
							.orElse(ItemStack.EMPTY));
		} else {
			for (int i = 0; i < ingredients.size(); i++)
				inv.setStackInSlot(i, convertIngredientToFilter(ingredients.get(i)));
		}
	}

	private static ItemStack convertIngredientToFilter(Ingredient ingredient) {
		boolean isCompoundIngredient = ingredient.getCustomIngredient() instanceof CompoundIngredient;
		if (ingredient.isEmpty())
			return ItemStack.EMPTY;

		if (!ingredient.isCustom()) {
			HolderSet<Item> values = ingredient.getValues();
			if (values.size() > 18)
				return ItemStack.EMPTY;

			Either<TagKey<Item>, List<Holder<Item>>> unwrapped = values.unwrap();
			if (unwrapped.left().isPresent())
				return convertTagToFilter(unwrapped.left().get());

			List<Holder<Item>> holders = unwrapped.right().orElse(List.of());
			if (holders.size() == 1)
				return new ItemStack(holders.get(0));

			ItemStack result = AllItems.FILTER.asStack();
			ItemStackHandler filterItems = AllItems.FILTER.get().getFilterItemHandler(result);
			for (int i = 0; i < holders.size(); i++)
				filterItems.setStackInSlot(i, new ItemStack(holders.get(i)));
			result.set(AllDataComponents.FILTER_ITEMS, ItemHelper.containerContentsFromHandler(filterItems));
			return result;
		}

		Collection<ItemStack> acceptedItems = ingredient.items()
			.map(ItemStack::new)
			.toList();
		if (acceptedItems.size() > 18)
			return ItemStack.EMPTY;
		if (acceptedItems.size() == 1 && !isCompoundIngredient)
			return acceptedItems.iterator().next();

		ItemStack result = AllItems.FILTER.asStack();
		ItemStackHandler filterItems = AllItems.FILTER.get().getFilterItemHandler(result);
		int i = 0;
		for (ItemStack itemStack : acceptedItems)
			filterItems.setStackInSlot(i++, itemStack);
		result.set(AllDataComponents.FILTER_ITEMS, ItemHelper.containerContentsFromHandler(filterItems));
		if (isCompoundIngredient)
			result.set(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, true);
		return result;
	}

	private static ItemStack convertTagToFilter(TagKey<Item> tag) {
		ItemStack filterItem = AllItems.ATTRIBUTE_FILTER.asStack();
		filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.WHITELIST_DISJ);
		List<ItemAttributeEntry> attributes = new ArrayList<>();
		ItemAttribute at = new InTagAttribute(ItemTags.create(tag.location()));
		attributes.add(new ItemAttribute.ItemAttributeEntry(at, false));
		filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, attributes);
		return filterItem;
	}

}
