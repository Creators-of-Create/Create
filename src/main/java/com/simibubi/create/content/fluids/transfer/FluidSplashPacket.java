package com.simibubi.create.content.fluids.transfer;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.fluids.FluidFX;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.fluids.FluidStack;

public record FluidSplashPacket(BlockPos pos, FluidStack fluid) implements ClientboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, FluidSplashPacket> STREAM_CODEC = StreamCodec.composite(
	        BlockPos.STREAM_CODEC, FluidSplashPacket::pos,
			FluidStack.OPTIONAL_STREAM_CODEC, FluidSplashPacket::fluid,
	        FluidSplashPacket::new
	);

	@Override
	public void handle(Player player) {
		if (player.position().distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ())) > 100)
			return;
		FluidFX.splash(pos, fluid);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.FLUID_SPLASH;
	}
}
