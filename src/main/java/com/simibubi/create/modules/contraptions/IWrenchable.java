package com.simibubi.create.modules.contraptions;

import com.simibubi.create.foundation.utility.VoxelShaper;
import com.simibubi.create.modules.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.modules.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.modules.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.RotatedPillarKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.world.World;

public interface IWrenchable {

	default ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getWorld();
		BlockState rotated = getRotatedBlockState(state, context.getFace());
		if (!rotated.isValidPosition(world, context.getPos()))
			return ActionResultType.PASS;

		KineticTileEntity.switchToBlockState(world, context.getPos(), updateAfterWrenched(rotated, context));

		TileEntity te = context.getWorld().getTileEntity(context.getPos());
		if (te != null)
			te.updateContainingBlockInfo();
		if (te instanceof GeneratingKineticTileEntity) {
			((GeneratingKineticTileEntity) te).updateGeneratedRotation();
		}

		return ActionResultType.SUCCESS;
	}

	default BlockState updateAfterWrenched(BlockState newState, ItemUseContext context) {
		return newState;
	}

	static BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace){
		BlockState newState = originalState;

		if (targetedFace.getAxis() == Direction.Axis.Y) {
			if (originalState.has(HorizontalAxisKineticBlock.HORIZONTAL_AXIS))
				return originalState.with(HorizontalAxisKineticBlock.HORIZONTAL_AXIS, VoxelShaper.axisAsFace(originalState.get(HorizontalAxisKineticBlock.HORIZONTAL_AXIS)).rotateAround(targetedFace.getAxis()).getAxis());
			if (originalState.has(HorizontalKineticBlock.HORIZONTAL_FACING))
				return originalState.with(HorizontalKineticBlock.HORIZONTAL_FACING, originalState.get(HorizontalKineticBlock.HORIZONTAL_FACING).rotateAround(targetedFace.getAxis()));
		}

		if (originalState.has(RotatedPillarKineticBlock.AXIS))
			return originalState.with(RotatedPillarKineticBlock.AXIS, VoxelShaper.axisAsFace(originalState.get(RotatedPillarKineticBlock.AXIS)).rotateAround(targetedFace.getAxis()).getAxis());

		if (!originalState.has(DirectionalKineticBlock.FACING)) return originalState;

		Direction stateFacing = originalState.get(DirectionalKineticBlock.FACING);

		if (stateFacing.getAxis().equals(targetedFace.getAxis())) {
			if (originalState.has(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE)) return originalState.cycle(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
			else return originalState;
		} else {
			do {
				newState = newState.with(DirectionalKineticBlock.FACING, newState.get(DirectionalKineticBlock.FACING).rotateAround(targetedFace.getAxis()));
				if (targetedFace.getAxis() == Direction.Axis.Y && newState.has(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE)) newState = newState.cycle(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
			} while (newState.get(DirectionalKineticBlock.FACING).getAxis().equals(targetedFace.getAxis()));
		}
		return newState;
	}

}
