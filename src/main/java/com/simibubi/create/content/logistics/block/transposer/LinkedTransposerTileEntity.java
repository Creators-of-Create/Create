package com.simibubi.create.content.logistics.block.transposer;

import static net.minecraft.state.properties.BlockStateProperties.POWERED;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.logistics.block.extractor.ExtractorSlots;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkBehaviour;

import net.minecraft.tileentity.TileEntityType;

public class LinkedTransposerTileEntity extends TransposerTileEntity {

	public boolean receivedSignal;
	public LinkBehaviour receiver;

	public LinkedTransposerTileEntity(TileEntityType<? extends LinkedTransposerTileEntity> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		Pair<ValueBoxTransform, ValueBoxTransform> slots = ValueBoxTransform.Dual.makeSlots(ExtractorSlots.Link::new);
		receiver = LinkBehaviour.receiver(this, slots, this::setSignal);
		behaviours.add(receiver);
		super.addBehaviours(behaviours);
	}

	public void setSignal(int powered) {
		receivedSignal = powered > 0;
	}

	@Override
	public void tick() {
		super.tick();
		if (world.isRemote)
			return;
		if (receivedSignal != getBlockState().get(POWERED)) {
			world.setBlockState(pos, getBlockState().cycle(POWERED));
			return;
		}
	}

}
