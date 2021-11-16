package com.simibubi.create.lib.entity;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class FakePlayer extends ServerPlayer {
	public FakePlayer(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile) {
		super(minecraftServer, serverLevel, gameProfile);
	}

	public FakePlayer(ServerLevel serverLevel, GameProfile gameProfile) {
		super(serverLevel.getServer(), serverLevel, gameProfile);
	}
}
