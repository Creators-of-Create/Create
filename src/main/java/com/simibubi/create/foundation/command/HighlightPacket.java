package com.simibubi.create.foundation.command;

import java.util.function.Supplier;

import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.tterrag.registrate.fabric.EnvExecutor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.shapes.Shapes;

public class HighlightPacket extends SimplePacketBase {

	private final BlockPos pos;

	public HighlightPacket(BlockPos pos) {
		this.pos = pos;
	}

	public HighlightPacket(FriendlyByteBuf buffer) {
		this.pos = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
	}

	@Override
	public void handle(Supplier<Context> ctx) {
		ctx.get()
			.enqueueWork(() -> EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> {
				performHighlight(pos);
			}));

		ctx.get()
			.setPacketHandled(true);
	}

	@Environment(EnvType.CLIENT)
	public static void performHighlight(BlockPos pos) {
		if (Minecraft.getInstance().level == null || !Minecraft.getInstance().level.isLoaded(pos))
			return;

		CreateClient.OUTLINER.showAABB("highlightCommand", Shapes.block()
				.bounds()
				.move(pos), 200)
				.lineWidth(1 / 32f)
				.colored(0xEeEeEe)
				// .colored(0x243B50)
				.withFaceTexture(AllSpecialTextures.SELECTION);
	}

}
