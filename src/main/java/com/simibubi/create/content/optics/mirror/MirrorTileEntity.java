package com.simibubi.create.content.optics.mirror;

import java.util.List;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.content.optics.ILightHandlerProvider;
import com.simibubi.create.content.optics.behaviour.RotationMode;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MirrorTileEntity extends KineticTileEntity implements ILightHandlerProvider, RotationMode.RotationModeProvider {
	protected ScrollOptionBehaviour<RotationMode> movementMode;
	protected MirrorBehaviour mirror;

	public MirrorTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		setLazyTickRate(20);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		movementMode = new ScrollOptionBehaviour<>(RotationMode.class, Lang.translate("optics.mirror.movement_mode"),
				this, new CenteredSideValueBoxTransform((state, d) -> getAxis() != d.getAxis()));
		movementMode.requiresWrench();
		behaviours.add(movementMode);

		mirror = new MirrorBehaviour(this);
		behaviours.add(mirror);
	}

	@Override
	public boolean shouldRenderAsTE() {
		return true;
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


	Direction.Axis getAxis() {
		return getBlockState().get(BlockStateProperties.AXIS);
	}

	@Override
	public ILightHandler getHandler() {
		return mirror;
	}

	@Override
	public RotationMode getMode() {
		return movementMode.get();
	}
}
