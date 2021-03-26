package com.simibubi.create.content.logistics.block.belts.tunnel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class BrassTunnelTileEntity extends BeltTunnelTileEntity {

	SidedFilteringBehaviour filtering;

	boolean connectedLeft;
	boolean connectedRight;

	ItemStack stackToDistribute;
	float distributionProgress;
	int distributionDistanceLeft;
	int distributionDistanceRight;
	int previousOutputIndex;

	// <filtered, non-filtered>
	Couple<List<Pair<BlockPos, Direction>>> distributionTargets;

	private boolean syncedOutputActive;
	private Set<BrassTunnelTileEntity> syncSet;

	protected ScrollOptionBehaviour<SelectionMode> selectionMode;
	private LazyOptional<IItemHandler> beltCapability;
	private LazyOptional<IItemHandler> tunnelCapability;

	public BrassTunnelTileEntity(TileEntityType<? extends BeltTunnelTileEntity> type) {
		super(type);
		distributionTargets = Couple.create(ArrayList::new);
		syncSet = new HashSet<>();
		stackToDistribute = ItemStack.EMPTY;
		beltCapability = LazyOptional.empty();
		tunnelCapability = LazyOptional.of(() -> new BrassTunnelItemHandler(this));
		previousOutputIndex = 0;
		syncedOutputActive = false;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.add(selectionMode = new ScrollOptionBehaviour<>(SelectionMode.class,
			Lang.translate("logistics.when_multiple_outputs_available"), this,
			new CenteredSideValueBoxTransform((state, d) -> d == Direction.UP)));
		selectionMode.requiresWrench();

		// Propagate settings across connected tunnels
		selectionMode.withCallback(setting -> {
			for (boolean side : Iterate.trueAndFalse) {
				if (!isConnected(side))
					continue;
				BrassTunnelTileEntity adjacent = getAdjacent(side);
				if (adjacent != null)
					adjacent.selectionMode.setValue(setting);
			}
		});
	}

	@Override
	public void tick() {
		super.tick();
		BeltTileEntity beltBelow = BeltHelper.getSegmentTE(world, pos.down());

		if (distributionProgress > 0)
			distributionProgress--;
		if (beltBelow == null || beltBelow.getSpeed() == 0)
			return;
		if (stackToDistribute.isEmpty() && !syncedOutputActive)
			return;
		if (world.isRemote)
			return;

		if (distributionProgress == -1) {
			distributionTargets.forEach(List::clear);
			distributionDistanceLeft = 0;
			distributionDistanceRight = 0;

			syncSet.clear();
			List<Pair<BrassTunnelTileEntity, Direction>> validOutputs = gatherValidOutputs();
			if (selectionMode.get() == SelectionMode.SYNCHRONIZE) {
				boolean allEmpty = true;
				boolean allFull = true;
				for (BrassTunnelTileEntity te : syncSet) {
					boolean hasStack = !te.stackToDistribute.isEmpty();
					allEmpty &= !hasStack;
					allFull &= hasStack;
				}
				final boolean notifySyncedOut = !allEmpty;
				if (allFull || allEmpty)
					syncSet.forEach(te -> te.syncedOutputActive = notifySyncedOut);
			}

			if (validOutputs == null)
				return;
			if (stackToDistribute.isEmpty())
				return;

			for (Pair<BrassTunnelTileEntity, Direction> pair : validOutputs) {
				BrassTunnelTileEntity tunnel = pair.getKey();
				Direction output = pair.getValue();
				if (insertIntoTunnel(tunnel, output, stackToDistribute, true) == null)
					continue;
				distributionTargets.get(!tunnel.flapFilterEmpty(output))
					.add(Pair.of(tunnel.pos, output));
				int distance = tunnel.pos.getX() + tunnel.pos.getZ() - pos.getX() - pos.getZ();
				if (distance < 0)
					distributionDistanceLeft = Math.max(distributionDistanceLeft, -distance);
				else
					distributionDistanceRight = Math.max(distributionDistanceRight, distance);
			}

			if (distributionTargets.getFirst()
				.isEmpty()
				&& distributionTargets.getSecond()
					.isEmpty())
				return;

			if (selectionMode.get() != SelectionMode.SYNCHRONIZE || syncedOutputActive) {
				distributionProgress = 10;
				sendData();
			}
			return;
		}

		if (distributionProgress != 0)
			return;

		distributionTargets.forEach(list -> {
			if (stackToDistribute.isEmpty())
				return;
			List<Pair<BrassTunnelTileEntity, Direction>> validTargets = new ArrayList<>();
			for (Pair<BlockPos, Direction> pair : list) {
				BlockPos tunnelPos = pair.getKey();
				Direction output = pair.getValue();
				TileEntity te = world.getTileEntity(tunnelPos);
				if (!(te instanceof BrassTunnelTileEntity))
					continue;
				validTargets.add(Pair.of((BrassTunnelTileEntity) te, output));
			}
			distribute(validTargets);
			distributionProgress = -1;
		});
	}

	private static Random rand = new Random();
	private static Map<Pair<BrassTunnelTileEntity, Direction>, ItemStack> distributed = new IdentityHashMap<>();
	private static Set<Pair<BrassTunnelTileEntity, Direction>> full = new HashSet<>();

	private void distribute(List<Pair<BrassTunnelTileEntity, Direction>> validTargets) {
		int amountTargets = validTargets.size();
		if (amountTargets == 0)
			return;

		distributed.clear();
		full.clear();

		int indexStart = previousOutputIndex % amountTargets;
		SelectionMode mode = selectionMode.get();
		boolean force = mode == SelectionMode.FORCED_ROUND_ROBIN || mode == SelectionMode.FORCED_SPLIT;
		boolean split = mode == SelectionMode.FORCED_SPLIT || mode == SelectionMode.SPLIT;

		if (mode == SelectionMode.RANDOMIZE)
			indexStart = rand.nextInt(amountTargets);
		if (mode == SelectionMode.PREFER_NEAREST || mode == SelectionMode.SYNCHRONIZE)
			indexStart = 0;

		ItemStack toDistribute = stackToDistribute.copy();
		for (boolean distributeAgain : Iterate.trueAndFalse) {
			ItemStack toDistributeThisCycle = null;
			int remainingOutputs = amountTargets;
			int leftovers = 0;

			for (boolean simulate : Iterate.trueAndFalse) {
				if (remainingOutputs == 0)
					break;

				leftovers = 0;
				int index = indexStart;
				int stackSize = toDistribute.getCount();
				int splitStackSize = stackSize / remainingOutputs;
				int splitRemainder = stackSize % remainingOutputs;
				int visited = 0;

				toDistributeThisCycle = toDistribute.copy();
				if (!(force || split) && simulate)
					continue;

				while (visited < amountTargets) {
					Pair<BrassTunnelTileEntity, Direction> pair = validTargets.get(index);
					BrassTunnelTileEntity tunnel = pair.getKey();
					Direction side = pair.getValue();
					index = (index + 1) % amountTargets;
					visited++;

					if (full.contains(pair)) {
						if (split && simulate)
							remainingOutputs--;
						continue;
					}

					int count = split ? splitStackSize + (splitRemainder > 0 ? 1 : 0) : stackSize;
					ItemStack toOutput = ItemHandlerHelper.copyStackWithSize(toDistributeThisCycle, count);

					// Grow by 1 to determine if target is full even after a successful transfer
					boolean testWithIncreasedCount = distributed.containsKey(pair);
					int increasedCount = testWithIncreasedCount ? distributed.get(pair)
						.getCount() : 0;
					if (testWithIncreasedCount)
						toOutput.grow(increasedCount);

					ItemStack remainder = insertIntoTunnel(tunnel, side, toOutput, true);

					if (remainder == null || remainder.getCount() == (testWithIncreasedCount ? count + 1 : count)) {
						if (force)
							return;
						if (split && simulate)
							remainingOutputs--;
						if (!simulate)
							full.add(pair);
						continue;
					} else if (!remainder.isEmpty() && !simulate) {
						full.add(pair);
					}

					if (!simulate) {
						toOutput.shrink(remainder.getCount());
						distributed.put(pair, toOutput);
					}

					leftovers += remainder.getCount();
					toDistributeThisCycle.shrink(count);
					if (toDistributeThisCycle.isEmpty())
						break;
					splitRemainder--;
					if (!split)
						break;
				}
			}

			toDistribute.setCount(toDistributeThisCycle.getCount() + leftovers);
			if (leftovers == 0 && distributeAgain)
				break;
			if (!split)
				break;
		}

		int failedTransferrals = 0;
		for (Entry<Pair<BrassTunnelTileEntity, Direction>, ItemStack> entry : distributed.entrySet()) {
			Pair<BrassTunnelTileEntity, Direction> pair = entry.getKey();
			failedTransferrals += insertIntoTunnel(pair.getKey(), pair.getValue(), entry.getValue(), false).getCount();
		}

		toDistribute.grow(failedTransferrals);
		stackToDistribute = ItemHandlerHelper.copyStackWithSize(stackToDistribute, toDistribute.getCount());
		previousOutputIndex++;
		previousOutputIndex %= amountTargets;
		notifyUpdate();
	}

	public void setStackToDistribute(ItemStack stack) {
		stackToDistribute = stack;
		distributionProgress = -1;
		sendData();
		markDirty();
	}

	public ItemStack getStackToDistribute() {
		return stackToDistribute;
	}

	@Nullable
	protected ItemStack insertIntoTunnel(BrassTunnelTileEntity tunnel, Direction side, ItemStack stack,
		boolean simulate) {
		if (stack.isEmpty())
			return stack;
		if (!tunnel.testFlapFilter(side, stack))
			return null;

		BeltTileEntity below = BeltHelper.getSegmentTE(world, tunnel.pos.down());
		if (below == null)
			return null;
		BlockPos offset = tunnel.getPos()
			.down()
			.offset(side);
		DirectBeltInputBehaviour sideOutput = TileEntityBehaviour.get(world, offset, DirectBeltInputBehaviour.TYPE);
		if (sideOutput != null) {
			if (!sideOutput.canInsertFromSide(side))
				return null;
			ItemStack result = sideOutput.handleInsertion(stack, side, simulate);
			if (result.isEmpty() && !simulate)
				tunnel.flap(side, true);
			return result;
		}

		Direction movementFacing = below.getMovementFacing();
		if (side == movementFacing)
			if (!BlockHelper.hasBlockSolidSide(world.getBlockState(offset), world, offset, side.getOpposite())) {
				BeltTileEntity controllerTE = below.getControllerTE();
				if (controllerTE == null)
					return null;

				if (!simulate) {
					tunnel.flap(side, true);
					ItemStack ejected = stack;
					float beltMovementSpeed = below.getDirectionAwareBeltMovementSpeed();
					float movementSpeed = Math.max(Math.abs(beltMovementSpeed), 1 / 8f);
					int additionalOffset = beltMovementSpeed > 0 ? 1 : 0;
					Vector3d outPos = BeltHelper.getVectorForOffset(controllerTE, below.index + additionalOffset);
					Vector3d outMotion = Vector3d.of(side.getDirectionVec()).scale(movementSpeed)
						.add(0, 1 / 8f, 0);
					outPos.add(outMotion.normalize());
					ItemEntity entity = new ItemEntity(world, outPos.x, outPos.y + 6 / 16f, outPos.z, ejected);
					entity.setMotion(outMotion);
					entity.setDefaultPickupDelay();
					entity.velocityChanged = true;
					world.addEntity(entity);
				}

				return ItemStack.EMPTY;
			}

		return null;
	}

	public boolean testFlapFilter(Direction side, ItemStack stack) {
		if (filtering == null)
			return false;
		if (filtering.get(side) == null) {
			FilteringBehaviour adjacentFilter =
				TileEntityBehaviour.get(world, pos.offset(side), FilteringBehaviour.TYPE);
			if (adjacentFilter == null)
				return true;
			return adjacentFilter.test(stack);
		}
		return filtering.test(side, stack);
	}

	public boolean flapFilterEmpty(Direction side) {
		if (filtering == null)
			return false;
		if (filtering.get(side) == null) {
			FilteringBehaviour adjacentFilter =
				TileEntityBehaviour.get(world, pos.offset(side), FilteringBehaviour.TYPE);
			if (adjacentFilter == null)
				return true;
			return adjacentFilter.getFilter()
				.isEmpty();
		}
		return filtering.getFilter(side)
			.isEmpty();
	}

	@Override
	public void initialize() {
		if (filtering == null) {
			filtering = createSidedFilter();
			attachBehaviourLate(filtering);
		}
		super.initialize();
	}

	public boolean canInsert(Direction side, ItemStack stack) {
		if (filtering != null && !filtering.test(side, stack))
			return false;
		if (!hasDistributionBehaviour())
			return true;
		if (!stackToDistribute.isEmpty())
			return false;
		return true;
	}

	public boolean hasDistributionBehaviour() {
		if (flaps.isEmpty())
			return false;
		if (connectedLeft || connectedRight)
			return true;
		BlockState blockState = getBlockState();
		if (!AllBlocks.BRASS_TUNNEL.has(blockState))
			return false;
		Axis axis = blockState.get(BrassTunnelBlock.HORIZONTAL_AXIS);
		for (Direction direction : flaps.keySet())
			if (direction.getAxis() != axis)
				return true;
		return false;
	}

	private List<Pair<BrassTunnelTileEntity, Direction>> gatherValidOutputs() {
		List<Pair<BrassTunnelTileEntity, Direction>> validOutputs = new ArrayList<>();
		boolean synchronize = selectionMode.get() == SelectionMode.SYNCHRONIZE;
		addValidOutputsOf(this, validOutputs);

		for (boolean left : Iterate.trueAndFalse) {
			BrassTunnelTileEntity adjacent = this;
			while (adjacent != null) {
				if (!world.isAreaLoaded(adjacent.getPos(), 1))
					return null;
				adjacent = adjacent.getAdjacent(left);
				if (adjacent == null)
					continue;
				addValidOutputsOf(adjacent, validOutputs);
			}
		}

		if (!syncedOutputActive && synchronize)
			return null;
		return validOutputs;
	}

	private void addValidOutputsOf(BrassTunnelTileEntity tunnelTE,
		List<Pair<BrassTunnelTileEntity, Direction>> validOutputs) {
		syncSet.add(tunnelTE);
		BeltTileEntity below = BeltHelper.getSegmentTE(world, tunnelTE.pos.down());
		if (below == null)
			return;
		Direction movementFacing = below.getMovementFacing();
		BlockState blockState = getBlockState();
		if (!AllBlocks.BRASS_TUNNEL.has(blockState))
			return;

		boolean prioritizeSides = tunnelTE == this;

		for (boolean sidePass : Iterate.trueAndFalse) {
			if (!prioritizeSides && sidePass)
				continue;
			for (Direction direction : Iterate.horizontalDirections) {
				if (direction == movementFacing && below.getSpeed() == 0)
					continue;
				if (prioritizeSides && sidePass == (direction.getAxis() == movementFacing.getAxis()))
					continue;
				if (direction == movementFacing.getOpposite())
					continue;
				if (tunnelTE.sides.contains(direction)) {
					BlockPos offset = tunnelTE.pos.down()
						.offset(direction);
					DirectBeltInputBehaviour inputBehaviour =
						TileEntityBehaviour.get(world, offset, DirectBeltInputBehaviour.TYPE);
					if (inputBehaviour == null) {
						if (direction == movementFacing)
							if (!BlockHelper.hasBlockSolidSide(world.getBlockState(offset), world, offset,
								direction.getOpposite()))
								validOutputs.add(Pair.of(tunnelTE, direction));
						continue;
					}
					if (inputBehaviour.canInsertFromSide(direction))
						validOutputs.add(Pair.of(tunnelTE, direction));
					continue;
				}
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
		return sides.contains(side);
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putBoolean("SyncedOutput", syncedOutputActive);
		compound.putBoolean("ConnectedLeft", connectedLeft);
		compound.putBoolean("ConnectedRight", connectedRight);
		compound.put("StackToDistribute", stackToDistribute.serializeNBT());
		compound.putFloat("DistributionProgress", distributionProgress);
		compound.putInt("PreviousIndex", previousOutputIndex);
		compound.putInt("DistanceLeft", distributionDistanceLeft);
		compound.putInt("DistanceRight", distributionDistanceRight);

		for (boolean filtered : Iterate.trueAndFalse) {
			compound.put(filtered ? "FilteredTargets" : "Targets",
				NBTHelper.writeCompoundList(distributionTargets.get(filtered), pair -> {
					CompoundNBT nbt = new CompoundNBT();
					nbt.put("Pos", NBTUtil.writeBlockPos(pair.getKey()));
					nbt.putInt("Face", pair.getValue()
						.getIndex());
					return nbt;
				}));
		}

		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		boolean wasConnectedLeft = connectedLeft;
		boolean wasConnectedRight = connectedRight;

		syncedOutputActive = compound.getBoolean("SyncedOutput");
		connectedLeft = compound.getBoolean("ConnectedLeft");
		connectedRight = compound.getBoolean("ConnectedRight");
		stackToDistribute = ItemStack.read(compound.getCompound("StackToDistribute"));
		distributionProgress = compound.getFloat("DistributionProgress");
		previousOutputIndex = compound.getInt("PreviousIndex");
		distributionDistanceLeft = compound.getInt("DistanceLeft");
		distributionDistanceRight = compound.getInt("DistanceRight");

		for (boolean filtered : Iterate.trueAndFalse) {
			distributionTargets.set(filtered, NBTHelper
				.readCompoundList(compound.getList(filtered ? "FilteredTargets" : "Targets", NBT.TAG_COMPOUND), nbt -> {
					BlockPos pos = NBTUtil.readBlockPos(nbt.getCompound("Pos"));
					Direction face = Direction.byIndex(nbt.getInt("Face"));
					return Pair.of(pos, face);
				}));
		}

		super.fromTag(state, compound, clientPacket);

		if (!clientPacket)
			return;
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
			if (adjacent != null && !world.isRemote) {
				adjacent.updateTunnelConnections();
				adjacent.selectionMode.setValue(selectionMode.getValue());
				AllTriggers.triggerForNearbyPlayers(AllTriggers.CONNECT_TUNNEL, world, pos, 4);
			}
		}

		if (connectedRight != nowConnectedRight) {
			connectedRight = nowConnectedRight;
			connectivityChanged = true;
			BrassTunnelTileEntity adjacent = getAdjacent(false);
			if (adjacent != null && !world.isRemote) {
				adjacent.updateTunnelConnections();
				adjacent.selectionMode.setValue(selectionMode.getValue());
			}
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
		if (adjacentTE.isRemoved())
			return null;
		if (!(adjacentTE instanceof BrassTunnelTileEntity))
			return null;
		return (BrassTunnelTileEntity) adjacentTE;
	}

	@Override
	public void remove() {
		tunnelCapability.invalidate();
		super.remove();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return tunnelCapability.cast();
		return super.getCapability(capability, side);
	}

	public LazyOptional<IItemHandler> getBeltCapability() {
		if (!beltCapability.isPresent()) {
			TileEntity tileEntity = world.getTileEntity(pos.down());
			if (tileEntity != null)
				beltCapability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		}
		return beltCapability;
	}

	public enum SelectionMode implements INamedIconOptions {
		SPLIT(AllIcons.I_TUNNEL_SPLIT),
		FORCED_SPLIT(AllIcons.I_TUNNEL_FORCED_SPLIT),
		ROUND_ROBIN(AllIcons.I_TUNNEL_ROUND_ROBIN),
		FORCED_ROUND_ROBIN(AllIcons.I_TUNNEL_FORCED_ROUND_ROBIN),
		PREFER_NEAREST(AllIcons.I_TUNNEL_PREFER_NEAREST),
		RANDOMIZE(AllIcons.I_TUNNEL_RANDOMIZE),
		SYNCHRONIZE(AllIcons.I_TUNNEL_SYNCHRONIZE),

		;

		private final String translationKey;
		private final AllIcons icon;

		SelectionMode(AllIcons icon) {
			this.icon = icon;
			this.translationKey = "tunnel.selection_mode." + Lang.asId(name());
		}

		@Override
		public AllIcons getIcon() {
			return icon;
		}

		@Override
		public String getTranslationKey() {
			return translationKey;
		}
	}

	public boolean canTakeItems() {
		return stackToDistribute.isEmpty() && !syncedOutputActive;
	}

}
