package com.simibubi.create.modules.logistics.block.extractor;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour.SlotPositioning;
import com.simibubi.create.modules.logistics.block.IExtractor;
import com.simibubi.create.modules.logistics.block.belts.AttachedLogisiticalBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class ExtractorTileEntity extends SmartTileEntity implements IExtractor, ITickableTileEntity {

	private static FilteringBehaviour.SlotPositioning slots;

	private State state;
	private int cooldown;
	private LazyOptional<IItemHandler> inventory;
	private FilteringBehaviour filtering;

	public ExtractorTileEntity() {
		this(AllTileEntities.EXTRACTOR.type);
	}

	protected ExtractorTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		state = State.ON_COOLDOWN;
		cooldown = CreateConfig.parameters.extractorDelay.get();
		inventory = LazyOptional.empty();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		if (slots == null)
			slots = new SlotPositioning(ExtractorBlock::getFilterSlotPosition, ExtractorBlock::getFilterSlotOrientation)
					.scale(.4f);
		filtering = new FilteringBehaviour(this).withCallback(this::filterChanged).withSlotPositioning(slots)
				.showCount();
		behaviours.add(filtering);
	}

	public void filterChanged(ItemStack stack) {
		neighborChanged();
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public void read(CompoundNBT compound) {
		if (compound.getBoolean("Locked"))
			setState(State.LOCKED);
		super.read(compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("Locked", getState() == State.LOCKED);
		return super.write(compound);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (world.isBlockPowered(pos))
			state = State.LOCKED;
		neighborChanged();
	}

	@Override
	public void tick() {
		super.tick();
		IExtractor.super.tick();
	}

	@Override
	public void setState(State state) {
		if (state == State.ON_COOLDOWN)
			cooldown = CreateConfig.parameters.extractorDelay.get();
		if (state == State.WAITING_FOR_INVENTORY)
			cooldown = CreateConfig.parameters.extractorInventoryScanDelay.get();
		this.state = state;
	}

	@Override
	public int tickCooldown() {
		return cooldown--;
	}

	@Override
	public BlockPos getInventoryPos() {
		BlockState blockState = getBlockState();
		Block block = blockState.getBlock();
		if (!(block instanceof ExtractorBlock))
			return null;
		return getPos().offset(AttachedLogisiticalBlock.getBlockFacing(blockState));
	}

	@Override
	public LazyOptional<IItemHandler> getInventory() {
		return inventory;
	}

	@Override
	public void setInventory(LazyOptional<IItemHandler> inventory) {
		this.inventory = inventory;
	}

}
