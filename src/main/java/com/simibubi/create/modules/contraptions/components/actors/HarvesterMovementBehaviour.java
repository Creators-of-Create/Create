package com.simibubi.create.modules.contraptions.components.actors;

import static net.minecraft.block.HorizontalBlock.HORIZONTAL_FACING;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.MovementBehaviour;
import com.simibubi.create.modules.contraptions.components.contraptions.MovementContext;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;

public class HarvesterMovementBehaviour extends MovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion,
				context.state.get(HORIZONTAL_FACING).getOpposite());
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public SuperByteBuffer renderInContraption(MovementContext context) {
		return HarvesterTileEntityRenderer.renderInContraption(context);
	}

	@Override
	public Vec3d getActiveAreaOffset(MovementContext context) {
		return new Vec3d(context.state.get(HORIZONTAL_FACING).getDirectionVec()).scale(.5);
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
		if (state.getCollisionShape(world, pos).isEmpty()) {
			for (IProperty<?> property : state.getProperties()) {
				if (!(property instanceof IntegerProperty))
					continue;
				if (!property.getName().equals(BlockStateProperties.AGE_0_1.getName()))
					continue;
				if (((IntegerProperty) property).getAllowedValues().size() - 1 != state.get((IntegerProperty) property)
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

		if (state.getCollisionShape(world, pos).isEmpty()) {
			for (IProperty<?> property : state.getProperties()) {
				if (!(property instanceof IntegerProperty))
					continue;
				if (!property.getName().equals(BlockStateProperties.AGE_0_1.getName()))
					continue;
				return false;
			}

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
			return Blocks.AIR.getDefaultState();
		}
		if (state.getCollisionShape(world, pos).isEmpty()) {
			for (IProperty<?> property : state.getProperties()) {
				if (!(property instanceof IntegerProperty))
					continue;
				if (!property.getName().equals(BlockStateProperties.AGE_0_1.getName()))
					continue;
				return state.with((IntegerProperty) property, Integer.valueOf(0));
			}
		}

		return Blocks.AIR.getDefaultState();
	}

}
