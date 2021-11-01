package com.simibubi.create.content.logistics.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import net.minecraft.world.item.Item.Properties;

public class CardboardBoxItem extends Item {

	static final int SLOTS = 9;
	static final List<CardboardBoxItem> ALL_BOXES = new ArrayList<>();

	public CardboardBoxItem(Properties properties) {
		super(properties);
		ALL_BOXES.add(this);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (!playerIn.isShiftKeyDown())
			return super.use(worldIn, playerIn, handIn);

		ItemStack box = playerIn.getItemInHand(handIn);
		for (ItemStack stack : getContents(box))
			playerIn.inventory.placeItemBackInInventory(worldIn, stack);

		if (!playerIn.isCreative()) {
			box.shrink(1);
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, box);
	}

	public static ItemStack containing(List<ItemStack> stacks) {
		ItemStack box = new ItemStack(randomBox());
		CompoundTag compound = new CompoundTag();

		NonNullList<ItemStack> list = NonNullList.create();
		list.addAll(stacks);
		ContainerHelper.saveAllItems(compound, list);

		box.setTag(compound);
		return box;
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
	}
	
	public static void addAddress(ItemStack box, String address) {
		box.getOrCreateTag().putString("Address", address);
	}

	public static boolean matchAddress(ItemStack box, String other) {
		String address = box.getTag().getString("Address");
		if (address == null || address.isEmpty())
			return false;
		if (address.equals("*"))
			return true;
		if (address.equals(other))
			return true;
		if (address.endsWith("*") && other.startsWith(address.substring(0, address.length() - 1)))
			return true;

		return false;
	}

	public static List<ItemStack> getContents(ItemStack box) {
		NonNullList<ItemStack> list = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(box.getOrCreateTag(), list);
		return list;
	}

	public static CardboardBoxItem randomBox() {
		return ALL_BOXES.get(new Random().nextInt(ALL_BOXES.size()));
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		CompoundTag compoundnbt = stack.getOrCreateTag();

		if (compoundnbt.contains("Address", Constants.NBT.TAG_STRING)) {
			tooltip.add(new TextComponent("-> " + compoundnbt.getString("Address"))
					.withStyle(ChatFormatting.GOLD));
		}

		if (!compoundnbt.contains("Items", Constants.NBT.TAG_LIST))
			return;

		int i = 0;
		int j = 0;

		for (ItemStack itemstack : getContents(stack)) {
			if (itemstack.isEmpty())
				continue;

			++j;
			if (i <= 4) {
				++i;
				Component itextcomponent = itemstack.getHoverName();
				tooltip.add(itextcomponent.plainCopy().append(" x").append(String.valueOf(itemstack.getCount()))
					.withStyle(ChatFormatting.GRAY));
			}
		}

		if (j - i > 0) {
			tooltip.add((new TranslatableComponent("container.shulkerBox.more", j - i))
					.withStyle(ChatFormatting.ITALIC));
		}
	}

}
