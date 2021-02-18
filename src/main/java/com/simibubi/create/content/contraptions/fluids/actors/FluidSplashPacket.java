package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.function.Supplier;

import com.simibubi.create.content.contraptions.fluids.FluidFX;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class FluidSplashPacket extends SimplePacketBase {

	private BlockPos pos;
	private FluidStack fluid;

	public FluidSplashPacket(BlockPos pos, FluidStack fluid) {
		this.pos = pos;
		this.fluid = fluid;
	}

	public FluidSplashPacket(PacketBuffer buffer) {
		pos = buffer.readBlockPos();
		fluid = buffer.readFluidStack();
	}

	public void write(PacketBuffer buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeFluidStack(fluid);
	}

	public void handle(Supplier<Context> ctx) {
		ctx.get()
			.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
				if (Minecraft.getInstance().player.getPositionVector()
					.distanceTo(new Vec3d(pos)) > 100)
					return;
				FluidFX.splash(pos, fluid);
			}));
		ctx.get()
			.setPacketHandled(true);
	}

}
