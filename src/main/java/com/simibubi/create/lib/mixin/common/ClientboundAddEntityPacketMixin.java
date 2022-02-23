package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.entity.ExtraSpawnDataEntity;
import com.simibubi.create.lib.extensions.ClientboundAddEntityPacketExtensions;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

// Use random magic value to ensure that custom data is written and read in the correct order if other mixins inject in the same place
@Mixin(value = ClientboundAddEntityPacket.class, priority = 32455)
public abstract class ClientboundAddEntityPacketMixin implements ClientboundAddEntityPacketExtensions {
	@Unique
	private FriendlyByteBuf create$extraDataBuf;

	@Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;I)V", at = @At("TAIL"))
	public void create$onEntityInit(Entity entity, int entityData, CallbackInfo ci) {
		create$setExtraData(entity);
	}

	@Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EntityType;ILnet/minecraft/core/BlockPos;)V", at = @At("TAIL"))
	public void create$onEntityInit(Entity entity, EntityType<?> entityType, int data, BlockPos pos, CallbackInfo ci) {
		create$setExtraData(entity);
	}

	@Unique
	private void create$setExtraData(Entity entity) {
		if (entity instanceof ExtraSpawnDataEntity extra) {
			create$extraDataBuf = PacketByteBufs.create();
			extra.writeSpawnData(create$extraDataBuf);
		}
	}

	@Inject(method = "write(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("TAIL"))
	public void create$onTailWrite(FriendlyByteBuf buf, CallbackInfo ci) {
		if (create$extraDataBuf != null) {
			buf.writeBoolean(true);
			buf.writeVarInt(create$extraDataBuf.writerIndex());
			buf.writeBytes(create$extraDataBuf);
		} else {
			buf.writeBoolean(false);
		}
	}

	@Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("TAIL"))
	public void create$onTailRead(FriendlyByteBuf buf, CallbackInfo ci) {
		boolean hasExtraData = buf.readBoolean();
		if (hasExtraData) {
			int readable = buf.readVarInt();
			if (readable != 0) {
				create$extraDataBuf = new FriendlyByteBuf(buf.readBytes(readable));
			}
		}
	}

	@Unique
	@Override
	public FriendlyByteBuf create$getExtraDataBuf() {
		return create$extraDataBuf;
	}
}
