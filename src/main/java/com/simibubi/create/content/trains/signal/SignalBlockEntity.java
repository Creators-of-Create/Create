package com.simibubi.create.content.trains.signal;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.compat.computercraft.events.SignalStateChangeEvent;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.signal.SignalBlock.SignalType;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class SignalBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {

	public static enum OverlayState {
		RENDER, SKIP, DUAL
	}

	public static enum SignalState {
		RED, YELLOW, GREEN, INVALID;

		public boolean isRedLight(float renderTime) {
			return this == RED || this == INVALID && renderTime % 40 < 3;
		}

		public boolean isYellowLight(float renderTime) {
			return this == YELLOW;
		}

		public boolean isGreenLight(float renderTime) {
			return this == GREEN;
		}
	}

	public TrackTargetingBehaviour<SignalBoundary> edgePoint;

	private SignalState state;
	private OverlayState overlay;
	private int switchToRedAfterTrainEntered;
	private boolean lastReportedPower;
	public AbstractComputerBehaviour computerBehaviour;

	public SignalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.state = SignalState.INVALID;
		this.overlay = OverlayState.SKIP;
		this.lastReportedPower = false;
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		if (Mods.COMPUTERCRAFT.isLoaded()) {
			event.registerBlockEntity(
				PeripheralCapability.get(),
				AllBlockEntityTypes.TRACK_SIGNAL.get(),
				(be, context) -> be.computerBehaviour.getPeripheralCapability()
			);
		}
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		NBTHelper.writeEnum(tag, "State", state);
		NBTHelper.writeEnum(tag, "Overlay", overlay);
		tag.putBoolean("Power", lastReportedPower);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		state = NBTHelper.readEnum(tag, "State", SignalState.class);
		overlay = NBTHelper.readEnum(tag, "Overlay", OverlayState.class);
		lastReportedPower = tag.getBoolean("Power");
		invalidateRenderBoundingBox();
	}

	@Nullable
	public SignalBoundary getSignal() {
		return edgePoint.getEdgePoint();
	}

	public boolean isPowered() {
		return state == SignalState.RED;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		edgePoint = new TrackTargetingBehaviour<>(this, EdgePointType.SIGNAL);
		behaviours.add(edgePoint);
		behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide)
			return;

		SignalBoundary boundary = getSignal();
		if (boundary == null) {
			enterState(SignalState.INVALID);
			setOverlay(OverlayState.RENDER);
			return;
		}

		BlockState blockState = getBlockState();

		blockState.getOptionalValue(SignalBlock.POWERED).ifPresent(powered -> {
			if (lastReportedPower == powered)
				return;
			lastReportedPower = powered;
			boundary.updateBlockEntityPower(this);
			notifyUpdate();
		});

		blockState.getOptionalValue(SignalBlock.TYPE)
			.ifPresent(stateType -> {
				SignalType targetType = boundary.getTypeFor(worldPosition);
				if (stateType != targetType) {
					level.setBlock(worldPosition, blockState.setValue(SignalBlock.TYPE, targetType), Block.UPDATE_ALL);
					refreshBlockState();
				}
			});

		enterState(boundary.getStateFor(worldPosition));
		setOverlay(boundary.getOverlayFor(worldPosition));
	}

	public boolean getReportedPower() {
		return lastReportedPower;
	}

	public SignalState getState() {
		return state;
	}

	public OverlayState getOverlay() {
		return overlay;
	}

	public void setOverlay(OverlayState state) {
		if (this.overlay == state)
			return;
		this.overlay = state;
		notifyUpdate();
	}

	public void enterState(SignalState state) {
		if (switchToRedAfterTrainEntered > 0)
			switchToRedAfterTrainEntered--;
		if (this.state == state)
			return;
		if (state == SignalState.RED && switchToRedAfterTrainEntered > 0)
			return;
		this.state = state;
		switchToRedAfterTrainEntered = state == SignalState.GREEN || state == SignalState.YELLOW ? 15 : 0;
		if (computerBehaviour.hasAttachedComputer())
			computerBehaviour.prepareComputerEvent(new SignalStateChangeEvent(state));
		notifyUpdate();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		computerBehaviour.removePeripheral();
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return new AABB(Vec3.atLowerCornerOf(worldPosition), Vec3.atLowerCornerOf(edgePoint.getGlobalPosition())).inflate(2);
	}

	@Override
	public void transform(BlockEntity be, StructureTransform transform) {
		edgePoint.transform(be, transform);
	}

}
