package com.simibubi.create.content.logistics.block.belts.tunnel;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class BrassTunnelTileEntity extends BeltTunnelTileEntity {

	SidedFilteringBehaviour filtering;

	boolean connectedLeft;
	boolean connectedRight;

	ItemStack stackToDistribute;
	float distributionProgress;
	List<Pair<BlockPos, Direction>> distributionTargets;
	int distributionDistanceLeft;
	int distributionDistanceRight;

	public BrassTunnelTileEntity(TileEntityType<? extends BeltTunnelTileEntity> type) {
		super(type);
		distributionTargets = new ArrayList<>();
		stackToDistribute = ItemStack.EMPTY;
	}

//	@Override
//	public void tick() {
//		super.tick();
//
//		if (stackToDistribute.isEmpty())
//			return;
//		if (distributionProgress == -1) {
//			distributionTargets.clear();
//			for (Pair<BrassTunnelTileEntity, Direction> pair : gatherValidOutputs()) {
//				
//			}
//		}
//
//	}

	@Override
	public void initialize() {
		if (filtering == null) {
			filtering = createSidedFilter();
			putBehaviour(filtering);
		}
		super.initialize();
	}

	public boolean canInsert(Direction side, ItemStack stack) {
		if (filtering != null && !filtering.test(side, stack))
			return false;
		if (!connectedLeft && !connectedRight)
			return true;
		if (!stackToDistribute.isEmpty())
			return false;
		return true;
	}

	public boolean onItemInserted(ItemStack stack) {
		if (!connectedLeft && !connectedRight)
			return false;
		stackToDistribute = stack.copy();
		sendData();
		markDirty();
		return true;
	}

	private List<Pair<BrassTunnelTileEntity, Direction>> gatherValidOutputs() {
		List<Pair<BrassTunnelTileEntity, Direction>> validOutputs = new ArrayList<>();
		addValidOutputsOf(this, validOutputs);
		for (boolean left : Iterate.trueAndFalse) {
			BrassTunnelTileEntity adjacent = this;
			while (adjacent != null) {
				if (!world.isAreaLoaded(adjacent.getPos(), 1))
					return null;
				adjacent = adjacent.getAdjacent(left);
				if (adjacent != null)
					addValidOutputsOf(adjacent, validOutputs);
			}
		}
		return validOutputs;
	}

	private void addValidOutputsOf(BrassTunnelTileEntity tunnelTE,
		List<Pair<BrassTunnelTileEntity, Direction>> validOutputs) {
		BeltTileEntity below = BeltHelper.getSegmentTE(world, tunnelTE.pos.down());
		if (below == null)
			return;
		if (below.getSpeed() != 0) {
			Direction direction = below.getMovementFacing();
			if (tunnelTE.flaps.containsKey(direction))
				validOutputs.add(Pair.of(tunnelTE, direction));
		}

		BlockState blockState = getBlockState();
		if (!AllBlocks.BRASS_TUNNEL.has(blockState))
			return;
		for (boolean left : Iterate.trueAndFalse) {
			Axis axis = blockState.get(BrassTunnelBlock.HORIZONTAL_AXIS);
			Direction baseDirection = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
			Direction direction = left ? baseDirection.rotateYCCW() : baseDirection.rotateY();
			if (tunnelTE.flaps.containsKey(direction)) {
				DirectBeltInputBehaviour inputBehaviour = TileEntityBehaviour.get(world, tunnelTE.pos.down()
					.offset(direction), DirectBeltInputBehaviour.TYPE);
				if (inputBehaviour.canInsertFromSide(direction))
					validOutputs.add(Pair.of(tunnelTE, direction));
			}
		}

	}

	@Override
	public void addBehavioursDeferred(List<TileEntityBehaviour> behaviours) {
		super.addBehavioursDeferred(behaviours);
		filtering = createSidedFilter();
		behaviours.add(filtering);
	}

	protected SidedFilteringBehaviour createSidedFilter() {
		return new SidedFilteringBehaviour(this, new BrassTunnelFilterSlot(), this::makeFilter,
			this::isValidFaceForFilter);
	}

	private FilteringBehaviour makeFilter(Direction side, FilteringBehaviour filter) {
		return filter;
	}

	private boolean isValidFaceForFilter(Direction side) {
		return flaps.containsKey(side);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("ConnectedLeft", connectedLeft);
		compound.putBoolean("ConnectedRight", connectedRight);

		compound.put("StackToDistribute", stackToDistribute.serializeNBT());
		compound.putFloat("DistributionProgress", distributionProgress);
		compound.putInt("DistanceLeft", distributionDistanceLeft);
		compound.putInt("DistanceRight", distributionDistanceRight);
		compound.put("Targets", NBTHelper.writeCompoundList(distributionTargets, pair -> {
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("Pos", NBTUtil.writeBlockPos(pair.getKey()));
			nbt.putInt("Face", pair.getValue()
				.getIndex());
			return nbt;
		}));

		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		connectedLeft = compound.getBoolean("ConnectedLeft");
		connectedRight = compound.getBoolean("ConnectedRight");

		stackToDistribute = ItemStack.read(compound.getCompound("StackToDistribute"));
		distributionProgress = compound.getFloat("DistributionProgress");
		distributionDistanceLeft = compound.getInt("DistanceLeft");
		distributionDistanceRight = compound.getInt("DistanceRight");
		distributionTargets = NBTHelper.readCompoundList(compound.getList("Targets", NBT.TAG_COMPOUND), nbt -> {
			BlockPos pos = NBTUtil.readBlockPos(nbt.getCompound("Pos"));
			Direction face = Direction.byIndex(nbt.getInt("Face"));
			return Pair.of(pos, face);
		});

		super.read(compound);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		boolean wasConnectedLeft = connectedLeft;
		boolean wasConnectedRight = connectedRight;
		super.readClientUpdate(tag);
		if (wasConnectedLeft != connectedLeft || wasConnectedRight != connectedRight) {
			requestModelDataUpdate();
			if (hasWorld())
				world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 16);
		}
		filtering.updateFilterPresence();
	}

	public boolean isConnected(boolean leftSide) {
		return leftSide ? connectedLeft : connectedRight;
	}

	@Override
	public void updateTunnelConnections() {
		super.updateTunnelConnections();
		boolean connectivityChanged = false;
		boolean nowConnectedLeft = determineIfConnected(true);
		boolean nowConnectedRight = determineIfConnected(false);

		if (connectedLeft != nowConnectedLeft) {
			connectedLeft = nowConnectedLeft;
			connectivityChanged = true;
			BrassTunnelTileEntity adjacent = getAdjacent(true);
			if (adjacent != null && !world.isRemote)
				adjacent.updateTunnelConnections();
		}
		if (connectedRight != nowConnectedRight) {
			connectedRight = nowConnectedRight;
			connectivityChanged = true;
			BrassTunnelTileEntity adjacent = getAdjacent(false);
			if (adjacent != null && !world.isRemote)
				adjacent.updateTunnelConnections();
		}

		if (filtering != null)
			filtering.updateFilterPresence();
		if (connectivityChanged)
			sendData();
	}

	protected boolean determineIfConnected(boolean leftSide) {
		if (flaps.isEmpty())
			return false;
		BrassTunnelTileEntity adjacentTunnelTE = getAdjacent(leftSide);
		return adjacentTunnelTE != null && !adjacentTunnelTE.flaps.isEmpty();
	}

	@Nullable
	protected BrassTunnelTileEntity getAdjacent(boolean leftSide) {
		if (!hasWorld())
			return null;

		BlockState blockState = getBlockState();
		if (!AllBlocks.BRASS_TUNNEL.has(blockState))
			return null;

		Axis axis = blockState.get(BrassTunnelBlock.HORIZONTAL_AXIS);
		Direction baseDirection = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
		Direction direction = leftSide ? baseDirection.rotateYCCW() : baseDirection.rotateY();
		BlockPos adjacentPos = pos.offset(direction);
		BlockState adjacentBlockState = world.getBlockState(adjacentPos);

		if (!AllBlocks.BRASS_TUNNEL.has(adjacentBlockState))
			return null;
		if (adjacentBlockState.get(BrassTunnelBlock.HORIZONTAL_AXIS) != axis)
			return null;
		TileEntity adjacentTE = world.getTileEntity(adjacentPos);
		if (!(adjacentTE instanceof BrassTunnelTileEntity))
			return null;
		return (BrassTunnelTileEntity) adjacentTE;
	}

}
