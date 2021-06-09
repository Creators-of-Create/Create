package com.simibubi.create.content.logistics.block.redstone;

import java.util.List;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour.InterfaceProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.TickPriority;
import net.minecraft.world.server.ServerWorld;
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

	public StockpileSwitchTileEntity(TileEntityType<?> typeIn) {
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
	protected void fromTag(BlockState blockState, CompoundNBT compound, boolean clientPacket) {
		onWhenAbove = compound.getFloat("OnAbove");
		offWhenBelow = compound.getFloat("OffBelow");
		currentLevel = compound.getFloat("Current");
		state = compound.getBoolean("Powered");
		inverted = compound.getBoolean("Inverted");
		poweredAfterDelay = compound.getBoolean("PoweredAfterDelay");
		super.fromTag(blockState, compound, clientPacket);
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putFloat("OnAbove", onWhenAbove);
		compound.putFloat("OffBelow", offWhenBelow);
		compound.putFloat("Current", currentLevel);
		compound.putBoolean("Powered", state);
		compound.putBoolean("Inverted", inverted);
		compound.putBoolean("PoweredAfterDelay", poweredAfterDelay);
		super.write(compound, clientPacket);
	}

	public float getLevel() {
		return currentLevel;
	}

	public void updateCurrentLevel() {
		boolean changed = false;
		observedInventory.findNewCapability();
		if (!observedInventory.hasInventory()) {
			if (currentLevel == -1)
				return;
			world.setBlockState(pos, getBlockState().with(StockpileSwitchBlock.INDICATOR, 0), 3);
			currentLevel = -1;
			state = false;
			world.updateNeighbors(pos, getBlockState().getBlock());
			sendData();
			return;
		}

		float occupied = 0;
		float totalSpace = 0;
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

		float level = (float) occupied / totalSpace;
		if (currentLevel != level)
			changed = true;
		currentLevel = level;
		currentLevel = MathHelper.clamp(currentLevel, 0, 1);

		boolean previouslyPowered = state;
		if (state && currentLevel <= offWhenBelow)
			state = false;
		else if (!state && currentLevel >= onWhenAbove)
			state = true;
		boolean update = previouslyPowered != state;

		int displayLevel = 0;
		if (currentLevel > 0)
			displayLevel = (int) (currentLevel * 6);
		world.setBlockState(pos, getBlockState().with(StockpileSwitchBlock.INDICATOR, displayLevel), update ? 3 : 2);

		if (update && !world.getPendingBlockTicks().isTickPending(pos, getBlockState().getBlock()))
				world.getPendingBlockTicks().scheduleTick(pos, getBlockState().getBlock(), 2, TickPriority.NORMAL);

		if (changed || update)
			sendData();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (world.isRemote)
			return;
		updateCurrentLevel();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		filtering = new FilteringBehaviour(this, new FilteredDetectorFilterSlot()).moveText(new Vector3d(0, 5, 0))
			.withCallback($ -> updateCurrentLevel());
		behaviours.add(filtering);

		observedInventory = new InvManipulationBehaviour(this, InterfaceProvider.towardBlockFacing()).bypassSidedness();
		behaviours.add(observedInventory);
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
		world.updateNeighbors(pos, getBlockState().getBlock());
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
		world.updateNeighbors(pos, getBlockState().getBlock());
	}
}
