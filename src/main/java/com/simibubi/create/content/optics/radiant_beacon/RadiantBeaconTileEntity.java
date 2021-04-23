package com.simibubi.create.content.optics.radiant_beacon;

import java.util.List;

import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.content.optics.behaviour.LightEmittingBehaviour;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RadiantBeaconTileEntity extends SmartTileEntity implements ILightHandler.ILightHandlerProvider {
	LightEmittingBehaviour<RadiantBeaconTileEntity> lightEmittingBehaviour;

	public RadiantBeaconTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		setLazyTickRate(20);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		lightEmittingBehaviour = new LightEmittingBehaviour<>(this);
		behaviours.add(lightEmittingBehaviour);
	}

	@Override
	public ILightHandler getHandler() {
		return lightEmittingBehaviour;
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
}
