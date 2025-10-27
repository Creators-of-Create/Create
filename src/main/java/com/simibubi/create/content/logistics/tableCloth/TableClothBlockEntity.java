package com.simibubi.create.content.logistics.tableCloth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.redstoneRequester.AutoRequestData;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem.ShoppingList;
import com.simibubi.create.foundation.blockEntity.RemoveBlockEntityPacket;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.data.IntAttached;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;

public class TableClothBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {

	public AbstractComputerBehaviour computerBehaviour;

	public AutoRequestData requestData;
	public List<ItemStack> manuallyAddedItems;
	public UUID owner;

	public Direction facing;
	public boolean sideOccluded;
	public FilteringBehaviour priceTag;

	private List<ItemStack> renderedItemsForShop;

	public TableClothBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		manuallyAddedItems = new ArrayList<>();
		requestData = new AutoRequestData();
		owner = null;
		facing = Direction.SOUTH;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(priceTag = new TableClothFilteringBehaviour(this));
		behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
		if (computerBehaviour.isPeripheralCap(cap))
			return computerBehaviour.getPeripheralCapability();
		return super.getCapability(cap, side);
	}

	public List<ItemStack> getItemsForRender() {
		if (isShop()) {
			if (renderedItemsForShop == null)
				renderedItemsForShop = requestData.encodedRequest.stacks()
					.stream()
					.map(b -> b.stack)
					.limit(4)
					.toList();
			return renderedItemsForShop;
		}

		return manuallyAddedItems;
	}

	public void invalidateItemsForRender() {
		renderedItemsForShop = null;
	}

	public void notifyShopUpdate() {
		AllPackets.getChannel()
			.send(packetTarget(), new ShopUpdatePacket(worldPosition));
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		BlockPos relativePos = worldPosition.relative(facing);
		sideOccluded = AllBlockTags.TABLE_CLOTHS.matches(level.getBlockState(relativePos))
			|| Block.isFaceFull(level.getBlockState(relativePos.below())
			.getOcclusionShape(level, relativePos.below()), facing.getOpposite());
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return super.createRenderBoundingBox().inflate(1);
	}

	public boolean isShop() {
		return !requestData.encodedRequest.isEmpty();
	}

	public InteractionResult use(Player player, BlockHitResult ray) {
		if (isShop())
			return useShop(player);

		ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

		if (heldItem.isEmpty()) {
			if (manuallyAddedItems.isEmpty())
				return InteractionResult.SUCCESS;
			player.setItemInHand(InteractionHand.MAIN_HAND, manuallyAddedItems.remove(manuallyAddedItems.size() - 1));
			level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.5f, 1f);

			if (manuallyAddedItems.isEmpty() && !computerBehaviour.hasAttachedComputer()) {
				level.setBlock(worldPosition, getBlockState().setValue(TableClothBlock.HAS_BE, false), 3);
				AllPackets.getChannel()
					.send(packetTarget(), new RemoveBlockEntityPacket(worldPosition));
			} else
				notifyUpdate();

			return InteractionResult.SUCCESS;
		}

		if (manuallyAddedItems.size() >= 4)
			return InteractionResult.SUCCESS;

		level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.5f, 1f);
		manuallyAddedItems.add(heldItem.copyWithCount(1));
		facing = player.getDirection()
			.getOpposite();
		heldItem.shrink(1);
		if (heldItem.isEmpty())
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		notifyUpdate();
		return InteractionResult.SUCCESS;
	}

	public boolean targetsPriceTag(Player player, BlockHitResult ray) {
		return priceTag != null && priceTag.mayInteract(player) && priceTag.getSlotPositioning()
			.testHit(level, worldPosition, getBlockState(), ray.getLocation()
				.subtract(Vec3.atLowerCornerOf(worldPosition)));
	}

	public InteractionResult useShop(Player player) {
		ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack prevListItem = ItemStack.EMPTY;
		boolean addOntoList = false;

		// Remove other lists from inventory
		for (int i = 0; i < 9; i++) {
			ItemStack item = player.getInventory()
				.getItem(i);
			if (!AllItems.SHOPPING_LIST.isIn(item))
				continue;
			prevListItem = item;
			addOntoList = true;
			player.getInventory()
				.setItem(i, ItemStack.EMPTY);
		}

		// add onto existing list if in hand
		if (AllItems.SHOPPING_LIST.isIn(itemInHand)) {
			prevListItem = itemInHand;
			addOntoList = true;
		}

		if (!itemInHand.isEmpty() && !addOntoList) {
			CreateLang.translate("stock_keeper.shopping_list_empty_hand")
				.sendStatus(player);
			AllSoundEvents.DENY.playOnServer(level, worldPosition, 0.5f, 1);
			return InteractionResult.SUCCESS;
		}

		if (getPaymentItem().isEmpty()) {
			CreateLang.translate("stock_keeper.no_price_set")
				.sendStatus(player);
			AllSoundEvents.DENY.playOnServer(level, worldPosition, 0.5f, 1);
			return InteractionResult.SUCCESS;
		}

		UUID tickerID = null;
		BlockPos tickerPos = requestData.targetOffset.offset(worldPosition);
		if (level.getBlockEntity(tickerPos) instanceof StockTickerBlockEntity stbe && stbe.isKeeperPresent())
			tickerID = stbe.behaviour.freqId;

		int stockLevel = getStockLevelForTrade(ShoppingListItem.getList(prevListItem));

		if (tickerID == null) {
			CreateLang.translate("stock_keeper.keeper_missing")
				.style(ChatFormatting.RED)
				.sendStatus(player);
			AllSoundEvents.DENY.playOnServer(level, worldPosition, 0.5f, 1);
			return InteractionResult.SUCCESS;
		}

		if (stockLevel == 0) {
			CreateLang.translate("stock_keeper.out_of_stock")
				.style(ChatFormatting.RED)
				.sendStatus(player);
			AllSoundEvents.DENY.playOnServer(level, worldPosition, 0.5f, 1);
			if (!prevListItem.isEmpty()) {
				if (player.getItemInHand(InteractionHand.MAIN_HAND)
					.isEmpty())
					player.setItemInHand(InteractionHand.MAIN_HAND, prevListItem);
				else
					player.getInventory()
						.placeItemBackInInventory(prevListItem);
			}

			return InteractionResult.SUCCESS;
		}

		ShoppingList list = new ShoppingList(new ArrayList<>(), owner, tickerID);

		if (addOntoList) {
			ShoppingList prevList = ShoppingListItem.getList(prevListItem);
			if (owner.equals(prevList.shopOwner()) && tickerID.equals(prevList.shopNetwork()))
				list = prevList;
			else
				addOntoList = false;
		}

		if (list.getPurchases(worldPosition) >= stockLevel) {
			for (IntAttached<BlockPos> entry : list.purchases())
				if (worldPosition.equals(entry.getValue()))
					entry.setFirst(Math.min(stockLevel, entry.getFirst()));

			CreateLang.translate("stock_keeper.limited_stock")
				.style(ChatFormatting.RED)
				.sendStatus(player);

		} else {
			AllSoundEvents.CONFIRM_2.playOnServer(level, worldPosition, 0.5f, 1.0f);
			list.addPurchases(worldPosition, 1);
			if (!addOntoList)
				CreateLang.translate("stock_keeper.use_list_to_add_purchases")
					.color(0xeeeeee)
					.sendStatus(player);
			if (!addOntoList)
				level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1, 1.5f);
		}

		ItemStack newListItem =
			ShoppingListItem.saveList(AllItems.SHOPPING_LIST.asStack(), list, requestData.encodedTargetAdress);

		if (player.getItemInHand(InteractionHand.MAIN_HAND)
			.isEmpty())
			player.setItemInHand(InteractionHand.MAIN_HAND, newListItem);
		else
			player.getInventory()
				.placeItemBackInInventory(newListItem);

		return InteractionResult.SUCCESS;
	}

	public int getStockLevelForTrade(@Nullable ShoppingList otherPurchases) {
		BlockPos tickerPos = requestData.targetOffset.offset(worldPosition);
		if (!(level.getBlockEntity(tickerPos) instanceof StockTickerBlockEntity stbe))
			return 0;

		InventorySummary recentSummary = null;

		if (level.isClientSide()) {
			if (stbe.getTicksSinceLastUpdate() > 15)
				stbe.refreshClientStockSnapshot();
			recentSummary = stbe.getLastClientsideStockSnapshotAsSummary();
		} else
			recentSummary = stbe.getRecentSummary();

		if (recentSummary == null)
			return 0;

		InventorySummary modifierSummary = new InventorySummary();
		if (otherPurchases != null)
			modifierSummary = otherPurchases.bakeEntries(level, worldPosition)
				.getFirst();

		int smallestQuotient = Integer.MAX_VALUE;
		for (BigItemStack entry : requestData.encodedRequest.stacks())
			if (entry.count > 0)
				smallestQuotient = Math.min(smallestQuotient,
					(recentSummary.getCountOf(entry.stack) - modifierSummary.getCountOf(entry.stack)) / entry.count);

		return smallestQuotient;
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.put("Items", NBTHelper.writeItemList(manuallyAddedItems));
		tag.putInt("Facing", facing.get2DDataValue());
		requestData.write(tag);
		if (owner != null)
			tag.putUUID("OwnerUUID", owner);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		manuallyAddedItems = NBTHelper.readItemList(tag.getList("Items", Tag.TAG_COMPOUND));
		requestData = AutoRequestData.read(tag);
		owner = tag.contains("OwnerUUID") ? tag.getUUID("OwnerUUID") : null;
		facing = Direction.from2DDataValue(Mth.positiveModulo(tag.getInt("Facing"), 4));
	}

	@Override
	public void destroy() {
		super.destroy();
		manuallyAddedItems.forEach(stack -> Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(),
			worldPosition.getZ(), stack));
		manuallyAddedItems.clear();
	}

	public ItemStack getPaymentItem() {
		return priceTag.getFilter();
	}

	public int getPaymentAmount() {
		return priceTag.getFilter()
			.isEmpty() ? 1 : priceTag.count;
	}

	public void transform(BlockEntity blockEntity, StructureTransform transform){
		facing = transform.mirrorFacing(facing);
		if (transform.rotationAxis == Direction.Axis.Y)
			facing = transform.rotateFacing(facing);
		notifyUpdate();
	}

}
