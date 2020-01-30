package com.simibubi.create.foundation.command;

import com.simibubi.create.CreateClientConfig;
import com.simibubi.create.foundation.packet.SimplePacketBase;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ConfigureConfigPacket extends SimplePacketBase {

	private String option;
	private String value;

	public ConfigureConfigPacket(String option, String value) {
		this.option = option;
		this.value = value;
	}

	public ConfigureConfigPacket(PacketBuffer buffer) {
		this.option = buffer.readString();
		this.value = buffer.readString();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeString(option);
		buffer.writeString(value);
	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			if (option.equals("rainbowDebug")){
				CreateClientConfig.instance.enableRainbowDebug.set(Boolean.parseBoolean(value));
			}
		}));

		ctx.get().setPacketHandled(true);
	}
}
