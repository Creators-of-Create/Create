package com.simibubi.create.content.logistics.block.transposer;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.logistics.block.AttachedLogisticalBlock;
import com.simibubi.create.content.logistics.block.extractor.ExtractorTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InsertingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InventoryManagementBehaviour.Attachments;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.ItemHandlerHelper;

public class TransposerTileEntity extends ExtractorTileEntity {

	private InsertingBehaviour inserting;

	public TransposerTileEntity() {
		this(AllTileEntities.TRANSPOSER.type);
	}

	protected TransposerTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		inserting = new InsertingBehaviour(this,
				Attachments.toward(() -> AttachedLogisticalBlock.getBlockFacing(getBlockState()).getOpposite()));
		behaviours.add(inserting);
		applyFilteringCallbacks();
	}

	public void applyFilteringCallbacks() {
		extracting.withAmountThreshold(this::amountToExtract).withAdditionalFilter(this::shouldExtract);
	}

	public void filterChanged(ItemStack stack) {
	}

	public int amountToExtract(ItemStack stack) {
		ItemStack tester = stack.copy();
		tester.setCount(64);
		return 64 - inserting.insert(tester, true).getCount();
	}

	public boolean shouldExtract(ItemStack stack) {
		if (isTargetingBelt()) {
			Direction facing = AttachedLogisticalBlock.getBlockFacing(getBlockState()).getOpposite();
			BlockPos targetPos = pos.offset(facing);
			BeltTileEntity te = (BeltTileEntity) world.getTileEntity(targetPos);
			return te.tryInsertingFromSide(facing, stack, true);
		}

		if (filtering.anyAmount())
			return true;
		return inserting.insert(stack, true).isEmpty();
	}

	@Override
	protected boolean isTargetingBelt() {
		BlockPos targetPos = pos.offset(AttachedLogisticalBlock.getBlockFacing(getBlockState()).getOpposite());
		if (!AllBlocks.BELT.has(world.getBlockState(targetPos)))
			return false;
		TileEntity te = world.getTileEntity(targetPos);
		if (te == null || !(te instanceof BeltTileEntity))
			return false;
		return ((KineticTileEntity) te).getSpeed() != 0;
	}

	@Override
	protected boolean canExtract() {
		return inserting.getInventory() != null;
	}

	@Override
	public void onExtract(ItemStack stack) {
		if (isTargetingBelt()) {
			Direction facing = AttachedLogisticalBlock.getBlockFacing(getBlockState()).getOpposite();
			BlockPos targetPos = pos.offset(facing);
			BeltTileEntity te = (BeltTileEntity) world.getTileEntity(targetPos);
			if (te.tryInsertingFromSide(facing, stack, false))
				return;
		}

		ItemStack remainder = inserting.insert(stack, false);
		if (!remainder.isEmpty())
			remainder = ItemHandlerHelper.insertItemStacked(extracting.getInventory(), remainder, false);
		if (!remainder.isEmpty())
			super.onExtract(remainder);
	}

}
