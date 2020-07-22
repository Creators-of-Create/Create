package com.simibubi.create.content.logistics.block.belts.observer;

import java.util.List;

import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.logistics.block.belts.observer.BeltObserverBlock.Mode;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class BeltObserverTileEntity extends SmartTileEntity {

	private FilteringBehaviour filtering;
	public int turnOffTicks = 0;

	public BeltObserverTileEntity(TileEntityType<? extends BeltObserverTileEntity> type) {
		super(type);
		setLazyTickRate(20);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		filtering = new FilteringBehaviour(this, new BeltObserverFilterSlot()).moveText(new Vector3d(0, 5, 0));
		behaviours.add(filtering);
	}

	@Override
	public void tick() {
		super.tick();
		if (turnOffTicks > 0) {
			turnOffTicks--;
			if (turnOffTicks == 0)
				world.getPendingBlockTicks().scheduleTick(pos, getBlockState().getBlock(), 1);
		}

		if (!isActive())
			return;
		if (getBlockState().get(BeltObserverBlock.MODE) != Mode.DETECT)
			return;

		BlockPos targetPos = pos.offset(getBlockState().get(BeltObserverBlock.HORIZONTAL_FACING));

		BeltTileEntity beltTE = BeltHelper.getSegmentTE(world, targetPos);
		if (beltTE == null)
			return;
		BeltTileEntity controllerTE = beltTE.getControllerTE();
		if (controllerTE == null)
			return;

		controllerTE.getInventory().applyToEachWithin(beltTE.index + .5f, .45f, stack -> {
			if (filtering.test(stack.stack) && turnOffTicks != 6) {
				world.setBlockState(pos, getBlockState().with(BeltObserverBlock.POWERED, true));
				world.notifyNeighborsOfStateChange(pos, getBlockState().getBlock());
				resetTurnOffCooldown();
			}
			return null;
		});

	}

	private boolean isActive() {
		return getBlockState().get(BeltObserverBlock.BELT);
	}

	public void resetTurnOffCooldown() {
		turnOffTicks = 6;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("TurnOff", turnOffTicks);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		turnOffTicks = compound.getInt("TurnOff");
		super.read(compound);
	}

}
