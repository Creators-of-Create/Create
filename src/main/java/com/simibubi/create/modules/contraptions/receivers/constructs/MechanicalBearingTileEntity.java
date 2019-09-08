package com.simibubi.create.modules.contraptions.receivers.constructs;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class MechanicalBearingTileEntity extends KineticTileEntity implements ITickableTileEntity {

	protected RotationConstruct movingConstruct;
	protected float angle;
	protected boolean running;
	protected boolean assembleNextTick;

	public MechanicalBearingTileEntity() {
		super(AllTileEntities.MECHANICAL_BEARING.type);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putBoolean("Running", running);
		tag.putFloat("Angle", angle);
		if (running)
			tag.put("Construct", movingConstruct.writeNBT());

		return super.write(tag);
	}

	@Override
	public void read(CompoundNBT tag) {
		running = tag.getBoolean("Running");
		angle = tag.getFloat("Angle");
		if (running)
			movingConstruct = RotationConstruct.fromNBT(tag.getCompound("Construct"));

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
		return speed / 2048;
	}

	public void assembleConstruct() {
		Direction direction = getBlockState().get(BlockStateProperties.FACING);

		// Collect Construct
		movingConstruct = RotationConstruct.getAttachedForRotating(getWorld(), getPos(), direction);
		if (movingConstruct == null)
			return;

		// Run
		running = true;
		angle = 0;
		sendData();

		for (BlockInfo info : movingConstruct.blocks.values()) {
			getWorld().setBlockState(info.pos.add(pos), Blocks.AIR.getDefaultState(), 67);
		}
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
		sendData();
	}

	@Override
	public void tick() {
		if (!world.isRemote && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				if (speed == 0 && (Math.abs(angle) < Math.PI / 4f || Math.abs(angle) > 7 * Math.PI / 4f)) {
					disassembleConstruct();
				}
				return;
			} else {
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
