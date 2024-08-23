package com.simibubi.create.content.trains.observer;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.compat.computercraft.events.TrainPassEvent;
import com.simibubi.create.content.contraptions.ITransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TrackObserverBlockEntity extends SmartBlockEntity implements ITransformableBlockEntity {

	public TrackTargetingBehaviour<TrackObserver> edgePoint;

	private FilteringBehaviour filtering;

	public AbstractComputerBehaviour computerBehaviour;
	public @Nullable UUID passingTrainUUID;

	public TrackObserverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(edgePoint = new TrackTargetingBehaviour<>(this, EdgePointType.OBSERVER));
		behaviours.add(filtering = createFilter().withCallback(this::onFilterChanged));
		behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
		filtering.setLabel(Lang.translateDirect("logistics.train_observer.cargo_filter"));
	}

	private void onFilterChanged(ItemStack newFilter) {
		if (level.isClientSide())
			return;
		TrackObserver observer = getObserver();
		if (observer != null)
			observer.setFilterAndNotify(level, newFilter);
	}

	@Override
	public void tick() {
		super.tick();

		if (level.isClientSide())
			return;

		boolean shouldBePowered = false;
		TrackObserver observer = getObserver();
		if (observer != null)
			shouldBePowered = observer.isActivated();
		if (isBlockPowered() == shouldBePowered)
			return;

		if (observer != null && computerBehaviour.hasAttachedComputer()) {
			if (shouldBePowered)
				passingTrainUUID = observer.getCurrentTrain();
			if (passingTrainUUID != null) {
				computerBehaviour.prepareComputerEvent(
						new TrainPassEvent(Create.RAILWAYS.trains.get(passingTrainUUID), shouldBePowered));
				if (!shouldBePowered)
					passingTrainUUID = null;
			}
		}

		BlockState blockState = getBlockState();
		if (blockState.hasProperty(TrackObserverBlock.POWERED))
			level.setBlock(worldPosition, blockState.setValue(TrackObserverBlock.POWERED, shouldBePowered), 3);
		DisplayLinkBlock.notifyGatherers(level, worldPosition);
	}

	@Nullable
	public TrackObserver getObserver() {
		return edgePoint.getEdgePoint();
	}
	
	public ItemStack getFilter() {
		return filtering.getFilter();
	}

	public boolean isBlockPowered() {
		return getBlockState().getOptionalValue(TrackObserverBlock.POWERED)
			.orElse(false);
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return new AABB(worldPosition, edgePoint.getGlobalPosition()).inflate(2);
	}

	@Override
	public void transform(StructureTransform transform) {
		edgePoint.transform(transform);
	}

	public FilteringBehaviour createFilter() {
		return new FilteringBehaviour(this, new ValueBoxTransform() {

			@Override
			public void rotate(BlockState state, PoseStack ms) {
				TransformStack.cast(ms)
					.rotateX(90);
			}

			@Override
			public Vec3 getLocalOffset(BlockState state) {
				return new Vec3(0.5, 15.5 / 16d, 0.5);
			}

		});
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
		if (computerBehaviour.isPeripheralCap(cap))
			return computerBehaviour.getPeripheralCapability();
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		computerBehaviour.removePeripheral();
	}

}
