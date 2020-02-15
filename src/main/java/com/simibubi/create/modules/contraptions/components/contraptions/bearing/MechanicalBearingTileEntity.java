package com.simibubi.create.modules.contraptions.components.contraptions.bearing;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.modules.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.Contraption;
import com.simibubi.create.modules.contraptions.components.contraptions.ContraptionEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MechanicalBearingTileEntity extends GeneratingKineticTileEntity implements IBearingTileEntity {

	protected boolean isWindmill;
	protected ContraptionEntity movedContraption;
	protected float angle;
	protected boolean running;
	protected boolean assembleNextTick;
	protected float clientAngleDiff;

	public MechanicalBearingTileEntity() {
		super(AllTileEntities.MECHANICAL_BEARING.type);
		isWindmill = false;
		setLazyTickRate(3);
	}

	@Override
	public float getAddedStressCapacity() {
		return isWindmill ? super.getAddedStressCapacity() : 0;
	}

	@Override
	public float getStressApplied() {
		return isWindmill ? 0 : super.getStressApplied();
	}

	public void neighbourChanged() {
		boolean shouldWindmill = world.isBlockPowered(pos);
		if (shouldWindmill == isWindmill)
			return;

		isWindmill = shouldWindmill;
		if (isWindmill && !running)
			assembleNextTick = true;
		if (isWindmill && running)
			updateGeneratedRotation();

		if (!isWindmill && running) {
			updateGeneratedRotation();
			if (getSpeed() == 0)
				assembleNextTick = true;
		}

		sendData();
	}

	@Override
	public void remove() {
		if (!world.isRemote)
			disassembleConstruct();
		super.remove();
	}

	@Override
	public float getGeneratedSpeed() {
		if (!running || !isWindmill)
			return 0;
		if (movedContraption == null)
			return 0;
		int sails = ((BearingContraption) movedContraption.getContraption()).getSailBlocks() / 8;
		return MathHelper.clamp(sails, 1, 64);
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putBoolean("Running", running);
		tag.putBoolean("Windmill", isWindmill);
		tag.putFloat("Angle", angle);
		return super.write(tag);
	}

	@Override
	public void read(CompoundNBT tag) {
		running = tag.getBoolean("Running");
		isWindmill = tag.getBoolean("Windmill");
		angle = tag.getFloat("Angle");
		super.read(tag);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		float angleBefore = angle;
		super.readClientUpdate(tag);
		clientAngleDiff = AngleHelper.getShortestAngleDiff(angleBefore, angle);
		angle = angleBefore;
	}

	@Override
	public float getInterpolatedAngle(float partialTicks) {
		if (movedContraption != null && movedContraption.isStalled())
			partialTicks = 0;
		return MathHelper.lerp(partialTicks, angle, angle + getAngularSpeed());
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		assembleNextTick = true;
	}

	public float getAngularSpeed() {
		float speed = getSpeed() * 3 / 10f;
		if (world.isRemote) {
			speed *= ServerSpeedProvider.get();
			speed += clientAngleDiff / 3f;
		}
		return speed;
	}

	public void assembleConstruct() {
		Direction direction = getBlockState().get(BlockStateProperties.FACING);

		// Collect Construct
		BearingContraption contraption = BearingContraption.assembleBearingAt(world, pos, direction);
		if (contraption == null)
			return;
		if (isWindmill && contraption.getSailBlocks() == 0)
			return;
		contraption.removeBlocksFromWorld(world, BlockPos.ZERO);
		movedContraption = ContraptionEntity.createStationary(world, contraption).controlledBy(this);
		BlockPos anchor = pos.offset(direction);
		movedContraption.setPosition(anchor.getX(), anchor.getY(), anchor.getZ());
		world.addEntity(movedContraption);

		// Run
		running = true;
		angle = 0;
		sendData();

		updateGeneratedRotation();
	}

	public void disassembleConstruct() {
		if (!running)
			return;
		if (movedContraption != null)
			movedContraption.disassemble();
		
		movedContraption = null;
		running = false;
		angle = 0;
		updateGeneratedRotation();
		sendData();
	}

	@Override
	public void tick() {
		super.tick();

		if (world.isRemote)
			clientAngleDiff /= 2;

		if (running && Contraption.isFrozen())
			disassembleConstruct();

		if (!world.isRemote && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				boolean canDisassemble = Math.abs(angle) < 45 || Math.abs(angle) > 7 * 45;
				if (speed == 0 && (canDisassemble || movedContraption == null
						|| movedContraption.getContraption().blocks.isEmpty())) {
					if (movedContraption != null)
						movedContraption.getContraption().stop(world);
					disassembleConstruct();
				}
				return;
			} else {
				if (speed == 0 && !isWindmill)
					return;
				assembleConstruct();
			}
			return;
		}

		if (!running)
			return;

		if (!(movedContraption != null && movedContraption.isStalled())) {
			float angularSpeed = getAngularSpeed();
			float newAngle = angle + angularSpeed;
			angle = (float) (newAngle % 360);
		}

		applyRotation();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (movedContraption != null && !world.isRemote)
			sendData();
	}

	protected void applyRotation() {
		if (movedContraption != null) {
			Axis axis = getBlockState().get(BlockStateProperties.FACING).getAxis();
			Direction direction = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
			Vec3d vec = new Vec3d(1, 1, 1).scale(angle).mul(new Vec3d(direction.getDirectionVec()));
			movedContraption.rotateTo(vec.x, vec.y, vec.z);
		}
	}

	@Override
	public void attach(ContraptionEntity contraption) {
		if (contraption.getContraption() instanceof BearingContraption) {
			this.movedContraption = contraption;
			markDirty();
			BlockPos anchor = pos.offset(getBlockState().get(BlockStateProperties.FACING));
			movedContraption.setPosition(anchor.getX(), anchor.getY(), anchor.getZ());
			if (!world.isRemote)
				sendData();
		}
	}

	@Override
	public void onStall() {
		if (!world.isRemote)
			sendData();
	}

	@Override
	public boolean isValid() {
		return !isRemoved();
	}

}
