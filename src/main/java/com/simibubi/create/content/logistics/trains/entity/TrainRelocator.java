package com.simibubi.create.content.logistics.trains.entity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandlerClient;
import com.simibubi.create.content.logistics.trains.GraphLocation;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackGraphHelper;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.ISignalBoundaryListener;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.ITrackSelector;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.SteerDirection;
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
	static Boolean lastHoveredResult;

	@OnlyIn(Dist.CLIENT)
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

		if (simulate) {
			if (lastHoveredPos != null && lastHoveredPos.equals(blockPos))
				return lastHoveredResult;
			lastHoveredPos = blockPos;
		}

		BlockState blockState = mc.level.getBlockState(blockPos);
		if (!(blockState.getBlock()instanceof ITrackBlock track))
			return lastHoveredResult = null;

		Vec3 lookAngle = mc.player.getLookAngle();
		boolean result = relocate(relocating, mc.level, blockPos, lookAngle, true);
		if (!simulate && result)
			AllPackets.channel
				.sendToServer(new TrainRelocationPacket(relocatingTrain, blockPos, lookAngle, relocatingEntityId));
		return lastHoveredResult = result;
	}

	public static boolean relocate(Train train, Level level, BlockPos pos, Vec3 lookAngle, boolean simulate) {
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
		ISignalBoundaryListener ignoreSignals = probe.ignoreSignals();
		List<Pair<Couple<TrackNode>, Double>> recordedLocations = new ArrayList<>();
		Consumer<TravellingPoint> recorder =
			tp -> recordedLocations.add(Pair.of(Couple.create(tp.node1, tp.node2), tp.position));
		ITrackSelector steer = probe.steer(SteerDirection.NONE, track.getUpNormal(level, pos, blockState));
		MutableBoolean blocked = new MutableBoolean(false);

		train.forEachTravellingPointBackwards((tp, d) -> {
			if (blocked.booleanValue())
				return;
			probe.travel(graph, d, steer, ignoreSignals);
			if (probe.blocked) {
				blocked.setTrue();
				return;
			}
			recorder.accept(probe);
		});

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

		train.status.successfulMigration();
		train.collectInitiallyOccupiedSignalBlocks();
		return true;
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

	@OnlyIn(Dist.CLIENT)
	public static boolean carriageWrenched(Vec3 vec3, CarriageContraptionEntity entity) {
		Train train = getTrainFromEntity(entity);
		if (train != null && !train.heldForAssembly) {
			relocatingOrigin = vec3;
			relocatingTrain = train.id;
			relocatingEntityId = entity.getId();
			return true;
		}
		return false;
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
