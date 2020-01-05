package com.simibubi.create.modules.logistics.block.transposer;

import static net.minecraft.state.properties.BlockStateProperties.POWERED;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.linked.LinkBehaviour;
import com.simibubi.create.foundation.behaviour.linked.LinkBehaviour.SlotPositioning;
import com.simibubi.create.modules.logistics.block.extractor.LinkedExtractorBlock;

public class LinkedTransposerTileEntity extends TransposerTileEntity {

	private static LinkBehaviour.SlotPositioning slots;
	public boolean receivedSignal;
	public LinkBehaviour receiver;

	public LinkedTransposerTileEntity() {
		super(AllTileEntities.LINKED_TRANSPOSER.type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		if (slots == null)
			slots = new SlotPositioning(LinkedExtractorBlock::getFrequencySlotPosition,
					LinkedExtractorBlock::getFrequencySlotOrientation).scale(.4f);
		receiver = LinkBehaviour.receiver(this, this::setSignal).withSlotPositioning(slots);
		behaviours.add(receiver);
		super.addBehaviours(behaviours);
	}

	public void setSignal(boolean powered) {
		receivedSignal = powered;
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
