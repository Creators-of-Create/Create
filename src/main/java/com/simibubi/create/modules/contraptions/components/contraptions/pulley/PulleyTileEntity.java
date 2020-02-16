package com.simibubi.create.modules.contraptions.components.contraptions.pulley;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.modules.contraptions.components.contraptions.ContraptionEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.LinearActuatorTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PulleyTileEntity extends LinearActuatorTileEntity {

	public PulleyTileEntity() {
		super(AllTileEntities.ROPE_PULLEY.type);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return super.getRenderBoundingBox().expand(0, -offset, 0);
	}

	@Override
	protected void assembleConstruct() {
		if (speed == 0)
			return;
		if (offset >= getExtensionRange() && getSpeed() > 0)
			return;
		if (offset <= 0 && getSpeed() < 0)
			return;

		// Collect Construct
		if (!world.isRemote) {
			BlockPos anchor = pos.down((int) (offset + 1));
			PulleyContraption contraption = PulleyContraption.assemblePulleyAt(world, anchor, (int) offset);
			if (contraption == null && getSpeed() > 0)
				return;
			
			for (int i = ((int) offset); i > 0; i--) {
				BlockPos offset = pos.down(i);
				world.setBlockState(offset, Blocks.AIR.getDefaultState(), 66);
			}
			
			if (contraption != null && !contraption.blocks.isEmpty()) {
				contraption.removeBlocksFromWorld(world, BlockPos.ZERO);
				movedContraption = ContraptionEntity.createStationary(world, contraption).controlledBy(this);
				movedContraption.setPosition(anchor.getX(), anchor.getY(), anchor.getZ());
				world.addEntity(movedContraption);
				forceMove = true;
			}
		}

		running = true;
		sendData();
	}

	@Override
	protected void disassembleConstruct() {
		if (!running)
			return;
		offset = getGridOffset(offset);
		if (movedContraption != null)
			applyContraptionPosition();

		if (!world.isRemote) {
			if (offset > 0) {
				BlockPos magnetPos = pos.down((int) offset);
				world.destroyBlock(magnetPos,
						world.getBlockState(magnetPos).getCollisionShape(world, magnetPos).isEmpty());
				world.setBlockState(magnetPos, AllBlocks.PULLEY_MAGNET.getDefault(), 66);
			}

			for (int i = 1; i <= ((int) offset) - 1; i++) {
				BlockPos ropePos = pos.down(i);
				world.destroyBlock(ropePos, world.getBlockState(ropePos).getCollisionShape(world, ropePos).isEmpty());
			}
			for (int i = 1; i <= ((int) offset) - 1; i++)
				world.setBlockState(pos.down(i), AllBlocks.ROPE.getDefault(), 66);

			if (movedContraption != null)
				movedContraption.disassemble();
		}

		if (movedContraption != null)
			movedContraption.remove();
		movedContraption = null;
		running = false;
		sendData();
	}

	@Override
	protected Vec3d toPosition(float offset) {
		if (movedContraption.getContraption() instanceof PulleyContraption) {
			PulleyContraption contraption = (PulleyContraption) movedContraption.getContraption();
			return new Vec3d(contraption.getAnchor()).add(0, contraption.initialOffset - offset, 0);

		}
		return Vec3d.ZERO;
	}

	@Override
	protected void visitNewPosition() {
		if (world.isRemote)
			return;
		if (movedContraption != null)
			return;
		if (getSpeed() <= 0)
			return;

		BlockPos posBelow = pos.down((int) (offset + getMovementSpeed()) + 1);
		BlockState stateBelow = world.getBlockState(posBelow);
		if (stateBelow.getMaterial().isReplaceable() || stateBelow.getShape(world, posBelow).isEmpty())
			return;

		disassembleConstruct();
		assembleNextTick = true;
	}

	@Override
	protected int getExtensionRange() {
		return Math.min(AllConfigs.SERVER.kinetics.maxRopeLength.get(), pos.getY() - 1);
	}

	@Override
	protected Vec3d toMotionVector(float speed) {
		return new Vec3d(0, -speed, 0);
	}

}
