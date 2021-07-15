package com.simibubi.create.content.contraptions.components.flywheel.engine;

import java.util.List;

import com.jozufozu.flywheel.backend.instancing.IInstanceRendered;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelBlock;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelTileEntity;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EngineTileEntity extends SmartTileEntity implements IInstanceRendered {

	public float appliedCapacity;
	public float appliedSpeed;
	protected FlywheelTileEntity poweredWheel;

	public EngineTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
	}

	protected AxisAlignedBB cachedBoundingBox;
	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if (cachedBoundingBox == null) {
			cachedBoundingBox = super.getRenderBoundingBox().inflate(1.5f);
		}
		return cachedBoundingBox;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (level.isClientSide)
			return;
		if (poweredWheel != null && poweredWheel.isRemoved())
			poweredWheel = null;
		if (poweredWheel == null)
			attachWheel();
	}

	public void attachWheel() {
		Direction engineFacing = getBlockState().getValue(EngineBlock.FACING);
		BlockPos wheelPos = worldPosition.relative(engineFacing, 2);
		BlockState wheelState = level.getBlockState(wheelPos);
		if (!AllBlocks.FLYWHEEL.has(wheelState))
			return;
		Direction wheelFacing = wheelState.getValue(FlywheelBlock.HORIZONTAL_FACING);
		if (wheelFacing.getAxis() != engineFacing.getClockWise().getAxis())
			return;
		if (FlywheelBlock.isConnected(wheelState)
				&& FlywheelBlock.getConnection(wheelState) != engineFacing.getOpposite())
			return;
		TileEntity te = level.getBlockEntity(wheelPos);
		if (te.isRemoved())
			return;
		if (te instanceof FlywheelTileEntity) {
			if (!FlywheelBlock.isConnected(wheelState))
				FlywheelBlock.setConnection(level, te.getBlockPos(), te.getBlockState(), engineFacing.getOpposite());
			poweredWheel = (FlywheelTileEntity) te;
			refreshWheelSpeed();
		}
	}

	public void detachWheel() {
		if (poweredWheel == null || poweredWheel.isRemoved())
			return;
		poweredWheel.setRotation(0, 0);
		FlywheelBlock.setConnection(level, poweredWheel.getBlockPos(), poweredWheel.getBlockState(), null);
		poweredWheel = null;
	}

	@Override
	public void setRemoved() {
		detachWheel();
		super.setRemoved();
	}

	protected void refreshWheelSpeed() {
		if (poweredWheel == null)
			return;
		poweredWheel.setRotation(appliedSpeed, appliedCapacity);
	}


}
