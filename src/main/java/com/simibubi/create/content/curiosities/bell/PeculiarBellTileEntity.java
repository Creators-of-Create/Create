package com.simibubi.create.content.curiosities.bell;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;

import net.minecraft.tileentity.TileEntityType;

public class PeculiarBellTileEntity extends AbstractBellTileEntity {

	public PeculiarBellTileEntity(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public PartialModel getBellModel() {
		return AllBlockPartials.PECULIAR_BELL;
	}

}
