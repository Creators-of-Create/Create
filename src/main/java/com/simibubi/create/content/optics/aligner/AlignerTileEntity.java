package com.simibubi.create.content.optics.aligner;

import java.util.List;

import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AlignerTileEntity extends SmartTileEntity implements ILightHandler.ILightHandlerProvider, ITE<AlignerTileEntity> {
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

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 256.0D;
	}

	@Override
	public Class<AlignerTileEntity> getTileEntityClass() {
		return AlignerTileEntity.class;
	}
}
