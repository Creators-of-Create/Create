package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public class FactoryPanelConnectionPacket extends BlockEntityConfigurationPacket<FactoryPanelBlockEntity> {
	public static final StreamCodec<ByteBuf, FactoryPanelConnectionPacket> STREAM_CODEC = StreamCodec.composite(
	    FactoryPanelPosition.STREAM_CODEC, packet -> packet.fromPos,
		FactoryPanelPosition.STREAM_CODEC, packet -> packet.toPos,
		ByteBufCodecs.BOOL, packet -> packet.relocate,
	    FactoryPanelConnectionPacket::new
	);

	private final FactoryPanelPosition fromPos;
	private final FactoryPanelPosition toPos;
	private final boolean relocate;

	public FactoryPanelConnectionPacket(FactoryPanelPosition fromPos, FactoryPanelPosition toPos, boolean relocate) {
		super(toPos.pos());
		this.fromPos = fromPos;
		this.toPos = toPos;
		this.relocate = relocate;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONNECT_FACTORY_PANEL;
	}

	@Override
	protected void applySettings(ServerPlayer player, FactoryPanelBlockEntity be) {
		FactoryPanelBehaviour behaviour = FactoryPanelBehaviour.at(be.getLevel(), toPos);
		if (behaviour == null)
			return;
		if (!mayInteract(player, behaviour))
			return;

		if (relocate) {
			if (!player.canInteractWithBlock(fromPos.pos(), maxRange()))
				return;
			behaviour.moveTo(fromPos, player);
			return;
		}

		FactoryPanelBehaviour panel = FactoryPanelBehaviour.at(be.getLevel(), fromPos);
		if (panel != null) {
			if (!mayInteract(player, panel))
				return;
			if (!canConnect(behaviour, panel))
				return;
			behaviour.addConnection(fromPos);
			return;
		}

		FactoryPanelSupportBehaviour link = FactoryPanelBehaviour.linkAt(be.getLevel(), fromPos);
		if (link == null)
			return;
		if (!canConnect(behaviour, link))
			return;
		behaviour.addConnection(fromPos);
	}

	private static boolean mayInteract(ServerPlayer player, FactoryPanelBehaviour behaviour) {
		return Create.LOGISTICS.mayInteract(behaviour.network, player);
	}

	private static boolean canConnect(FactoryPanelBehaviour from, FactoryPanelBehaviour to) {
		if (from.targetedBy.containsKey(to.getPanelPosition()))
			return false;
		if (from.targetedBy.size() >= 9)
			return false;

		BlockState state1 = to.blockEntity.getBlockState();
		BlockState state2 = from.blockEntity.getBlockState();
		BlockPos diff = to.getPos()
			.subtract(from.getPos());

		if (state1.setValue(FactoryPanelBlock.WATERLOGGED, false)
			.setValue(FactoryPanelBlock.POWERED, false) != state2.setValue(FactoryPanelBlock.WATERLOGGED, false)
				.setValue(FactoryPanelBlock.POWERED, false))
			return false;

		if (FactoryPanelBlock.connectedDirection(state1)
			.getAxis()
			.choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
			return false;

		if (!diff.closerThan(BlockPos.ZERO, 16))
			return false;

		if (to.panelBE().restocker)
			return false;

		return !to.getFilter()
			.isEmpty()
			&& !from.getFilter()
				.isEmpty();
	}

	private static boolean canConnect(FactoryPanelBehaviour from, FactoryPanelSupportBehaviour to) {
		BlockState state1 = from.blockEntity.getBlockState();
		BlockState state2 = to.blockEntity.getBlockState();
		BlockPos diff = to.getPos()
			.subtract(from.getPos());
		Direction connectedDirection = FactoryPanelBlock.connectedDirection(state1);

		if (connectedDirection != state2.getOptionalValue(WrenchableDirectionalBlock.FACING)
			.orElse(connectedDirection))
			return false;

		if (connectedDirection.getAxis()
			.choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
			return false;

		return diff.closerThan(BlockPos.ZERO, 16);
	}

	@Override
	protected int maxRange() {
		return super.maxRange() * 2;
	}

}
