package com.simibubi.create.modules.logistics.management.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Predicates;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.modules.logistics.management.controller.LogisticalInventoryControllerTileEntity;

import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class LogisticalCasingTileEntity extends SyncedTileEntity {

	Set<BlockPos> controllers = new HashSet<>();

	public LogisticalCasingTileEntity() {
		super(AllTileEntities.LOGISTICAL_CASING.type);
	}

	public boolean controllerPresent() {
		if (controllers.isEmpty())
			return false;
		for (BlockPos blockPos : controllers) {
			if (world.isBlockPresent(blockPos))
				return true;
		}
		return false;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		ListNBT contollerNBT = new ListNBT();
		controllers.forEach(pos -> contollerNBT.add(NBTUtil.writeBlockPos(pos)));
		compound.put("Controllers", contollerNBT);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		controllers.clear();
		ListNBT controllerNBT = compound.getList("Controllers", NBT.TAG_COMPOUND);
		controllerNBT.forEach(tag -> controllers.add(NBTUtil.readBlockPos((CompoundNBT) tag)));
		super.read(compound);
	}

	public void neighbourChanged(BlockPos neighbour) {
		if (!controllerPresent())
			return;
		for (LogisticalActorTileEntity controller : getControllers()) {
			if (!(controller instanceof LogisticalInventoryControllerTileEntity))
				continue;
			((LogisticalInventoryControllerTileEntity) controller).inventoryChanged(neighbour);
		}
	}

	public void addController(BlockPos pos) {
		controllers.add(pos);
		attachController(pos);
		markDirty();
	}

	public void removeController(BlockPos pos) {
		controllers.remove(pos);
		detachController(pos);
		markDirty();

		if (controllers.isEmpty())
			world.setBlockState(getPos(), getBlockState().with(LogisticalCasingBlock.ACTIVE, false));
	}

	public void detachController(BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof LogisticalInventoryControllerTileEntity))
			return;
		for (Direction facing : Direction.values()) {
			((LogisticalInventoryControllerTileEntity) tileEntity).detachInventory(getPos().offset(facing));
			notifyAttachments(facing);
		}
	}

	public void attachController(BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof LogisticalInventoryControllerTileEntity))
			return;
		for (Direction facing : Direction.values()) {
			((LogisticalInventoryControllerTileEntity) tileEntity).inventoryChanged(getPos().offset(facing));
			notifyAttachments(facing);
		}
	}

	private void notifyAttachments(Direction d) {
		BlockPos offset = pos.offset(d);
		Block block = world.getBlockState(offset).getBlock();
		if (block instanceof ILogisticalCasingAttachment)
			((ILogisticalCasingAttachment) block).onCasingUpdated(world, offset, this);
	}

	@Override
	public void remove() {
		controllers.forEach(this::detachController);
		super.remove();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (!controllerPresent())
			return LazyOptional.empty();
		List<LogisticalActorTileEntity> TEs = getControllers();
		if (controllers.isEmpty())
			return LazyOptional.empty();
		List<T> invs = TEs.stream().map(te -> te.getCasingCapability(cap, side).orElse(null))
				.filter(Predicates.notNull()).filter(inv -> inv instanceof IItemHandlerModifiable)
				.collect(Collectors.toList());
		IItemHandlerModifiable[] params = new IItemHandlerModifiable[invs.size()];
		invs.toArray(params);
		return LazyOptional.of(() -> new CombinedInvWrapper(params)).cast();
	}

	public List<LogisticalActorTileEntity> getControllers() {
		List<LogisticalActorTileEntity> TEs = new ArrayList<>(controllers.size());
		for (BlockPos controllerPos : controllers) {
			TileEntity tileEntity = world.getTileEntity(controllerPos);
			if (tileEntity instanceof LogisticalActorTileEntity)
				TEs.add((LogisticalActorTileEntity) tileEntity);
		}
		return TEs;
	}

}
