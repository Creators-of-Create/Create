package com.simibubi.create.infrastructure.debugInfo.element;

import com.simibubi.create.infrastructure.debugInfo.InfoProvider;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record InfoEntry(String name, InfoProvider provider) implements InfoElement {
	@Override
	public void write(Player player, FriendlyByteBuf buffer) {
		buffer.writeBoolean(false);
		buffer.writeUtf(name);
		buffer.writeUtf(provider.getInfoSafe(player));
	}

	@Override
	public void print(int depth, @Nullable Player player, Consumer<String> lineConsumer) {
		String value = provider.getInfoSafe(player);
		String indent = Stream.generate(() -> "\t").limit(depth).collect(Collectors.joining(""));
		if (value.contains("\n")) {
			lineConsumer.accept(indent + "<details>");
			lineConsumer.accept(indent + "<summary>" + name + "</summary>");

			for (String line : value.split("\n")) {
				lineConsumer.accept(indent + '\t' + line);
			}

			lineConsumer.accept(indent + "</details>");
		} else {
			lineConsumer.accept(indent + name + ": " + value);
		}

	}

	public static InfoEntry read(FriendlyByteBuf buffer) {
		String name = buffer.readUtf();
		String value = buffer.readUtf();
		return new InfoEntry(name, player -> value);
	}
}
