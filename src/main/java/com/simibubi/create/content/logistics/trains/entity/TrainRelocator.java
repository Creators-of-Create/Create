package com.simibubi.create.content.logistics.trains.entity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandlerClient;
import com.simibubi.create.content.logistics.trains.GraphLocation;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackGraphHelper;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.IEdgePointListener;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.ITrackSelector;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.ITurnListener;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.SteerDirection;
import com.simibubi.create.content.logistics.trains.track.BezierTrackPointLocation;
import com.simibubi.create.content.logistics.trains.track.TrackBlockOutline;
import com.simibubi.create.content.logistics.trains.track.TrackBlockOutline.BezierPointSelection;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;

public class TrainRelocator {

	static WeakReference<CarriageContraptionEntity> hoveredEntity = new WeakReference<>(null);
	static UUID relocatingTrain;
	static Vec3 relocatingOrigin;
	static int relocatingEntityId;

	static BlockPos lastHoveredPos;
	static BezierTrackPointLocation lastHoveredBezierSegment;
	static Boolean lastHoveredResult;
	static List<Vec3> toVisualise;

	public static boolean isRelocating() {
		return relocatingTrain != null;
	}

	@OnlyIn(Dist.CLIENT)
	public static void onClicked(ClickInputEvent event) {
		if (relocatingTrain == null)
			return;

		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null)
			return;
		if (player.isSpectator())
			return;

		if (!player.position()
			.closerThan(relocatingOrigin, 24) || player.isSteppingCarefully()) {
			relocatingTrain = null;
			player.displayClientMessage(Lang.translateDirect("train.relocate.abort")
				.withStyle(ChatFormatting.RED), true);
			return;
		}

		if (player.isPassenger())
			return;
		if (mc.level == null)
			return;
		Train relocating = getRelocating(mc.level);
		if (relocating != null) {
			Boolean relocate = relocateClient(relocating, false);
			if (relocate != null && relocate.booleanValue())
				relocatingTrain = null;
			if (relocate != null)
				event.setCanceled(true);
		}
	}

	@Nullable
	@OnlyIn(Dist.CLIENT)
	public static Boolean relocateClient(Train relocating, boolean simulate) {
		Minecraft mc = Minecraft.getInstance();
		HitResult hitResult = mc.hitResult;
		if (!(hitResult instanceof BlockHitResult blockhit))
			return null;

		BlockPos blockPos = blockhit.getBlockPos();
		BezierTrackPointLocation hoveredBezier = null;

		boolean upsideDown = relocating.carriages.get(0).leadingBogey().isUpsideDown();
		Vec3 offset = upsideDown ? new Vec3(0, -0.5, 0) : Vec3.ZERO;

		if (simulate && toVisualise != null && lastHoveredResult != null) {
			for (int i = 0; i < toVisualise.size() - 1; i++) {
				Vec3 vec1 = toVisualise.get(i).add(offset);
				Vec3 vec2 = toVisualise.get(i + 1).add(offset);
				CreateClient.OUTLINER.showLine(Pair.of(relocating, i), vec1.add(0, -.925f, 0), vec2.add(0, -.925f, 0))
					.colored(lastHoveredResult || i != toVisualise.size() - 2 ? 0x95CD41 : 0xEA5C2B)
					.disableLineNormals()
					.lineWidth(i % 2 == 1 ? 1 / 6f : 1 / 4f);
			}
		}

		BezierPointSelection bezierSelection = TrackBlockOutline.result;
		if (bezierSelection != null) {
			blockPos = bezierSelection.te()
				.getBlockPos();
			hoveredBezier = bezierSelection.loc();
		}

		if (simulate) {
			if (lastHoveredPos != null && lastHoveredPos.equals(blockPos)
				&& Objects.equals(lastHoveredBezierSegment, hoveredBezier))
				return lastHoveredResult;
			lastHoveredPos = blockPos;
			lastHoveredBezierSegment = hoveredBezier;
			toVisualise = null;
		}

		BlockState blockState = mc.level.getBlockState(blockPos);
		if (!(blockState.getBlock()instanceof ITrackBlock track))
			return lastHoveredResult = null;

		Vec3 lookAngle = mc.player.getLookAngle();
		boolean direction = bezierSelection != null && lookAngle.dot(bezierSelection.direction()) < 0;
		boolean result = relocate(relocating, mc.level, blockPos, hoveredBezier, direction, lookAngle, true);
		if (!simulate && result) {
			relocating.carriages.forEach(c -> c.forEachPresentEntity(e -> e.nonDamageTicks = 10));
			AllPackets.channel.sendToServer(new TrainRelocationPacket(relocatingTrain, blockPos, hoveredBezier,
				direction, lookAngle, relocatingEntityId));
		}

		return lastHoveredResult = result;
	}

	public static boolean relocate(Train train, Level level, BlockPos pos, BezierTrackPointLocation bezier,
		boolean bezierDirection, Vec3 lookAngle, boolean simulate) {
		BlockState blockState = level.getBlockState(pos);
		if (!(blockState.getBlock()instanceof ITrackBlock track))
			return false;

		Pair<Vec3, AxisDirection> nearestTrackAxis = track.getNearestTrackAxis(level, pos, blockState, lookAngle);
		GraphLocation graphLocation = bezier != null
			? TrackGraphHelper.getBezierGraphLocationAt(level, pos,
				bezierDirection ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, bezier)
			: TrackGraphHelper.getGraphLocationAt(level, pos, nearestTrackAxis.getSecond(),
				nearestTrackAxis.getFirst());

		if (graphLocation == null)
			return false;

		TrackGraph graph = graphLocation.graph;
		TrackNode node1 = graph.locateNode(graphLocation.edge.getFirst());
		TrackNode node2 = graph.locateNode(graphLocation.edge.getSecond());
		TrackEdge edge = graph.getConnectionsFrom(node1)
			.get(node2);
		if (edge == null)
			return false;

		TravellingPoint probe = new TravellingPoint(node1, node2, edge, graphLocation.position, false);
		IEdgePointListener ignoreSignals = probe.ignoreEdgePoints();
		ITurnListener ignoreTurns = probe.ignoreTurns();
		List<Pair<Couple<TrackNode>, Double>> recordedLocations = new ArrayList<>();
		List<Vec3> recordedVecs = new ArrayList<>();
		Consumer<TravellingPoint> recorder = tp -> {
			recordedLocations.add(Pair.of(Couple.create(tp.node1, tp.node2), tp.position));
			recordedVecs.add(tp.getPosition());
		};
		ITrackSelector steer = probe.steer(SteerDirection.NONE, track.getUpNormal(level, pos, blockState));
		MutableBoolean blocked = new MutableBoolean(false);
		MutableBoolean portal = new MutableBoolean(false);

		MutableInt blockingIndex = new MutableInt(0);
		train.forEachTravellingPointBackwards((tp, d) -> {
			if (blocked.booleanValue())
				return;
			probe.travel(graph, d, steer, ignoreSignals, ignoreTurns, $ -> {
				portal.setTrue();
				return true;
			});
			recorder.accept(probe);
			if (probe.blocked || portal.booleanValue()) {
				blocked.setTrue();
				return;
			}
			blockingIndex.increment();
		});

		if (level.isClientSide && simulate && !recordedVecs.isEmpty()) {
			toVisualise = new ArrayList<>();
			toVisualise.add(recordedVecs.get(0));
		}

		for (int i = 0; i < recordedVecs.size() - 1; i++) {
			Vec3 vec1 = recordedVecs.get(i);
			Vec3 vec2 = recordedVecs.get(i + 1);
			boolean blocking = i >= blockingIndex.intValue() - 1;
			boolean collided = !blocked.booleanValue()
				&& Train.findCollidingTrain(level, vec1, vec2, train, level.dimension()) != null;
			if (level.isClientSide && simulate)
				toVisualise.add(vec2);
			if (collided || blocking)
				return false;
		}

		if (blocked.booleanValue())
			return false;

		if (simulate)
			return true;

		train.leaveStation();
		train.derailed = false;
		train.navigation.waitingForSignal = null;
		train.occupiedSignalBlocks.clear();
		train.graph = graph;
		train.speed = 0;
		train.migratingPoints.clear();
		train.cancelStall();

		if (train.navigation.destination != null)
			train.navigation.cancelNavigation();

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

		for (Carriage carriage : train.carriages)
			carriage.updateContraptionAnchors();

		train.status.successfulMigration();
		train.collectInitiallyOccupiedSignalBlocks();
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public static void visualise(Train train, int i, Vec3 v1, Vec3 v2, boolean valid) {
		CreateClient.OUTLINER.showLine(Pair.of(train, i), v1.add(0, -.825f, 0), v2.add(0, -.825f, 0))
			.colored(valid ? 0x95CD41 : 0xEA5C2B)
			.disableLineNormals()
			.lineWidth(i % 2 == 1 ? 1 / 6f : 1 / 4f);
	}

	@OnlyIn(Dist.CLIENT)
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
			Train relocating = getRelocating(mc.level);
			if (relocating == null) {
				relocatingTrain = null;
				return;
			}

			Entity entity = mc.level.getEntity(relocatingEntityId);
			if (entity instanceof AbstractContraptionEntity ce && Math.abs(ce.getPosition(0)
				.subtract(ce.getPosition(1))
				.lengthSqr()) > 1 / 1024d) {
				player.displayClientMessage(Lang.translateDirect("train.cannot_relocate_moving")
					.withStyle(ChatFormatting.RED), true);
				relocatingTrain = null;
				return;
			}

			if (!AllItems.WRENCH.isIn(player.getMainHandItem())) {
				player.displayClientMessage(Lang.translateDirect("train.relocate.abort")
					.withStyle(ChatFormatting.RED), true);
				relocatingTrain = null;
				return;
			}

			if (!player.position()
				.closerThan(relocatingOrigin, 24)) {
				player.displayClientMessage(Lang.translateDirect("train.relocate.too_far")
					.withStyle(ChatFormatting.RED), true);
				return;
			}

			Boolean success = relocateClient(relocating, true);
			if (success == null)
				player.displayClientMessage(Lang.translateDirect("train.relocate", relocating.name), true);
			else if (success.booleanValue())
				player.displayClientMessage(Lang.translateDirect("train.relocate.valid")
					.withStyle(ChatFormatting.GREEN), true);
			else
				player.displayClientMessage(Lang.translateDirect("train.relocate.invalid")
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

	@OnlyIn(Dist.CLIENT)
	public static boolean carriageWrenched(Vec3 vec3, CarriageContraptionEntity entity) {
		Train train = getTrainFromEntity(entity);
		if (train == null)
			return false;
		relocatingOrigin = vec3;
		relocatingTrain = train.id;
		relocatingEntityId = entity.getId();
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean addToTooltip(List<Component> tooltip, boolean shiftKeyDown) {
		Train train = getTrainFromEntity(hoveredEntity.get());
		if (train != null && train.derailed) {
			TooltipHelper.addHint(tooltip, "hint.derailed_train");
			return true;
		}
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	private static Train getRelocating(LevelAccessor level) {
		return relocatingTrain == null ? null : Create.RAILWAYS.sided(level).trains.get(relocatingTrain);
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
