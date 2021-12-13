package me.pepperbell.simplenetworking;

import me.pepperbell.simplenetworking.SimpleChannel.ResponseTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public interface S2CPacket extends Packet {
	/**
	 * This method will be run on the network thread. Most method calls should be performed on the client thread by wrapping the code in a lambda:
	 * <pre>
	 * <code>client.execute(() -> {
	 * 	// code here
	 * }</code></pre>
	 */
	void execute(Minecraft client, ClientPacketListener handler, ResponseTarget responseTarget);
}
