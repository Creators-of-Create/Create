package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;

public class HosePulleyTileEntity extends KineticTileEntity {

	LerpedFloat offset;
	boolean isMoving;

	private SmartFluidTank internalTank;
	private LazyOptional<IFluidHandler> capability;
	private FluidDrainingBehaviour drainer;
	private FluidFillingBehaviour filler;
	private HosePulleyFluidHandler handler;
	private boolean infinite;

	public HosePulleyTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		offset = LerpedFloat.linear()
			.startWithValue(0);
		isMoving = true;
		internalTank = new SmartFluidTank(1500, this::onTankContentsChanged);
		handler = new HosePulleyFluidHandler(internalTank, filler, drainer,
			() -> pos.down((int) Math.ceil(offset.getValue())), () -> !this.isMoving);
		capability = LazyOptional.of(() -> handler);
	}

	@Override
	public void sendData() {
		infinite = filler.infinite || drainer.infinite;
		super.sendData();
	}

	@Override
	public boolean addToGoggleTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking) {
		boolean addToGoggleTooltip = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		if (infinite)
			TooltipHelper.addHint(tooltip, "hint.hose_pulley");
		return addToGoggleTooltip;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		drainer = new FluidDrainingBehaviour(this);
		filler = new FluidFillingBehaviour(this);
		behaviours.add(drainer);
		behaviours.add(filler);
		super.addBehaviours(behaviours);
	}

	protected void onTankContentsChanged(FluidStack contents) {}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		isMoving = true;
		if (getSpeed() == 0) {
			offset.forceNextSync();
			offset.setValue(Math.round(offset.getValue()));
			isMoving = false;
		}

		if (isMoving) {
			float newOffset = offset.getValue() + getMovementSpeed();
			if (newOffset < 0)
				isMoving = false;
			if (!world.getBlockState(pos.down((int) Math.ceil(newOffset)))
				.getMaterial()
				.isReplaceable()) {
				isMoving = false;
			}
			if (isMoving) {
				drainer.reset();
				filler.reset();
			}
		}

		super.onSpeedChanged(previousSpeed);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return super.getRenderBoundingBox().expand(0, -offset.getValue(), 0);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return super.getMaxRenderDistanceSquared() + offset.getValue() * offset.getValue();
	}

	@Override
	public void tick() {
		super.tick();
		float newOffset = offset.getValue() + getMovementSpeed();
		if (newOffset < 0) {
			newOffset = 0;
			isMoving = false;
		}
		if (!world.getBlockState(pos.down((int) Math.ceil(newOffset)))
			.getMaterial()
			.isReplaceable()) {
			newOffset = (int) newOffset;
			isMoving = false;
		}
		if (getSpeed() == 0)
			isMoving = false;

		offset.setValue(newOffset);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (world.isRemote)
			return;
		if (isMoving)
			return;

		int ceil = (int) Math.ceil(offset.getValue() + getMovementSpeed());
		if (getMovementSpeed() > 0 && world.getBlockState(pos.down(ceil))
			.getMaterial()
			.isReplaceable()) {
			isMoving = true;
			drainer.reset();
			filler.reset();
			return;
		}

		sendData();
	}

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		compound.put("Offset", offset.writeNBT());
		compound.put("Tank", internalTank.writeToNBT(new CompoundNBT()));
		super.write(compound, clientPacket);
		if (clientPacket)
			compound.putBoolean("Infinite", infinite);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		offset.readNBT(compound.getCompound("Offset"), clientPacket);
		internalTank.readFromNBT(compound.getCompound("Tank"));
		super.fromTag(state, compound, clientPacket);
		if (clientPacket)
			infinite = compound.getBoolean("Infinite");
	}

	@Override
	public void remove() {
		super.remove();
		capability.invalidate();
	}

	public float getMovementSpeed() {
		float movementSpeed = getSpeed() / 512f;
		if (world.isRemote)
			movementSpeed *= ServerSpeedProvider.get();
		return movementSpeed;
	}

	public float getInterpolatedOffset(float pt) {
		return offset.getValue(pt);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (isFluidHandlerCap(cap)
			&& (side == null || HosePulleyBlock.hasPipeTowards(world, pos, getBlockState(), side)))
			return this.capability.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public boolean shouldRenderAsTE() {
		return true;
	}
}
