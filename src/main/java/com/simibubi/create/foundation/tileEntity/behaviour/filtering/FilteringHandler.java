package com.simibubi.create.foundation.tileEntity.behaviour.filtering;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.RaycastHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.items.ItemHandlerHelper;

@EventBusSubscriber
public class FilteringHandler {

	@SubscribeEvent
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		Level world = event.getWorld();
		BlockPos pos = event.getPos();
		Player player = event.getPlayer();
		InteractionHand hand = event.getHand();

		if (player.isShiftKeyDown() || player.isSpectator())
			return;

		FilteringBehaviour behaviour = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
		if (behaviour == null)
			return;

		BlockHitResult ray = RaycastHelper.rayTraceRange(world, player, 10);
		if (ray == null)
			return;
		if (behaviour instanceof SidedFilteringBehaviour) {
			behaviour = ((SidedFilteringBehaviour) behaviour).get(ray.getDirection());
			if (behaviour == null)
				return;
		}
		if (!behaviour.isActive())
			return;
		if (behaviour.slotPositioning instanceof ValueBoxTransform.Sided)
			((Sided) behaviour.slotPositioning).fromSide(ray.getDirection());
		if (!behaviour.testHit(ray.getLocation()))
			return;

		ItemStack toApply = player.getItemInHand(hand)
			.copy();

		if (AllItems.WRENCH.isIn(toApply))
			return;
		if (AllBlocks.MECHANICAL_ARM.isIn(toApply))
			return;
		
		if (event.getSide() != LogicalSide.CLIENT) {
			if (!player.isCreative()) {
				if (toApply.getItem() instanceof FilterItem)
					player.getItemInHand(hand)
						.shrink(1);
				if (behaviour.getFilter()
					.getItem() instanceof FilterItem)
					player.getInventory().placeItemBackInInventory(behaviour.getFilter());
			}
			if (toApply.getItem() instanceof FilterItem)
				toApply.setCount(1);
			behaviour.setFilter(toApply);

		} else {
			ItemStack filter = behaviour.getFilter();
			String feedback = "apply_click_again";
			if (toApply.getItem() instanceof FilterItem || !behaviour.isCountVisible())
				feedback = "apply";
			else if (ItemHandlerHelper.canItemStacksStack(toApply, filter))
				feedback = "apply_count";
			Component formattedText = world.getBlockState(pos)
				.getBlock()
				.getName();
			player.displayClientMessage(Lang.translateDirect("logistics.filter." + feedback, formattedText)
				.withStyle(ChatFormatting.WHITE), true);
		}

		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		world.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean onScroll(double delta) {
		HitResult objectMouseOver = Minecraft.getInstance().hitResult;
		if (!(objectMouseOver instanceof BlockHitResult))
			return false;

		BlockHitResult result = (BlockHitResult) objectMouseOver;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel world = mc.level;
		BlockPos blockPos = result.getBlockPos();

		FilteringBehaviour filtering = TileEntityBehaviour.get(world, blockPos, FilteringBehaviour.TYPE);
		if (filtering == null)
			return false;
		if (mc.player.isShiftKeyDown())
			return false;
		if (!mc.player.mayBuild())
			return false;
		if (!filtering.isCountVisible())
			return false;
		if (!filtering.isActive())
			return false;
		if (filtering.slotPositioning instanceof ValueBoxTransform.Sided)
			((Sided) filtering.slotPositioning).fromSide(result.getDirection());
		if (!filtering.testHit(objectMouseOver.getLocation()))
			return false;
		
		ItemStack filterItem = filtering.getFilter();
		filtering.ticksUntilScrollPacket = 10;
		int maxAmount = (filterItem.getItem() instanceof FilterItem) ? 64 : filterItem.getMaxStackSize();
		int prev = filtering.scrollableValue;
		filtering.scrollableValue =
			(int) Mth.clamp(filtering.scrollableValue + delta * (AllKeys.ctrlDown() ? 16 : 1), 0, maxAmount);
		
		if (prev != filtering.scrollableValue) {
			float pitch = (filtering.scrollableValue) / (float) (maxAmount);
			pitch = Mth.lerp(pitch, 1.5f, 2f);
			AllSoundEvents.SCROLL_VALUE.play(world, mc.player, blockPos, 1, pitch);
		}

		return true;
	}

}
