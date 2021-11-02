package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidStack;

public abstract class FluidManipulationBehaviour extends TileEntityBehaviour {

	protected static class BlockPosEntry {
		public BlockPos pos;
		public int distance;

		public BlockPosEntry(BlockPos pos, int distance) {
			this.pos = pos;
			this.distance = distance;
		}
	}

	MutableBoundingBox affectedArea;
	BlockPos rootPos;
	boolean infinite;
	protected boolean counterpartActed;

	// Search
	static final int searchedPerTick = 256;
	static final int validationTimerMin = 160;
	List<BlockPosEntry> frontier;
	Set<BlockPos> visited;

	int revalidateIn;

	public FluidManipulationBehaviour(SmartTileEntity te) {
		super(te);
		setValidationTimer();
		infinite = false;
		visited = new HashSet<>();
		frontier = new ArrayList<>();
	}

	public void counterpartActed() {
		counterpartActed = true;
	}

	protected int validationTimer() {
		int maxBlocks = maxBlocks();
		// Allow enough time for the server's infinite block threshold to be reached
		return maxBlocks < 0 ? validationTimerMin : Math.max(validationTimerMin, maxBlocks / searchedPerTick + 1);
	}

	protected int setValidationTimer() {
		return revalidateIn = validationTimer();
	}

	protected int setLongValidationTimer() {
		return revalidateIn = validationTimer() * 2;
	}

	protected int maxRange() {
		return AllConfigs.SERVER.fluids.hosePulleyRange.get();
	}

	protected int maxBlocks() {
		return AllConfigs.SERVER.fluids.hosePulleyBlockThreshold.get();
	}

	protected boolean fillInfinite() {
		return AllConfigs.SERVER.fluids.fillInfinite.get();
	}

	public void reset() {
		if (affectedArea != null)
			scheduleUpdatesInAffectedArea();
		affectedArea = null;
		setValidationTimer();
		frontier.clear();
		visited.clear();
		infinite = false;
	}

	@Override
	public void destroy() {
		reset();
		super.destroy();
	}

	protected void scheduleUpdatesInAffectedArea() {
		World world = getWorld();
		BlockPos
			.betweenClosedStream(new BlockPos(affectedArea.x0 - 1, affectedArea.y0 - 1, affectedArea.z0 - 1),
				new BlockPos(affectedArea.x1 + 1, affectedArea.y1 + 1, affectedArea.z1 + 1))
			.forEach(pos -> {
				FluidState nextFluidState = world.getFluidState(pos);
				if (nextFluidState.isEmpty())
					return;
				world.getLiquidTicks()
					.scheduleTick(pos, nextFluidState.getType(), world.getRandom()
						.nextInt(5));
			});
	}

	protected int comparePositions(BlockPosEntry e1, BlockPosEntry e2) {
		Vector3d centerOfRoot = VecHelper.getCenterOf(rootPos);
		BlockPos pos2 = e2.pos;
		BlockPos pos1 = e1.pos;
		if (pos1.getY() != pos2.getY())
			return Integer.compare(pos2.getY(), pos1.getY());
		int compareDistance = Integer.compare(e2.distance, e1.distance);
		if (compareDistance != 0)
			return compareDistance;
		return Double.compare(VecHelper.getCenterOf(pos2)
			.distanceToSqr(centerOfRoot),
			VecHelper.getCenterOf(pos1)
				.distanceToSqr(centerOfRoot));
	}

	protected Fluid search(Fluid fluid, List<BlockPosEntry> frontier, Set<BlockPos> visited,
		BiConsumer<BlockPos, Integer> add, boolean searchDownward) {
		World world = getWorld();
		int maxBlocks = maxBlocks();
		int maxRange = canDrainInfinitely(fluid) ? maxRange() : maxRange() / 2;
		int maxRangeSq = maxRange * maxRange;
		int i;

		for (i = 0; i < searchedPerTick && !frontier.isEmpty()
			&& (visited.size() <= maxBlocks || !canDrainInfinitely(fluid)); i++) {
			BlockPosEntry entry = frontier.remove(0);
			BlockPos currentPos = entry.pos;
			if (visited.contains(currentPos))
				continue;
			visited.add(currentPos);

			FluidState fluidState = world.getFluidState(currentPos);
			if (fluidState.isEmpty())
				continue;

			Fluid currentFluid = FluidHelper.convertToStill(fluidState.getType());
			if (fluid == null)
				fluid = currentFluid;
			if (!currentFluid.isSame(fluid))
				continue;

			add.accept(currentPos, entry.distance);

			for (Direction side : Iterate.directions) {
				if (!searchDownward && side == Direction.DOWN)
					continue;

				BlockPos offsetPos = currentPos.relative(side);
				if (visited.contains(offsetPos))
					continue;
				if (offsetPos.distSqr(rootPos) > maxRangeSq)
					continue;

				FluidState nextFluidState = world.getFluidState(offsetPos);
				if (nextFluidState.isEmpty())
					continue;
				Fluid nextFluid = nextFluidState.getType();
				if (nextFluid == FluidHelper.convertToFlowing(nextFluid) && side == Direction.UP
					&& !VecHelper.onSameAxis(rootPos, offsetPos, Axis.Y))
					continue;

				frontier.add(new BlockPosEntry(offsetPos, entry.distance + 1));
			}
		}

		return fluid;
	}

	protected void playEffect(World world, BlockPos pos, Fluid fluid, boolean fillSound) {
		BlockPos splooshPos = pos == null ? tileEntity.getBlockPos() : pos;

		SoundEvent soundevent = fillSound ? fluid.getAttributes()
			.getFillSound()
			: fluid.getAttributes()
				.getEmptySound();
		if (soundevent == null)
			soundevent =
				fluid.is(FluidTags.LAVA) ? fillSound ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_EMPTY_LAVA
					: fillSound ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_EMPTY;

		world.playSound(null, splooshPos, soundevent, SoundCategory.BLOCKS, 0.3F, 1.0F);
		if (world instanceof ServerWorld)
			AllPackets.sendToNear(world, splooshPos, 10, new FluidSplashPacket(splooshPos, new FluidStack(fluid, 1)));
	}

	protected boolean canDrainInfinitely(Fluid fluid) {
		return maxBlocks() != -1 && !AllTags.AllFluidTags.NO_INFINITE_DRAINING.matches(fluid);
	}

	@Override
	public void write(CompoundNBT nbt, boolean clientPacket) {
		if (rootPos != null)
			nbt.put("LastPos", NBTUtil.writeBlockPos(rootPos));
		if (affectedArea != null) {
			nbt.put("AffectedAreaFrom",
				NBTUtil.writeBlockPos(new BlockPos(affectedArea.x0, affectedArea.y0, affectedArea.z0)));
			nbt.put("AffectedAreaTo",
				NBTUtil.writeBlockPos(new BlockPos(affectedArea.x1, affectedArea.y1, affectedArea.z1)));
		}
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundNBT nbt, boolean clientPacket) {
		if (nbt.contains("LastPos"))
			rootPos = NBTUtil.readBlockPos(nbt.getCompound("LastPos"));
		if (nbt.contains("AffectedAreaFrom") && nbt.contains("AffectedAreaTo"))
			affectedArea = new MutableBoundingBox(NBTUtil.readBlockPos(nbt.getCompound("AffectedAreaFrom")),
				NBTUtil.readBlockPos(nbt.getCompound("AffectedAreaTo")));
		super.read(nbt, clientPacket);
	}

}
