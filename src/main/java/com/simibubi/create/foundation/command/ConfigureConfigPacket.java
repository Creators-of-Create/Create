package com.simibubi.create.foundation.command;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;

import com.simibubi.create.content.contraptions.goggles.GoggleConfigScreen;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class ConfigureConfigPacket extends SimplePacketBase {

	private final String option;
	private final String value;

	public ConfigureConfigPacket(String option, String value) {
		this.option = option;
		this.value = value;
	}

	public ConfigureConfigPacket(PacketBuffer buffer) {
		this.option = buffer.readString(32767);
		this.value = buffer.readString(32767);
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeString(option);
		buffer.writeString(value);
	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			try {
				Actions.valueOf(option).performAction(value);
			} catch (IllegalArgumentException e) {
				LogManager.getLogger().warn("Received ConfigureConfigPacket with invalid Option: " + option);
			}
		}));

		ctx.get().setPacketHandled(true);
	}

	enum Actions {
		rainbowDebug((value) -> {
			AllConfigs.CLIENT.rainbowDebug.set(Boolean.parseBoolean(value));
		}),
		overlayScreen((value) -> {
			overlayScreenAction();
		}),
		overlayReset((value) -> {
			AllConfigs.CLIENT.overlayOffsetX.set(0);
			AllConfigs.CLIENT.overlayOffsetY.set(0);
		}),

		;

		private final Consumer<String> consumer;

		Actions(Consumer<String> action) {
			this.consumer = action;
		}

		void performAction(String value){
			consumer.accept(value);
		}

		@OnlyIn(Dist.CLIENT)
		private static void overlayScreenAction(){
			//this doesn't work if i move it into the enum constructor like the other two. if there's a proper way to do this, please let me know
			ScreenOpener.open(new GoggleConfigScreen());
		}
	}
}
