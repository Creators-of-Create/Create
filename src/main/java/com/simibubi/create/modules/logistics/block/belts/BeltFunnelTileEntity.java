package com.simibubi.create.modules.logistics.block.belts;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.modules.logistics.block.IInventoryManipulator;
import com.simibubi.create.modules.logistics.entity.CardboardBoxEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class BeltFunnelTileEntity extends SyncedTileEntity implements ITickableTileEntity, IInventoryManipulator {

	private LazyOptional<IItemHandler> inventory;
	protected boolean waitingForInventorySpace;
	private boolean initialize;

	public BeltFunnelTileEntity() {
		super(AllTileEntities.BELT_FUNNEL.type);
		inventory = LazyOptional.empty();
	}

	@Override
	public void read(CompoundNBT compound) {
		waitingForInventorySpace = compound.getBoolean("Waiting");
		super.read(compound);
	}

	@Override
	public void onLoad() {
		initialize = true;
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		super.readClientUpdate(tag);
		if (!waitingForInventorySpace)
			neighborChanged();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("Waiting", waitingForInventorySpace);
		return super.write(compound);
	}

	@Override
	public BlockPos getInventoryPos() {
		return pos.offset(getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
	}

	@Override
	public LazyOptional<IItemHandler> getInventory() {
		return inventory;
	}

	@Override
	public void tick() {
		if (initialize && hasWorld()) {
			neighborChanged();
			initialize = false;
		}
	}

	@Override
	public void setInventory(LazyOptional<IItemHandler> inventory) {
		this.inventory = inventory;
	}

	@Override
	public void neighborChanged() {
		IInventoryManipulator.super.neighborChanged();
		waitingForInventorySpace = false;
		if (!world.isRemote)
			sendData();
	}

	public void tryToInsert(Entity entity) {
		if (!inventory.isPresent())
			return;
		if (waitingForInventorySpace)
			return;

		ItemStack stack = null;
		if (entity instanceof ItemEntity)
			stack = ((ItemEntity) entity).getItem().copy();
		if (entity instanceof CardboardBoxEntity)
			stack = ((CardboardBoxEntity) entity).getBox().copy();

		IItemHandler inv = inventory.orElse(null);
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			stack = inv.insertItem(slot, stack, world.isRemote);
			if (stack.isEmpty()) {
				if (!world.isRemote) {
					entity.remove();
					world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.BLOCKS, .125f, 1f);
				} else {
					Vec3i directionVec = getBlockState().get(BlockStateProperties.HORIZONTAL_FACING).getDirectionVec();
					float xSpeed = directionVec.getX() * 1 / 8f;
					float zSpeed = directionVec.getZ() * 1 / 8f;
					world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), entity.posX,
							entity.posY, entity.posZ, xSpeed, 1 / 6f, zSpeed);
				}
				return;
			}
		}

		waitingForInventorySpace = true;
		sendData();

		if (entity instanceof ItemEntity)
			if (!stack.equals(((ItemEntity) entity).getItem(), false))
				((ItemEntity) entity).setItem(stack);

	}

}
