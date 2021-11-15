package com.simibubi.create.foundation.utility;

import java.util.function.Supplier;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.animation.InterpolatedChasingValue;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraftforge.fmllegacy.network.PacketDistributor;

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

	@Environment(EnvType.CLIENT)
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

		public Packet(FriendlyByteBuf buffer) {}

		@Override
		public void write(FriendlyByteBuf buffer) {}

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
					// Set this to -1 because packets are processed before ticks.
					// ServerSpeedProvider#clientTick will increment it to 0 at the end of this tick.
					// Setting it to 0 causes consistent desync, as the client ends up counting too many ticks.
					clientTimer = -1;

				});
			context.get()
				.setPacketHandled(true);
		}

	}

}
