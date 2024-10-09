package com.simibubi.create.compat.trainmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.CreateClient;
import com.simibubi.create.compat.trainmap.TrainMapSync.SignalState;
import com.simibubi.create.compat.trainmap.TrainMapSync.TrainMapSyncEntry;
import com.simibubi.create.compat.trainmap.TrainMapSync.TrainState;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CClient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TrainMapManager {

	public static void tick() {
		TrainMapRenderer map = TrainMapRenderer.INSTANCE;
		if (map.trackingVersion != CreateClient.RAILWAYS.version
			|| map.trackingDim != Minecraft.getInstance().level.dimension()
			|| map.trackingTheme != AllConfigs.client().trainMapColorTheme.get()) {
			redrawAll();
		}
	}

	public static List<FormattedText> renderAndPick(GuiGraphics graphics, int mouseX, int mouseY, float pt,
		boolean linearFiltering, Rect2i bounds) {
		Object hoveredElement = null;
		
		int offScreenMargin = 32;
		bounds.setX(bounds.getX() - offScreenMargin);
		bounds.setY(bounds.getY() - offScreenMargin);
		bounds.setWidth(bounds.getWidth() + 2 * offScreenMargin);
		bounds.setHeight(bounds.getHeight() + 2 * offScreenMargin);

		TrainMapRenderer.INSTANCE.render(graphics, mouseX, mouseY, pt, linearFiltering, bounds);
		hoveredElement = drawTrains(graphics, mouseX, mouseY, pt, hoveredElement, bounds);
		hoveredElement = drawPoints(graphics, mouseX, mouseY, pt, hoveredElement, bounds);

		graphics.bufferSource()
			.endBatch();

		if (hoveredElement instanceof GlobalStation station)
			return List.of(Components.literal(station.name));

		if (hoveredElement instanceof Train train)
			return listTrainDetails(train);

		return null;
	}

	public static void renderToggleWidget(GuiGraphics graphics, int x, int y) {
		boolean enabled = AllConfigs.client().showTrainMapOverlay.get();
		if (CreateClient.RAILWAYS.trackNetworks.isEmpty())
			return;
		RenderSystem.enableBlend();
		PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.translate(0, 0, 300);
		AllGuiTextures.TRAINMAP_TOGGLE_PANEL.render(graphics, x, y);
		(enabled ? AllGuiTextures.TRAINMAP_TOGGLE_ON : AllGuiTextures.TRAINMAP_TOGGLE_OFF).render(graphics, x + 18,
			y + 3);
		pose.popPose();
	}

	public static boolean handleToggleWidgetClick(int mouseX, int mouseY, int x, int y) {
		if (!isToggleWidgetHovered(mouseX, mouseY, x, y))
			return false;
		CClient config = AllConfigs.client();
		config.showTrainMapOverlay.set(!config.showTrainMapOverlay.get());
		return true;
	}

	public static boolean isToggleWidgetHovered(int mouseX, int mouseY, int x, int y) {
		if (CreateClient.RAILWAYS.trackNetworks.isEmpty())
			return false;
		if (mouseX < x || mouseX >= x + AllGuiTextures.TRAINMAP_TOGGLE_PANEL.width)
			return false;
		if (mouseY < y || mouseY >= y + AllGuiTextures.TRAINMAP_TOGGLE_PANEL.height)
			return false;
		return true;
	}

	private static List<FormattedText> listTrainDetails(Train train) {
		List<FormattedText> output = new ArrayList<>();
		int blue = 0xD3DEDC;
		int darkBlue = 0x92A9BD;
		int bright = 0xFFEFEF;
		int orange = 0xFFAD60;

		TrainMapSyncEntry trainEntry = TrainMapSyncClient.currentData.get(train.id);
		if (trainEntry == null)
			return Collections.emptyList();
		TrainState state = trainEntry.state;
		SignalState signalState = trainEntry.signalState;

		Lang.text(train.name.getString())
			.color(bright)
			.addTo(output);

		if (!trainEntry.ownerName.isBlank())
			Lang.translate("train_map.train_owned_by", trainEntry.ownerName)
				.color(blue)
				.addTo(output);

		switch (state) {

		case CONDUCTOR_MISSING:
			Lang.translate("train_map.conductor_missing")
				.color(orange)
				.addTo(output);
			return output;
		case DERAILED:
			Lang.translate("train_map.derailed")
				.color(orange)
				.addTo(output);
			return output;
		case NAVIGATION_FAILED:
			Lang.translate("train_map.navigation_failed")
				.color(orange)
				.addTo(output);
			return output;
		case SCHEDULE_INTERRUPTED:
			Lang.translate("train_map.schedule_interrupted")
				.color(orange)
				.addTo(output);
			return output;
		case RUNNING_MANUALLY:
			Lang.translate("train_map.player_controlled")
				.color(blue)
				.addTo(output);
			break;

		case RUNNING:
		default:
			break;
		}

		String currentStation = trainEntry.targetStationName;
		int targetStationDistance = trainEntry.targetStationDistance;

		if (!currentStation.isBlank()) {
			if (targetStationDistance == 0)
				Lang.translate("train_map.train_at_station", currentStation)
					.color(darkBlue)
					.addTo(output);
			else
				Lang.translate("train_map.train_moving_to_station", currentStation, targetStationDistance)
					.color(darkBlue)
					.addTo(output);
		}

		if (signalState != SignalState.NOT_WAITING) {
			boolean chainSignal = signalState == SignalState.CHAIN_SIGNAL;
			Lang.translate("train_map.waiting_at_signal")
				.color(orange)
				.addTo(output);

			if (signalState == SignalState.WAITING_FOR_REDSTONE)
				Lang.translate("train_map.redstone_powered")
					.color(blue)
					.addTo(output);
			else {
				UUID waitingFor = trainEntry.waitingForTrain;
				boolean trainFound = false;

				if (waitingFor != null) {
					Train trainWaitingFor = CreateClient.RAILWAYS.trains.get(waitingFor);
					if (trainWaitingFor != null) {
						Lang.translate("train_map.for_other_train", trainWaitingFor.name.getString())
							.color(blue)
							.addTo(output);
						trainFound = true;
					}
				}

				if (!trainFound) {
					if (chainSignal)
						Lang.translate("train_map.cannot_traverse_section")
							.color(blue)
							.addTo(output);
					else
						Lang.translate("train_map.section_reserved")
							.color(blue)
							.addTo(output);
				}
			}
		}

		if (trainEntry.fueled)
			Lang.translate("train_map.fuel_boosted")
				.color(darkBlue)
				.addTo(output);

		return output;
	}

	private static Object drawPoints(GuiGraphics graphics, int mouseX, int mouseY, float pt, Object hoveredElement,
		Rect2i bounds) {
		PoseStack pose = graphics.pose();
		RenderSystem.enableDepthTest();

		for (TrackGraph graph : CreateClient.RAILWAYS.trackNetworks.values()) {
			for (GlobalStation station : graph.getPoints(EdgePointType.STATION)) {

				Couple<TrackNodeLocation> edgeLocation = station.edgeLocation;
				TrackNode node = graph.locateNode(edgeLocation.getFirst());
				TrackNode other = graph.locateNode(edgeLocation.getSecond());
				if (node == null || other == null)
					continue;
				if (node.getLocation().dimension != TrainMapRenderer.INSTANCE.trackingDim)
					continue;

				TrackEdge edge = graph.getConnection(Couple.create(node, other));
				if (edge == null)
					continue;

				double tLength = station.getLocationOn(edge);
				double t = tLength / edge.getLength();
				Vec3 position = edge.getPosition(graph, t);

				int x = Mth.floor(position.x());
				int y = Mth.floor(position.z());

				if (!bounds.contains(x, y))
					continue;

				Vec3 diff = edge.getDirectionAt(tLength)
					.normalize();
				int rotation = Mth.positiveModulo(Mth.floor(0.5
					+ (Math.atan2(diff.z, diff.x) * Mth.RAD_TO_DEG + 90 + (station.isPrimary(node) ? 180 : 0)) / 45),
					8);

				AllGuiTextures sprite = AllGuiTextures.TRAINMAP_STATION_ORTHO;
				AllGuiTextures highlightSprite = AllGuiTextures.TRAINMAP_STATION_ORTHO_HIGHLIGHT;
				if (rotation % 2 != 0) {
					sprite = AllGuiTextures.TRAINMAP_STATION_DIAGO;
					highlightSprite = AllGuiTextures.TRAINMAP_STATION_DIAGO_HIGHLIGHT;
				}

				boolean highlight = hoveredElement == null && Math.max(Math.abs(mouseX - x), Math.abs(mouseY - y)) < 3;

				pose.pushPose();
				pose.translate(x - 2, y - 2, 5);

				pose.translate(sprite.width / 2.0, sprite.height / 2.0, 0);
				pose.mulPose(Axis.ZP.rotationDegrees(90 * (rotation / 2)));
				pose.translate(-sprite.width / 2.0, -sprite.height / 2.0, 0);

				sprite.render(graphics, 0, 0);
				sprite.render(graphics, 0, 0);

				if (highlight) {
					pose.translate(0, 0, 5);
					highlightSprite.render(graphics, -1, -1);
					hoveredElement = station;
				}

				pose.popPose();
			}
		}

		return hoveredElement;
	}

	private static Object drawTrains(GuiGraphics graphics, int mouseX, int mouseY, float pt, Object hoveredElement,
		Rect2i bounds) {
		PoseStack pose = graphics.pose();
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();

		int spriteYOffset = -3;

		double time = AnimationTickHolder.getTicks();
		time += AnimationTickHolder.getPartialTicks();
		time -= TrainMapSyncClient.lastPacket;
		time /= TrainMapSync.lightPacketInterval;
		time = Mth.clamp(time, 0, 1);

		int[] sliceXShiftByRotationIndex = new int[] { 0, 1, 2, 2, 3, -2, -2, -1 };
		int[] sliceYShiftByRotationIndex = new int[] { 3, 2, 2, 1, 0, 1, 2, 2 };

		for (Train train : CreateClient.RAILWAYS.trains.values()) {
			TrainMapSyncEntry trainEntry = TrainMapSyncClient.currentData.get(train.id);
			if (trainEntry == null)
				continue;

			Vec3 frontPos = Vec3.ZERO;
			List<Carriage> carriages = train.carriages;
			boolean otherDim = true;
			double avgY = 0;

			for (int i = 0; i < carriages.size(); i++) {
				for (boolean firstBogey : Iterate.trueAndFalse)
					avgY += trainEntry.getPosition(i, firstBogey, time)
						.y();
			}

			avgY /= carriages.size() * 2;

			for (int i = 0; i < carriages.size(); i++) {
				Carriage carriage = carriages.get(i);

				Vec3 pos1 = trainEntry.getPosition(i, true, time);
				Vec3 pos2 = trainEntry.getPosition(i, false, time);

				ResourceKey<Level> dim = trainEntry.dimensions.get(i);
				if (dim == null || dim != TrainMapRenderer.INSTANCE.trackingDim)
					continue;
				if (!bounds.contains(Mth.floor(pos1.x()), Mth.floor(pos1.z()))
					&& !bounds.contains(Mth.floor(pos2.x()), Mth.floor(pos2.z())))
					continue;

				otherDim = false;

				if (!trainEntry.backwards && i == 0)
					frontPos = pos1;
				if (trainEntry.backwards && i == train.carriages.size() - 1)
					frontPos = pos2;

				Vec3 diff = pos2.subtract(pos1);
				int size = carriage.bogeySpacing + 1;
				Vec3 center = pos1.add(pos2)
					.scale(0.5);

				double pX = center.x;
				double pY = center.z;
				int rotation =
					Mth.positiveModulo(Mth.floor(0.5 + (Math.atan2(diff.x, diff.z) * Mth.RAD_TO_DEG) / 22.5), 8);

				if (trainEntry.state == TrainState.DERAILED)
					rotation =
						Mth.positiveModulo((AnimationTickHolder.getTicks() / 8 + i * 3) * (i % 2 == 0 ? 1 : -1), 8);

				AllGuiTextures sprite = AllGuiTextures.TRAINMAP_SPRITES;

				int slices = 2;

				if (rotation == 0 || rotation == 4) {
					// Orthogonal, slices add 3 pixels
					slices += Mth.floor((size - 2) / (3.0) + 0.5);
				}

				else if (rotation == 2 || rotation == 6) {
					// Diagonal, slices add 2*sqrt(2) pixels
					slices += Mth.floor((size - (5 - 2 * Mth.SQRT_OF_TWO)) / (2 * Mth.SQRT_OF_TWO) + 0.5);
				}

				else {
					// Slanty, slices add sqrt(5) pixels
					slices += Mth.floor((size - (5 - Mth.sqrt(5))) / (Mth.sqrt(5)) + 0.5);
				}

				slices = Math.max(2, slices);

				sprite.bind();
				pose.pushPose();

				float pivotX = 7.5f + (slices - 3) * sliceXShiftByRotationIndex[rotation] / 2.0f;
				float pivotY = 6.5f + (slices - 3) * sliceYShiftByRotationIndex[rotation] / 2.0f;
				// Ysort at home
				pose.translate(pX - pivotX, pY - pivotY, 10 + (avgY / 512.0) + (1024.0 + center.z() % 8192.0) / 1024.0);

				int trainColorIndex = train.mapColorIndex;
				int colorRow = trainColorIndex / 4;
				int colorCol = trainColorIndex % 4;

				for (int slice = 0; slice < slices; slice++) {
					int row = slice == 0 ? 1 : slice == slices - 1 ? 2 : 3;
					int sliceShifts = slice == 0 ? 0 : slice == slices - 1 ? slice - 2 : slice - 1;
					int col = rotation;

					int positionX = sliceShifts * sliceXShiftByRotationIndex[rotation];
					int positionY = sliceShifts * sliceYShiftByRotationIndex[rotation] + spriteYOffset;
					int sheetX = col * 16 + colorCol * 128;
					int sheetY = row * 16 + colorRow * 64;

					graphics.blit(sprite.location, positionX, positionY, sheetX, sheetY, 16, 16, sprite.width,
						sprite.height);
				}

				pose.popPose();

				int margin = 1;
				int sizeX = 8 + (slices - 3) * sliceXShiftByRotationIndex[rotation];
				int sizeY = 12 + (slices - 3) * sliceYShiftByRotationIndex[rotation];
				double pXm = pX - sizeX / 2;
				double pYm = pY - sizeY / 2 + spriteYOffset;
				if (hoveredElement == null && mouseX < pXm + margin + sizeX && mouseX > pXm - margin
					&& mouseY < pYm + margin + sizeY && mouseY > pYm - margin)
					hoveredElement = train;
			}

			if (otherDim)
				continue;

			if (trainEntry.signalState != SignalState.NOT_WAITING) {
				pose.pushPose();
				pose.translate(frontPos.x - 0.5, frontPos.z - 0.5, 20 + (1024.0 + frontPos.z() % 8192.0) / 1024.0);
				AllGuiTextures.TRAINMAP_SIGNAL.render(graphics, 0, -3);
				pose.popPose();
			}
		}

		return hoveredElement;
	}

	// Background first so we can mindlessly paint over it
	static final int PHASE_BACKGROUND = 0;
	// Straights before curves so that curves anti-alias properly at the transition
	static final int PHASE_STRAIGHTS = 1;
	static final int PHASE_CURVES = 2;

	public static void redrawAll() {
		TrainMapRenderer map = TrainMapRenderer.INSTANCE;
		map.trackingVersion = CreateClient.RAILWAYS.version;
		map.trackingDim = Minecraft.getInstance().level.dimension();
		map.trackingTheme = AllConfigs.client().trainMapColorTheme.get();
		map.startDrawing();

		int mainColor = 0xFF_7C57D4;
		int darkerColor = 0xFF_70437D;
		int darkerColorShadow = 0xFF_4A2754;

		switch (map.trackingTheme) {
		case GREY:
			mainColor = 0xFF_A8B5B5;
			darkerColor = 0xFF_776E6C;
			darkerColorShadow = 0xFF_56504E;
			break;
		case WHITE:
			mainColor = 0xFF_E8F9F9;
			darkerColor = 0xFF_889595;
			darkerColorShadow = 0xFF_56504E;
			break;
		default:
			break;
		}

		List<Couple<Integer>> collisions = new ObjectArrayList<>();

		for (int phase = 0; phase <= 2; phase++)
			renderPhase(map, collisions, mainColor, darkerColor, phase);

		highlightYDifferences(map, collisions, mainColor, darkerColor, darkerColor, darkerColorShadow);

		map.finishDrawing();
	}

	private static void renderPhase(TrainMapRenderer map, List<Couple<Integer>> collisions, int mainColor,
		int darkerColor, int phase) {
		int outlineColor = 0xFF_000000;

		int portalFrameColor = 0xFF_4C2D5B;
		int portalColor = 0xFF_FF7FD6;

		for (TrackGraph graph : CreateClient.RAILWAYS.trackNetworks.values()) {
			for (TrackNodeLocation nodeLocation : graph.getNodes()) {
				if (nodeLocation.dimension != map.trackingDim)
					continue;
				TrackNode node = graph.locateNode(nodeLocation);
				Map<TrackNode, TrackEdge> connectionsFrom = graph.getConnectionsFrom(node);

				int hashCode = node.hashCode();
				for (Entry<TrackNode, TrackEdge> entry : connectionsFrom.entrySet()) {
					TrackNode other = entry.getKey();
					TrackNodeLocation otherLocation = other.getLocation();
					TrackEdge edge = entry.getValue();
					BezierConnection turn = edge.getTurn();

					// Portal track
					if (edge.isInterDimensional()) {
						Vec3 vec = node.getLocation()
							.getLocation();
						int x = Mth.floor(vec.x);
						int z = Mth.floor(vec.z);
						if (phase == PHASE_CURVES)
							continue;
						if (phase == PHASE_BACKGROUND) {
							map.setPixels(x - 3, z - 2, x + 3, z + 2, outlineColor);
							map.setPixels(x - 2, z - 3, x + 2, z + 3, outlineColor);
							continue;
						}

						int a = mapYtoAlpha(Mth.floor(vec.y()));
						for (int xi = x - 2; xi <= x + 2; xi++) {
							for (int zi = z - 2; zi <= z + 2; zi++) {
								int alphaAt = map.alphaAt(xi, zi);
								if (alphaAt > 0 && alphaAt != a)
									collisions.add(Couple.create(xi, zi));
								int c = (xi - x) * (xi - x) + (zi - z) * (zi - z) > 2 ? portalFrameColor : portalColor;
								if (alphaAt <= a) {
									map.setPixel(xi, zi, markY(c, vec.y()));
								}
							}
						}
						continue;
					}

					if (other.hashCode() > hashCode)
						continue;

					if (turn == null) {
						if (phase == PHASE_CURVES)
							continue;

						float x1 = nodeLocation.getX();
						float z1 = nodeLocation.getZ();
						float x2 = otherLocation.getX();
						float z2 = otherLocation.getZ();

						double y1 = nodeLocation.getLocation()
							.y();
						double y2 = otherLocation.getLocation()
							.y();

						float xDiffSign = Math.signum(x2 - x1);
						float zDiffSign = Math.signum(z2 - z1);
						boolean diagonal = xDiffSign != 0 && zDiffSign != 0;

						if (xDiffSign != 0) {
							x2 -= xDiffSign * .25;
							x1 += xDiffSign * .25;
						}

						if (zDiffSign != 0) {
							z2 -= zDiffSign * .25;
							z1 += zDiffSign * .25;
						}

						x1 /= 2;
						x2 /= 2;
						z1 /= 2;
						z2 /= 2;

						int y = Mth.floor(y1);
						int a = mapYtoAlpha(y);

						// Diagonal
						if (diagonal) {
							int z = Mth.floor(z1);
							int x = Mth.floor(x1);

							for (int s = 0; s <= Math.abs(x1 - x2); s++) {
								if (phase == PHASE_BACKGROUND) {
									map.setPixels(x - 1, z, x + 1, z + 1, outlineColor);
									map.setPixels(x, z - 1, x, z + 2, outlineColor);
									x += xDiffSign;
									z += zDiffSign;
									continue;
								}

								int alphaAt = map.alphaAt(x, z);
								if (alphaAt > 0 && alphaAt != a)
									collisions.add(Couple.create(x, z));
								if (alphaAt <= a) {
									map.setPixel(x, z, markY(mainColor, y));
								}

								if (map.alphaAt(x, z + 1) < a) {
									map.setPixel(x, z + 1, markY(darkerColor, y));
								}

								x += xDiffSign;
								z += zDiffSign;
							}

							continue;
						}

						// Straight
						if (phase == PHASE_BACKGROUND) {
							int x1i = Mth.floor(Math.min(x1, x2));
							int z1i = Mth.floor(Math.min(z1, z2));
							int x2i = Mth.floor(Math.max(x1, x2));
							int z2i = Mth.floor(Math.max(z1, z2));

							map.setPixels(x1i - 1, z1i, x2i + 1, z2i, outlineColor);
							map.setPixels(x1i, z1i - 1, x2i, z2i + 1, outlineColor);
							continue;
						}

						int z = Mth.floor(z1);
						int x = Mth.floor(x1);
						float diff = Math.max(Math.abs(x1 - x2), Math.abs(z1 - z2));
						double yStep = (y2 - y1) / diff;

						for (int s = 0; s <= diff; s++) {
							int alphaAt = map.alphaAt(x, z);
							if (alphaAt > 0 && alphaAt != a)
								collisions.add(Couple.create(x, z));
							if (alphaAt <= a) {
								map.setPixel(x, z, markY(mainColor, y));
							}
							x += xDiffSign;
							y += yStep;
							z += zDiffSign;
						}

						continue;
					}

					if (phase == PHASE_STRAIGHTS)
						continue;

					BlockPos origin = turn.bePositions.getFirst();
					Map<Pair<Integer, Integer>, Double> rasterise = turn.rasterise();

					for (boolean antialias : Iterate.falseAndTrue) {
						for (Entry<Pair<Integer, Integer>, Double> offset : rasterise.entrySet()) {
							Pair<Integer, Integer> xz = offset.getKey();
							int x = origin.getX() + xz.getFirst();
							int y = Mth.floor(origin.getY() + offset.getValue() + 0.5);
							int z = origin.getZ() + xz.getSecond();

							if (phase == PHASE_BACKGROUND) {
								map.setPixels(x - 1, z, x + 1, z, outlineColor);
								map.setPixels(x, z - 1, x, z + 1, outlineColor);
								continue;
							}

							int a = mapYtoAlpha(y);

							if (!antialias) {
								int alphaAt = map.alphaAt(x, z);
								if (alphaAt > 0 && alphaAt != a)
									collisions.add(Couple.create(x, z));
								if (alphaAt > a)
									continue;

								map.setPixel(x, z, markY(mainColor, y));
								continue;
							}

							boolean mainColorBelowLeft =
								map.is(x + 1, z + 1, mainColor) && Math.abs(map.alphaAt(x + 1, z + 1) - a) <= 1;
							boolean mainColorBelowRight =
								map.is(x - 1, z + 1, mainColor) && Math.abs(map.alphaAt(x - 1, z + 1) - a) <= 1;

							if (mainColorBelowLeft || mainColorBelowRight) {
								int alphaAt = map.alphaAt(x, z + 1);
								if (alphaAt > 0 && alphaAt != a)
									collisions.add(Couple.create(x, z));
								if (alphaAt >= a)
									continue;

								map.setPixel(x, z + 1, markY(darkerColor, y));

								// Adjust background
								if (map.isEmpty(x + 1, z + 1))
									map.setPixel(x + 1, z + 1, outlineColor);
								if (map.isEmpty(x - 1, z + 1))
									map.setPixel(x - 1, z + 1, outlineColor);
								if (map.isEmpty(x, z + 2))
									map.setPixel(x, z + 2, outlineColor);
							}
						}
						if (phase == PHASE_BACKGROUND)
							break;
					}
				}
			}
		}
	}

	private static void highlightYDifferences(TrainMapRenderer map, List<Couple<Integer>> collisions, int mainColor,
		int darkerColor, int mainColorShadow, int darkerColorShadow) {
		for (Couple<Integer> couple : collisions) {
			int x = couple.getFirst();
			int z = couple.getSecond();
			int a = map.alphaAt(x, z);
			if (a == 0)
				continue;

			for (int xi = x - 2; xi <= x + 2; xi++) {
				for (int zi = z - 2; zi <= z + 2; zi++) {
					if (map.alphaAt(xi, zi) >= a)
						continue;
					if (map.is(xi, zi, mainColor))
						map.setPixel(xi, zi, FastColor.ABGR32.color(a, mainColorShadow));
					else if (map.is(xi, zi, darkerColor))
						map.setPixel(xi, zi, FastColor.ABGR32.color(a, darkerColorShadow));
				}
			}
		}
	}

	private static int mapYtoAlpha(double y) {
		int minY = Minecraft.getInstance().level.getMinBuildHeight();
		return Mth.clamp(32 + Mth.floor((y - minY) / 4.0), 0, 255);
	}

	private static int markY(int color, double y) {
		return FastColor.ABGR32.color(mapYtoAlpha(y), color);
	}

}
