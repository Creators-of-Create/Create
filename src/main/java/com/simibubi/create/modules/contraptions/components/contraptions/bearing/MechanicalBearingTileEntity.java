package com.simibubi.create.modules.contraptions.components.contraptions.bearing;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.Contraption;
import com.simibubi.create.modules.contraptions.components.contraptions.ContraptionEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.IControlContraption;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MechanicalBearingTileEntity extends GeneratingKineticTileEntity implements IControlContraption {

	protected ContraptionEntity movedContraption;
	protected float angle;
	protected boolean running;
	protected boolean assembleNextTick;
	protected boolean isWindmill;

	public MechanicalBearingTileEntity() {
		super(AllTileEntities.MECHANICAL_BEARING.type);
		isWindmill = false;
	}

	@Override
	public float getAddedStressCapacity() {
		return isWindmill ? super.getAddedStressCapacity() : 0;
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
				disassembleConstruct();
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
		int sails = ((BearingContraption) movedContraption.getContraption()).getSailBlocks();
		return MathHelper.clamp(sails, 0, 128);
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

	public float getInterpolatedAngle(float partialTicks) {
		return MathHelper.lerp(partialTicks, angle, angle + getAngularSpeed());
	}

	@Override
	public void onSpeedChanged() {
		super.onSpeedChanged();
		assembleNextTick = true;
	}

	public float getAngularSpeed() {
		return getSpeed() / 2048;
	}

	public void assembleConstruct() {
		Direction direction = getBlockState().get(BlockStateProperties.FACING);

		// Collect Construct
		BearingContraption contraption = BearingContraption.assembleBearingAt(world, pos, direction);
		if (contraption == null)
			return;
		if (isWindmill && contraption.getSailBlocks() == 0)
			return;
		movedContraption = new ContraptionEntity(world, contraption, 0).controlledBy(this);
		BlockPos anchor = pos.offset(direction);
		contraption.removeBlocksFromWorld(world, BlockPos.ZERO);
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

		if (running && Contraption.isFrozen())
			disassembleConstruct();

		if (!world.isRemote && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				boolean canDisassemble = Math.abs(angle) < Math.PI / 4f || Math.abs(angle) > 7 * Math.PI / 4f;
				if (speed == 0 && (canDisassemble || movedContraption == null
						|| movedContraption.getContraption().blocks.isEmpty())) {
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

		float angularSpeed = getAngularSpeed();
		float newAngle = angle + angularSpeed;
		angle = (float) (newAngle % (2 * Math.PI));
		applyRotation();
	}

	private void applyRotation() {
		if (movedContraption != null) {
			Direction direction = getBlockState().get(BlockStateProperties.FACING);
			Vec3d vec = new Vec3d(1, 1, 1).scale(angle * 180 / Math.PI).mul(new Vec3d(direction.getDirectionVec()));
			movedContraption.rotateTo(vec.x, vec.y, -vec.z);
		}
	}

	@Override
	public void attach(ContraptionEntity contraption) {
		if (contraption.getContraption() instanceof BearingContraption) {
			this.movedContraption = contraption;
			BlockPos anchor = pos.offset(getBlockState().get(BlockStateProperties.FACING));
			movedContraption.setPosition(anchor.getX(), anchor.getY(), anchor.getZ());
			if (!world.isRemote)
				sendData();
		}
	}

}
