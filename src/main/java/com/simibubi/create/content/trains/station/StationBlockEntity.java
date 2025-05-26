package com.simibubi.create.content.trains.station;

import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;


public class StationBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {
	public TrackTargetingBehaviour<GlobalStation> edgePoint;

	public StationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

	}

	@Override
	public void transform(BlockEntity blockEntity, StructureTransform transform) {
		edgePoint.transform(blockEntity, transform);
	}
}
