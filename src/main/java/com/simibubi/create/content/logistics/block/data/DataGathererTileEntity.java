package com.simibubi.create.content.logistics.block.data;

import java.util.List;

import com.simibubi.create.content.logistics.block.data.source.DataGathererSource;
import com.simibubi.create.content.logistics.block.data.target.DataGathererTarget;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DataGathererTileEntity extends SmartTileEntity {

	protected BlockPos targetOffset;

	public DataGathererSource activeSource;
	private CompoundTag sourceConfig;

	public DataGathererTarget activeTarget;
	public int targetLine;

	public int refreshTicks;

	public DataGathererTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		targetOffset = BlockPos.ZERO;
		sourceConfig = new CompoundTag();
		targetLine = 0;
	}

	@Override
	public void tick() {
		super.tick();

		if (activeSource == null)
			return;
		if (level.isClientSide)
			return;
		refreshTicks++;
		if (refreshTicks < activeSource.getPassiveRefreshTicks())
			return;
		tickSource();
	}

	public void tickSource() {
		refreshTicks = 0;
		if (getBlockState().getOptionalValue(DataGathererBlock.POWERED)
			.orElse(true))
			return;
		if (!level.isClientSide)
			updateGatheredData();
	}

	public void onNoLongerPowered() {
		if (activeSource == null)
			return;
		refreshTicks = 0;
		activeSource.onSignalReset(new DataGathererContext(level, this));
		updateGatheredData();
	}

	public void updateGatheredData() {
		BlockPos sourcePosition = getSourcePosition();
		BlockPos targetPosition = getTargetPosition();

		if (!level.isAreaLoaded(targetPosition, 1) || !level.isAreaLoaded(sourcePosition, 1))
			return;

		DataGathererTarget target = AllDataGathererBehaviours.targetOf(level, targetPosition);
		List<DataGathererSource> sources = AllDataGathererBehaviours.sourcesOf(level, sourcePosition);
		boolean notify = false;

		if (activeTarget != target) {
			activeTarget = target;
			notify = true;
		}

		if (activeSource != null && !sources.contains(activeSource)) {
			activeSource = null;
			sourceConfig = new CompoundTag();
			notify = true;
		}

		if (notify)
			notifyUpdate();
		if (activeSource == null || activeTarget == null)
			return;

		DataGathererContext context = new DataGathererContext(level, this);
		activeSource.transferData(context, activeTarget, targetLine);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public void writeSafe(CompoundTag tag, boolean clientPacket) {
		super.writeSafe(tag, clientPacket);
		writeGatheredData(tag, clientPacket);
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		writeGatheredData(tag, clientPacket);
		if (clientPacket && activeTarget != null)
			tag.putString("TargetType", activeTarget.id.toString());
	}

	private void writeGatheredData(CompoundTag tag, boolean clientPacket) {
		tag.put("TargetOffset", NbtUtils.writeBlockPos(targetOffset));
		tag.putInt("TargetLine", targetLine);

		if (activeSource != null) {
			CompoundTag data = sourceConfig.copy();
			data.putString("Id", activeSource.id.toString());
			tag.put("Source", data);
		}
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		targetOffset = NbtUtils.readBlockPos(tag.getCompound("TargetOffset"));
		targetLine = tag.getInt("TargetLine");

		if (clientPacket && tag.contains("TargetType"))
			activeTarget = AllDataGathererBehaviours.getTarget(new ResourceLocation(tag.getString("TargetType")));

		if (!tag.contains("Source"))
			return;

		CompoundTag data = tag.getCompound("Source");
		activeSource = AllDataGathererBehaviours.getSource(new ResourceLocation(data.getString("Id")));
		sourceConfig = new CompoundTag();
		if (activeSource != null)
			sourceConfig = data.copy();
	}

	public void target(BlockPos targetPosition) {
		this.targetOffset = targetPosition.subtract(worldPosition);
	}

	public BlockPos getSourcePosition() {
		return worldPosition.relative(getDirection());
	}

	public CompoundTag getSourceConfig() {
		return sourceConfig;
	}

	public void setSourceConfig(CompoundTag sourceConfig) {
		this.sourceConfig = sourceConfig;
	}

	public Direction getDirection() {
		return getBlockState().getOptionalValue(DataGathererBlock.FACING)
			.orElse(Direction.UP)
			.getOpposite();
	}

	public BlockPos getTargetPosition() {
		return worldPosition.offset(targetOffset);
	}

}
