package com.simibubi.create.content.contraptions.actors.plough;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.contraptions.actors.AttachedActorBlock;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.neoforged.neoforge.common.util.FakePlayer;

import org.jetbrains.annotations.NotNull;

public class PloughBlock extends AttachedActorBlock {

	public static final MapCodec<PloughBlock> CODEC = simpleCodec(PloughBlock::new);

	public PloughBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
	}

	/**
	 * The OnHoeUse event takes a player, so we better not pass null
	 */
	static class PloughFakePlayer extends FakePlayer {

		public static final GameProfile PLOUGH_PROFILE =
				new GameProfile(UUID.fromString("9e2faded-eeee-4ec2-c314-dad129ae971d"), "Plough");

		public PloughFakePlayer(ServerLevel world) {
			super(world, PLOUGH_PROFILE);
		}

	}

	@Override
	protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}
}
