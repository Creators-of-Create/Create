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
		return state.get(AXIS);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
		ISelectionContext context) {
		return AllShapes.CRUSHING_WHEEL_COLLISION_SHAPE;
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {

		for (Direction d : Iterate.directions) {
			if (d.getAxis() == state.get(AXIS))
				continue;
			if (AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(worldIn.getBlockState(pos.offset(d))))
				worldIn.setBlockState(pos.offset(d), Blocks.AIR.getDefaultState());
		}

		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			worldIn.removeTileEntity(pos);
		}
	}

	public void updateControllers(BlockState state, World world, BlockPos pos, Direction side) {
		if (side.getAxis() == state.get(AXIS))
			return;
		if (world == null)
			return;

		BlockPos controllerPos = pos.offset(side);
		BlockPos otherWheelPos = pos.offset(side, 2);

		boolean controllerExists = AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(world.getBlockState(controllerPos));
		boolean controllerIsValid = controllerExists && world.getBlockState(controllerPos)
				.get(VALID);
		Direction controllerOldDirection = controllerExists
				? world.getBlockState(controllerPos)
				.get(FACING)
				: null;

		boolean controllerShouldExist = false;
		boolean controllerShouldBeValid = false;
		Direction controllerNewDirection = Direction.DOWN;

		BlockState otherState = world.getBlockState(otherWheelPos);
		if (AllBlocks.CRUSHING_WHEEL.has(otherState)) {
			controllerShouldExist = true;

			try {
				CrushingWheelTileEntity te = getTileEntity(world, pos);
				CrushingWheelTileEntity otherTe = getTileEntity(world, otherWheelPos);

				if (te != null && otherTe != null && (te.getSpeed() > 0) != (otherTe.getSpeed() > 0)
						&& te.getSpeed() != 0) {
					Axis wheelAxis = state.get(AXIS);
					Axis sideAxis = side.getAxis();
					int controllerADO = Math.round(Math.signum(te.getSpeed())) * side.getAxisDirection().getOffset();
					Vector3d controllerDirVec = new Vector3d(wheelAxis == Axis.X ? 1 : 0
							, wheelAxis == Axis.Y ? 1 : 0
							, wheelAxis == Axis.Z ? 1 : 0)
							.crossProduct(new Vector3d(sideAxis == Axis.X ? 1 : 0
									, sideAxis == Axis.Y ? 1 : 0
									, sideAxis == Axis.Z ? 1 : 0));

					controllerNewDirection = Direction.getFacingFromVector(controllerDirVec.x * controllerADO
							, controllerDirVec.y * controllerADO
							, controllerDirVec.z * controllerADO);

					controllerShouldBeValid = true;
				}
				if (otherState.get(AXIS) != state.get(AXIS))
					controllerShouldExist = false;

			} catch (TileEntityException e) {
				controllerShouldExist = false;
			}
		}

		if (!controllerShouldExist) {
			if (controllerExists)
				world.setBlockState(controllerPos, Blocks.AIR.getDefaultState());
			return;
		}

		if (!controllerExists) {
			if (!world.getBlockState(controllerPos)
					.getMaterial()
					.isReplaceable())
				return;
			world.setBlockState(controllerPos, AllBlocks.CRUSHING_WHEEL_CONTROLLER.getDefaultState()
					.with(VALID, controllerShouldBeValid)
					.with(FACING, controllerNewDirection));
		} else if (controllerIsValid != controllerShouldBeValid || controllerOldDirection != controllerNewDirection) {
			world.setBlockState(controllerPos, world.getBlockState(controllerPos)
					.with(VALID, controllerShouldBeValid)
					.with(FACING, controllerNewDirection));
		}

		((CrushingWheelControllerBlock) AllBlocks.CRUSHING_WHEEL_CONTROLLER.get())
				.updateSpeed(world.getBlockState(controllerPos), world, controllerPos);

	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		try {
			CrushingWheelTileEntity te = getTileEntity(worldIn, pos);
			if (entityIn.getY() < pos.getY() + 1.25f || !entityIn.isOnGround())
				return;

			double x = 0;
			double z = 0;

			if (state.get(AXIS) == Axis.X) {
				z = te.getSpeed() / 20f;
				x += (pos.getX() + .5f - entityIn.getX()) * .1f;
			}
			if (state.get(AXIS) == Axis.Z) {
				x = te.getSpeed() / -20f;
				z += (pos.getZ() + .5f - entityIn.getZ()) * .1f;
			}
			entityIn.setMotion(entityIn.getMotion()
				.add(x, 0, z));

		} catch (TileEntityException e) {
		}
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		for (Direction direction : Iterate.directions) {
			BlockPos neighbourPos = pos.offset(direction);
			BlockState neighbourState = worldIn.getBlockState(neighbourPos);
			Axis stateAxis = state.get(AXIS);
			if (AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(neighbourState) && direction.getAxis() != stateAxis)
				return false;
			if (!AllBlocks.CRUSHING_WHEEL.has(neighbourState))
				continue;
			if (neighbourState.get(AXIS) != stateAxis || stateAxis != direction.getAxis())
				return false;
		}

		return true;
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(AXIS);
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
