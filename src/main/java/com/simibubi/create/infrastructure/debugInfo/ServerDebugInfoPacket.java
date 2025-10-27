package com.simibubi.create.infrastructure.debugInfo;

import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.simibubi.create.infrastructure.debugInfo.element.DebugInfoSection;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record ServerDebugInfoPacket(String serverInfo) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, ServerDebugInfoPacket> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(
			ServerDebugInfoPacket::new, ServerDebugInfoPacket::serverInfo
	);

	public ServerDebugInfoPacket(Player target) {
		this(printServerInfo(target));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		StringBuilder output = new StringBuilder();
		List<DebugInfoSection> clientInfo = DebugInformation.getClientInfo();

		printInfo("Client", player, clientInfo, output);
		output.append("\n\n");
		output.append(this.serverInfo);

		String text = output.toString();
		Minecraft.getInstance().keyboardHandler.setClipboard(text);
		CreateLang.translate("command.debuginfo.saved_to_clipboard")
				.color(DyeHelper.getDyeColors(DyeColor.LIME)
						.getFirst())
				.sendChat(player);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.SERVER_DEBUG_INFO;
	}

	private static String printServerInfo(Player player) {
		List<DebugInfoSection> sections = DebugInformation.getServerInfo();
		StringBuilder output = new StringBuilder();
		printInfo("Server", player, sections, output);
		return output.toString();
	}

	private static void printInfo(String side, Player player, List<DebugInfoSection> sections, StringBuilder output) {
		output.append("<details>");
		output.append('\n');
		output.append("<summary>")
			.append(side)
			.append(" Info")
			.append("</summary>");
		output.append('\n')
			.append('\n');
		output.append("```");
		output.append('\n');

		for (int i = 0; i < sections.size(); i++) {
			if (i != 0) {
				output.append('\n');
			}
			sections.get(i)
				.print(player, line -> output.append(line)
					.append('\n'));
		}

		output.append("```");
		output.append('\n')
			.append('\n');
		output.append("</details>");
		output.append('\n');
	}
}
