package com.simibubi.create.content.kinetics.gantry;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.gantry.GantryCarriageBlock;
import com.simibubi.create.content.contraptions.gantry.GantryCarriageBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;
import static com.simibubi.create.content.kinetics.gantry.GantryShaftBlock.PART;

public class GantryShaftBlockEntity extends KineticBlockEntity {

	public GantryShaftBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}

	@Override
	protected boolean syncSequenceContext() {
		return true;
	}

	public void checkAttachedCarriageBlocks() {
		if (!canAssembleOn())
			return;
		for (Direction d : Iterate.directions) {
			if (d.getAxis() == getBlockState().getValue(GantryShaftBlock.FACING)
					.getAxis())
				continue;
			BlockPos offset = worldPosition.relative(d);
			BlockState pinionState = level.getBlockState(offset);
			if (!AllBlocks.GANTRY_CARRIAGE.has(pinionState))
				continue;
			if (pinionState.getValue(GantryCarriageBlock.FACING) != d)
				continue;
			BlockEntity blockEntity = level.getBlockEntity(offset);
			if (blockEntity instanceof GantryCarriageBlockEntity)
				((GantryCarriageBlockEntity) blockEntity).queueAssembly();
		}
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);
		checkAttachedCarriageBlocks();
	}

	@Override
	public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
		boolean connectedViaAxes, boolean connectedViaCogs) {
		float defaultModifier =
			super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);

		if (connectedViaAxes)
			return defaultModifier;
		if (!stateFrom.getValue(GantryShaftBlock.POWERED))
			return defaultModifier;
		if (!AllBlocks.GANTRY_CARRIAGE.has(stateTo))
			return defaultModifier;

		Direction direction = Direction.getNearest(diff.getX(), diff.getY(), diff.getZ());
		if (stateTo.getValue(GantryCarriageBlock.FACING) != direction)
			return defaultModifier;
		return GantryCarriageBlockEntity.getGantryPinionModifier(stateFrom.getValue(GantryShaftBlock.FACING),
			stateTo.getValue(GantryCarriageBlock.FACING));
	}

	@Override
	public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
		if (!AllBlocks.GANTRY_CARRIAGE.has(otherState))
			return false;
		final BlockPos diff = other.getBlockPos()
			.subtract(worldPosition);
		Direction direction = Direction.getNearest(diff.getX(), diff.getY(), diff.getZ());
		return otherState.getValue(GantryCarriageBlock.FACING) == direction;
	}

	public boolean canAssembleOn() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.GANTRY_SHAFT.has(blockState))
			return false;
		if (blockState.getValue(GantryShaftBlock.POWERED))
			return false;
		float speed = getPinionMovementSpeed();

		switch (blockState.getValue(GantryShaftBlock.PART)) {
		case END:
			return speed < 0;
		case MIDDLE:
			return speed != 0;
		case START:
			return speed > 0;
		case SINGLE:
		default:
			return false;
		}
	}

	public float getPinionMovementSpeed() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.GANTRY_SHAFT.has(blockState))
			return 0;
		return Mth.clamp(convertToLinear(-getSpeed()), -.49f, .49f);
	}

	@Override
	protected boolean isNoisy() {
		return false;
	}

	public int attachedShafts() {
		if (level.getBlockState(worldPosition).getBlock() instanceof GantryShaftBlock) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(GantryShaftBlock.FACING);
			GantryShaftBlock.Part part = state.getValue(PART);
			int toReturn = 1;
			int yOffset = 1;
			if (part.equals(GantryShaftBlock.Part.START)) {
				while (true) {
					BlockState targetBlockState = level.getBlockState(worldPosition.relative(facing, yOffset));
					if (targetBlockState.getBlock() instanceof GantryShaftBlock) {
						if (targetBlockState.getValue(FACING).equals(facing)) {
							toReturn++;
							yOffset++;
						} else {
							break;
						}
					} else {
						break;
					}
				}
			} else if (part.equals(GantryShaftBlock.Part.END)) {
				yOffset = -1;
				while (true) {
					BlockState targetBlockState = level.getBlockState(worldPosition.relative(facing, yOffset));
					if (targetBlockState.getBlock() instanceof GantryShaftBlock) {
						if (targetBlockState.getValue(FACING).equals(facing)) {
							toReturn++;
							yOffset--;
						} else {
							break;
						}
					} else {
						break;
					}
				}
			} else if (part.equals(GantryShaftBlock.Part.MIDDLE)) {
				while (true) {
					BlockState targetBlockState = level.getBlockState(worldPosition.relative(facing, yOffset));
					if (targetBlockState.getBlock() instanceof GantryShaftBlock) {
						if (targetBlockState.getValue(FACING).equals(facing)) {
							toReturn++;
							yOffset++;
						} else {
							break;
						}
					} else {
						break;
					}
				}
				yOffset = -1;
				while (true) {
					BlockState targetBlockState = level.getBlockState(worldPosition.relative(facing, yOffset));
					if (targetBlockState.getBlock() instanceof GantryShaftBlock) {
						if (targetBlockState.getValue(FACING).equals(facing)) {
							toReturn++;
							yOffset--;
						} else {
							break;
						}
					} else {
						break;
					}
				}
			}
			return toReturn;
		}
		return -1;
	}

	public int findGantryOffset() {
		if (getBlockState().getBlock() instanceof GantryShaftBlock) {
			if (level.getBlockEntity(getBlockPos()) instanceof GantryShaftBlockEntity gsbe) {
				int start = gsbe.startPos();
				BlockPos startPos = switch (getBlockState().getValue(FACING).getAxis()) {
					case X -> new BlockPos(start, getBlockPos().getY(), getBlockPos().getZ());
					case Y -> new BlockPos(getBlockPos().getX(), start, getBlockPos().getZ());
					case Z -> new BlockPos(getBlockPos().getX(), getBlockPos().getY(), start);
				};
				int offset = 0;
				while (true) {
					BlockPos targetBlockPos = startPos.relative(getBlockState().getValue(FACING), offset);
					if (!(level.getBlockEntity(targetBlockPos) instanceof GantryShaftBlockEntity))
						break;
					offset ++;
					for (Direction d: Direction.values()) {
						if (level.getBlockEntity(targetBlockPos.relative(d)) instanceof GantryCarriageBlockEntity) {
							return offset;
						}
					}
				}
			}
		}
		return -1;
	}

	public int startPos() {
		if (getBlockState().getBlock() instanceof GantryShaftBlock) {
			BlockState state = getBlockState();
			Direction facing = state.getValue(GantryShaftBlock.FACING);
			GantryShaftBlock.Part part = state.getValue(PART);
			if (part.equals(GantryShaftBlock.Part.START) || part.equals(GantryShaftBlock.Part.SINGLE)) {
				return getBlockPos().get(facing.getAxis());
			} else if (part.equals(GantryShaftBlock.Part.END) || part.equals(GantryShaftBlock.Part.MIDDLE)) {
				int offset = -1;
				while (true) {
					BlockState targetBlockState = level.getBlockState(worldPosition.relative(facing, offset));
					if (targetBlockState.getValue(PART).equals(GantryShaftBlock.Part.START)) {
						return worldPosition.relative(facing, offset).get(facing.getAxis());
					} else {
						offset--;
					}
				}
			}
		}
		return -1;
	}
}
