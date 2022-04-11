package com.simibubi.create.foundation.command;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.GlobalRailwayManager;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgePointType;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalBoundary;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.GlobalStation;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleRuntime;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleRuntime.State;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class DumpRailwaysCommand {

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("dumpRailways")
			.requires(cs -> cs.hasPermission(1))
			.executes(ctx -> {
				CommandSourceStack source = ctx.getSource();
				fillReport(source.getLevel(),
					(s, f) -> source.sendSuccess(new TextComponent(s).withStyle(st -> st.withColor(f)), false));
				return 1;
			});
	}

	static void fillReport(ServerLevel level, BiConsumer<String, Integer> chat) {
		GlobalRailwayManager railways = Create.RAILWAYS;
		int white = ChatFormatting.WHITE.getColor();
		int blue = 0xD3DEDC;
		int darkBlue = 0x92A9BD;
		int bright = 0xFFEFEF;
		int orange = 0xFFAD60;

		for (int i = 0; i < 10; i++)
			chat.accept("", white);
		chat.accept("-+------<< Railways Summary: >>------+-", white);
		chat.accept("Track Networks: " + railways.trackNetworks.size(), blue);
		chat.accept("Signal Groups: " + railways.signalEdgeGroups.size(), blue);
		chat.accept("Trains: " + railways.trains.size(), blue);
		chat.accept("", white);

		for (Entry<UUID, TrackGraph> entry : railways.trackNetworks.entrySet()) {
			TrackGraph graph = entry.getValue();
			UUID id = entry.getKey();
			chat.accept(id.toString()
				.substring(0, 5) + ": Track Graph, "
				+ graph.getNodes()
					.size()
				+ " Nodes", graph.color.getRGB());
			Collection<SignalBoundary> signals = graph.getPoints(EdgePointType.SIGNAL);
			if (!signals.isEmpty())
				chat.accept(" -> " + signals.size() + " registered Signals", blue);
			for (GlobalStation globalStation : graph.getPoints(EdgePointType.STATION)) {
				BlockPos pos = globalStation.getTilePos();
				if (pos == null)
					pos = BlockPos.ZERO;
				chat.accept(" -> " + globalStation.name + " (" + globalStation.id.toString()
					.substring(0, 5) + ") [" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]", darkBlue);
				if (globalStation.getPresentTrain() != null) {
					chat.accept("  > Currently Occupied by " + globalStation.getPresentTrain().name.getString(), blue);
				} else {
					Train imminentTrain = globalStation.getImminentTrain();
					if (imminentTrain != null) {
						chat.accept("  > Reserved by " + imminentTrain.name.getString() + " ("
							+ Mth.floor(imminentTrain.navigation.distanceToDestination) + "m away)", blue);
					}
				}
			}
			chat.accept("", white);
		}

		for (Train train : railways.trains.values()) {
			chat.accept(train.id.toString()
				.substring(0, 5) + ": " + train.name.getString() + ", " + train.carriages.size() + " Wagons", bright);
			if (train.graph != null)
				chat.accept(" -> On Track: " + train.graph.id.toString()
					.substring(0, 5), train.graph.color.getRGB());
			if (train.derailed)
				chat.accept(" -> Derailed", orange);
			LivingEntity owner = train.getOwner(level);
			if (owner != null)
				chat.accept("  > Owned by " + owner.getName()
					.getString(), blue);
			GlobalStation currentStation = train.getCurrentStation();
			if (currentStation != null) {
				chat.accept("  > Waiting at: " + currentStation.name, blue);
			} else if (train.navigation.destination != null) {
				chat.accept("  > Travelling to " + train.navigation.destination.name + " ("
					+ Mth.floor(train.navigation.distanceToDestination) + "m away)", darkBlue);
			}
			ScheduleRuntime runtime = train.runtime;
			if (runtime.getSchedule() != null) {
				chat.accept("  > Schedule, Entry " + runtime.currentEntry + ", "
					+ (runtime.paused ? "Paused"
						: runtime.state.name()
							.replaceAll("_", " ")),
					runtime.paused ? darkBlue : blue);
				if (!runtime.paused && runtime.state != State.POST_TRANSIT) {
					for (Component component : runtime.getSchedule().entries.get(runtime.currentEntry).instruction
						.getTitleAs("destination")) {
						chat.accept("   - " + component.getString(), blue);
					}
				}
			} else
				chat.accept("  > Idle, No Schedule", darkBlue);
			chat.accept("", white);
		}
		chat.accept("-+--------------------------------+-", white);
	}

}
