package com.simibubi.create.lib.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.network.protocol.PacketUtils;

import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.block.CustomDataPacketHandlingTileEntity;
import com.simibubi.create.lib.entity.ExtraSpawnDataEntity;
import com.simibubi.create.lib.extensions.ClientboundAddEntityPacketExtensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
	@Unique
	private static final Logger CREATE$LOGGER = LogManager.getLogger();
	@Final
	@Shadow
	private Connection connection;
	@Unique
	private boolean create$tileEntityHandled;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;putNonPlayerEntity(ILnet/minecraft/world/entity/Entity;)V", shift = Shift.AFTER),
			method = "handleAddEntity",
			locals = LocalCapture.CAPTURE_FAILHARD)
	public void create$afterAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci, EntityType<?> entityType, Entity entity) {
		if (entity instanceof ExtraSpawnDataEntity) {
			FriendlyByteBuf extraData = ((ClientboundAddEntityPacketExtensions) packet).create$getExtraDataBuf();
			if (extraData != null) {
				((ExtraSpawnDataEntity) entity).readSpawnData(extraData);
			}
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;load(Lnet/minecraft/nbt/CompoundTag;)V"),
		method = "handleBlockEntityData"
	)
	private void create$teIsHandled1(ClientboundBlockEntityDataPacket sUpdateTileEntityPacket, CallbackInfo ci) {
		create$tileEntityHandled = true;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CommandBlockEditScreen;updateGui()V"),
		method = "handleBlockEntityData"
	)
	private void create$teIsHandled2(ClientboundBlockEntityDataPacket sUpdateTileEntityPacket, CallbackInfo ci) {
		create$tileEntityHandled = true;
	}

	@Inject(at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD,
			method = "handleBlockEntityData",
			cancellable = true)
	public void create$handleCustomTileEntity(ClientboundBlockEntityDataPacket sUpdateTileEntityPacket, CallbackInfo ci, BlockPos pos, BlockEntity tileEntity) {
		if (!create$tileEntityHandled) {
			if (tileEntity == null) {
				CREATE$LOGGER.error("Received invalid update packet for null TileEntity");
				ci.cancel();
			} else if (tileEntity instanceof CustomDataPacketHandlingTileEntity) {
				((CustomDataPacketHandlingTileEntity) tileEntity).onDataPacket(connection, sUpdateTileEntityPacket);
			}
		} else {
			create$tileEntityHandled = false;
		}
	}
}
