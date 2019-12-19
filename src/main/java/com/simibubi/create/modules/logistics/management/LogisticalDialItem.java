package com.simibubi.create.modules.logistics.management;

import java.util.UUID;

import com.simibubi.create.foundation.item.IAddedByOther;
import com.simibubi.create.foundation.item.IItemWithColorHandler;
import com.simibubi.create.modules.logistics.management.base.LogisticalActorTileEntity;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class LogisticalDialItem extends Item implements IItemWithColorHandler, IAddedByOther {

	public LogisticalDialItem(Properties properties) {
		super(properties);
	}

	@Override
	public IItemColor getColorHandler() {
		return (stack, layer) -> {
			if (layer == 1 && stack.getOrCreateTag().contains("NetworkIDLeast"))
				return LogisticalActorTileEntity.colorFromUUID(stack.getTag().getUniqueId("NetworkID"));
			return 0xFFFFFF;
		};
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (worldIn.isRemote && !stack.hasTag())
			stack.getOrCreateTag().putUniqueId("NetworkID", UUID.randomUUID());
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		ItemStack heldItem = context.getItem();
		boolean isRemote = context.getWorld().isRemote;

		if (!context.getPlayer().isAllowEdit())
			return super.onItemUse(context);

		TileEntity te = context.getWorld().getTileEntity(context.getPos());
		if (!(te instanceof LogisticalActorTileEntity)) {
			if (context.isPlacerSneaking()) {
				if (!isRemote)
					heldItem.getTag().putUniqueId("NetworkID", UUID.randomUUID());
				context.getPlayer().getCooldownTracker().setCooldown(heldItem.getItem(), 5);
				return ActionResultType.SUCCESS;
			}
			return super.onItemUse(context);
		}

		LogisticalActorTileEntity tileEntity = (LogisticalActorTileEntity) te;
		if (context.isPlacerSneaking()) {
			if (!isRemote)
				heldItem.getTag().putUniqueId("NetworkID", tileEntity.getNetworkId());
			context.getPlayer().getCooldownTracker().setCooldown(heldItem.getItem(), 5);
			return ActionResultType.SUCCESS;
		}

		tileEntity.setNetworkId(heldItem.getTag().getUniqueId("NetworkID"));
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (!playerIn.isSneaking())
			return super.onItemRightClick(worldIn, playerIn, handIn);

		ItemStack heldItem = playerIn.getHeldItem(handIn);
		if (!worldIn.isRemote) {
			heldItem.getTag().putUniqueId("NetworkID", UUID.randomUUID());
			playerIn.inventory.markDirty();
		}
		playerIn.getCooldownTracker().setCooldown(heldItem.getItem(), 5);
		return new ActionResult<>(ActionResultType.SUCCESS, heldItem);
	}

}
