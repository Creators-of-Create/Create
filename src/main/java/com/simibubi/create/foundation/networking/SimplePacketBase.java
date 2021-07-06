package com.simibubi.create.foundation.networking;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public abstract class SimplePacketBase {

	public abstract void write(PacketBuffer buffer);

	public abstract void handle(Supplier<Context> context);

}
