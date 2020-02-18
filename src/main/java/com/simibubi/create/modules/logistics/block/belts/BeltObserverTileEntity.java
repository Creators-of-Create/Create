package com.simibubi.create.modules.logistics.block.belts;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.logistics.block.belts.BeltObserverBlock.Mode;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;

public class BeltObserverTileEntity extends SmartTileEntity {

	private FilteringBehaviour filtering;
	public int turnOffTicks = 0;

	public BeltObserverTileEntity() {
		super(AllTileEntities.ENTITY_DETECTOR.type);
		setLazyTickRate(20);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		filtering = new FilteringBehaviour(this, new BeltObserverFilterSlot()).moveText(new Vec3d(0, 5, 0));
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

		TileEntity tileEntity =
			world.getTileEntity(pos.offset(getBlockState().get(BeltObserverBlock.HORIZONTAL_FACING)));
		if (!(tileEntity instanceof BeltTileEntity))
			return;
		BeltTileEntity belt = (BeltTileEntity) tileEntity;
		BeltTileEntity controllerTE = belt.getControllerTE();
		if (controllerTE == null)
			return;

		controllerTE.getInventory().forEachWithin(belt.index + .5f, .45f, stack -> {
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
