package com.simibubi.create.content.equipment.potatoCannon;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.equipment.zapper.ShootGadgetPacket;
import com.simibubi.create.content.equipment.zapper.ShootableGadgetRenderHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotatoCannonPacket extends ShootGadgetPacket {

	private float pitch;
	private Vec3 motion;
	private ItemStack item;

	public PotatoCannonPacket(Vec3 location, Vec3 motion, ItemStack item, InteractionHand hand, float pitch, boolean self) {
		super(location, hand, self);
		this.motion = motion;
		this.item = item;
		this.pitch = pitch;
	}

	public PotatoCannonPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void readAdditional(FriendlyByteBuf buffer) {
		pitch = buffer.readFloat();
		motion = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		item = buffer.readItem();
	}

	@Override
	protected void writeAdditional(FriendlyByteBuf buffer) {
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
