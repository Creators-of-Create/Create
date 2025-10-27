package com.simibubi.create.content.logistics.box;

import com.simibubi.create.AllPackets;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record PackageDestroyPacket(Vec3 location, ItemStack box) implements ClientboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, PackageDestroyPacket> STREAM_CODEC = StreamCodec.composite(
		CatnipStreamCodecs.VEC3, PackageDestroyPacket::location,
		ItemStack.STREAM_CODEC, PackageDestroyPacket::box,
		PackageDestroyPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.PACKAGE_DESTROYED;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		ClientLevel level = Minecraft.getInstance().level;
		Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, level.getRandom(), .125f);
		Vec3 pos = location.add(motion.scale(4));
		level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, box), pos.x, pos.y,
			pos.z, motion.x, motion.y, motion.z);
	}
}
