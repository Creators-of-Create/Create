package com.simibubi.create.content.contraptions.components.crusher;

import static com.simibubi.create.content.contraptions.components.crusher.CrushingWheelControllerBlock.VALID;
import static net.minecraft.block.DirectionalBlock.FACING;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class CrushingWheelBlock extends RotatedPillarKineticBlock implements ITE<CrushingWheelTileEntity> {

	public CrushingWheelBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.CRUSHING_WHEEL.create();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(AXIS);
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
		ISelectionContext context) {
		return AllShapes.CRUSHING_WHEEL_COLLISION_SHAPE;
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {

		for (Direction d : Iterate.directions) {
			if (d.getAxis() == state.getValue(AXIS))
				continue;
			if (AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(worldIn.getBlockState(pos.relative(d))))
				worldIn.setBlockAndUpdate(pos.relative(d), Blocks.AIR.defaultBlockState());
		}

		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			worldIn.removeBlockEntity(pos);
		}
	}

	public void updateControllers(BlockState state, World world, BlockPos pos, Direction side) {
		if (side.getAxis() == state.getValue(AXIS))
			return;
		if (world == null)
			return;

		BlockPos controllerPos = pos.relative(side);
		BlockPos otherWheelPos = pos.relative(side, 2);

		boolean controllerExists = AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(world.getBlockState(controllerPos));
		boolean controllerIsValid = controllerExists && world.getBlockState(controllerPos)
				.getValue(VALID);
		Direction controllerOldDirection = controllerExists
				? world.getBlockState(controllerPos)
				.getValue(FACING)
				: null;

		boolean controllerShouldExist = false;
		boolean controllerShouldBeValid = false;
		Direction controllerNewDirection = Direction.DOWN;

		BlockState otherState = world.getBlockState(otherWheelPos);
		if (AllBlocks.CRUSHING_WHEEL.has(otherState)) {
			controllerShouldExist = true;

			CrushingWheelTileEntity te = getTileEntity(world, pos);
			CrushingWheelTileEntity otherTe = getTileEntity(world, otherWheelPos);

			if (te != null && otherTe != null && (te.getSpeed() > 0) != (otherTe.getSpeed() > 0)
					&& te.getSpeed() != 0) {
				Axis wheelAxis = state.getValue(AXIS);
				Axis sideAxis = side.getAxis();
				int controllerADO = Math.round(Math.signum(te.getSpeed())) * side.getAxisDirection().getStep();
				Vector3d controllerDirVec = new Vector3d(wheelAxis == Axis.X ? 1 : 0
						, wheelAxis == Axis.Y ? 1 : 0
						, wheelAxis == Axis.Z ? 1 : 0)
						.cross(new Vector3d(sideAxis == Axis.X ? 1 : 0
								, sideAxis == Axis.Y ? 1 : 0
								, sideAxis == Axis.Z ? 1 : 0));

				controllerNewDirection = Direction.getNearest(controllerDirVec.x * controllerADO
						, controllerDirVec.y * controllerADO
						, controllerDirVec.z * controllerADO);

				controllerShouldBeValid = true;
			}
			if (otherState.getValue(AXIS) != state.getValue(AXIS))
				controllerShouldExist = false;
		}

		if (!controllerShouldExist) {
			if (controllerExists)
				world.setBlockAndUpdate(controllerPos, Blocks.AIR.defaultBlockState());
			return;
		}

		if (!controllerExists) {
			if (!world.getBlockState(controllerPos)
					.getMaterial()
					.isReplaceable())
				return;
			world.setBlockAndUpdate(controllerPos, AllBlocks.CRUSHING_WHEEL_CONTROLLER.getDefaultState()
					.setValue(VALID, controllerShouldBeValid)
					.setValue(FACING, controllerNewDirection));
		} else if (controllerIsValid != controllerShouldBeValid || controllerOldDirection != controllerNewDirection) {
			world.setBlockAndUpdate(controllerPos, world.getBlockState(controllerPos)
					.setValue(VALID, controllerShouldBeValid)
					.setValue(FACING, controllerNewDirection));
		}

		((CrushingWheelControllerBlock) AllBlocks.CRUSHING_WHEEL_CONTROLLER.get())
				.updateSpeed(world.getBlockState(controllerPos), world, controllerPos);

	}

	@Override
	public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (entityIn.getY() < pos.getY() + 1.25f || !entityIn.isOnGround())
			return;

		float speed = getTileEntityOptional(worldIn, pos).map(CrushingWheelTileEntity::getSpeed)
				.orElse(0f);

		double x = 0;
		double z = 0;

		if (state.getValue(AXIS) == Axis.X) {
			z = speed / 20f;
			x += (pos.getX() + .5f - entityIn.getX()) * .1f;
		}
		if (state.getValue(AXIS) == Axis.Z) {
			x = speed / -20f;
			z += (pos.getZ() + .5f - entityIn.getZ()) * .1f;
		}
		entityIn.setDeltaMovement(entityIn.getDeltaMovement()
				.add(x, 0, z));
	}

	@Override
	public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
		for (Direction direction : Iterate.directions) {
			BlockPos neighbourPos = pos.relative(direction);
			BlockState neighbourState = worldIn.getBlockState(neighbourPos);
			Axis stateAxis = state.getValue(AXIS);
			if (AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(neighbourState) && direction.getAxis() != stateAxis)
				return false;
			if (!AllBlocks.CRUSHING_WHEEL.has(neighbourState))
				continue;
			if (neighbourState.getValue(AXIS) != stateAxis || stateAxis != direction.getAxis())
				return false;
		}

		return true;
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.getValue(AXIS);
	}

	@Override
	public float getParticleTargetRadius() {
		return 1.125f;
	}

	@Override
	public float getParticleInitialRadius() {
		return 1f;
	}

	@Override
	public Class<CrushingWheelTileEntity> getTileEntityClass() {
		return CrushingWheelTileEntity.class;
	}

}
