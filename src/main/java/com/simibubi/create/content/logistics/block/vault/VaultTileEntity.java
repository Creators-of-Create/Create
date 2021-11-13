package com.simibubi.create.content.logistics.block.vault;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import com.simibubi.create.lib.utility.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class VaultTileEntity extends SmartTileEntity {

	protected LazyOptional<IItemHandler> itemCapability;

	protected ItemStackHandler inventory;
	protected BlockPos controller;
	protected BlockPos lastKnownPos;
	protected boolean updateConnectivity;
	protected int radius;
	protected int length;
	protected Axis axis;

	public VaultTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
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
		VaultConnectivityHandler.formVaults(this);
	}

	protected void updateComparators() {
		VaultTileEntity controllerTE = getControllerTE();
		if (controllerTE == null)
			return;

		BlockPos pos = controllerTE.getBlockPos();
		for (int y = 0; y < controllerTE.radius; y++) {
			for (int z = 0; z < (controllerTE.axis == Axis.X ? controllerTE.radius : controllerTE.length); z++) {
				for (int x = 0; x < (controllerTE.axis == Axis.Z ? controllerTE.radius : controllerTE.length); x++) {
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

	public boolean isController() {
		return controller == null || worldPosition.getX() == controller.getX()
			&& worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
	}

	private void onPositionChanged() {
		removeController(true);
		lastKnownPos = worldPosition;
	}

	public VaultTileEntity getControllerTE() {
		if (isController())
			return this;
		BlockEntity tileEntity = level.getBlockEntity(controller);
		if (tileEntity instanceof VaultTileEntity)
			return (VaultTileEntity) tileEntity;
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
		if (VaultBlock.isVault(state)) {
			state = state.setValue(VaultBlock.LARGE, false);
			getLevel().setBlock(worldPosition, state, 22);
		}

		itemCapability.invalidate();
		setChanged();
		sendData();
	}

	public void setController(BlockPos controller) {
		if (level.isClientSide())
			return;
		if (controller.equals(this.controller))
			return;
		this.controller = controller;
		itemCapability.invalidate();
		setChanged();
		sendData();
	}

	public BlockPos getController() {
		return isController() ? worldPosition : controller;
	}

	@Override
	protected void fromTag(CompoundTag compound, boolean clientPacket) {
		super.fromTag(compound, clientPacket);

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

		if (!clientPacket)
			compound.put("Inventory", inventory.serializeNBT());
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
			VaultTileEntity controllerTE = getControllerTE();
			if (controllerTE == null)
				return;
			controllerTE.initCapability();
			itemCapability = controllerTE.itemCapability;
			return;
		}

		boolean alongZ = VaultBlock.getVaultBlockAxis(getBlockState()) == Axis.Z;
		IItemHandlerModifiable[] invs = new IItemHandlerModifiable[length * radius * radius];
		for (int yOffset = 0; yOffset < length; yOffset++) {
			for (int xOffset = 0; xOffset < radius; xOffset++) {
				for (int zOffset = 0; zOffset < radius; zOffset++) {
					BlockPos vaultPos = alongZ ? worldPosition.offset(xOffset, zOffset, yOffset)
						: worldPosition.offset(yOffset, xOffset, zOffset);
					VaultTileEntity vaultAt =
						VaultConnectivityHandler.vaultAt(AllTileEntities.ITEM_VAULT.get(), level, vaultPos);
					invs[yOffset * radius * radius + xOffset * radius + zOffset] =
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

}
