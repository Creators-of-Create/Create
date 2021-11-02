package com.simibubi.create.content.logistics.block.redstone;

import java.util.List;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.CapManipulationBehaviourBase.InterfaceProvider;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.TankManipulationBehaviour;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public class StockpileSwitchTileEntity extends SmartTileEntity {

	public float onWhenAbove;
	public float offWhenBelow;
	public float currentLevel;
	private boolean state;
	private boolean inverted;
	private boolean poweredAfterDelay;

	private FilteringBehaviour filtering;
	private InvManipulationBehaviour observedInventory;
	private TankManipulationBehaviour observedTank;

	public StockpileSwitchTileEntity(BlockEntityType<?> typeIn) {
		super(typeIn);
		onWhenAbove = .75f;
		offWhenBelow = .25f;
		currentLevel = -1;
		state = false;
		inverted = false;
		poweredAfterDelay = false;
		setLazyTickRate(10);
	}

	@Override
	protected void fromTag(BlockState blockState, CompoundTag compound, boolean clientPacket) {
		onWhenAbove = compound.getFloat("OnAbove");
		offWhenBelow = compound.getFloat("OffBelow");
		currentLevel = compound.getFloat("Current");
		state = compound.getBoolean("Powered");
		inverted = compound.getBoolean("Inverted");
		poweredAfterDelay = compound.getBoolean("PoweredAfterDelay");
		super.fromTag(blockState, compound, clientPacket);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putFloat("OnAbove", onWhenAbove);
		compound.putFloat("OffBelow", offWhenBelow);
		compound.putFloat("Current", currentLevel);
		compound.putBoolean("Powered", state);
		compound.putBoolean("Inverted", inverted);
		compound.putBoolean("PoweredAfterDelay", poweredAfterDelay);
		super.write(compound, clientPacket);
	}

	public float getStockLevel() {
		return currentLevel;
	}

	public void updateCurrentLevel() {
		boolean changed = false;
		float occupied = 0;
		float totalSpace = 0;

		observedInventory.findNewCapability();
		if (observedInventory.hasInventory()) {
			// Item inventory
			IItemHandler inv = observedInventory.getInventory();
			for (int slot = 0; slot < inv.getSlots(); slot++) {
				ItemStack stackInSlot = inv.getStackInSlot(slot);
				int space = Math.min(stackInSlot.getMaxStackSize(), inv.getSlotLimit(slot));
				int count = stackInSlot.getCount();
				if (space == 0)
					continue;

				totalSpace += 1;
				if (filtering.test(stackInSlot))
					occupied += count * (1f / space);
			}

		} else {
			observedTank.findNewCapability();
			if (observedTank.hasInventory()) {
				// Fluid inventory
				IFluidHandler tank = observedTank.getInventory();
				for (int slot = 0; slot < tank.getTanks(); slot++) {
					FluidStack stackInSlot = tank.getFluidInTank(slot);
					int space = tank.getTankCapacity(slot);
					int count = stackInSlot.getAmount();
					if (space == 0)
						continue;

					totalSpace += 1;
					if (filtering.test(stackInSlot))
						occupied += count * (1f / space);
				}

			} else {
				// No compatible inventories found
				if (currentLevel == -1)
					return;
				level.setBlock(worldPosition, getBlockState().setValue(StockpileSwitchBlock.INDICATOR, 0), 3);
				currentLevel = -1;
				state = false;
				sendData();
				scheduleBlockTick();
				return;
			}
		}

		float stockLevel = occupied / totalSpace;
		if (currentLevel != stockLevel)
			changed = true;
		currentLevel = stockLevel;
		currentLevel = Mth.clamp(currentLevel, 0, 1);

		boolean previouslyPowered = state;
		if (state && currentLevel <= offWhenBelow)
			state = false;
		else if (!state && currentLevel >= onWhenAbove)
			state = true;
		boolean update = previouslyPowered != state;

		int displayLevel = 0;
		if (currentLevel > 0)
			displayLevel = (int) (currentLevel * 6);
		level.setBlock(worldPosition, getBlockState().setValue(StockpileSwitchBlock.INDICATOR, displayLevel),
			update ? 3 : 2);

		if (update)
			scheduleBlockTick();

		if (changed || update)
			sendData();
	}

	protected void scheduleBlockTick() {
		TickList<Block> blockTicks = level.getBlockTicks();
		Block block = getBlockState().getBlock();
		if (!blockTicks.willTickThisTick(worldPosition, block))
			blockTicks.scheduleTick(worldPosition, block, 2, TickPriority.NORMAL);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (level.isClientSide)
			return;
		updateCurrentLevel();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		filtering = new FilteringBehaviour(this, new FilteredDetectorFilterSlot()).moveText(new Vec3(0, 5, 0))
			.withCallback($ -> updateCurrentLevel());
		behaviours.add(filtering);

		InterfaceProvider towardBlockFacing = InterfaceProvider.towardBlockFacing();
		behaviours.add(observedInventory = new InvManipulationBehaviour(this, towardBlockFacing).bypassSidedness());
		behaviours.add(observedTank = new TankManipulationBehaviour(this, towardBlockFacing).bypassSidedness());
	}

	public float getLevelForDisplay() {
		return currentLevel == -1 ? 0 : currentLevel;
	}

	public boolean getState() {
		return state;
	}

	public boolean shouldBePowered() {
		return inverted != state;
	}

	public void updatePowerAfterDelay() {
		poweredAfterDelay = shouldBePowered();
		level.blockUpdated(worldPosition, getBlockState().getBlock());
	}

	public boolean isPowered() {
		return poweredAfterDelay;
	}

	public boolean isInverted() {
		return inverted;
	}

	public void setInverted(boolean inverted) {
		if (inverted == this.inverted)
			return;
		this.inverted = inverted;
		updatePowerAfterDelay();
	}
}
