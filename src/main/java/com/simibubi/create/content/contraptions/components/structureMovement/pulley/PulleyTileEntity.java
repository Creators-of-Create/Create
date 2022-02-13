package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionCollider;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.LinearActuatorTileEntity;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class PulleyTileEntity extends LinearActuatorTileEntity {

	protected int initialOffset;
	private float prevAnimatedOffset;

	public PulleyTileEntity(TileEntityType<? extends PulleyTileEntity> type) {
		super(type);
	}

	@Override
	public AxisAlignedBB makeRenderBoundingBox() {
		return super.makeRenderBoundingBox().expandTowards(0, -offset, 0);
	}

	@Override
	public double getViewDistance() {
		return super.getViewDistance() + offset * offset;
	}

	@Override
	public void tick() {
		super.tick();
		if (isVirtual())
			prevAnimatedOffset = offset;
	}

	@Override
	protected void assemble() throws AssemblyException {
		if (!(level.getBlockState(worldPosition)
			.getBlock() instanceof PulleyBlock))
			return;
		if (speed == 0)
			return;
		int maxLength = AllConfigs.SERVER.kinetics.maxRopeLength.get();
		int i = 1;
		while (i <= maxLength) {
			BlockPos ropePos = worldPosition.below(i);
			BlockState ropeState = level.getBlockState(ropePos);
			if (!AllBlocks.ROPE.has(ropeState) && !AllBlocks.PULLEY_MAGNET.has(ropeState)) {
				break;
			}
			++i;
		}
		offset = i - 1;
		if (offset >= getExtensionRange() && getSpeed() > 0)
			return;
		if (offset <= 0 && getSpeed() < 0)
			return;

		// Collect Construct
		if (!level.isClientSide) {
			needsContraption = false;
			BlockPos anchor = worldPosition.below(MathHelper.floor(offset + 1));
			initialOffset = MathHelper.floor(offset);
			PulleyContraption contraption = new PulleyContraption(initialOffset);
			boolean canAssembleStructure = contraption.assemble(level, anchor);

			if (canAssembleStructure) {
				Direction movementDirection = getSpeed() > 0 ? Direction.DOWN : Direction.UP;
				if (ContraptionCollider.isCollidingWithWorld(level, contraption, anchor.relative(movementDirection),
					movementDirection))
					canAssembleStructure = false;
			}

			if (!canAssembleStructure && getSpeed() > 0)
				return;

			for (i = ((int) offset); i > 0; i--) {
				BlockPos offset = worldPosition.below(i);
				BlockState oldState = level.getBlockState(offset);
				if (oldState.getBlock() instanceof IWaterLoggable && oldState.hasProperty(BlockStateProperties.WATERLOGGED)
					&& oldState.getValue(BlockStateProperties.WATERLOGGED)) {
					level.setBlock(offset, Blocks.WATER.defaultBlockState(), 66);
					continue;
				}
				level.setBlock(offset, Blocks.AIR.defaultBlockState(), 66);
			}

			if (!contraption.getBlocks().isEmpty()) {
				contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
				movedContraption = ControlledContraptionEntity.create(level, this, contraption);
				movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
				level.addFreshEntity(movedContraption);
				forceMove = true;
				needsContraption = true;
			}
		}

		clientOffsetDiff = 0;
		running = true;
		sendData();
	}

	@Override
	public void disassemble() {
		if (!running && movedContraption == null)
			return;
		offset = getGridOffset(offset);
		if (movedContraption != null)
			resetContraptionToOffset();

		if (!level.isClientSide) {
			if (!remove) {
				if (offset > 0) {
					BlockPos magnetPos = worldPosition.below((int) offset);
					FluidState ifluidstate = level.getFluidState(magnetPos);
					level.destroyBlock(magnetPos, level.getBlockState(magnetPos)
						.getCollisionShape(level, magnetPos)
						.isEmpty());
					level.setBlock(magnetPos, AllBlocks.PULLEY_MAGNET.getDefaultState()
						.setValue(BlockStateProperties.WATERLOGGED,
							Boolean.valueOf(ifluidstate.getType() == Fluids.WATER)),
						66);
				}

				boolean[] waterlog = new boolean[(int) offset];

				for (int i = 1; i <= ((int) offset) - 1; i++) {
					BlockPos ropePos = worldPosition.below(i);
					FluidState ifluidstate = level.getFluidState(ropePos);
					waterlog[i] = ifluidstate.getType() == Fluids.WATER;
					level.destroyBlock(ropePos, level.getBlockState(ropePos)
						.getCollisionShape(level, ropePos)
						.isEmpty());
				}
				for (int i = 1; i <= ((int) offset) - 1; i++)
					level.setBlock(worldPosition.below(i), AllBlocks.ROPE.getDefaultState()
						.setValue(BlockStateProperties.WATERLOGGED, waterlog[i]), 66);
			}

			if (movedContraption != null)
				movedContraption.disassemble();
		}

		if (movedContraption != null)
			movedContraption.remove();
		movedContraption = null;
		initialOffset = 0;
		running = false;
		sendData();
	}

	@Override
	protected Vector3d toPosition(float offset) {
		if (movedContraption.getContraption() instanceof PulleyContraption) {
			PulleyContraption contraption = (PulleyContraption) movedContraption.getContraption();
			return Vector3d.atLowerCornerOf(contraption.anchor).add(0, contraption.initialOffset - offset, 0);

		}
		return Vector3d.ZERO;
	}

	@Override
	protected void visitNewPosition() {
		super.visitNewPosition();
		if (level.isClientSide)
			return;
		if (movedContraption != null)
			return;
		if (getSpeed() <= 0)
			return;

		BlockPos posBelow = worldPosition.below((int) (offset + getMovementSpeed()) + 1);
		BlockState state = level.getBlockState(posBelow);
		if (!BlockMovementChecks.isMovementNecessary(state, level, posBelow))
			return;
		if (BlockMovementChecks.isBrittle(state))
			return;

		disassemble();
		assembleNextTick = true;
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		initialOffset = compound.getInt("InitialOffset");
		needsContraption = compound.getBoolean("NeedsContraption");
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putInt("InitialOffset", initialOffset);
		super.write(compound, clientPacket);
	}

	@Override
	protected int getExtensionRange() {
		return Math.max(0, Math.min(AllConfigs.SERVER.kinetics.maxRopeLength.get(), worldPosition.getY() - 1));
	}

	@Override
	protected int getInitialOffset() {
		return initialOffset;
	}

	@Override
	protected Vector3d toMotionVector(float speed) {
		return new Vector3d(0, -speed, 0);
	}

	@Override
	protected ValueBoxTransform getMovementModeSlot() {
		return new CenteredSideValueBoxTransform((state, d) -> d == Direction.UP);
	}

	@Override
	public float getInterpolatedOffset(float partialTicks) {
		if (isVirtual())
			return MathHelper.lerp(partialTicks, prevAnimatedOffset, offset);
		boolean moving = running && (movedContraption == null || !movedContraption.isStalled());
		return super.getInterpolatedOffset(moving ? partialTicks : 0.5f);
	}

	public void animateOffset(float forcedOffset) {
		offset = forcedOffset;
	}

	@Override
	public boolean shouldRenderNormally() {
		return false;
	}
}
