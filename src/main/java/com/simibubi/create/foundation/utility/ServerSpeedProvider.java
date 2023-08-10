package com.simibubi.create.foundation.utility;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.animation.LerpedFloat.Chaser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;

public class ServerSpeedProvider {

	static int clientTimer = 0;
	static int serverTimer = 0;
	static boolean initialized = false;
	static LerpedFloat modifier = LerpedFloat.linear();

	public static void serverTick() {
		serverTimer++;
		if (serverTimer > getSyncInterval()) {
			AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), new Packet());
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
		modifier.tickChaser();
		clientTimer++;
	}

	public static Integer getSyncInterval() {
		return AllConfigs.server().tickrateSyncTimer.get();
	}

	public static float get() {
		return modifier.getValue();
	}

	public static class Packet extends SimplePacketBase {

		public Packet() {}

		public Packet(FriendlyByteBuf buffer) {}

		@Override
		public void write(FriendlyByteBuf buffer) {}

		@Override
		public boolean handle(Context context) {
			context.enqueueWork(() -> {
				if (!initialized) {
					initialized = true;
					clientTimer = 0;
					return;
				}
				float target = ((float) getSyncInterval()) / Math.max(clientTimer, 1);
				modifier.chase(Math.min(target, 1), .25, Chaser.EXP);
				// Set this to -1 because packets are processed before ticks.
				// ServerSpeedProvider#clientTick will increment it to 0 at the end of this tick.
				// Setting it to 0 causes consistent desync, as the client ends up counting too many ticks.
				clientTimer = -1;
			});
			return true;
		}

	}

}
