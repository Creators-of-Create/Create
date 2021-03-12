package com.simibubi.create.foundation.networking;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderUI;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PonderPacket extends SimplePacketBase {
	private final ResourceLocation scene;

	public PonderPacket(ResourceLocation scene) {
		this.scene = scene;
	}

	public PonderPacket(PacketBuffer buffer) {
		this.scene = buffer.readResourceLocation();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeResourceLocation(scene);
	}

	@Override
	public void handle(Supplier<NetworkEvent.Context> context) {
		NetworkEvent.Context ctx = context.get();
		if (ctx.getDirection() != NetworkDirection.PLAY_TO_CLIENT)
			return;
		if (PonderRegistry.all.containsKey(scene))
			ScreenOpener.transitionTo(new PonderUI(PonderRegistry.compile(PonderRegistry.all.get(scene))));
		else
			Create.logger.error("Could not find ponder scene: " + scene);
		ctx.setPacketHandled(true);
	}
}
