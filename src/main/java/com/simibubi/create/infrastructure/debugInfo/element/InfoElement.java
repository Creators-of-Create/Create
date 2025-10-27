package com.simibubi.create.infrastructure.debugInfo.element;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public sealed interface InfoElement permits DebugInfoSection, InfoEntry {
	void print(int depth, @Nullable Player player, Consumer<String> lineConsumer);

	default void print(@Nullable Player player, Consumer<String> lineConsumer) {
		print(0, player, lineConsumer);
	}
}
