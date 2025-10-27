package com.simibubi.create.content.kinetics.chainConveyor;

import java.util.List;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ChainPackageInteractionPacket extends BlockEntityConfigurationPacket<ChainConveyorBlockEntity> {
	private BlockPos selectedConnection;
	private float chainPosition;
	private boolean removingPackage;

	public ChainPackageInteractionPacket(BlockPos pos, BlockPos selectedConnection, float chainPosition, boolean removingPackage) {
		super(pos);
		this.selectedConnection = selectedConnection == null ? BlockPos.ZERO : selectedConnection;
		this.chainPosition = chainPosition;
		this.removingPackage = removingPackage;
	}

	public ChainPackageInteractionPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(selectedConnection);
		buffer.writeFloat(chainPosition);
		buffer.writeBoolean(removingPackage);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		selectedConnection = buffer.readBlockPos();
		chainPosition = buffer.readFloat();
		removingPackage = buffer.readBoolean();
	}

	@Override
	protected int maxRange() {
		return AllConfigs.server().kinetics.maxChainConveyorLength.get() + 16;
	}

	@Override
	protected void applySettings(ChainConveyorBlockEntity be) {
	}

	@Override
	protected void applySettings(ServerPlayer player, ChainConveyorBlockEntity be) {
		if (removingPackage) {
			float bestDiff = Float.POSITIVE_INFINITY;
			ChainConveyorPackage best = null;
			List<ChainConveyorPackage> list = selectedConnection.equals(BlockPos.ZERO) ? be.loopingPackages
				: be.travellingPackages.get(selectedConnection);

			if (list == null || list.isEmpty())
				return;

			for (ChainConveyorPackage liftPackage : list) {
				float diff = Math.abs(selectedConnection == null
					? AngleHelper.getShortestAngleDiff(liftPackage.chainPosition, chainPosition)
					: liftPackage.chainPosition - chainPosition);
				if (diff > bestDiff)
					continue;
				bestDiff = diff;
				best = liftPackage;
			}

			if (player.getMainHandItem().isEmpty()) {
				player.setItemInHand(InteractionHand.MAIN_HAND, best.item.copy());
			} else {
				player.getInventory().placeItemBackInInventory(best.item.copy());
			}

			list.remove(best);
			be.sendData();
		} else {
			ChainConveyorPackage chainConveyorPackage = new ChainConveyorPackage(chainPosition, player.getMainHandItem().copy());
			if (!be.canAcceptPackagesFor(selectedConnection)) {
				return;
			}

			if (!player.isCreative()) {
				player.getMainHandItem().shrink(1);
				if (player.getMainHandItem().isEmpty()) {
					player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				}
			}

			if (selectedConnection.equals(BlockPos.ZERO)) {
				be.addLoopingPackage(chainConveyorPackage);
			} else {
				be.addTravellingPackage(chainConveyorPackage, selectedConnection);
			}
		}
	}
}
