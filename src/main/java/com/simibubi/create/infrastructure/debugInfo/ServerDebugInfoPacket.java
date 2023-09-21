package com.simibubi.create.infrastructure.debugInfo;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.infrastructure.debugInfo.element.DebugInfoSection;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class ServerDebugInfoPacket extends SimplePacketBase {
	public static final Component COPIED = Components.literal(
			"Debug information has been copied to your clipboard."
			).withStyle(ChatFormatting.GREEN);

	private final DebugInfoSection serverInfo;
	private final Player player;

	public ServerDebugInfoPacket(Player player) {
		this.serverInfo = DebugInformation.getServerInfo();
		this.player = player;
	}

	public ServerDebugInfoPacket(FriendlyByteBuf buffer) {
		buffer.readBoolean(); // excess marker
		this.serverInfo = DebugInfoSection.read(buffer);
		this.player = null;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		this.serverInfo.write(this.player, buffer);
	}

	@Override
	public boolean handle(NetworkEvent.Context context) {
		context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			Player player = Minecraft.getInstance().player;

			StringBuilder output = new StringBuilder();
			serverInfo.print(player, line -> output.append(line).append("\n\n"));
			output.append("\n\n");
			DebugInformation.getClientInfo().print(player, line -> output.append(line).append("\n\n"));
			String text = output.toString();

			Minecraft.getInstance().keyboardHandler.setClipboard(text);
			player.displayClientMessage(COPIED, true);
		}));
		return true;
	}
}
