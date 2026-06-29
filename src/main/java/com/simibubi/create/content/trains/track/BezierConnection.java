package com.simibubi.create.content.trains.track;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.simibubi.create.AllBlocks;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BezierConnection implements Iterable<BezierConnection.Segment> {

	public final Couple<BlockPos> bePositions;
	public final Couple<Vec3> starts;
	public final Couple<Vec3> axes;
	public final Couple<Vec3> normals;
	@Nullable
	public Couple<Integer> smoothing;
	public final boolean primary;
	public final boolean hasGirder;
	protected TrackMaterial trackMaterial;

	// runtime
	private final AtomicReference<@Nullable Runtime> lazyRuntime = new AtomicReference<>(null);

	public BezierConnection(Couple<BlockPos> positions, Couple<Vec3> starts, Couple<Vec3> axes, Couple<Vec3> normals,
		boolean primary, boolean girder, TrackMaterial material) {
		bePositions = positions;
		this.starts = starts;
		this.axes = axes;
		this.normals = normals;
		this.primary = primary;
		this.hasGirder = girder;
		this.trackMaterial = material;
	}

	public BezierConnection secondary() {
		BezierConnection bezierConnection = new BezierConnection(bePositions.swap(), starts.swap(), axes.swap(),
			normals.swap(), !primary, hasGirder, trackMaterial);
		if (smoothing != null)
			bezierConnection.smoothing = smoothing.swap();
		return bezierConnection;
	}

	public BezierConnection clone() {
		var out = new BezierConnection(bePositions.copy(), starts.copy(), axes.copy(), normals.copy(), primary, hasGirder, trackMaterial);
		if (smoothing != null) {
			out.smoothing = smoothing.copy();
		}
		return out;
	}

	private static boolean coupleEquals(Couple<?> a, Couple<?> b) {
		return (a.getFirst()
			.equals(b.getFirst())
			&& a.getSecond()
				.equals(b.getSecond()))
			|| (a.getFirst() instanceof Vec3 aFirst && a.getSecond() instanceof Vec3 aSecond
				&& b.getFirst() instanceof Vec3 bFirst && b.getSecond() instanceof Vec3 bSecond
				&& aFirst.closerThan(bFirst, 1e-6) && aSecond.closerThan(bSecond, 1e-6));
	}

	public boolean equalsSansMaterial(BezierConnection other) {
		return equalsSansMaterialInner(other) || equalsSansMaterialInner(other.secondary());
	}

	private boolean equalsSansMaterialInner(BezierConnection other) {
		return this == other || (other != null && coupleEquals(this.bePositions, other.bePositions)
			&& coupleEquals(this.starts, other.starts) && coupleEquals(this.axes, other.axes)
			&& coupleEquals(this.normals, other.normals) && this.hasGirder == other.hasGirder);
	}

	public BezierConnection(CompoundTag compound, BlockPos localTo) {
		this(Couple.deserializeEach(compound.getList("Positions", Tag.TAG_COMPOUND), t -> NBTHelper.readBlockPos(t, "Pos"))
			.map(b -> b.offset(localTo)),
			Couple.deserializeEach(compound.getList("Starts", Tag.TAG_COMPOUND), VecHelper::readNBTCompound)
				.map(v -> v.add(Vec3.atLowerCornerOf(localTo))),
			Couple.deserializeEach(compound.getList("Axes", Tag.TAG_COMPOUND), VecHelper::readNBTCompound),
			Couple.deserializeEach(compound.getList("Normals", Tag.TAG_COMPOUND), VecHelper::readNBTCompound),
			compound.getBoolean("Primary"), compound.getBoolean("Girder"),
			TrackMaterial.deserialize(compound.getString("Material")));

		if (compound.contains("Smoothing"))
			smoothing =
				Couple.deserializeEach(compound.getList("Smoothing", Tag.TAG_COMPOUND), NBTHelper::intFromCompound);
	}

	public CompoundTag write(BlockPos localTo) {
		Couple<BlockPos> tePositions = this.bePositions.map(b -> b.subtract(localTo));
		Couple<Vec3> starts = this.starts.map(v -> v.subtract(Vec3.atLowerCornerOf(localTo)));

		CompoundTag compound = new CompoundTag();
		compound.putBoolean("Girder", hasGirder);
		compound.putBoolean("Primary", primary);
		compound.put("Positions", tePositions.serializeEach(t -> {
			CompoundTag tag = new CompoundTag();
			tag.put("Pos", NbtUtils.writeBlockPos(t));
			return tag;
		}));
		compound.put("Starts", starts.serializeEach(VecHelper::writeNBTCompound));
		compound.put("Axes", axes.serializeEach(VecHelper::writeNBTCompound));
		compound.put("Normals", normals.serializeEach(VecHelper::writeNBTCompound));
		compound.putString("Material", getMaterial().id.toString());

		if (smoothing != null)
			compound.put("Smoothing", smoothing.serializeEach(NBTHelper::intToCompound));

		return compound;
	}

	public BezierConnection(FriendlyByteBuf buffer) {
		this(Couple.create(buffer::readBlockPos), Couple.create(() -> VecHelper.read(buffer)),
			Couple.create(() -> VecHelper.read(buffer)), Couple.create(() -> VecHelper.read(buffer)),
			buffer.readBoolean(), buffer.readBoolean(), TrackMaterial.deserialize(buffer.readUtf()));
		if (buffer.readBoolean())
			smoothing = Couple.create(buffer::readVarInt);
	}

	public void write(FriendlyByteBuf buffer) {
		bePositions.forEach(buffer::writeBlockPos);
		starts.forEach(v -> VecHelper.write(v, buffer));
		axes.forEach(v -> VecHelper.write(v, buffer));
		normals.forEach(v -> VecHelper.write(v, buffer));
		buffer.writeBoolean(primary);
		buffer.writeBoolean(hasGirder);
		buffer.writeUtf(getMaterial().id.toString());
		buffer.writeBoolean(smoothing != null);
		if (smoothing != null)
			smoothing.forEach(buffer::writeVarInt);
	}

	public BlockPos getKey() {
		return bePositions.getSecond();
	}

	public boolean isPrimary() {
		return primary;
	}

	public int yOffsetAt(Vec3 end) {
		if (smoothing == null)
			return 0;
		if (TrackBlockEntityTilt.compareHandles(starts.getFirst(), end))
			return smoothing.getFirst();
		if (TrackBlockEntityTilt.compareHandles(starts.getSecond(), end))
			return smoothing.getSecond();
		return 0;
	}

	// Runtime information

	public double getLength() {
		return resolve().length;
	}

	public float[] getStepLUT() {
		return resolve().stepLUT;
	}

	public int getSegmentCount() {
		return resolve().segments;
	}

	public Vec3 getPosition(double t) {
		var runtime = resolve();
		return VecHelper.bezier(starts.getFirst(), starts.getSecond(), runtime.finish1, runtime.finish2, (float) t);
	}

	public double getRadius() {
		return resolve().radius;
	}

	public double getHandleLength() {
		return resolve().handleLength;
	}

	public float getSegmentT(int index) {
		return resolve().getSegmentT(index);
	}

	public double incrementT(double currentT, double distance) {
		var runtime = resolve();
		double dx =
			VecHelper.bezierDerivative(starts.getFirst(), starts.getSecond(), runtime.finish1, runtime.finish2, (float) currentT)
				.length() / getLength();
		return currentT + distance / dx;
	}

	public AABB getBounds() {
		return resolve().bounds;
	}

	public Vec3 getNormal(double t) {
		var runtime = resolve();
		Vec3 end1 = starts.getFirst();
		Vec3 end2 = starts.getSecond();
		Vec3 fn1 = normals.getFirst();
		Vec3 fn2 = normals.getSecond();

		Vec3 derivative = VecHelper.bezierDerivative(end1, end2, runtime.finish1, runtime.finish2, (float) t)
			.normalize();
		Vec3 faceNormal = fn1.equals(fn2) ? fn1 : VecHelper.slerp((float) t, fn1, fn2);
		Vec3 normal = faceNormal.cross(derivative)
			.normalize();
		return derivative.cross(normal);
	}

	@NotNull
	private Runtime resolve() {
		var out = lazyRuntime.get();

		if (out == null) {
			// Since this can be accessed from multiple threads, we consolidate the intermediary
			// computation into a class and only publish complete results.
			out = new Runtime(starts, axes);
			// Doesn't matter if this one becomes the canonical value because all results are the same.
			lazyRuntime.set(out);
		}

		return out;
	}

	@Override
	public Iterator<Segment> iterator() {
		var offset = Vec3.atLowerCornerOf(bePositions.getFirst())
			.scale(-1)
			.add(0, 3 / 16f, 0);
		return new Bezierator(this, offset);
	}

	public void addItemsToPlayer(Player player) {
		Inventory inv = player.getInventory();
		int tracks = getTrackItemCost();
		while (tracks > 0) {
			inv.placeItemBackInInventory(new ItemStack(getMaterial().getBlock(), Math.min(64, tracks)));
			tracks -= 64;
		}
		int girders = getGirderItemCost();
		while (girders > 0) {
			inv.placeItemBackInInventory(AllBlocks.METAL_GIRDER.asStack(Math.min(64, girders)));
			girders -= 64;
		}
	}

	public int getGirderItemCost() {
		return hasGirder ? getTrackItemCost() * 2 : 0;
	}

	public int getTrackItemCost() {
		return (getSegmentCount() + 1) / 2;
	}

	public void spawnItems(Level level) {
		if (!level.getGameRules()
			.getBoolean(GameRules.RULE_DOBLOCKDROPS))
			return;
		Vec3 origin = Vec3.atLowerCornerOf(bePositions.getFirst());
		for (Segment segment : this) {
			if (segment.index % 2 != 0 || segment.index == getSegmentCount())
				continue;
			Vec3 v = VecHelper.offsetRandomly(segment.position, level.random, .125f)
				.add(origin);
			ItemEntity entity = new ItemEntity(level, v.x, v.y, v.z, getMaterial().asStack());
			entity.setDefaultPickUpDelay();
			level.addFreshEntity(entity);
			if (!hasGirder)
				continue;
			for (int i = 0; i < 2; i++) {
				entity = new ItemEntity(level, v.x, v.y, v.z, AllBlocks.METAL_GIRDER.asStack());
				entity.setDefaultPickUpDelay();
				level.addFreshEntity(entity);
			}
		}
	}

	public void spawnDestroyParticles(Level level) {
		BlockParticleOption data = new BlockParticleOption(ParticleTypes.BLOCK, getMaterial().getBlock()
			.defaultBlockState());
		BlockParticleOption girderData =
			new BlockParticleOption(ParticleTypes.BLOCK, AllBlocks.METAL_GIRDER.getDefaultState());
		if (!(level instanceof ServerLevel slevel))
			return;
		Vec3 origin = Vec3.atLowerCornerOf(bePositions.getFirst());
		for (Segment segment : this) {
			for (int offset : Iterate.positiveAndNegative) {
				Vec3 v = segment.position.add(segment.normal.scale(14 / 16f * offset))
					.add(origin);
				slevel.sendParticles(data, v.x, v.y, v.z, 1, 0, 0, 0, 0);
				if (!hasGirder)
					continue;
				slevel.sendParticles(girderData, v.x, v.y - .5f, v.z, 1, 0, 0, 0, 0);
			}
		}
	}

	public TrackMaterial getMaterial() {
		return trackMaterial;
	}

	public void setMaterial(TrackMaterial material) {
		trackMaterial = material;
	}

	private static class Runtime {
		private final Vec3 finish1;
		private final Vec3 finish2;
		private final double length;
		private final float[] stepLUT;
		private final int segments;

		private double radius;
		private double handleLength;

		private final AABB bounds;

		private Runtime(Couple<Vec3> starts, Couple<Vec3> axes) {
			Vec3 end1 = starts.getFirst();
			Vec3 end2 = starts.getSecond();
			Vec3 axis1 = axes.getFirst()
				.normalize();
			Vec3 axis2 = axes.getSecond()
				.normalize();

			determineHandles(end1, end2, axis1, axis2);

			finish1 = axis1.scale(handleLength)
				.add(end1);
			finish2 = axis2.scale(handleLength)
				.add(end2);

			int scanCount = 16;

			this.length = computeLength(finish1, finish2, end1, end2, scanCount);

			segments = (int) (length * 2);
			stepLUT = new float[segments + 1];
			stepLUT[0] = 1;
			float combinedDistance = 0;

			AABB bounds = new AABB(end1, end2);

			// determine step lut
			{
				Vec3 previous = end1;
				for (int i = 0; i <= segments; i++) {
					float t = i / (float) segments;
					Vec3 result = VecHelper.bezier(end1, end2, finish1, finish2, t);
					bounds = bounds.minmax(new AABB(result, result));
					if (i > 0) {
						combinedDistance += result.distanceTo(previous) / length;
						stepLUT[i] = (float) (t / combinedDistance);
					}
					previous = result;
				}
			}

			this.bounds = bounds.inflate(1.375f);
		}

		private static double computeLength(Vec3 finish1, Vec3 finish2, Vec3 end1, Vec3 end2, int scanCount) {
			double length = 0;

			Vec3 previous = end1;
			for (int i = 0; i <= scanCount; i++) {
				float t = i / (float) scanCount;
				Vec3 result = VecHelper.bezier(end1, end2, finish1, finish2, t);
				if (previous != null)
					length += result.distanceTo(previous);
				previous = result;
			}
			return length;
		}

		public float getSegmentT(int index) {
			return index == segments ? 1 : index * stepLUT[index] / segments;
		}

		private void determineHandles(Vec3 end1, Vec3 end2, Vec3 axis1, Vec3 axis2) {
			Vec3 cross1 = axis1.cross(new Vec3(0, 1, 0));
			Vec3 cross2 = axis2.cross(new Vec3(0, 1, 0));

			radius = 0;
			double a1 = Mth.atan2(-axis2.z, -axis2.x);
			double a2 = Mth.atan2(axis1.z, axis1.x);
			double angle = a1 - a2;

			float circle = 2 * Mth.PI;
			angle = (angle + circle) % circle;
			if (Math.abs(circle - angle) < Math.abs(angle))
				angle = circle - angle;

			if (Mth.equal(angle, 0)) {
				double[] intersect = VecHelper.intersect(end1, end2, axis1, cross2, Axis.Y);
				if (intersect != null) {
					double t = Math.abs(intersect[0]);
					double u = Math.abs(intersect[1]);
					double min = Math.min(t, u);
					double max = Math.max(t, u);

					if (min > 1.2 && max / min > 1 && max / min < 3) {
						handleLength = (max - min);
						return;
					}
				}

				handleLength = end2.distanceTo(end1) / 3;
				return;
			}

			double n = circle / angle;
			double factor = 4 / 3d * Math.tan(Math.PI / (2 * n));
			double[] intersect = VecHelper.intersect(end1, end2, cross1, cross2, Axis.Y);

			if (intersect == null) {
				handleLength = end2.distanceTo(end1) / 3;
				return;
			}

			radius = Math.abs(intersect[1]);
			handleLength = radius * factor;
			if (Mth.equal(handleLength, 0))
				handleLength = 1;
		}
	}

	public static class Segment {

		public int index;
		public Vec3 position;
		public Vec3 derivative;
		public Vec3 faceNormal;
		public Vec3 normal;

	}

	private static class Bezierator implements Iterator<Segment> {
		private final Segment segment;
		private final Vec3 end1;
		private final Vec3 end2;
		private final Vec3 finish1;
		private final Vec3 finish2;
		private final Vec3 faceNormal1;
		private final Vec3 faceNormal2;
		private final Runtime runtime;

		private Bezierator(BezierConnection bc, Vec3 offset) {
			runtime = bc.resolve();

			end1 = bc.starts.getFirst()
				.add(offset);
			end2 = bc.starts.getSecond()
				.add(offset);

			finish1 = bc.axes.getFirst()
				.scale(runtime.handleLength)
				.add(end1);
			finish2 = bc.axes.getSecond()
				.scale(runtime.handleLength)
				.add(end2);

			faceNormal1 = bc.normals.getFirst();
			faceNormal2 = bc.normals.getSecond();
			segment = new Segment();
			segment.index = -1; // will get incremented to 0 in #next()
		}

		@Override
		public boolean hasNext() {
			return segment.index + 1 <= runtime.segments;
		}

		@Override
		public Segment next() {
			segment.index++;
			float t = runtime.getSegmentT(segment.index);
			segment.position = VecHelper.bezier(end1, end2, finish1, finish2, t);
			segment.derivative = VecHelper.bezierDerivative(end1, end2, finish1, finish2, t)
				.normalize();
			segment.faceNormal =
				faceNormal1.equals(faceNormal2) ? faceNormal1 : VecHelper.slerp(t, faceNormal1, faceNormal2);
			segment.normal = segment.faceNormal.cross(segment.derivative)
				.normalize();
			return segment;
		}
	}

	public static class SegmentAngles {
		public final int length;
		public final @NotNull Pose[] tieTransform;
		public final @NotNull Couple<Pose>[] railTransforms;
		public final @NotNull BlockPos[] lightPosition;

		private SegmentAngles(BezierConnection bc) {
			int segmentCount = bc.getSegmentCount();

			length = segmentCount + 1;

			tieTransform = new Pose[segmentCount + 1];
			railTransforms = new Couple[segmentCount + 1];
			lightPosition = new BlockPos[segmentCount + 1];

			Couple<Vec3> previousOffsets = null;

			for (BezierConnection.Segment segment : bc) {
				int i = segment.index;
				boolean end = i == 0 || i == segmentCount;

				Couple<Vec3> railOffsets = Couple.create(segment.position.add(segment.normal.scale(.965f)),
					segment.position.subtract(segment.normal.scale(.965f)));
				Vec3 railMiddle = railOffsets.getFirst()
					.add(railOffsets.getSecond())
					.scale(.5);

				if (previousOffsets == null) {
					previousOffsets = railOffsets;
					continue;
				}

				// Tie
				Vec3 prevMiddle = previousOffsets.getFirst()
					.add(previousOffsets.getSecond())
					.scale(.5);
				Vec3 tieAngles = TrackRenderer.getModelAngles(segment.normal, railMiddle.subtract(prevMiddle));
				lightPosition[i] = BlockPos.containing(railMiddle);
				railTransforms[i] = Couple.create(null, null);

				PoseStack poseStack = new PoseStack();
				TransformStack.of(poseStack)
					.translate(prevMiddle)
					.rotateY((float) tieAngles.y)
					.rotateX((float) tieAngles.x)
					.rotateZ((float) tieAngles.z)
					.translate(-1 / 2f, -2 / 16f - 1 / 256f, 0);
				tieTransform[i] = poseStack.last();

				// Rails
				float scale = end ? 2.2f : 2.1f;
				for (boolean first : Iterate.trueAndFalse) {
					Vec3 railI = railOffsets.get(first);
					Vec3 prevI = previousOffsets.get(first);
					Vec3 diff = railI.subtract(prevI);
					Vec3 anglesI = TrackRenderer.getModelAngles(segment.normal, diff);

					poseStack = new PoseStack();
					TransformStack.of(poseStack)
						.translate(prevI)
						.rotateY((float) anglesI.y)
						.rotateX((float) anglesI.x)
						.rotateZ((float) anglesI.z)
						.translate(0, -2 / 16f - 1 / 256f, -1 / 32f)
						.scale(1, 1, (float) diff.length() * scale);
					railTransforms[i].set(first, poseStack.last());
				}

				previousOffsets = railOffsets;
			}
		}

	}

	public static class GirderAngles {
		public final int length;
		public final Couple<Pose>[] beams;
		public final Couple<Couple<Pose>>[] beamCaps;
		public final BlockPos[] lightPosition;

		private GirderAngles(BezierConnection bc) {
			int segmentCount = bc.getSegmentCount();
			length = segmentCount + 1;

			beams = new Couple[length];
			beamCaps = new Couple[length];
			lightPosition = new BlockPos[length];

			Couple<Couple<Vec3>> previousOffsets = null;

			for (BezierConnection.Segment segment : bc) {
				int i = segment.index;
				boolean end = i == 0 || i == segmentCount;
				Vec3 leftGirder = segment.position.add(segment.normal.scale(.965f));
				Vec3 rightGirder = segment.position.subtract(segment.normal.scale(.965f));
				Vec3 upNormal = segment.derivative.normalize()
					.cross(segment.normal);
				Vec3 firstGirderOffset = upNormal.scale(-8 / 16f);
				Vec3 secondGirderOffset = upNormal.scale(-10 / 16f);
				Vec3 leftTop = segment.position.add(segment.normal.scale(1))
					.add(firstGirderOffset);
				Vec3 rightTop = segment.position.subtract(segment.normal.scale(1))
					.add(firstGirderOffset);
				Vec3 leftBottom = leftTop.add(secondGirderOffset);
				Vec3 rightBottom = rightTop.add(secondGirderOffset);

				lightPosition[i] = BlockPos.containing(leftGirder.add(rightGirder)
					.scale(.5));

				Couple<Couple<Vec3>> offsets =
					Couple.create(Couple.create(leftTop, rightTop), Couple.create(leftBottom, rightBottom));

				if (previousOffsets == null) {
					previousOffsets = offsets;
					continue;
				}

				beams[i] = Couple.create(null, null);
				beamCaps[i] = Couple.create(Couple.create(null, null), Couple.create(null, null));
				float scale = end ? 2.3f : 2.2f;

				for (boolean first : Iterate.trueAndFalse) {

					// Middle
					Vec3 currentBeam = offsets.getFirst()
						.get(first)
						.add(offsets.getSecond()
							.get(first))
						.scale(.5);
					Vec3 previousBeam = previousOffsets.getFirst()
						.get(first)
						.add(previousOffsets.getSecond()
							.get(first))
						.scale(.5);
					Vec3 beamDiff = currentBeam.subtract(previousBeam);
					Vec3 beamAngles = TrackRenderer.getModelAngles(segment.normal, beamDiff);

					PoseStack poseStack = new PoseStack();
					TransformStack.of(poseStack)
						.translate(previousBeam)
						.rotateY((float) beamAngles.y)
						.rotateX((float) beamAngles.x)
						.rotateZ((float) beamAngles.z)
						.translate(0, 2 / 16f + (segment.index % 2 == 0 ? 1 : -1) / 2048f - 1 / 1024f, -1 / 32f)
						.scale(1, 1, (float) beamDiff.length() * scale);
					beams[i].set(first, poseStack.last());

					// Caps
					for (boolean top : Iterate.trueAndFalse) {
						Vec3 current = offsets.get(top)
							.get(first);
						Vec3 previous = previousOffsets.get(top)
							.get(first);
						Vec3 diff = current.subtract(previous);
						Vec3 capAngles = TrackRenderer.getModelAngles(segment.normal, diff);

						poseStack = new PoseStack();
						TransformStack.of(poseStack)
							.translate(previous)
							.rotateY((float) capAngles.y)
							.rotateX((float) capAngles.x)
							.rotateZ((float) capAngles.z)
							.translate(0, 2 / 16f + (segment.index % 2 == 0 ? 1 : -1) / 2048f - 1 / 1024f, -1 / 32f)
							.rotateZ(top ? 0 : 0)
							.scale(1, 1, (float) diff.length() * scale);
						beamCaps[i].get(top)
							.set(first, poseStack.last());
					}
				}

				previousOffsets = offsets;

			}
		}

	}

	private final AtomicReference<@Nullable SegmentAngles> bakedSegments = new AtomicReference<>(null);
	private final AtomicReference<@Nullable GirderAngles> bakedGirders = new AtomicReference<>(null);

	public SegmentAngles getBakedSegments() {
		var out = bakedSegments.get();

		if (out == null) {
			out = new BezierConnection.SegmentAngles(this);
			bakedSegments.set(out);
		}
		return out;
	}

	public GirderAngles getBakedGirders() {
		var out = bakedGirders.get();

		if (out == null) {
			out = new BezierConnection.GirderAngles(this);
			bakedGirders.set(out);
		}
		return out;
	}

	public Map<Pair<Integer, Integer>, Double> rasterise() {
		Map<Pair<Integer, Integer>, Double> yLevels = new HashMap<>();
		BlockPos tePosition = bePositions.getFirst();
		Vec3 end1 = starts.getFirst()
			.subtract(Vec3.atLowerCornerOf(tePosition))
			.add(0, 3 / 16f, 0);
		Vec3 end2 = starts.getSecond()
			.subtract(Vec3.atLowerCornerOf(tePosition))
			.add(0, 3 / 16f, 0);
		Vec3 axis1 = axes.getFirst();
		Vec3 axis2 = axes.getSecond();

		double handleLength = getHandleLength();
		Vec3 finish1 = axis1.scale(handleLength)
			.add(end1);
		Vec3 finish2 = axis2.scale(handleLength)
			.add(end2);

		Vec3 faceNormal1 = normals.getFirst();
		Vec3 faceNormal2 = normals.getSecond();

		int segCount = getSegmentCount();
		float[] lut = getStepLUT();
		Vec3[] samples = new Vec3[segCount];

		for (int i = 0; i < segCount; i++) {
			float t = Mth.clamp((i + 0.5f) * lut[i] / segCount, 0, 1);
			Vec3 result = VecHelper.bezier(end1, end2, finish1, finish2, t);
			Vec3 derivative = VecHelper.bezierDerivative(end1, end2, finish1, finish2, t)
				.normalize();
			Vec3 faceNormal =
				faceNormal1.equals(faceNormal2) ? faceNormal1 : VecHelper.slerp(t, faceNormal1, faceNormal2);
			Vec3 normal = faceNormal.cross(derivative)
				.normalize();
			Vec3 below = result.add(faceNormal.scale(-.25f));
			Vec3 rail1 = below.add(normal.scale(.05f));
			Vec3 rail2 = below.subtract(normal.scale(.05f));
			Vec3 railMiddle = rail1.add(rail2)
				.scale(.5);
			samples[i] = railMiddle;
		}

		Vec3 center = end1.add(end2)
			.scale(0.5);

		Pair<Integer, Integer> prev = null;
		Pair<Integer, Integer> prev2 = null;
		Pair<Integer, Integer> prev3 = null;

		for (int i = 0; i < segCount; i++) {
			Vec3 railMiddle = samples[i];
			BlockPos pos = BlockPos.containing(railMiddle);
			Pair<Integer, Integer> key = Pair.of(pos.getX(), pos.getZ());
			boolean alreadyPresent = yLevels.containsKey(key);
			if (alreadyPresent && yLevels.get(key) <= railMiddle.y)
				continue;
			yLevels.put(key, railMiddle.y);
			if (alreadyPresent)
				continue;

			if (prev3 != null) { // Remove obsolete pixels
				boolean doubledViaPrev = isLineDoubled(prev2, prev, key);
				boolean doubledViaPrev2 = isLineDoubled(prev3, prev2, prev);
				boolean prevCloser = diff(prev, center) > diff(prev2, center);

				if (doubledViaPrev2 && (!doubledViaPrev || !prevCloser)) {
					yLevels.remove(prev2);
					prev2 = prev;
					prev = key;
					continue;

				} else if (doubledViaPrev && doubledViaPrev2 && prevCloser) {
					yLevels.remove(prev);
					prev = key;
					continue;
				}
			}

			prev3 = prev2;
			prev2 = prev;
			prev = key;
		}

		return yLevels;
	}

	private double diff(Pair<Integer, Integer> pFrom, Vec3 to) {
		return to.distanceToSqr(pFrom.getFirst() + 0.5, to.y, pFrom.getSecond() + 0.5);
	}

	private boolean isLineDoubled(Pair<Integer, Integer> pFrom, Pair<Integer, Integer> pVia,
		Pair<Integer, Integer> pTo) {
		int diff1x = pVia.getFirst() - pFrom.getFirst();
		int diff1z = pVia.getSecond() - pFrom.getSecond();
		int diff2x = pTo.getFirst() - pVia.getFirst();
		int diff2z = pTo.getSecond() - pVia.getSecond();
		return Math.abs(diff1x) + Math.abs(diff1z) == 1 && Math.abs(diff2x) + Math.abs(diff2z) == 1 && diff1x != diff2x
			&& diff1z != diff2z;
	}

}
