package com.simibubi.create.content.curiosities.clipboard;

import javax.annotation.Nonnull;

import com.simibubi.create.foundation.gui.ScreenOpener;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ClipboardItem extends Item {

	public ClipboardItem(Properties pProperties) {
		super(pProperties);
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer() == null)
			return InteractionResult.PASS;
		return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);
		if (hand == InteractionHand.OFF_HAND)
			return InteractionResultHolder.pass(heldItem);

		player.getCooldowns()
			.addCooldown(heldItem.getItem(), 10);
		if (world.isClientSide)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> openScreen(player, heldItem));
		CompoundTag tag = heldItem.getOrCreateTag();
		tag.putInt("Type", ClipboardOverrides.ClipboardType.EDITING.ordinal());
		heldItem.setTag(tag);
		
		return InteractionResultHolder.success(heldItem);
	}

	@OnlyIn(Dist.CLIENT)
	private void openScreen(Player player, ItemStack stack) {
		if (Minecraft.getInstance().player == player)
			ScreenOpener.open(new ClipboardScreen(player.getInventory().selected, stack));
	}

	public void registerModelOverrides() {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClipboardOverrides.registerModelOverridesClient(this));
	}

}
