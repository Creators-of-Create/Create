package com.simibubi.create.content.logistics.packagePort;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.animatedContainer.AnimatedContainerBehaviour;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.api.data.codec.CatnipCodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public abstract class PackagePortBlockEntity extends SmartBlockEntity implements MenuProvider, Clearable {
	public boolean acceptsPackages;
	public String addressFilter;
	public PackagePortTarget target;
	public SmartInventory inventory;

	protected AnimatedContainerBehaviour<PackagePortMenu> openTracker;

	protected IItemHandler itemHandler;

	public PackagePortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		addressFilter = "";
		acceptsPackages = true;
		inventory = new SmartInventory(18, this, (slot, stack) -> PackageItem.isPackage(stack));
		itemHandler = new PackagePortAutomationInventoryWrapper(inventory, this);
	}

	public boolean isBackedUp() {
		for (int i = 0; i < inventory.getSlots(); i++)
			if (inventory.getStackInSlot(i)
				.isEmpty())
				return false;
		return true;
	}

	public void filterChanged() {
		if (target != null) {
			target.deregister(this, level, worldPosition);
			target.register(this, level, worldPosition);
		}
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (target != null)
			target.register(this, level, worldPosition);
	}

	public String getFilterString() {
		return acceptsPackages ? addressFilter : null;
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		if (target != null)
			tag.put("Target", CatnipCodecUtils.encode(PackagePortTarget.CODEC, registries, target).orElseThrow());
		tag.putString("AddressFilter", addressFilter);
		tag.putBoolean("AcceptsPackages", acceptsPackages);
		tag.put("Inventory", com.simibubi.create.foundation.utility.LegacyItemStackNbtBridge.serializeHandler(inventory, registries));
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		com.simibubi.create.foundation.utility.LegacyItemStackNbtBridge.deserializeHandler(inventory, registries, tag.getCompoundOrEmpty("Inventory"));
		PackagePortTarget prevTarget = target;
		target = CatnipCodecUtils.decodeOrNull(PackagePortTarget.CODEC, registries, tag.getCompoundOrEmpty("Target"));
		addressFilter = tag.getStringOr("AddressFilter", "");
		acceptsPackages = tag.getBooleanOr("AcceptsPackages", false);
		if (clientPacket && prevTarget != target)
			invalidateRenderBoundingBox();
	}

	@Override
	public void invalidate() {
		super.invalidate();
	}

	@Override
	public void clearContent() {
		inventory.clearContent();
	}

	@Override
	public void destroy() {
		if (target != null)
			target.deregister(this, level, worldPosition);
		super.destroy();
		for (int i = 0; i < inventory.getSlots(); i++)
			drop(inventory.getStackInSlot(i));
	}

	public void drop(ItemStack box) {
		if (box.isEmpty())
			return;
		Block.popResource(level, worldPosition, box);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(openTracker = new AnimatedContainerBehaviour<>(this, PackagePortMenu.class));
		openTracker.onOpenChanged(this::onOpenChange);
	}

	protected abstract void onOpenChange(boolean open);

	public InteractionResult use(Player player) {
		if (player == null || player.isCrouching())
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		if (player instanceof FakePlayer)
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		ItemStack mainHandItem = player.getMainHandItem();
		boolean clipboard = AllBlocks.CLIPBOARD.isIn(mainHandItem);

		if (level.isClientSide()) {
			if (!clipboard)
				onOpenedManually();
			return InteractionResult.SUCCESS;
		}

		if (clipboard) {
			addAddressToClipboard(player, mainHandItem);
			return InteractionResult.SUCCESS;
		}

		player.openMenu(this, worldPosition);
		return InteractionResult.SUCCESS;
	}

	protected void onOpenedManually() {
	}

	private void addAddressToClipboard(Player player, ItemStack mainHandItem) {
		if (addressFilter == null || addressFilter.isBlank())
			return;

		ClipboardContent clipboard = mainHandItem.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
		List<List<ClipboardEntry>> list = ClipboardEntry.readAll(clipboard);
		for (List<ClipboardEntry> page : list) {
			for (ClipboardEntry entry : page) {
				String existing = entry.text.getString();
				if (existing.equals("#" + addressFilter) || existing.equals("# " + addressFilter))
					return;
			}
		}

		List<ClipboardEntry> page = null;

		for (List<ClipboardEntry> freePage : list) {
			if (freePage.size() > 11)
				continue;
			page = freePage;
			break;
		}

		if (page == null) {
			page = new ArrayList<>();
			list.add(page);
		}

		page.add(new ClipboardEntry(false, Component.literal("#" + addressFilter)));
		player.sendSystemMessage(CreateLang.translate("clipboard.address_added", addressFilter)
			.component());


		clipboard = clipboard.setPages(list).setType(ClipboardType.WRITTEN);
		mainHandItem.set(AllDataComponents.CLIPBOARD_CONTENT, clipboard);
	}

	@Override
	public Component getDisplayName() {
		return Component.empty();
	}

	@Override
	public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
		return PackagePortMenu.create(pContainerId, pPlayerInventory, this);
	}

	public int getComparatorOutput() {
		return ItemHandlerHelper.calcRedstoneFromInventory(inventory);
	}
}
