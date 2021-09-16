package com.simibubi.create.content.contraptions.relays.elementary;

import java.util.List;

import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

public class SimpleKineticTileEntity extends KineticTileEntity {

	public SimpleKineticTileEntity(BlockEntityType<? extends SimpleKineticTileEntity> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new BracketedTileEntityBehaviour(this, state -> state.getBlock() instanceof AbstractShaftBlock)
			.withTrigger(state -> AllTriggers.BRACKET_APPLY_TRIGGER.constructTriggerFor(state.getBlock())));
		super.addBehaviours(behaviours);
	}

	@Override
	public AABB makeRenderBoundingBox() {
		return new AABB(worldPosition).inflate(1);
	}

	@Override
	public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
		if (!ICogWheel.isLargeCog(state))
			return super.addPropagationLocations(block, state, neighbours);

		BlockPos.betweenClosedStream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1))
			.forEach(offset -> {
				if (offset.distSqr(0, 0, 0, false) == BlockPos.ZERO.distSqr(1, 1, 0, false))
					neighbours.add(worldPosition.offset(offset));
			});
		return neighbours;
	}

	@Override
	protected boolean isNoisy() {
		return false;
	}

}
