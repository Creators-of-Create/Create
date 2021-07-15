package com.simibubi.create.content.logistics.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import net.minecraft.item.Item.Properties;

public class CardboardBoxItem extends Item {

	static final int SLOTS = 9;
	static final List<CardboardBoxItem> ALL_BOXES = new ArrayList<>();

	public CardboardBoxItem(Properties properties) {
		super(properties);
		ALL_BOXES.add(this);
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (!playerIn.isShiftKeyDown())
			return super.use(worldIn, playerIn, handIn);

		ItemStack box = playerIn.getItemInHand(handIn);
		for (ItemStack stack : getContents(box))
			playerIn.inventory.placeItemBackInInventory(worldIn, stack);

		if (!playerIn.isCreative()) {
			box.shrink(1);
		}
		return new ActionResult<>(ActionResultType.SUCCESS, box);
	}

	public static ItemStack containing(List<ItemStack> stacks) {
		ItemStack box = new ItemStack(randomBox());
		CompoundNBT compound = new CompoundNBT();

		NonNullList<ItemStack> list = NonNullList.create();
		list.addAll(stacks);
		ItemStackHelper.saveAllItems(compound, list);

		box.setTag(compound);
		return box;
	}

	@Override
	public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
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
		ItemStackHelper.loadAllItems(box.getOrCreateTag(), list);
		return list;
	}

	public static CardboardBoxItem randomBox() {
		return ALL_BOXES.get(new Random().nextInt(ALL_BOXES.size()));
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		CompoundNBT compoundnbt = stack.getOrCreateTag();

		if (compoundnbt.contains("Address", Constants.NBT.TAG_STRING)) {
			tooltip.add(new StringTextComponent("-> " + compoundnbt.getString("Address"))
					.withStyle(TextFormatting.GOLD));
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
				ITextComponent itextcomponent = itemstack.getHoverName();
				tooltip.add(itextcomponent.plainCopy().append(" x").append(String.valueOf(itemstack.getCount()))
					.withStyle(TextFormatting.GRAY));
			}
		}

		if (j - i > 0) {
			tooltip.add((new TranslationTextComponent("container.shulkerBox.more", j - i))
					.withStyle(TextFormatting.ITALIC));
		}
	}

}
