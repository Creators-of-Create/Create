package com.simibubi.create.foundation.ponder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.antlr.v4.runtime.misc.IntegerList;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.ponder.content.PonderIndex;
import com.simibubi.create.foundation.ponder.content.PonderTag;
import com.simibubi.create.foundation.ponder.elements.PonderOverlayElement;
import com.simibubi.create.foundation.ponder.elements.PonderSceneElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instructions.HideAllInstruction;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class PonderScene {

	public static final String TITLE_KEY = "header";

	boolean finished;
	int sceneIndex;
	int textIndex;
	String sceneId;

	IntegerList keyframeTimes;

	List<PonderInstruction> schedule, activeSchedule;
	Map<UUID, PonderElement> linkedElements;
	Set<PonderElement> elements;
	List<PonderTag> tags;

	PonderWorld world;
	ResourceLocation component;
	SceneTransform transform;
	SceneRenderInfo info;
	Outliner outliner;
	String defaultTitle;

	Vec3d pointOfInterest;
	Vec3d chasingPointOfInterest;
	WorldSectionElement baseWorldSection;
	Entity renderViewEntity;

	int basePlateOffsetX;
	int basePlateOffsetZ;
	int basePlateSize;
	float scaleFactor;
	float yOffset;

	boolean stoppedCounting;
	int totalTime;
	int currentTime;

	public PonderScene(PonderWorld world, ResourceLocation component, Collection<PonderTag> tags) {
		world.scene = this;
		pointOfInterest = Vec3d.ZERO;
		textIndex = 1;

		this.world = world;
		this.component = component;

		outliner = new Outliner();
		elements = new HashSet<>();
		linkedElements = new HashMap<>();
		this.tags = new ArrayList<>(tags);
		schedule = new ArrayList<>();
		activeSchedule = new ArrayList<>();
		transform = new SceneTransform();
		basePlateSize = getBounds().getXSize();
		info = new SceneRenderInfo();
		baseWorldSection = new WorldSectionElement();
		renderViewEntity = new ArmorStandEntity(world, 0, 0, 0);
		keyframeTimes = new IntegerList(4);
		scaleFactor = 1;
		yOffset = 0;

		setPointOfInterest(new Vec3d(0, 4, 0));
	}

	public void deselect() {
		forEach(WorldSectionElement.class, WorldSectionElement::resetSelectedBlock);
	}

	public Pair<ItemStack, BlockPos> rayTraceScene(Vec3d from, Vec3d to) {
		MutableObject<Pair<WorldSectionElement, BlockPos>> nearestHit = new MutableObject<>();
		MutableDouble bestDistance = new MutableDouble(0);

		forEach(WorldSectionElement.class, wse -> {
			wse.resetSelectedBlock();
			if (!wse.isVisible())
				return;
			Pair<Vec3d, BlockPos> rayTrace = wse.rayTrace(world, from, to);
			if (rayTrace == null)
				return;
			double distanceTo = rayTrace.getFirst()
				.distanceTo(from);
			if (nearestHit.getValue() != null && distanceTo >= bestDistance.getValue())
				return;

			nearestHit.setValue(Pair.of(wse, rayTrace.getSecond()));
			bestDistance.setValue(distanceTo);
		});

		if (nearestHit.getValue() == null)
			return Pair.of(ItemStack.EMPTY, null);

		BlockPos selectedPos = nearestHit.getValue()
			.getSecond();

		BlockPos origin = new BlockPos(basePlateOffsetX, 0, basePlateOffsetZ);
		if (!world.getBounds()
			.isVecInside(selectedPos))
			return Pair.of(ItemStack.EMPTY, null);
		if (new MutableBoundingBox(origin, origin.add(new Vec3i(basePlateSize - 1, 0, basePlateSize - 1)))
			.isVecInside(selectedPos)) {
			if (PonderIndex.EDITOR_MODE)
				nearestHit.getValue()
					.getFirst()
					.selectBlock(selectedPos);
			return Pair.of(ItemStack.EMPTY, selectedPos);
		}

		nearestHit.getValue()
			.getFirst()
			.selectBlock(selectedPos);
		BlockState blockState = world.getBlockState(selectedPos);
		ItemStack pickBlock = blockState.getPickBlock(
			new BlockRayTraceResult(VecHelper.getCenterOf(selectedPos), Direction.UP, selectedPos, true), world,
			selectedPos, Minecraft.getInstance().player);

		return Pair.of(pickBlock, selectedPos);
	}

	public String getTitle() {
		return getString(TITLE_KEY);
	}

	public String getString(String key) {
		return PonderLocalization.getSpecific(sceneId, key);
	}

	public void reset() {
		currentTime = 0;
		activeSchedule.clear();
		schedule.forEach(mdi -> mdi.reset(this));
	}

	public void begin() {
		reset();
		forEach(pe -> pe.reset(this));

		world.restore();
		elements.clear();
		linkedElements.clear();
		keyframeTimes.clear();

		transform = new SceneTransform();
		finished = false;
		setPointOfInterest(new Vec3d(0, 4, 0));

		baseWorldSection.setEmpty();
		baseWorldSection.forceApplyFade(1);
		elements.add(baseWorldSection);

		totalTime = 0;
		stoppedCounting = false;
		activeSchedule.addAll(schedule);
		activeSchedule.forEach(i -> i.onScheduled(this));
	}

	public WorldSectionElement getBaseWorldSection() {
		return baseWorldSection;
	}

	public float getSceneProgress() {
		return totalTime == 0 ? 0 : currentTime / (float) totalTime;
	}

	public void fadeOut() {
		reset();
		activeSchedule.add(new HideAllInstruction(10, null));
	}

	public void renderScene(SuperRenderTypeBuffer buffer, MatrixStack ms, float pt) {
		ms.push();
		Minecraft mc = Minecraft.getInstance();
		Entity prevRVE = mc.renderViewEntity;

		mc.renderViewEntity = this.renderViewEntity;
		forEachVisible(PonderSceneElement.class, e -> e.renderFirst(world, buffer, ms, pt));
		mc.renderViewEntity = prevRVE;

		for (RenderType type : RenderType.getBlockLayers())
			forEachVisible(PonderSceneElement.class, e -> e.renderLayer(world, buffer, type, ms, pt));
		forEachVisible(PonderSceneElement.class, e -> e.renderLast(world, buffer, ms, pt));
		info.set(transform.xRotation.getValue(pt), transform.yRotation.getValue(pt));
		world.renderEntities(ms, buffer, info, pt);
		world.renderParticles(ms, buffer, info, pt);
		outliner.renderOutlines(ms, buffer, pt);

		ms.pop();
	}

	public void renderOverlay(PonderUI screen, MatrixStack ms, float partialTicks) {
		ms.push();
		forEachVisible(PonderOverlayElement.class, e -> e.render(this, screen, ms, partialTicks));
		ms.pop();
	}

	public void setPointOfInterest(Vec3d poi) {
		if (chasingPointOfInterest == null)
			pointOfInterest = poi;
		chasingPointOfInterest = poi;
	}

	public Vec3d getPointOfInterest() {
		return pointOfInterest;
	}

	public void tick() {
		if (chasingPointOfInterest != null)
			pointOfInterest = VecHelper.lerp(.25f, pointOfInterest, chasingPointOfInterest);

		outliner.tickOutlines();
		world.tick();
		transform.tick();
		forEach(e -> e.tick(this));

		if (currentTime < totalTime)
			currentTime++;

		for (Iterator<PonderInstruction> iterator = activeSchedule.iterator(); iterator.hasNext();) {
			PonderInstruction instruction = iterator.next();
			instruction.tick(this);
			if (instruction.isComplete()) {
				iterator.remove();
				if (instruction.isBlocking())
					break;
				continue;
			}
			if (instruction.isBlocking())
				break;
		}

		if (activeSchedule.isEmpty())
			finished = true;
	}

	public void seekToTime(int time) {
		if (time < currentTime)
			throw new IllegalStateException("Cannot seek backwards. Rewind first.");

		while (currentTime < time && !finished) {
			tick();
		}
	}

	public void addToSceneTime(int time) {
		if (!stoppedCounting)
			totalTime += time;
	}

	public void stopCounting() {
		stoppedCounting = true;
	}

	public void markKeyframe(int offset) {
		if (!stoppedCounting)
			keyframeTimes.add(totalTime + offset);
	}

	public void addElement(PonderElement e) {
		elements.add(e);
	}

	public <E extends PonderElement> void linkElement(E e, ElementLink<E> link) {
		linkedElements.put(link.getId(), e);
	}

	public <E extends PonderElement> E resolve(ElementLink<E> link) {
		return link.cast(linkedElements.get(link.getId()));
	}

	public <E extends PonderElement> void runWith(ElementLink<E> link, Consumer<E> callback) {
		callback.accept(resolve(link));
	}

	public <E extends PonderElement, F> F applyTo(ElementLink<E> link, Function<E, F> function) {
		return function.apply(resolve(link));
	}

	public PonderWorld getWorld() {
		return world;
	}

	public Set<PonderElement> getElements() {
		return elements;
	}

	public void forEach(Consumer<? super PonderElement> function) {
		for (PonderElement elemtent : elements)
			function.accept(elemtent);
	}

	public <T extends Entity> void forEachWorldEntity(Class<T> type, Consumer<T> function) {
		for (Entity element : world.getEntities())
			if (type.isInstance(element))
				function.accept(type.cast(element));
	}

	public <T extends PonderElement> void forEach(Class<T> type, Consumer<T> function) {
		for (PonderElement element : elements)
			if (type.isInstance(element))
				function.accept(type.cast(element));
	}

	public <T extends PonderElement> void forEachVisible(Class<T> type, Consumer<T> function) {
		for (PonderElement element : elements)
			if (type.isInstance(element) && element.isVisible())
				function.accept(type.cast(element));
	}

	public MutableBoundingBox getBounds() {
		return world == null ? new MutableBoundingBox() : world.getBounds();
	}

	public Supplier<String> registerText(String defaultText) {
		final String key = "text_" + textIndex;
		PonderLocalization.registerSpecific(sceneId, key, defaultText);
		Supplier<String> supplier = () -> PonderLocalization.getSpecific(sceneId, key);
		textIndex++;
		return supplier;
	}

	public SceneBuilder builder() {
		return new SceneBuilder(this);
	}

	public SceneBuildingUtil getSceneBuildingUtil() {
		return new SceneBuildingUtil(getBounds());
	}

	public SceneTransform getTransform() {
		return transform;
	}

	public class SceneTransform {

		public LerpedFloat xRotation, yRotation;

		// Screen params
		int width, height;
		double offset;
		Matrix4f cachedMat;

		public SceneTransform() {
			xRotation = LerpedFloat.angular()
				.startWithValue(-35);
			yRotation = LerpedFloat.angular()
				.startWithValue(55 + 90);
		}

		public void tick() {
			xRotation.tickChaser();
			yRotation.tickChaser();
		}

		public void updateScreenParams(int width, int height, double offset) {
			this.width = width;
			this.height = height;
			this.offset = offset;
			cachedMat = null;
		}

		public MatrixStack apply(MatrixStack ms) {
			return apply(ms, AnimationTickHolder.getPartialTicks(world), false);
		}

		public MatrixStack apply(MatrixStack ms, float pt, boolean overlayCompatible) {
			ms.translate(width / 2, height / 2, 200 + offset);

			MatrixStacker.of(ms)
				.rotateX(-35)
				.rotateY(55);
			ms.translate(offset, 0, 0);
			MatrixStacker.of(ms)
				.rotateY(-55)
				.rotateX(35);
			MatrixStacker.of(ms)
				.rotateX(xRotation.getValue(pt))
				.rotateY(yRotation.getValue(pt));

			float f = 30 * scaleFactor;

			if (!overlayCompatible) {
				ms.scale(f, -f, f);
				ms.translate((basePlateSize + basePlateOffsetX) / -2f, -1f + yOffset,
					(basePlateSize + basePlateOffsetZ) / -2f);
			} else {
				// For block breaking overlay; Don't ask
				ms.scale(f, f, f);
				ms.translate((basePlateSize + basePlateOffsetX) / -2f, -yOffset,
					(basePlateSize + basePlateOffsetZ) / -2f);
				float y = (float) (0.5065 * Math.pow(2.2975, Math.log(1 / scaleFactor) / Math.log(2))) / 30;
				ms.scale(y, -y, -y);
			}

			return ms;
		}

		public void updateSceneRVE() {
			Vec3d v = screenToScene(width / 2, height / 2, 500);
			renderViewEntity.setPosition(v.x, v.y, v.z);
		}

		public Vec3d screenToScene(double x, double y, int depth) {
			refreshMatrix();
			float pt = AnimationTickHolder.getPartialTicks();
			Vec3d vec = new Vec3d(x, y, depth);

			vec = vec.subtract(width / 2, height / 2, 200 + offset);
			vec = VecHelper.rotate(vec, 35, Axis.X);
			vec = VecHelper.rotate(vec, -55, Axis.Y);
			vec = vec.subtract(offset, 0, 0);
			vec = VecHelper.rotate(vec, 55, Axis.Y);
			vec = VecHelper.rotate(vec, -35, Axis.X);
			vec = VecHelper.rotate(vec, -xRotation.getValue(pt), Axis.X);
			vec = VecHelper.rotate(vec, -yRotation.getValue(pt), Axis.Y);

			float f = 1f / (30 * scaleFactor);

			vec = vec.mul(f, -f, f);
			vec = vec.subtract((basePlateSize + basePlateOffsetX) / -2f, -1f + yOffset,
				(basePlateSize + basePlateOffsetZ) / -2f);

			return vec;
		}

		public Vec2f sceneToScreen(Vec3d vec) {
			refreshMatrix();
			Vector4f vec4 = new Vector4f((float) vec.x, (float) vec.y, (float) vec.z, 1);
			vec4.transform(cachedMat);
			return new Vec2f(vec4.getX(), vec4.getY());
		}

		protected void refreshMatrix() {
			if (cachedMat != null)
				return;
			cachedMat = apply(new MatrixStack()).peek()
				.getModel();
		}

	}

	public class SceneRenderInfo extends ActiveRenderInfo {

		public void set(float xRotation, float yRotation) {
			setDirection(yRotation, xRotation);
		}

	}

	public Outliner getOutliner() {
		return outliner;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

}