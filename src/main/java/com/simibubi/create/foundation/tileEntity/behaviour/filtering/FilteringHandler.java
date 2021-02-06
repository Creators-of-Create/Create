package com.simibubi.create.foundation.tileEntity.behaviour.filtering;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.RaycastHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		PlayerEntity player = event.getPlayer();
		Hand hand = event.getHand();

		if (player.isSneaking() || player.isSpectator())
			return;

		FilteringBehaviour behaviour = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
		if (behaviour == null)
			return;

		BlockRayTraceResult ray = RaycastHelper.rayTraceRange(world, player, 10);
		if (ray == null)
			return;
		if (behaviour instanceof SidedFilteringBehaviour) {
			behaviour = ((SidedFilteringBehaviour) behaviour).get(ray.getFace());
			if (behaviour == null)
				return;
		}
		if (!behaviour.isActive())
			return;
		if (behaviour.slotPositioning instanceof ValueBoxTransform.Sided)
			((Sided) behaviour.slotPositioning).fromSide(ray.getFace());
		if (!behaviour.testHit(ray.getHitVec()))
			return;

		ItemStack toApply = player.getHeldItem(hand)
			.copy();

		if (AllItems.WRENCH.isIn(toApply))
			return;
		if (AllBlocks.MECHANICAL_ARM.isIn(toApply))
			return;
		
		if (event.getSide() != LogicalSide.CLIENT) {
			if (!player.isCreative()) {
				if (toApply.getItem() instanceof FilterItem)
					player.getHeldItem(hand)
						.shrink(1);
				if (behaviour.getFilter()
					.getItem() instanceof FilterItem)
					player.inventory.placeItemBackInInventory(world, behaviour.getFilter());
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
			String translationKey = world.getBlockState(pos)
				.getBlock()
				.getTranslationKey();
			ITextComponent formattedText = new TranslationTextComponent(translationKey);
			player.sendStatusMessage(Lang.createTranslationTextComponent("logistics.filter." + feedback, formattedText)
				.formatted(TextFormatting.WHITE), true);
		}

		event.setCanceled(true);
		event.setCancellationResult(ActionResultType.SUCCESS);
		world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, .25f, .1f);
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean onScroll(double delta) {
		RayTraceResult objectMouseOver = Minecraft.getInstance().objectMouseOver;
		if (!(objectMouseOver instanceof BlockRayTraceResult))
			return false;

		BlockRayTraceResult result = (BlockRayTraceResult) objectMouseOver;
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;
		BlockPos blockPos = result.getPos();

		FilteringBehaviour filtering = TileEntityBehaviour.get(world, blockPos, FilteringBehaviour.TYPE);
		if (filtering == null)
			return false;
		if (mc.player.isSneaking())
			return false;
		if (!mc.player.isAllowEdit())
			return false;
		if (!filtering.isCountVisible())
			return false;
		if (filtering.slotPositioning instanceof ValueBoxTransform.Sided)
			((Sided) filtering.slotPositioning).fromSide(result.getFace());
		if (!filtering.testHit(objectMouseOver.getHitVec()))
			return false;
		ItemStack filterItem = filtering.getFilter();
		filtering.ticksUntilScrollPacket = 10;
		int maxAmount = (filterItem.getItem() instanceof FilterItem) ? 64 : filterItem.getMaxStackSize();
		filtering.scrollableValue =
			(int) MathHelper.clamp(filtering.scrollableValue + delta * (AllKeys.ctrlDown() ? 16 : 1), 0, maxAmount);

		return true;
	}

}
