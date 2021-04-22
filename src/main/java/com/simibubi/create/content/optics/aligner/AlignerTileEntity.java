package com.simibubi.create.content.optics.aligner;

import java.util.List;

import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.tileentity.TileEntityType;

public class AlignerTileEntity extends SmartTileEntity implements ILightHandler.ILightHandlerProvider {
	protected AlignerBehaviour aligner;

	public AlignerTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		setLazyTickRate(20);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		aligner = new AlignerBehaviour(this);
		behaviours.add(aligner);
	}

	@Override
	public ILightHandler getHandler() {
		return aligner;
	}
}
