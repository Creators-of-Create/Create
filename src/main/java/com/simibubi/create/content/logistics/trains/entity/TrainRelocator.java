package com.simibubi.create.content.logistics.trains.entity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandlerClient;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackGraphHelper;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.ITrackSelector;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.SteerDirection;
import com.simibubi.create.content.logistics.trains.management.GraphLocation;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;

public class TrainRelocator {

	static WeakReference<CarriageContraptionEntity> hoveredEntity = new WeakReference<>(null);
	static UUID relocatingTrain;
	static Vec3 relocatingOrigin;

	static BlockPos lastHoveredPos;
	static Boolean lastHoveredResult;

	public static void onClicked(ClickInputEvent event) {
		if (relocatingTrain == null)
			return;

		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null)
			return;

		if (!player.position()
			.closerThan(relocatingOrigin, 24) || player.isSteppingCarefully()) {
			relocatingTrain = null;
			player.displayClientMessage(Lang.translate("train.relocate.abort")
				.withStyle(ChatFormatting.RED), true);
			return;
		}

		if (player.isPassenger())
			return;
		if (mc.level == null)
			return;
		Train relocating = getRelocating();
		if (relocating != null) {
			Boolean relocate = relocateClient(relocating, false); // TODO send packet
			if (relocate != null && relocate.booleanValue()) {
				relocatingTrain = null;
				player.displayClientMessage(Lang.translate("train.relocate.success")
					.withStyle(ChatFormatting.GREEN), true);
			}
			if (relocate != null)
				event.setCanceled(true);
		}
	}

	@Nullable
	public static Boolean relocateClient(Train relocating, boolean simulate) {
		Minecraft mc = Minecraft.getInstance();
		HitResult hitResult = mc.hitResult;
		if (!(hitResult instanceof BlockHitResult blockhit))
			return null;
		BlockPos blockPos = blockhit.getBlockPos();

		if (simulate) {
			if (lastHoveredPos != null && lastHoveredPos.equals(blockPos))
				return lastHoveredResult;
			lastHoveredPos = blockPos;
		}

		BlockState blockState = mc.level.getBlockState(blockPos);
		if (!(blockState.getBlock()instanceof ITrackBlock track))
			return lastHoveredResult = null;
		return lastHoveredResult = relocate(relocating, mc.player, blockPos, simulate);
	}

	public static boolean relocate(Train train, Player player, BlockPos pos, boolean simulate) {
		Vec3 lookAngle = player.getLookAngle();
		Level level = player.getLevel();
		BlockState blockState = level.getBlockState(pos);
		if (!(blockState.getBlock()instanceof ITrackBlock track))
			return false;

		Pair<Vec3, AxisDirection> nearestTrackAxis = track.getNearestTrackAxis(level, pos, blockState, lookAngle);
		GraphLocation graphLocation =
			TrackGraphHelper.getGraphLocationAt(level, pos, nearestTrackAxis.getSecond(), nearestTrackAxis.getFirst());

		if (graphLocation == null)
			return false;

		TrackGraph graph = graphLocation.graph;
		TrackNode node1 = graph.locateNode(graphLocation.edge.getFirst());
		TrackNode node2 = graph.locateNode(graphLocation.edge.getSecond());
		TrackEdge edge = graph.getConnectionsFrom(node1)
			.get(node2);
		if (edge == null)
			return false;

		TravellingPoint probe = new TravellingPoint(node1, node2, edge, graphLocation.position);
		List<Pair<Couple<TrackNode>, Double>> recordedLocations = new ArrayList<>();
		Consumer<TravellingPoint> recorder =
			tp -> recordedLocations.add(Pair.of(Couple.create(tp.node1, tp.node2), tp.position));
		recorder.accept(probe);

		double lastWheelOffset = 0;
		ITrackSelector steer = probe.steer(SteerDirection.NONE, track.getUpNormal(level, pos, blockState));
		for (int i = 0; i < train.carriages.size(); i++) {
			int index = train.carriages.size() - i - 1;
			Carriage carriage = train.carriages.get(index);
			double trailSpacing = carriage.trailingBogey().type.getWheelPointSpacing();
			if (i > 0) {
				probe.travel(graph, train.carriageSpacing.get(index) - lastWheelOffset - trailSpacing / 2, steer);
				if (probe.blocked)
					return false;
				recorder.accept(probe);
			}

			// inside 1st bogey
			probe.travel(graph, trailSpacing, steer);
			if (probe.blocked)
				return false;
			recorder.accept(probe);

			lastWheelOffset = trailSpacing / 2;

			if (!carriage.isOnTwoBogeys())
				continue;

			double leadSpacing = carriage.leadingBogey().type.getWheelPointSpacing();

			// between bogeys
			probe.travel(graph, carriage.bogeySpacing - lastWheelOffset - leadSpacing / 2, steer);
			if (probe.blocked)
				return false;
			recorder.accept(probe);

			// inside 2nd bogey
			probe.travel(graph, leadSpacing, steer);
			if (probe.blocked)
				return false;
			recorder.accept(probe);

			lastWheelOffset = leadSpacing / 2;
		}

		if (simulate)
			return true;

		train.derailed = false;
		train.graph = graph;
		train.migratingPoints.clear();
		train.forEachTravellingPoint(tp -> {
			Pair<Couple<TrackNode>, Double> last = recordedLocations.remove(recordedLocations.size() - 1);
			tp.node1 = last.getFirst()
				.getFirst();
			tp.node2 = last.getFirst()
				.getSecond();
			tp.position = last.getSecond();
			tp.edge = graph.getConnectionsFrom(tp.node1)
				.get(tp.node2);
		});

		train.status.successfulMigration();
		train.leaveStation();
		return true;
	}

	public static void clientTick() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		if (player == null)
			return;
		if (player.isPassenger())
			return;
		if (mc.level == null)
			return;

		if (relocatingTrain != null) {
			Train relocating = getRelocating();
			if (relocating == null) {
				relocatingTrain = null;
				return;
			}

			if (Math.abs(relocating.speed) > 1 / 1024d) {
				player.displayClientMessage(Lang.translate("train.cannot_relocate_moving")
					.withStyle(ChatFormatting.RED), true);
				relocatingTrain = null;
				return;
			}

			if (!AllItems.WRENCH.isIn(player.getMainHandItem())) {
				player.displayClientMessage(Lang.translate("train.relocate.abort")
					.withStyle(ChatFormatting.RED), true);
				relocatingTrain = null;
				return;
			}

			if (!player.position()
				.closerThan(relocatingOrigin, 24)) {
				player.displayClientMessage(Lang.translate("train.relocate.too_far")
					.withStyle(ChatFormatting.RED), true);
				return;
			}

			Boolean success = relocateClient(relocating, true);
			if (success == null)
				player.displayClientMessage(Lang.translate("train.relocate", relocating.name), true);
			else if (success.booleanValue())
				player.displayClientMessage(Lang.translate("train.relocate.valid")
					.withStyle(ChatFormatting.GREEN), true);
			else
				player.displayClientMessage(Lang.translate("train.relocate.invalid")
					.withStyle(ChatFormatting.RED), true);
			return;
		}

		Couple<Vec3> rayInputs = ContraptionHandlerClient.getRayInputs(player);
		Vec3 origin = rayInputs.getFirst();
		Vec3 target = rayInputs.getSecond();

		CarriageContraptionEntity currentEntity = hoveredEntity.get();
		if (currentEntity != null) {
			if (ContraptionHandlerClient.rayTraceContraption(origin, target, currentEntity) != null)
				return;
			hoveredEntity = new WeakReference<>(null);
		}

		AABB aabb = new AABB(origin, target);
		List<CarriageContraptionEntity> intersectingContraptions =
			mc.level.getEntitiesOfClass(CarriageContraptionEntity.class, aabb);

		for (CarriageContraptionEntity contraptionEntity : intersectingContraptions) {
			if (ContraptionHandlerClient.rayTraceContraption(origin, target, contraptionEntity) == null)
				continue;
			hoveredEntity = new WeakReference<>(contraptionEntity);
		}
	}

	public static boolean carriageWrenched(Vec3 vec3, CarriageContraptionEntity entity) {
		Train train = getTrainFromEntity(entity);
		if (train != null && !train.heldForAssembly) {
			relocatingOrigin = vec3;
			relocatingTrain = train.id;
			return true;
		}
		return false;
	}

	public static boolean addToTooltip(List<Component> tooltip, boolean shiftKeyDown) {
		Train train = getTrainFromEntity(hoveredEntity.get());
		if (train != null && train.derailed) {
			TooltipHelper.addHint(tooltip, "hint.derailed_train");
			return true;
		}
		return false;
	}

	private static Train getRelocating() {
		if (relocatingTrain == null)
			return null;
		return Create.RAILWAYS.trains.get(relocatingTrain); // TODO: thread breach
	}

	private static Train getTrainFromEntity(CarriageContraptionEntity carriageContraptionEntity) {
		if (carriageContraptionEntity == null)
			return null;
		Carriage carriage = carriageContraptionEntity.getCarriage();
		if (carriage == null)
			return null;
		return carriage.train;
	}

}
