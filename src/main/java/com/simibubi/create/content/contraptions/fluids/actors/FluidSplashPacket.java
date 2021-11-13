package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.function.Supplier;

import com.simibubi.create.content.contraptions.fluids.FluidFX;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.minecraftforge.fluids.FluidStack;
import com.tterrag.registrate.fabric.EnvExecutor;


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

	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeFluidStack(fluid);
	}

	public void handle(Supplier<Context> ctx) {
		ctx.get()
			.enqueueWork(() -> EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> {
				if (Minecraft.getInstance().player.position()
					.distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ())) > 100)
					return;
				FluidFX.splash(pos, fluid);
			}));
		ctx.get()
			.setPacketHandled(true);
	}

}
