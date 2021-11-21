package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.entity.ExtraSpawnDataEntity;
import com.simibubi.create.lib.extensions.ClientboundAddEntityPacketExtensions;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

@Mixin(ClientboundAddEntityPacket.class)
public abstract class ClientboundAddEntityPacketMixin implements ClientboundAddEntityPacketExtensions {
	@Unique
	private FriendlyByteBuf create$extraDataBuf;

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/entity/Entity;I)V")
	public void create$onEntityCtor(Entity entity, int entityData, CallbackInfo ci) {
		create$setExtraData(entity);
	}

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EntityType;ILnet/minecraft/core/BlockPos;)V")
	public void create$onEntityCtor(Entity entity, EntityType<?> entityType, int data, BlockPos pos, CallbackInfo ci) {
		create$setExtraData(entity);
	}

	@Unique
	private void create$setExtraData(Entity entity) {
		if (entity instanceof ExtraSpawnDataEntity) {
			create$extraDataBuf = new FriendlyByteBuf(Unpooled.buffer());
			((ExtraSpawnDataEntity) entity).writeSpawnData(create$extraDataBuf);
		}
	}

	@Inject(at = @At("TAIL"), method = "write")
	public void create$onTailWrite(FriendlyByteBuf buf, CallbackInfo ci) {
		if (create$extraDataBuf != null) {
			buf.writeBytes(create$extraDataBuf);
		}
	}

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V")
	public void create$onTailRead(FriendlyByteBuf buf, CallbackInfo ci) {
		int readable = buf.readableBytes();
		if (readable != 0) {
			this.create$extraDataBuf = new FriendlyByteBuf(buf.readBytes(readable));
		}
	}

	@Inject(at = @At("TAIL"), method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V")
	public void create$onTailApply(ClientGamePacketListener listener, CallbackInfo ci) {
		if (create$extraDataBuf != null) {
			create$extraDataBuf.release();
		}
	}

	@Unique
	@Override
	public FriendlyByteBuf create$getExtraDataBuf() {
		return create$extraDataBuf;
	}
}
