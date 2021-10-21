package com.simibubi.create.content.curiosities.weapons;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.curiosities.zapper.ShootGadgetPacket;
import com.simibubi.create.content.curiosities.zapper.ShootableGadgetRenderHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotatoCannonPacket extends ShootGadgetPacket {

	private float pitch;
	private Vector3d motion;
	private ItemStack item;

	public PotatoCannonPacket(Vector3d location, Vector3d motion, ItemStack item, Hand hand, float pitch, boolean self) {
		super(location, hand, self);
		this.motion = motion;
		this.item = item;
		this.pitch = pitch;
	}

	public PotatoCannonPacket(PacketBuffer buffer) {
		super(buffer);
	}

	@Override
	protected void readAdditional(PacketBuffer buffer) {
		pitch = buffer.readFloat();
		motion = new Vector3d(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		item = buffer.readItem();
	}

	@Override
	protected void writeAdditional(PacketBuffer buffer) {
		buffer.writeFloat(pitch);
		buffer.writeFloat((float) motion.x);
		buffer.writeFloat((float) motion.y);
		buffer.writeFloat((float) motion.z);
		buffer.writeItem(item);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void handleAdditional() {
		CreateClient.POTATO_CANNON_RENDER_HANDLER.beforeShoot(pitch, location, motion, item);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected ShootableGadgetRenderHandler getHandler() {
		return CreateClient.POTATO_CANNON_RENDER_HANDLER;
	}

}
