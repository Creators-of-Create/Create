package com.simibubi.create.content.curiosities.clipboard;

import javax.annotation.Nonnull;

import com.simibubi.create.foundation.gui.ScreenOpener;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.ItemHandlerHelper;

public class ClipboardBlockItem extends BlockItem {

	public ClipboardBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null)
			return InteractionResult.PASS;
		if (player.isSteppingCarefully())
			return super.useOn(context);
		return use(context.getLevel(), player, context.getHand()).getResult();
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, Player pPlayer, ItemStack pStack,
		BlockState pState) {
		if (pLevel.isClientSide())
			return false;
		if (!(pLevel.getBlockEntity(pPos) instanceof ClipboardBlockEntity cbe))
			return false;
		cbe.dataContainer = ItemHandlerHelper.copyStackWithSize(pStack, 1);
		cbe.notifyUpdate();
		return true;
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
			ScreenOpener.open(new ClipboardScreen(player.getInventory().selected, stack, null));
	}

	public void registerModelOverrides() {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClipboardOverrides.registerModelOverridesClient(this));
	}

}
