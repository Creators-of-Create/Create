package com.simibubi.create.content.contraptions.components.actors;

import static net.minecraft.block.HorizontalBlock.HORIZONTAL_FACING;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.KelpBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.state.Property;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

public class HarvesterMovementBehaviour extends MovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.get(HORIZONTAL_FACING)
			.getOpposite());
	}

	@Override
	public void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffers) {
		HarvesterRenderer.renderInContraption(context, ms, msLocal, buffers);
	}

	@Override
	public Vector3d getActiveAreaOffset(MovementContext context) {
		return Vector3d.of(context.state.get(HORIZONTAL_FACING)
			.getDirectionVec()).scale(.45);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		World world = context.world;
		BlockState stateVisited = world.getBlockState(pos);
		boolean notCropButCuttable = false;

		if (world.isRemote)
			return;

		if (!isValidCrop(world, pos, stateVisited)) {
			if (isValidOther(world, pos, stateVisited))
				notCropButCuttable = true;
			else
				return;
		}

		MutableBoolean seedSubtracted = new MutableBoolean(notCropButCuttable);
		BlockState state = stateVisited;
		BlockHelper.destroyBlock(world, pos, 1, stack -> {
			if (!seedSubtracted.getValue() && stack.isItemEqual(new ItemStack(state.getBlock()))) {
				stack.shrink(1);
				seedSubtracted.setTrue();
			}
			dropItem(context, stack);
		});

		world.setBlockState(pos, cutCrop(world, pos, stateVisited));
	}

	private boolean isValidCrop(World world, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof CropsBlock) {
			CropsBlock crop = (CropsBlock) state.getBlock();
			if (!crop.isMaxAge(state))
				return false;
			return true;
		}
		if (state.getCollisionShape(world, pos)
			.isEmpty() || state.getBlock() instanceof CocoaBlock) {
			for (Property<?> property : state.getProperties()) {
				if (!(property instanceof IntegerProperty))
					continue;
				if (!property.getName()
					.equals(BlockStateProperties.AGE_0_1.getName()))
					continue;
				if (((IntegerProperty) property).getAllowedValues()
					.size() - 1 != state.get((IntegerProperty) property)
						.intValue())
					continue;
				return true;
			}
		}

		return false;
	}

	private boolean isValidOther(World world, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof CropsBlock)
			return false;
		if (state.getBlock() instanceof SugarCaneBlock)
			return true;

		if (state.getCollisionShape(world, pos)
			.isEmpty() || state.getBlock() instanceof CocoaBlock) {
			for (Property<?> property : state.getProperties()) {
				if (!(property instanceof IntegerProperty))
					continue;
				if (!property.getName()
					.equals(BlockStateProperties.AGE_0_1.getName()))
					continue;
				return false;
			}

			if (state.getBlock() instanceof KelpBlock)
				return true;
			if (state.getBlock() instanceof IPlantable)
				return true;
		}

		return false;
	}

	private BlockState cutCrop(World world, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof CropsBlock) {
			CropsBlock crop = (CropsBlock) state.getBlock();
			return crop.withAge(0);
		}
		if (state.getBlock() == Blocks.SUGAR_CANE) {
			if (state.getFluidState()
				.isEmpty())
				return Blocks.AIR.getDefaultState();
			return state.getFluidState()
				.getBlockState();
		}
		if (state.getCollisionShape(world, pos)
			.isEmpty() || state.getBlock() instanceof CocoaBlock) {
			for (Property<?> property : state.getProperties()) {
				if (!(property instanceof IntegerProperty))
					continue;
				if (!property.getName()
					.equals(BlockStateProperties.AGE_0_1.getName()))
					continue;
				return state.with((IntegerProperty) property, Integer.valueOf(0));
			}
		}

		if (state.getFluidState()
			.isEmpty())
			return Blocks.AIR.getDefaultState();
		return state.getFluidState()
			.getBlockState();
	}

}
