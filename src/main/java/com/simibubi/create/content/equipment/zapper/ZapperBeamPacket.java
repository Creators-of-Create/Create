package com.simibubi.create.content.equipment.zapper;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.equipment.zapper.ZapperRenderHandler.LaserBeam;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ZapperBeamPacket extends ShootGadgetPacket {

	public Vec3 target;

	public ZapperBeamPacket(Vec3 start, Vec3 target, InteractionHand hand, boolean self) {
		super(start, hand, self);
		this.target = target;
	}

	public ZapperBeamPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void readAdditional(FriendlyByteBuf buffer) {
		target = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
	}

	@Override
	protected void writeAdditional(FriendlyByteBuf buffer) {
		buffer.writeDouble(target.x);
		buffer.writeDouble(target.y);
		buffer.writeDouble(target.z);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected ShootableGadgetRenderHandler getHandler() {
		return CreateClient.ZAPPER_RENDER_HANDLER;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void handleAdditional() {
		CreateClient.ZAPPER_RENDER_HANDLER.addBeam(new LaserBeam(location, target));
	}

}
