package com.simibubi.create.content.logistics.redstoneRequester;

import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.simibubi.create.content.logistics.packagerLink.WiFiParticle;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.stockTicker.StockCheckingBlockEntity;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import java.util.List;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.Direction;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkHooks;

import org.jetbrains.annotations.NotNull;

public class RedstoneRequesterBlockEntity extends StockCheckingBlockEntity implements MenuProvider {

	public boolean allowPartialRequests;
	public PackageOrderWithCrafts encodedRequest = PackageOrderWithCrafts.empty();
	public String encodedTargetAdress = "";

	public boolean lastRequestSucceeded;

	protected boolean redstonePowered;

	public RedstoneRequesterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		allowPartialRequests = false;
	}

	public AbstractComputerBehaviour computerBehaviour;

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
		if (computerBehaviour.isPeripheralCap(cap))
			return computerBehaviour.getPeripheralCapability();
		return super.getCapability(cap, side);
	}

	protected void onRedstonePowerChanged() {
		boolean hasNeighborSignal = level.hasNeighborSignal(worldPosition);
		if (redstonePowered == hasNeighborSignal)
			return;

		lastRequestSucceeded = false;
		if (hasNeighborSignal)
			triggerRequest();

		redstonePowered = hasNeighborSignal;
		notifyUpdate();
	}

	public void triggerRequest() {
		if (encodedRequest.isEmpty())
			return;

		boolean anySucceeded = false;

		InventorySummary summaryOfOrder = new InventorySummary();
		encodedRequest.stacks()
			.forEach(summaryOfOrder::add);

		InventorySummary summary = getAccurateSummary();
		for (BigItemStack entry : summaryOfOrder.getStacks()) {
			if (summary.getCountOf(entry.stack) >= entry.count) {
				anySucceeded = true;
				continue;
			}
			if (!allowPartialRequests) {
				AllPackets.sendToNear(level, worldPosition, 32,
					new RedstoneRequesterEffectPacket(worldPosition, false));
				return;
			}
		}

		broadcastPackageRequest(RequestType.REDSTONE, encodedRequest, null, encodedTargetAdress);
		AllPackets.sendToNear(level, worldPosition, 32, new RedstoneRequesterEffectPacket(worldPosition, anySucceeded));
		lastRequestSucceeded = true;
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		redstonePowered = tag.getBoolean("Powered");
		lastRequestSucceeded = tag.getBoolean("Success");
		allowPartialRequests = tag.getBoolean("AllowPartial");
		encodedRequest = PackageOrderWithCrafts.read(tag.getCompound("EncodedRequest"));
		encodedTargetAdress = tag.getString("EncodedAddress");
	}

	@Override
	public void writeSafe(CompoundTag tag) {
		super.writeSafe(tag);
		tag.putBoolean("AllowPartial", allowPartialRequests);
		tag.putString("EncodedAddress", encodedTargetAdress);
		tag.put("EncodedRequest", encodedRequest.write());
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.putBoolean("Powered", redstonePowered);
		tag.putBoolean("Success", lastRequestSucceeded);
		tag.putBoolean("AllowPartial", allowPartialRequests);
		tag.putString("EncodedAddress", encodedTargetAdress);
		tag.put("EncodedRequest", encodedRequest.write());
	}

	public InteractionResult use(Player player) {
		if (player == null || player.isCrouching())
			return InteractionResult.PASS;
		if (player instanceof FakePlayer)
			return InteractionResult.PASS;
		if (level.isClientSide)
			return InteractionResult.SUCCESS;
		if (!behaviour.mayInteractMessage(player))
			return InteractionResult.SUCCESS;

		NetworkHooks.openScreen((ServerPlayer) player, this, worldPosition);
		return InteractionResult.SUCCESS;
	}

	@Override
	public Component getDisplayName() {
		return Component.empty();
	}

	@Override
	public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
		return RedstoneRequesterMenu.create(pContainerId, pPlayerInventory, this);
	}

	public void playEffect(boolean success) {
		Vec3 vec3 = Vec3.atCenterOf(worldPosition);
		if (success) {
			AllSoundEvents.CONFIRM.playAt(level, worldPosition, 0.5f, 1.5f, false);
			AllSoundEvents.STOCK_LINK.playAt(level, worldPosition, 1.0f, 1.0f, false);
			level.addParticle(new WiFiParticle.Data(), vec3.x, vec3.y, vec3.z, 1, 1, 1);
		} else {
			AllSoundEvents.DENY.playAt(level, worldPosition, 0.5f, 1, false);
			level.addParticle(ParticleTypes.ENCHANTED_HIT, vec3.x, vec3.y + 1, vec3.z, 0, 0, 0);
		}
	}

}
