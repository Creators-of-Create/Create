package com.simibubi.create.content.contraptions.processing;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.simibubi.create.content.contraptions.fluids.CombinedFluidHandler;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class BasinTileEntity extends SmartTileEntity implements ITickableTileEntity {

	public BasinInputInventory inputInventory;
	protected SmartInventory outputInventory;
	protected LazyOptional<IItemHandlerModifiable> itemCapability;
	protected LazyOptional<CombinedFluidHandler> fluidCapability;
	
	private boolean contentsChanged;
	private FilteringBehaviour filtering;

	public BasinTileEntity(TileEntityType<? extends BasinTileEntity> type) {
		super(type);
		inputInventory = new BasinInputInventory(9, this);
		inputInventory.withMaxStackSize(8).forbidExtraction();
		outputInventory = new SmartInventory(9, this).forbidInsertion();
		itemCapability = LazyOptional.of(() -> new CombinedInvWrapper(inputInventory, outputInventory));
		fluidCapability = LazyOptional.of(() -> new CombinedFluidHandler(9, 1000));
		contentsChanged = true;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
		filtering = new FilteringBehaviour(this, new BasinValueBox()).moveText(new Vector3d(2, -8, 0))
			.withCallback(newFilter -> contentsChanged = true)
			.forRecipes();
		behaviours.add(filtering);
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		inputInventory.deserializeNBT(compound.getCompound("InputItems"));
		outputInventory.deserializeNBT(compound.getCompound("OutputItems"));
		if (compound.contains("fluids"))
			fluidCapability
				.ifPresent(combinedFluidHandler -> combinedFluidHandler.readFromNBT(compound.getList("fluids", 10)));
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.put("InputItems", inputInventory.serializeNBT());
		compound.put("OutputItems", outputInventory.serializeNBT());
		fluidCapability.ifPresent(combinedFuidHandler -> {
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
		itemCapability.invalidate();
		fluidCapability.invalidate();
		super.remove();
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return itemCapability.cast();
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return fluidCapability.cast();
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
	
	public void notifyChangeOfContents() {
		contentsChanged = true;
	}
	
	public SmartInventory getInputInventory() {
		return inputInventory;
	}
	
	public SmartInventory getOutputInventory() {
		return outputInventory;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 256;
	}

	class BasinValueBox extends ValueBoxTransform.Sided {

		@Override
		protected Vector3d getSouthLocation() {
			return VecHelper.voxelSpace(8, 12, 16);
		}

		@Override
		protected boolean isSideActive(BlockState state, Direction direction) {
			return direction.getAxis()
				.isHorizontal();
		}

	}

}
