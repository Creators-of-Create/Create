package com.simibubi.create.content.logistics.factoryBoard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.createmod.catnip.codecs.stream.CatnipLargerStreamCodecs;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FactoryPanelConfigurationPacket extends BlockEntityConfigurationPacket<FactoryPanelBlockEntity> {
	public static final StreamCodec<RegistryFriendlyByteBuf, FactoryPanelConfigurationPacket> STREAM_CODEC = CatnipLargerStreamCodecs.composite(
		FactoryPanelPosition.STREAM_CODEC, packet -> packet.position,
		ByteBufCodecs.STRING_UTF8, packet -> packet.address,
	    ByteBufCodecs.map(HashMap::new, FactoryPanelPosition.STREAM_CODEC, ByteBufCodecs.INT), packet -> packet.inputAmounts,
		ItemStack.OPTIONAL_LIST_STREAM_CODEC, packet -> packet.craftingArrangement,
		ByteBufCodecs.VAR_INT, packet -> packet.outputAmount,
		ByteBufCodecs.VAR_INT, packet -> packet.promiseClearingInterval,
		CatnipStreamCodecBuilders.nullable(FactoryPanelPosition.STREAM_CODEC), packet -> packet.removeConnection,
		ByteBufCodecs.BOOL, packet -> packet.clearPromises,
		ByteBufCodecs.BOOL, packet -> packet.reset,
		ByteBufCodecs.BOOL, packet -> packet.redstoneReset,
		FactoryPanelConfigurationPacket::new
	);

	private final FactoryPanelPosition position;
	private final String address;
	private final Map<FactoryPanelPosition, Integer> inputAmounts;
	private final List<ItemStack> craftingArrangement;
	private final int outputAmount;
	private final int promiseClearingInterval;
	private final FactoryPanelPosition removeConnection;
	private final boolean clearPromises;
	private final boolean reset;
	private final boolean redstoneReset;

	public FactoryPanelConfigurationPacket(FactoryPanelPosition position, String address,
		Map<FactoryPanelPosition, Integer> inputAmounts, List<ItemStack> craftingArrangement, int outputAmount,
		int promiseClearingInterval, @Nullable FactoryPanelPosition removeConnection, boolean clearPromises,
		boolean reset, boolean sendRedstoneReset) {
		super(position.pos());
		this.position = position;
		this.address = address;
		this.inputAmounts = inputAmounts;
		this.craftingArrangement = craftingArrangement;
		this.outputAmount = outputAmount;
		this.promiseClearingInterval = promiseClearingInterval;
		this.removeConnection = removeConnection;
		this.clearPromises = clearPromises;
		this.reset = reset;
		this.redstoneReset = sendRedstoneReset;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONFIGURE_FACTORY_PANEL;
	}

	@Override
	protected void applySettings(ServerPlayer player, FactoryPanelBlockEntity be) {
		FactoryPanelBehaviour behaviour = be.panels.get(position.slot());
		if (behaviour == null)
			return;

		behaviour.recipeAddress = reset ? "" : address;
		behaviour.recipeOutput = reset ? 1 : outputAmount;
		behaviour.promiseClearingInterval = reset ? -1 : promiseClearingInterval;
		behaviour.activeCraftingArrangement = reset ? List.of() : craftingArrangement;

		if (reset) {
			behaviour.forceClearPromises = true;
			behaviour.disconnectAll();
			behaviour.setFilter(ItemStack.EMPTY);
			behaviour.count = 0;
			be.redraw = true;
			be.notifyUpdate();
			return;
		}

		if (redstoneReset) {
			behaviour.disconnectAllLinks();
			be.notifyUpdate();
			return;
		}

		for (Entry<FactoryPanelPosition, Integer> entry : inputAmounts.entrySet()) {
			FactoryPanelPosition key = entry.getKey();
			FactoryPanelConnection connection = behaviour.targetedBy.get(key);
			if (connection != null)
				connection.amount = entry.getValue();
		}

		if (removeConnection != null) {
			behaviour.targetedBy.remove(removeConnection);
			FactoryPanelBehaviour source = FactoryPanelBehaviour.at(be.getLevel(), removeConnection);
			if (source != null) {
				source.targeting.remove(behaviour.getPanelPosition());
				source.blockEntity.sendData();
			}
		}

		if (clearPromises)
			behaviour.forceClearPromises = true;

		be.notifyUpdate();
	}
}
