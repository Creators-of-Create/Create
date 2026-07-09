package com.simibubi.create.content.equipment.clipboard;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.foundation.recipe.ItemCopyingRecipe.SupportsItemCopying;

import net.createmod.catnip.api.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ClipboardBlockItem extends BlockItem implements SupportsItemCopying {

	public ClipboardBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null)
			return InteractionResult.PASS;
		if (player.isShiftKeyDown())
			return super.useOn(context);
		return use(context.getLevel(), player, context.getHand());
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, Player pPlayer, ItemStack pStack,
		BlockState pState) {
		if (pLevel.isClientSide())
			return false;
		if (!(pLevel.getBlockEntity(pPos) instanceof ClipboardBlockEntity cbe))
			return false;
		cbe.notifyUpdate();
		return true;
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);
		if (hand == InteractionHand.OFF_HAND)
			return InteractionResult.PASS;

		player.getCooldowns()
			.addCooldown(heldItem, 10);
		if (world.isClientSide())
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> ClipboardClient.openScreen(player, heldItem.getComponents(), null));
		ClipboardContent content = heldItem.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
		heldItem.set(AllDataComponents.CLIPBOARD_CONTENT, content.setType(ClipboardType.EDITING));

		return InteractionResult.SUCCESS.heldItemTransformedTo(heldItem);
	}

	public void registerModelOverrides() {
		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> ClipboardClient.registerModelOverrides(this));
	}

	@Override
	public DataComponentType<?> getComponentType() {
		return AllDataComponents.CLIPBOARD_CONTENT;
	}

}
