package com.simibubi.create.content.contraptions.components.actors;

import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.SingleTargetAutoExtractingBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class PortableStorageInterfaceMovement extends MovementBehaviour {

	private static final String _exporting_ = "Exporting";
	private static final String _delay_ = "Delay";
	private static final String _workingPos_ = "WorkingPos";

	@Override
	public Vec3d getActiveAreaOffset(MovementContext context) {
		return new Vec3d(context.state.get(PortableStorageInterfaceBlock.FACING)
			.getDirectionVec()).scale(.85f);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		Direction currentFacing = getCurrentFacing(context);
		PortableStorageInterfaceTileEntity psi =
			getValidStationaryInterface(context.world, pos, currentFacing.getAxis());
		if (psi == null)
			return;
		if (psi.isTransferring())
			return;
		context.data.put(_workingPos_, NBTUtil.writeBlockPos(pos));
		context.stall = true;
	}

	@Override
	public void tick(MovementContext context) {
		if (!context.data.contains(_workingPos_))
			return;
		if (context.world.isRemote)
			return;

		BlockPos pos = NBTUtil.readBlockPos(context.data.getCompound(_workingPos_));
		PortableStorageInterfaceTileEntity stationaryInterface =
			getValidStationaryInterface(context.world, pos, getCurrentFacing(context).getAxis());
		if (stationaryInterface == null) {
			reset(context);
			return;
		}

		int nextExtract = context.data.getInt(_delay_);
		if (nextExtract > 0) {
			nextExtract--;
			context.data.putInt(_delay_, nextExtract);
			return;
		}

		boolean extract = context.data.getBoolean(_exporting_);
		boolean success = false;
		IItemHandlerModifiable inv = context.contraption.inventory;
		SingleTargetAutoExtractingBehaviour extracting =
			TileEntityBehaviour.get(stationaryInterface, SingleTargetAutoExtractingBehaviour.TYPE);
		FilteringBehaviour filtering = TileEntityBehaviour.get(stationaryInterface, FilteringBehaviour.TYPE);

		if (extract) {
			// Export from Contraption
			Predicate<ItemStack> test = extracting.getFilterTest();
			int exactAmount = extracting.getAmountFromFilter();
			ItemStack itemExtracted = ItemStack.EMPTY;
			if (exactAmount != -1)
				itemExtracted = ItemHelper.extract(inv, test, exactAmount, false);
			else
				itemExtracted = ItemHelper.extract(inv, test, stationaryInterface::amountToExtract, false);

			if (!itemExtracted.isEmpty()) {
				stationaryInterface.onExtract(itemExtracted);
				success = exactAmount == -1;
			}

		} else {
			// Import to Contraption
			if (extracting != null) {
				extracting.setSynchronized(false);
				extracting.withAdditionalFilter(stack -> {
					if (filtering.anyAmount())
						return true;
					return ItemHandlerHelper.insertItemStacked(inv, stack, true)
						.isEmpty();
				});

				extracting.withAmountThreshold(stack -> {
					ItemStack tester = stack.copy();
					tester.setCount(tester.getMaxStackSize());
					return stack.getCount() - ItemHandlerHelper.insertItemStacked(inv, stack, true)
						.getCount();
				});

				extracting.setCallback(stack -> {
					ItemHandlerHelper.insertItemStacked(inv, stack, false);
				});

				success = extracting.extract() && filtering.anyAmount();
				extracting.setSynchronized(true);
				stationaryInterface.applyFilteringCallbacks();
				extracting.setCallback(stationaryInterface::onExtract);
			}
		}

		if (!success) {
			reset(context);
			return;
		}

		context.data.putInt(_delay_, AllConfigs.SERVER.logistics.defaultExtractionTimer.get());
	}

	@Override
	public void stopMoving(MovementContext context) {
		reset(context);
	}

	public void reset(MovementContext context) {
		context.data.remove(_workingPos_);
		context.data.remove(_delay_);
		context.data.remove(_exporting_);
		context.stall = false;
	}

	private PortableStorageInterfaceTileEntity getValidStationaryInterface(World world, BlockPos pos, Axis validAxis) {
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof PortableStorageInterfaceTileEntity))
			return null;
		BlockState blockState = world.getBlockState(pos);
		if (!AllBlocks.PORTABLE_STORAGE_INTERFACE.has(blockState))
			return null;
		if (blockState.get(PortableStorageInterfaceBlock.FACING)
			.getAxis() != validAxis)
			return null;
		if (world.isBlockPowered(pos))
			return null;
		return (PortableStorageInterfaceTileEntity) te;
	}

	private Direction getCurrentFacing(MovementContext context) {
		Vec3d directionVec = new Vec3d(context.state.get(PortableStorageInterfaceBlock.FACING)
			.getDirectionVec());
		directionVec = VecHelper.rotate(directionVec, context.rotation.x, context.rotation.y, context.rotation.z);
		return Direction.getFacingFromVector(directionVec.x, directionVec.y, directionVec.z);
	}

}
