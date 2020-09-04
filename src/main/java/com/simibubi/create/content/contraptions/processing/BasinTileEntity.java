package com.simibubi.create.content.contraptions.processing;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.simibubi.create.content.contraptions.fluids.CombinedFluidHandler;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class BasinTileEntity extends SmartTileEntity implements ITickableTileEntity {

	public boolean contentsChanged;
	private FilteringBehaviour filtering;

	protected ItemStackHandler outputItemInventory = new ItemStackHandler(9) {
		protected void onContentsChanged(int slot) {
			sendData();
			markDirty();
		}
	};

	public class BasinInputInventory extends RecipeWrapper {
		public BasinInputInventory() {
			super(inputItemInventory);
		}
	}

	protected ItemStackHandler inputItemInventory = new ItemStackHandler(9) {
		protected void onContentsChanged(int slot) {
			contentsChanged = true;
			sendData();
			markDirty();
		}

		@Nonnull
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			for (int i = 0; i < getSlots(); i++) {
				ItemStack stackInSlot = getStackInSlot(i);
				if (ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
					if (stackInSlot.getCount() == getStackLimit(i, stackInSlot))
						return stack;
			}
			return super.insertItem(slot, stack, simulate);
		}
	};

	public static class BasinInventory extends CombinedInvWrapper {
		public BasinInventory(ItemStackHandler input, ItemStackHandler output) {
			super(input, output);
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (isInput(slot))
				return ItemStack.EMPTY;
			return super.extractItem(slot, amount, simulate);
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			if (!isInput(slot))
				return stack;
			return super.insertItem(slot, stack, simulate);
		}

		public boolean isInput(int slot) {
			return getIndexForSlot(slot) == 0;
		}

		public IItemHandlerModifiable getInputHandler() {
			return itemHandler[0];
		}

		public IItemHandlerModifiable getOutputHandler() {
			return itemHandler[1];
		}

	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 256;
	}

	protected LazyOptional<IItemHandlerModifiable> inventory =
		LazyOptional.of(() -> new BasinInventory(inputItemInventory, outputItemInventory));

	protected LazyOptional<CombinedFluidHandler> fluidInventory =
		LazyOptional.of(() -> new CombinedFluidHandler(9, 1000));

	public BasinInputInventory recipeInventory;

	public BasinTileEntity(TileEntityType<? extends BasinTileEntity> type) {
		super(type);
		contentsChanged = true;
		recipeInventory = new BasinInputInventory();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
		filtering = new FilteringBehaviour(this, new BasinValueBox()).moveText(new Vec3d(2, -8, 0))
			.withCallback(newFilter -> contentsChanged = true)
			.forRecipes();
		behaviours.add(filtering);
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		inputItemInventory.deserializeNBT(compound.getCompound("InputItems"));
		outputItemInventory.deserializeNBT(compound.getCompound("OutputItems"));
		if (compound.contains("fluids"))
			fluidInventory
				.ifPresent(combinedFluidHandler -> combinedFluidHandler.readFromNBT(compound.getList("fluids", 10)));
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.put("InputItems", inputItemInventory.serializeNBT());
		compound.put("OutputItems", outputItemInventory.serializeNBT());
		fluidInventory.ifPresent(combinedFuidHandler -> {
			ListNBT nbt = combinedFuidHandler.getListNBT();
			compound.put("fluids", nbt);
		});
	}

	public void onEmptied() {
		getOperator().ifPresent(te -> te.basinRemoved = true);
	}

	@Override
	public void remove() {
		onEmptied();
		inventory.invalidate();
		fluidInventory.invalidate();
		super.remove();
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return inventory.cast();
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return fluidInventory.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public void tick() {
		if (!contentsChanged)
			return;
		contentsChanged = false;
		getOperator().ifPresent(te -> te.basinChecker.scheduleUpdate());
	}

	private Optional<BasinOperatingTileEntity> getOperator() {
		if (world == null)
			return Optional.empty();
		TileEntity te = world.getTileEntity(pos.up(2));
		if (te instanceof BasinOperatingTileEntity)
			return Optional.of((BasinOperatingTileEntity) te);
		return Optional.empty();
	}

	public FilteringBehaviour getFilter() {
		return filtering;
	}

	class BasinValueBox extends ValueBoxTransform.Sided {

		@Override
		protected Vec3d getSouthLocation() {
			return VecHelper.voxelSpace(8, 12, 16);
		}

		@Override
		protected boolean isSideActive(BlockState state, Direction direction) {
			return direction.getAxis()
				.isHorizontal();
		}

	}

}
