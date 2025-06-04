package com.simibubi.create.content.kinetics.chainConveyor;

import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ChainPackageInteractionPacket extends BlockEntityConfigurationPacket<ChainConveyorBlockEntity> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ChainPackageInteractionPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, packet -> packet.pos,
		CatnipStreamCodecBuilders.nullable(BlockPos.STREAM_CODEC), packet -> packet.selectedConnection,
		ByteBufCodecs.FLOAT, packet -> packet.chainPosition,
		ItemStack.OPTIONAL_STREAM_CODEC, packet -> packet.insertedPackage,
	    ChainPackageInteractionPacket::new
	);

	private final BlockPos selectedConnection;
	private final float chainPosition;
	private final ItemStack insertedPackage;

	public ChainPackageInteractionPacket(BlockPos pos, BlockPos selectedConnection, float chainPosition,
		ItemStack insertedPackage) {
		super(pos);
		this.selectedConnection = selectedConnection == null ? BlockPos.ZERO : selectedConnection;
		this.chainPosition = chainPosition;
		this.insertedPackage = insertedPackage == null ? ItemStack.EMPTY : insertedPackage;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CHAIN_PACKAGE_INTERACTION;
	}

	@Override
	protected int maxRange() {
		return AllConfigs.server().kinetics.maxChainConveyorLength.get() + 16;
	}

	@Override
	protected void applySettings(ServerPlayer player, ChainConveyorBlockEntity be) {
		if (insertedPackage.isEmpty()) {

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

			if (best == null)
				return;

			if (player.getMainHandItem()
				.isEmpty())
				player.setItemInHand(InteractionHand.MAIN_HAND, best.item.copy());
			else
				player.getInventory()
					.placeItemBackInInventory(best.item.copy());

			list.remove(best);
			be.sendData();

			return;
		}

		ChainConveyorPackage chainConveyorPackage = new ChainConveyorPackage(chainPosition, insertedPackage);
		if (!be.canAcceptPackagesFor(selectedConnection))
			return;

		if (!player.isCreative()) {
			if(!(ItemStack.matches(player.getMainHandItem(), this.insertedPackage))){
				return;
			}
			player.getMainHandItem()
				.shrink(1);
			if (player.getMainHandItem()
				.isEmpty())
				player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		}

		if (selectedConnection.equals(BlockPos.ZERO))
			be.addLoopingPackage(chainConveyorPackage);
		else
			be.addTravellingPackage(chainConveyorPackage, selectedConnection);

	}

}
