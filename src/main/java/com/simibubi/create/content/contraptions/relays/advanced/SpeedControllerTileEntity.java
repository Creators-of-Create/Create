package com.simibubi.create.content.contraptions.relays.advanced;

import java.util.List;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.motor.CreativeMotorTileEntity;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.content.contraptions.solver.KineticControllerSerial;
import com.simibubi.create.content.contraptions.solver.KineticNode;
import com.simibubi.create.content.contraptions.solver.KineticSolver;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SpeedControllerTileEntity extends KineticTileEntity {

	public static final int DEFAULT_SPEED = 16;
	protected ScrollValueBehaviour targetSpeed;

	boolean hasBracket;

	public SpeedControllerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		hasBracket = false;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		updateBracket();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		Integer max = AllConfigs.SERVER.kinetics.maxRotationSpeed.get();

		targetSpeed =
			new ScrollValueBehaviour(Lang.translate("generic.speed"), this, new ControllerValueBoxTransform());
		targetSpeed.between(-max, max);
		targetSpeed.value = DEFAULT_SPEED;
		targetSpeed.moveText(new Vec3(9, 0, 10));
		targetSpeed.withUnit(i -> Lang.translate("generic.unit.rpm"));
		targetSpeed.withStepFunction(CreativeMotorTileEntity::step);
		behaviours.add(targetSpeed);
	}

	public float getTargetSpeed() {
		return targetSpeed.getValue();
	}

	@Override
	public void onUpdate(Level level, KineticSolver solver, KineticNode node) {
		BlockPos pos = node.getPos(), above = pos.above();
		if (solver.isStressOnlyConnected(pos, above)) {
			solver.getNode(above).get().setController(node, KineticControllerSerial.SPEED_CONTROLLER_COG);
		}
	}

	public void updateBracket() {
		if (level == null || !level.isClientSide)
			return;
		BlockState stateAbove = level.getBlockState(worldPosition.above());
		hasBracket = ICogWheel.isDedicatedCogWheel(stateAbove.getBlock()) && ICogWheel.isLargeCog(stateAbove)
			&& stateAbove.getValue(CogWheelBlock.AXIS).isHorizontal();
	}

	@Override
	public boolean shouldRenderNormally() {
		return true;
	}

	private static class ControllerValueBoxTransform extends ValueBoxTransform.Sided {

		@Override
		protected Vec3 getSouthLocation() {
			return VecHelper.voxelSpace(8, 11f, 16);
		}

		@Override
		protected boolean isSideActive(BlockState state, Direction direction) {
			if (direction.getAxis()
				.isVertical())
				return false;
			return state.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) != direction.getAxis();
		}

		@Override
		protected float getScale() {
			return 0.275f;
		}

	}

}
