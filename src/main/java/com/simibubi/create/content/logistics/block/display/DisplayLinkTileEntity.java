package com.simibubi.create.content.logistics.block.display;

import java.util.List;

import com.simibubi.create.content.logistics.block.display.source.DisplaySource;
import com.simibubi.create.content.logistics.block.display.target.DisplayTarget;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DisplayLinkTileEntity extends SmartTileEntity {

	protected BlockPos targetOffset;

	public DisplaySource activeSource;
	private CompoundTag sourceConfig;

	public DisplayTarget activeTarget;
	public int targetLine;

	public LerpedFloat glow;
	private boolean sendPulse;
	
	public int refreshTicks;

	public DisplayLinkTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		targetOffset = BlockPos.ZERO;
		sourceConfig = new CompoundTag();
		targetLine = 0;
		glow = LerpedFloat.linear()
			.startWithValue(0);
		glow.chase(0, 0.5f, Chaser.EXP);
	}

	@Override
	public void tick() {
		super.tick();
		
		if (isVirtual()) {
			glow.tickChaser();
			return;
		}

		if (activeSource == null)
			return;
		if (level.isClientSide) {
			glow.tickChaser();
			return;
		}
		
		refreshTicks++;
		if (refreshTicks < activeSource.getPassiveRefreshTicks())
			return;
		tickSource();
	}

	public void tickSource() {
		refreshTicks = 0;
		if (getBlockState().getOptionalValue(DisplayLinkBlock.POWERED)
			.orElse(true))
			return;
		if (!level.isClientSide)
			updateGatheredData();
	}

	public void onNoLongerPowered() {
		if (activeSource == null)
			return;
		refreshTicks = 0;
		activeSource.onSignalReset(new DisplayLinkContext(level, this));
		updateGatheredData();
	}

	public void updateGatheredData() {
		BlockPos sourcePosition = getSourcePosition();
		BlockPos targetPosition = getTargetPosition();

		if (!level.isLoaded(targetPosition) || !level.isLoaded(sourcePosition))
			return;

		DisplayTarget target = AllDisplayBehaviours.targetOf(level, targetPosition);
		List<DisplaySource> sources = AllDisplayBehaviours.sourcesOf(level, sourcePosition);
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

		DisplayLinkContext context = new DisplayLinkContext(level, this);
		activeSource.transferData(context, activeTarget, targetLine);
		sendPulse = true;
		sendData();
		
		award(AllAdvancements.DISPLAY_LINK);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		registerAwardables(behaviours, AllAdvancements.DISPLAY_LINK, AllAdvancements.DISPLAY_BOARD);
	}

	@Override
	public void writeSafe(CompoundTag tag) {
		super.writeSafe(tag);
		writeGatheredData(tag);
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		writeGatheredData(tag);
		if (clientPacket && activeTarget != null) 
			tag.putString("TargetType", activeTarget.id.toString());
		if (clientPacket && sendPulse) {
			sendPulse = false;
			NBTHelper.putMarker(tag, "Pulse");
		}
	}

	private void writeGatheredData(CompoundTag tag) {
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
			activeTarget = AllDisplayBehaviours.getTarget(new ResourceLocation(tag.getString("TargetType")));
		if (clientPacket && tag.contains("Pulse"))
			glow.setValue(2);

		if (!tag.contains("Source"))
			return;

		CompoundTag data = tag.getCompound("Source");
		activeSource = AllDisplayBehaviours.getSource(new ResourceLocation(data.getString("Id")));
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
		return getBlockState().getOptionalValue(DisplayLinkBlock.FACING)
			.orElse(Direction.UP)
			.getOpposite();
	}

	public BlockPos getTargetPosition() {
		return worldPosition.offset(targetOffset);
	}

}
