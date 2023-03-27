package com.simibubi.create.content.logistics.block.verticalvault;

import java.util.List;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.IMultiTileContainer;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class VerticalItemVaultTileEntity extends SmartTileEntity implements IMultiTileContainer.Inventory {

	protected LazyOptional<IItemHandler> itemCapability;

	protected ItemStackHandler inventory;
	protected BlockPos controller;
	protected BlockPos lastKnownPos;
	protected boolean updateConnectivity;
	protected int radius;
	protected int length;
	protected Axis axis;

	public VerticalItemVaultTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);

		inventory = new ItemStackHandler(AllConfigs.SERVER.logistics.vaultCapacity.get()) {
			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);
				updateComparators();
			}
		};

		itemCapability = LazyOptional.empty();
		radius = 1;
		length = 1;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	protected void updateConnectivity() {
		updateConnectivity = false;
		if (level.isClientSide())
			return;
		if (!isController())
			return;
		ConnectivityHandler.formMulti(this);
	}

	protected void updateComparators() {
		VerticalItemVaultTileEntity controllerTE = getControllerTE();
		if (controllerTE == null)
			return;

		level.blockEntityChanged(controllerTE.worldPosition);

		BlockPos pos = controllerTE.getBlockPos();
		for (int x = 0; x < controllerTE.radius; x++) {
			for (int z = 0; z < controllerTE.radius ; z++) {
				for (int y = 0; y < controllerTE.length; y++) {
					level.updateNeighbourForOutputSignal(pos.offset(x, y, z), getBlockState().getBlock());
				}
			}
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (lastKnownPos == null)
			lastKnownPos = getBlockPos();
		else if (!lastKnownPos.equals(worldPosition) && worldPosition != null) {
			onPositionChanged();
			return;
		}

		if (updateConnectivity)
			updateConnectivity();
	}

	@Override
	public BlockPos getLastKnownPos() {
		return lastKnownPos;
	}

	@Override
	public boolean isController() {
		return controller == null || worldPosition.getX() == controller.getX()
			&& worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
	}

	private void onPositionChanged() {
		removeController(true);
		lastKnownPos = worldPosition;
	}

	@SuppressWarnings("unchecked")
	@Override
	public VerticalItemVaultTileEntity getControllerTE() {
		if (isController())
			return this;
		BlockEntity tileEntity = level.getBlockEntity(controller);
		if (tileEntity instanceof VerticalItemVaultTileEntity)
			return (VerticalItemVaultTileEntity) tileEntity;
		return null;
	}

	public void removeController(boolean keepContents) {
		if (level.isClientSide())
			return;
		updateConnectivity = true;
		controller = null;
		radius = 1;
		length = 1;

		BlockState state = getBlockState();
		if (VerticalItemVaultBlock.isVault(state)) {
			state = state.setValue(VerticalItemVaultBlock.LARGE, false);
			getLevel().setBlock(worldPosition, state, 22);
		}

		itemCapability.invalidate();
		setChanged();
		sendData();
	}

	@Override
	public void setController(BlockPos controller) {
		if (level.isClientSide && !isVirtual())
			return;
		if (controller.equals(this.controller))
			return;
		this.controller = controller;
		itemCapability.invalidate();
		setChanged();
		sendData();
	}

	@Override
	public BlockPos getController() {
		return isController() ? worldPosition : controller;
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);

		BlockPos controllerBefore = controller;
		int prevSize = radius;
		int prevLength = length;

		updateConnectivity = compound.contains("Uninitialized");
		controller = null;
		lastKnownPos = null;

		if (compound.contains("LastKnownPos"))
			lastKnownPos = NbtUtils.readBlockPos(compound.getCompound("LastKnownPos"));
		if (compound.contains("Controller"))
			controller = NbtUtils.readBlockPos(compound.getCompound("Controller"));

		if (isController()) {
			radius = compound.getInt("Size");
			length = compound.getInt("Length");
		}

		if (!clientPacket) {
			inventory.deserializeNBT(compound.getCompound("Inventory"));
			return;
		}

		boolean changeOfController =
			controllerBefore == null ? controller != null : !controllerBefore.equals(controller);
		if (hasLevel() && (changeOfController || prevSize != radius || prevLength != length))
			level.setBlocksDirty(getBlockPos(), Blocks.AIR.defaultBlockState(), getBlockState());
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		if (updateConnectivity)
			compound.putBoolean("Uninitialized", true);
		if (lastKnownPos != null)
			compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
		if (!isController())
			compound.put("Controller", NbtUtils.writeBlockPos(controller));
		if (isController()) {
			compound.putInt("Size", radius);
			compound.putInt("Length", length);
		}

		super.write(compound, clientPacket);

		if (!clientPacket) {
			compound.putString("StorageType", "CombinedInv");
			compound.put("Inventory", inventory.serializeNBT());
		}
	}

	public ItemStackHandler getInventoryOfBlock() {
		return inventory;
	}

	public void applyInventoryToBlock(ItemStackHandler handler) {
		for (int i = 0; i < inventory.getSlots(); i++)
			inventory.setStackInSlot(i, i < handler.getSlots() ? handler.getStackInSlot(i) : ItemStack.EMPTY);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (isItemHandlerCap(cap)) {
			initCapability();
			return itemCapability.cast();
		}
		return super.getCapability(cap, side);
	}

	private void initCapability() {
		if (itemCapability.isPresent())
			return;
		if (!isController()) {
			VerticalItemVaultTileEntity controllerTE = getControllerTE();
			if (controllerTE == null)
				return;
			controllerTE.initCapability();
			itemCapability = controllerTE.itemCapability;
			return;
		}

		IItemHandlerModifiable[] invs = new IItemHandlerModifiable[radius * length * length];
		for (int yOffset = 0; yOffset < radius; yOffset++) {
			for (int xOffset = 0; xOffset < length; xOffset++) {
				for (int zOffset = 0; zOffset < length; zOffset++) {
					BlockPos vaultPos = worldPosition.offset(yOffset, xOffset, zOffset);
					VerticalItemVaultTileEntity vaultAt =
						ConnectivityHandler.partAt(AllTileEntities.VERTICAL_ITEM_VAULT.get(), level, vaultPos);
					invs[yOffset * length * length + xOffset * length + zOffset] =
						vaultAt != null ? vaultAt.inventory : new ItemStackHandler();
				}
			}
		}

		CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(invs);
		itemCapability = LazyOptional.of(() -> combinedInvWrapper);
	}

	public static int getMaxLength(int radius) {
		return radius * 3;
	}

	@Override
	public void preventConnectivityUpdate() { updateConnectivity = false; }

	@Override
	public void notifyMultiUpdated() {
		BlockState state = this.getBlockState();
		if (VerticalItemVaultBlock.isVault(state)) { // safety
			level.setBlock(getBlockPos(), state.setValue(VerticalItemVaultBlock.LARGE, radius > 2), 6);
		}
		itemCapability.invalidate();
		setChanged();
	}

	@Override
	public Axis getMainConnectionAxis() { return getMainAxisOf(this); }

	@Override
	public int getMaxLength(Axis longAxis, int width) {
		if (longAxis != Axis.Y) return getMaxWidth();
		return getMaxLength(width);
	}

	@Override
	public int getMaxWidth() {
		return 3;
	}

	@Override
	public int getHeight() { return length; }

	@Override
	public int getWidth() { return radius; }

	@Override
	public void setHeight(int height) { this.length = height; }

	@Override
	public void setWidth(int width) { this.radius = width; }

	@Override
	public boolean hasInventory() { return true; }
}
