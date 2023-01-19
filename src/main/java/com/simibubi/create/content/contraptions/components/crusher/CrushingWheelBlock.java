package com.simibubi.create.content.contraptions.components.crusher;

import static com.simibubi.create.content.contraptions.components.crusher.CrushingWheelControllerBlock.VALID;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrushingWheelBlock extends RotatedPillarKineticBlock implements IBE<CrushingWheelBlockEntity> {

	public CrushingWheelBlock(Properties properties) {
		super(properties);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(AXIS);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.CRUSHING_WHEEL_COLLISION_SHAPE;
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		for (Direction d : Iterate.directions) {
			if (d.getAxis() == state.getValue(AXIS))
				continue;
			if (AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(worldIn.getBlockState(pos.relative(d))))
				worldIn.removeBlock(pos.relative(d), isMoving);
		}

		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	public void updateControllers(BlockState state, Level world, BlockPos pos, Direction side) {
		if (side.getAxis() == state.getValue(AXIS))
			return;
		if (world == null)
			return;

		BlockPos controllerPos = pos.relative(side);
		BlockPos otherWheelPos = pos.relative(side, 2);

		boolean controllerExists = AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(world.getBlockState(controllerPos));
		boolean controllerIsValid = controllerExists && world.getBlockState(controllerPos)
			.getValue(VALID);
		Direction controllerOldDirection = controllerExists ? world.getBlockState(controllerPos)
			.getValue(CrushingWheelControllerBlock.FACING) : null;

		boolean controllerShouldExist = false;
		boolean controllerShouldBeValid = false;
		Direction controllerNewDirection = Direction.DOWN;

		BlockState otherState = world.getBlockState(otherWheelPos);
		if (AllBlocks.CRUSHING_WHEEL.has(otherState)) {
			controllerShouldExist = true;

			CrushingWheelBlockEntity be = getBlockEntity(world, pos);
			CrushingWheelBlockEntity otherBE = getBlockEntity(world, otherWheelPos);

			if (be != null && otherBE != null && (be.getSpeed() > 0) != (otherBE.getSpeed() > 0)
				&& be.getSpeed() != 0) {
				Axis wheelAxis = state.getValue(AXIS);
				Axis sideAxis = side.getAxis();
				int controllerADO = Math.round(Math.signum(be.getSpeed())) * side.getAxisDirection()
					.getStep();
				Vec3 controllerDirVec = new Vec3(wheelAxis == Axis.X ? 1 : 0, wheelAxis == Axis.Y ? 1 : 0,
					wheelAxis == Axis.Z ? 1 : 0).cross(
						new Vec3(sideAxis == Axis.X ? 1 : 0, sideAxis == Axis.Y ? 1 : 0, sideAxis == Axis.Z ? 1 : 0));

				controllerNewDirection = Direction.getNearest(controllerDirVec.x * controllerADO,
					controllerDirVec.y * controllerADO, controllerDirVec.z * controllerADO);

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
				.setValue(CrushingWheelControllerBlock.FACING, controllerNewDirection));
		} else if (controllerIsValid != controllerShouldBeValid || controllerOldDirection != controllerNewDirection) {
			world.setBlockAndUpdate(controllerPos, world.getBlockState(controllerPos)
				.setValue(VALID, controllerShouldBeValid)
				.setValue(CrushingWheelControllerBlock.FACING, controllerNewDirection));
		}

		((CrushingWheelControllerBlock) AllBlocks.CRUSHING_WHEEL_CONTROLLER.get())
			.updateSpeed(world.getBlockState(controllerPos), world, controllerPos);

	}

	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		if (entityIn.getY() < pos.getY() + 1.25f || !entityIn.isOnGround())
			return;

		float speed = getBlockEntityOptional(worldIn, pos).map(CrushingWheelBlockEntity::getSpeed)
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
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
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
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
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
	public Class<CrushingWheelBlockEntity> getBlockEntityClass() {
		return CrushingWheelBlockEntity.class;
	}
	
	@Override
	public BlockEntityType<? extends CrushingWheelBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.CRUSHING_WHEEL.get();
	}

}
