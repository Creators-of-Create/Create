package com.simibubi.create.modules.contraptions.receivers.constructs;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.modules.contraptions.base.GeneratingKineticTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MechanicalBearingTileEntity extends GeneratingKineticTileEntity {

	protected RotationConstruct movingConstruct;
	protected float angle;
	protected boolean running;
	protected boolean assembleNextTick;
	protected boolean isWindmill;

	public MechanicalBearingTileEntity() {
		super(AllTileEntities.MECHANICAL_BEARING.type);
		isWindmill = false;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return super.getMaxRenderDistanceSquared() * 16;
	}

	@Override
	public float getAddedStressCapacity() {
		return isWindmill ? CreateConfig.parameters.mechanicalBearingCapacity.get().floatValue() : 0;
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
		int sails = movingConstruct.getSailBlocks();
		return MathHelper.clamp(sails, 0, 128);
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putBoolean("Running", running);
		tag.putBoolean("Windmill", isWindmill);
		tag.putFloat("Angle", angle);
		if (running && !RotationConstruct.isFrozen())
			tag.put("Construct", movingConstruct.writeNBT());

		return super.write(tag);
	}

	@Override
	public void read(CompoundNBT tag) {
		running = tag.getBoolean("Running");
		isWindmill = tag.getBoolean("Windmill");
		angle = tag.getFloat("Angle");
		if (running && !RotationConstruct.isFrozen())
			movingConstruct = RotationConstruct.fromNBT(tag.getCompound("Construct"));

		super.read(tag);
	}

	public float getInterpolatedAngle(float partialTicks) {
		if (RotationConstruct.isFrozen())
			return 0;
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
		movingConstruct = RotationConstruct.getAttachedForRotating(getWorld(), getPos(), direction);
		if (movingConstruct == null)
			return;
		if (isWindmill && movingConstruct.getSailBlocks() == 0)
			return;

		// Run
		running = true;
		angle = 0;
		sendData();

		for (BlockInfo info : movingConstruct.blocks.values()) {
			getWorld().setBlockState(info.pos.add(pos), Blocks.AIR.getDefaultState(), 67);
		}

		updateGeneratedRotation();
	}

	public void disassembleConstruct() {
		if (!running)
			return;

		for (BlockInfo block : movingConstruct.blocks.values()) {
			BlockPos targetPos = block.pos.add(pos);
			BlockState state = block.state;

			for (Direction face : Direction.values())
				state = state.updatePostPlacement(face, world.getBlockState(targetPos.offset(face)), world, targetPos,
						targetPos.offset(face));

			world.destroyBlock(targetPos, world.getBlockState(targetPos).getCollisionShape(world, targetPos).isEmpty());
			getWorld().setBlockState(targetPos, state, 3);
			TileEntity tileEntity = world.getTileEntity(targetPos);
			if (tileEntity != null && block.nbt != null) {
				((ChassisTileEntity) tileEntity).setRange(block.nbt.getInt("Range"));
			}
		}

		running = false;
		movingConstruct = null;
		angle = 0;
		updateGeneratedRotation();
		sendData();
	}

	@Override
	public void tick() {
		super.tick();

		if (running && RotationConstruct.isFrozen())
			disassembleConstruct();

		if (!world.isRemote && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				boolean canDisassemble = Math.abs(angle) < Math.PI / 4f || Math.abs(angle) > 7 * Math.PI / 4f;
				if (speed == 0 && (canDisassemble || movingConstruct == null || movingConstruct.blocks.isEmpty())) {
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
	}

}
