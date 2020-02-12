package com.simibubi.create.foundation.utility;

import java.util.function.Supplier;

import com.simibubi.create.AllPackets;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.packet.SimplePacketBase;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber
public class ServerSpeedProvider {

	static int clientTimer = 0;
	static int serverTimer = 0;
	static boolean initialized = false;
	static InterpolatedChasingValue modifier = new InterpolatedChasingValue().withSpeed(.25f);

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == Phase.START)
			return;
		serverTimer++;
		if (serverTimer > getSyncInterval()) {
			AllPackets.channel.send(PacketDistributor.ALL.noArg(), new Packet());
			serverTimer = 0;
		}
	}

	public static Integer getSyncInterval() {
		return AllConfigs.SERVER.tickrateSyncTimer.get();
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == Phase.START)
			return;
		modifier.tick();
		clientTimer++;
	}

	public static float get() {
		return modifier.value;
	}

	public static class Packet extends SimplePacketBase {

		public Packet() {
		}

		public Packet(PacketBuffer buffer) {
		}

		@Override
		public void write(PacketBuffer buffer) {
		}

		@Override
		public void handle(Supplier<Context> context) {
			context.get().enqueueWork(() -> {
				if (!initialized) {
					initialized = true;
					clientTimer = 0;
					return;
				}
				float target = ((float) getSyncInterval()) / Math.max(clientTimer, 1);
				modifier.target(target);
				clientTimer = 0;

			});
			context.get().setPacketHandled(true);
		}

	}

}
