package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.content.contraptions.fluids.FluidFX;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class FluidSplashPacket extends SimplePacketBase {

	private BlockPos pos;
	private FluidStack fluid;

	public FluidSplashPacket(BlockPos pos, FluidStack fluid) {
		this.pos = pos;
		this.fluid = fluid;
	}

	public FluidSplashPacket(FriendlyByteBuf buffer) {
		pos = buffer.readBlockPos();
		fluid = buffer.readFluidStack();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeFluidStack(fluid);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			if (Minecraft.getInstance().player.position()
				.distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ())) > 100)
				return;
			FluidFX.splash(pos, fluid);
		}));
		return true;
	}

}
