package com.simibubi.create.foundation.utility;

import java.util.function.Supplier;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;

public class ServerSpeedProvider {

	static int clientTimer = 0;
	static int serverTimer = 0;
	static boolean initialized = false;
	static InterpolatedChasingValue modifier = new InterpolatedChasingValue().withSpeed(.25f);

	public static void serverTick() {
		serverTimer++;
		if (serverTimer > getSyncInterval()) {
			AllPackets.channel.send(PacketDistributor.ALL.noArg(), new Packet());
			serverTimer = 0;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void clientTick() {
		if (Minecraft.getInstance()
			.hasSingleplayerServer()
			&& Minecraft.getInstance()
				.isPaused())
			return;
		modifier.tick();
		clientTimer++;
	}

	public static Integer getSyncInterval() {
		return AllConfigs.SERVER.tickrateSyncTimer.get();
	}

	public static float get() {
		return modifier.value;
	}

	public static class Packet extends SimplePacketBase {

		public Packet() {}

		public Packet(PacketBuffer buffer) {}

		@Override
		public void write(PacketBuffer buffer) {}

		@Override
		public void handle(Supplier<Context> context) {
			context.get()
				.enqueueWork(() -> {
					if (!initialized) {
						initialized = true;
						clientTimer = 0;
						return;
					}
					float target = ((float) getSyncInterval()) / Math.max(clientTimer, 1);
					modifier.target(Math.min(target, 1));
					clientTimer = 0;

				});
			context.get()
				.setPacketHandled(true);
		}

	}

}
