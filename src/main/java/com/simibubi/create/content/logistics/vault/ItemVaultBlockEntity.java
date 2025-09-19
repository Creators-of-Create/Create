package com.simibubi.create.content.logistics.vault;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryWrapper;
import com.simibubi.create.foundation.utility.SameSizeCombinedInvWrapper;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class ItemVaultBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer.Inventory {

	protected LazyOptional<IItemHandler> itemCapability;
	protected InventoryIdentifier invId;

	protected ItemStackHandler inventory;
	protected BlockPos controller;
	protected BlockPos lastKnownPos;
	protected boolean updateConnectivity;
	protected int radius;
	protected int length;

	public ItemVaultBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);

		inventory = new ItemStackHandler(AllConfigs.server().logistics.vaultCapacity.get()) {
			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);
				updateComparators();
				level.blockEntityChanged(worldPosition);
			}
		};

		itemCapability = LazyOptional.empty();
		radius = 1;
		length = 1;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	protected void updateConnectivity() {
		updateConnectivity = false;
		if (level.isClientSide())
			return;
		if (!isController())
			return;
		ConnectivityHandler.formMulti(this);
	}

	protected void updateComparators() {
		ItemVaultBlockEntity controllerBE = getControllerBE();
		if (controllerBE == null)
			return;

		level.blockEntityChanged(controllerBE.worldPosition);

		BlockPos pos = controllerBE.getBlockPos();

		int radius = controllerBE.radius;
		int length = controllerBE.length;

		Axis axis = controllerBE.getMainConnectionAxis();

		int zMax = (axis == Axis.X ? radius : length);
		int xMax = (axis == Axis.Z ? radius : length);

		// Mutable position we'll use for the blocks we poke updates at.
		MutableBlockPos updatePos = new MutableBlockPos();
		// Mutable position we'll set to be the vault block next to the update position.
		MutableBlockPos provokingPos = new MutableBlockPos();

		for (int y = 0; y < radius; y++) {
			for (int z = 0; z < zMax; z++) {
				for (int x = 0; x < xMax; x++) {
					// Emulate the effect of this line, but only for blocks along the surface of the vault:
					// level.updateNeighbourForOutputSignal(pos.offset(x, y, z), getBlockState().getBlock());
					// That method pokes all 6 directions in order. We want to preserve the update order
					// but skip the wasted work of checking other blocks that are part of this vault.

					var sectionX = SectionPos.blockToSectionCoord(pos.getX() + x);
					var sectionZ = SectionPos.blockToSectionCoord(pos.getZ() + z);
					if (!level.hasChunk(sectionX, sectionZ)) {
						continue;
					}
					provokingPos.setWithOffset(pos, x, y, z);

					// Technically all this work is wasted for the inner blocks of a long 3x3 vault, but
					// this is fast enough and relatively simple.
					Block provokingBlock = level.getBlockState(provokingPos).getBlock();

					// The 6 calls below should match the order of Direction.values().
					if (y == 0) {
						updateComaratorsInner(level, provokingBlock, provokingPos, updatePos, Direction.DOWN);
					}
					if (y == radius - 1) {
						updateComaratorsInner(level, provokingBlock, provokingPos, updatePos, Direction.UP);
					}
					if (z == 0) {
						updateComaratorsInner(level, provokingBlock, provokingPos, updatePos, Direction.NORTH);
					}
					if (z == zMax - 1) {
						updateComaratorsInner(level, provokingBlock, provokingPos, updatePos, Direction.SOUTH);
					}
					if (x == 0) {
						updateComaratorsInner(level, provokingBlock, provokingPos, updatePos, Direction.WEST);
					}
					if (x == xMax - 1) {
						updateComaratorsInner(level, provokingBlock, provokingPos, updatePos, Direction.EAST);
					}
				}
			}
		}
	}

	/**
	 * See {@link Level#updateNeighbourForOutputSignal(BlockPos, Block)}.
	 */
	private static void updateComaratorsInner(Level level, Block provokingBlock, BlockPos provokingPos, MutableBlockPos updatePos, Direction direction) {
		updatePos.setWithOffset(provokingPos, direction);

		var sectionX = SectionPos.blockToSectionCoord(updatePos.getX());
		var sectionZ = SectionPos.blockToSectionCoord(updatePos.getZ());
		if (!level.hasChunk(sectionX, sectionZ)) {
			return;
		}

		BlockState blockstate = level.getBlockState(updatePos);
		blockstate.onNeighborChange(level, updatePos, provokingPos);
		if (blockstate.isRedstoneConductor(level, updatePos)) {
			updatePos.move(direction);
			blockstate = level.getBlockState(updatePos);
			if (blockstate.getWeakChanges(level, updatePos)) {
				level.neighborChanged(blockstate, updatePos, provokingBlock, provokingPos, false);
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
	public ItemVaultBlockEntity getControllerBE() {
		if (isController())
			return this;
		BlockEntity blockEntity = level.getBlockEntity(controller);
		if (blockEntity instanceof ItemVaultBlockEntity)
			return (ItemVaultBlockEntity) blockEntity;
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
		if (ItemVaultBlock.isVault(state)) {
			state = state.setValue(ItemVaultBlock.LARGE, false);
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

	public InventoryIdentifier getInvId() {
		// ensure capability is up to date first, which sets the ID
		this.initCapability();
		return this.invId;
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
			ItemVaultBlockEntity controllerBE = getControllerBE();
			if (controllerBE == null)
				return;
			controllerBE.initCapability();
			itemCapability = controllerBE.itemCapability;
			invId = controllerBE.invId;
			return;
		}

		boolean alongZ = ItemVaultBlock.getVaultBlockAxis(getBlockState()) == Axis.Z;
		IItemHandlerModifiable[] invs = new IItemHandlerModifiable[length * radius * radius];
		for (int yOffset = 0; yOffset < length; yOffset++) {
			for (int xOffset = 0; xOffset < radius; xOffset++) {
				for (int zOffset = 0; zOffset < radius; zOffset++) {
					BlockPos vaultPos = alongZ ? worldPosition.offset(xOffset, zOffset, yOffset)
						: worldPosition.offset(yOffset, xOffset, zOffset);
					ItemVaultBlockEntity vaultAt =
						ConnectivityHandler.partAt(AllBlockEntityTypes.ITEM_VAULT.get(), level, vaultPos);
					invs[yOffset * radius * radius + xOffset * radius + zOffset] =
						vaultAt != null ? vaultAt.inventory : new ItemStackHandler();
				}
			}
		}

		IItemHandler itemHandler = new VersionedInventoryWrapper(SameSizeCombinedInvWrapper.create(invs));
		itemCapability = LazyOptional.of(() -> itemHandler);

		// build an identifier encompassing all component vaults
		BlockPos farCorner = alongZ
			? worldPosition.offset(radius, radius, length)
			: worldPosition.offset(length, radius, radius);
		BoundingBox bounds = BoundingBox.fromCorners(this.worldPosition, farCorner);
		this.invId = new InventoryIdentifier.Bounds(bounds);
	}

	public static int getMaxLength(int radius) {
		return radius * 3;
	}

	@Override
	public void preventConnectivityUpdate() { updateConnectivity = false; }

	@Override
	public void notifyMultiUpdated() {
		BlockState state = this.getBlockState();
		if (ItemVaultBlock.isVault(state)) { // safety
			level.setBlock(getBlockPos(), state.setValue(ItemVaultBlock.LARGE, radius > 2), 6);
		}
		itemCapability.invalidate();
		setChanged();
	}

	@Override
	public Direction.Axis getMainConnectionAxis() { return getMainAxisOf(this); }

	@Override
	public int getMaxLength(Direction.Axis longAxis, int width) {
		if (longAxis == Direction.Axis.Y) return getMaxWidth();
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
