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

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

import com.jozufozu.flywheel.util.DiffuseLightCalculator;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.ponder.content.PonderIndex;
import com.simibubi.create.foundation.ponder.element.PonderElement;
import com.simibubi.create.foundation.ponder.element.PonderOverlayElement;
import com.simibubi.create.foundation.ponder.element.PonderSceneElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instruction.HideAllInstruction;
import com.simibubi.create.foundation.ponder.instruction.PonderInstruction;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import com.simibubi.create.foundation.render.ForcedDiffuseState;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class PonderScene {

	public static final String TITLE_KEY = "header";

	private boolean finished;
//	private int sceneIndex;
	private int textIndex;
	ResourceLocation sceneId;

	private IntList keyframeTimes;

	List<PonderInstruction> schedule;
	private List<PonderInstruction> activeSchedule;
	private Map<UUID, PonderElement> linkedElements;
	private Set<PonderElement> elements;
	private List<PonderTag> tags;

	private PonderWorld world;
	private String namespace;
	private ResourceLocation component;
	private SceneTransform transform;
	private SceneCamera camera;
	private Outliner outliner;
//	private String defaultTitle;

	private Vec3 pointOfInterest;
	private Vec3 chasingPointOfInterest;
	private WorldSectionElement baseWorldSection;
	@Nullable
	private Entity renderViewEntity;

	int basePlateOffsetX;
	int basePlateOffsetZ;
	int basePlateSize;
	float scaleFactor;
	float yOffset;
	boolean hidePlatformShadow;

	private boolean stoppedCounting;
	private int totalTime;
	private int currentTime;

	public PonderScene(PonderWorld world, String namespace, ResourceLocation component, Collection<PonderTag> tags) {
		if (world != null)
			world.scene = this;

		pointOfInterest = Vec3.ZERO;
		textIndex = 1;
		hidePlatformShadow = false;

		this.world = world;
		this.namespace = namespace;
		this.component = component;

		outliner = new Outliner();
		elements = new HashSet<>();
		linkedElements = new HashMap<>();
		this.tags = new ArrayList<>(tags);
		schedule = new ArrayList<>();
		activeSchedule = new ArrayList<>();
		transform = new SceneTransform();
		basePlateSize = getBounds().getXSpan();
		camera = new SceneCamera();
		baseWorldSection = new WorldSectionElement();
		renderViewEntity = world != null ? new ArmorStand(world, 0, 0, 0) : null;
		keyframeTimes = new IntArrayList(4);
		scaleFactor = 1;
		yOffset = 0;

		setPointOfInterest(new Vec3(0, 4, 0));
	}

	public void deselect() {
		forEach(WorldSectionElement.class, WorldSectionElement::resetSelectedBlock);
	}

	public Pair<ItemStack, BlockPos> rayTraceScene(Vec3 from, Vec3 to) {
		MutableObject<Pair<WorldSectionElement, Pair<Vec3, BlockHitResult>>> nearestHit = new MutableObject<>();
		MutableDouble bestDistance = new MutableDouble(0);

		forEach(WorldSectionElement.class, wse -> {
			wse.resetSelectedBlock();
			if (!wse.isVisible())
				return;
			Pair<Vec3, BlockHitResult> rayTrace = wse.rayTrace(world, from, to);
			if (rayTrace == null)
				return;
			double distanceTo = rayTrace.getFirst()
				.distanceTo(from);
			if (nearestHit.getValue() != null && distanceTo >= bestDistance.getValue())
				return;

			nearestHit.setValue(Pair.of(wse, rayTrace));
			bestDistance.setValue(distanceTo);
		});

		if (nearestHit.getValue() == null)
			return Pair.of(ItemStack.EMPTY, null);

		Pair<Vec3, BlockHitResult> selectedHit = nearestHit.getValue()
			.getSecond();
		BlockPos selectedPos = selectedHit.getSecond()
			.getBlockPos();

		BlockPos origin = new BlockPos(basePlateOffsetX, 0, basePlateOffsetZ);
		if (!world.getBounds()
			.isInside(selectedPos))
			return Pair.of(ItemStack.EMPTY, null);
		if (BoundingBox.fromCorners(origin, origin.offset(new Vec3i(basePlateSize - 1, 0, basePlateSize - 1)))
			.isInside(selectedPos)) {
			if (PonderIndex.editingModeActive())
				nearestHit.getValue()
					.getFirst()
					.selectBlock(selectedPos);
			return Pair.of(ItemStack.EMPTY, selectedPos);
		}

		nearestHit.getValue()
			.getFirst()
			.selectBlock(selectedPos);
		BlockState blockState = world.getBlockState(selectedPos);

		Direction direction = selectedHit.getSecond()
			.getDirection();
		Vec3 location = selectedHit.getSecond()
			.getLocation();

		ItemStack pickBlock = blockState.getCloneItemStack(new BlockHitResult(location, direction, selectedPos, true),
			world, selectedPos, Minecraft.getInstance().player);

		return Pair.of(pickBlock, selectedPos);
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
		setPointOfInterest(new Vec3(0, 4, 0));

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

	public void renderScene(SuperRenderTypeBuffer buffer, PoseStack ms, float pt) {
		ForcedDiffuseState.pushCalculator(DiffuseLightCalculator.DEFAULT);
		ms.pushPose();

		Minecraft mc = Minecraft.getInstance();
		Entity prevRVE = mc.cameraEntity;

		mc.cameraEntity = this.renderViewEntity;
		forEachVisible(PonderSceneElement.class, e -> e.renderFirst(world, buffer, ms, pt));
		mc.cameraEntity = prevRVE;

		for (RenderType type : RenderType.chunkBufferLayers())
			forEachVisible(PonderSceneElement.class, e -> e.renderLayer(world, buffer, type, ms, pt));

		forEachVisible(PonderSceneElement.class, e -> e.renderLast(world, buffer, ms, pt));
		camera.set(transform.xRotation.getValue(pt) + 90, transform.yRotation.getValue(pt) + 180);
		world.renderEntities(ms, buffer, camera, pt);
		world.renderParticles(ms, buffer, camera, pt);
		outliner.renderOutlines(ms, buffer, pt);

		ms.popPose();
		ForcedDiffuseState.popCalculator();
	}

	public void renderOverlay(PonderUI screen, PoseStack ms, float partialTicks) {
		ms.pushPose();
		forEachVisible(PonderOverlayElement.class, e -> e.render(this, screen, ms, partialTicks));
		ms.popPose();
	}

	public void setPointOfInterest(Vec3 poi) {
		if (chasingPointOfInterest == null)
			pointOfInterest = poi;
		chasingPointOfInterest = poi;
	}

	public Vec3 getPointOfInterest() {
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
			forEach(e -> e.whileSkipping(this));
			tick();
		}

		forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
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

	public void forEach(Consumer<? super PonderElement> function) {
		for (PonderElement elemtent : elements)
			function.accept(elemtent);
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

	public <T extends Entity> void forEachWorldEntity(Class<T> type, Consumer<T> function) {
		world.getEntityStream()
			.filter(type::isInstance)
			.map(type::cast)
			.forEach(function);
		/*
		 * for (Entity element : world.getEntities()) { if (type.isInstance(element))
		 * function.accept(type.cast(element)); }
		 */
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

	public String getTitle() {
		return getString(TITLE_KEY);
	}

	public String getString(String key) {
		return PonderLocalization.getSpecific(sceneId, key);
	}

	public PonderWorld getWorld() {
		return world;
	}

	public String getNamespace() {
		return namespace;
	}

	public int getKeyframeCount() {
		return keyframeTimes.size();
	}

	public int getKeyframeTime(int index) {
		return keyframeTimes.getInt(index);
	}

	public List<PonderTag> getTags() {
		return tags;
	}

	public ResourceLocation getComponent() {
		return component;
	}

	public Set<PonderElement> getElements() {
		return elements;
	}

	public BoundingBox getBounds() {
		return world == null ? new BoundingBox(BlockPos.ZERO) : world.getBounds();
	}

	public ResourceLocation getId() {
		return sceneId;
	}

	public SceneTransform getTransform() {
		return transform;
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

	public int getBasePlateOffsetX() {
		return basePlateOffsetX;
	}

	public int getBasePlateOffsetZ() {
		return basePlateOffsetZ;
	}
	
	public boolean shouldHidePlatformShadow() {
		return hidePlatformShadow;
	}

	public int getBasePlateSize() {
		return basePlateSize;
	}

	public float getScaleFactor() {
		return scaleFactor;
	}

	public float getYOffset() {
		return yOffset;
	}

	public int getTotalTime() {
		return totalTime;
	}

	public int getCurrentTime() {
		return currentTime;
	}

	public class SceneTransform {

		public LerpedFloat xRotation, yRotation;

		// Screen params
		private int width, height;
		private double offset;
		private Matrix4f cachedMat;

		public SceneTransform() {
			xRotation = LerpedFloat.angular()
				.disableSmartAngleChasing()
				.startWithValue(-35);
			yRotation = LerpedFloat.angular()
				.disableSmartAngleChasing()
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

		public PoseStack apply(PoseStack ms) {
			return apply(ms, AnimationTickHolder.getPartialTicks(world));
		}

		public PoseStack apply(PoseStack ms, float pt) {
			ms.translate(width / 2, height / 2, 200 + offset);

			TransformStack.cast(ms)
				.rotateX(-35)
				.rotateY(55)
				.translate(offset, 0, 0)
				.rotateY(-55)
				.rotateX(35)
				.rotateX(xRotation.getValue(pt))
				.rotateY(yRotation.getValue(pt));

			UIRenderHelper.flipForGuiRender(ms);
			float f = 30 * scaleFactor;
			ms.scale(f, f, f);
			ms.translate((basePlateSize) / -2f - basePlateOffsetX, -1f + yOffset,
				(basePlateSize) / -2f - basePlateOffsetZ);

			return ms;
		}

		public void updateSceneRVE(float pt) {
			Vec3 v = screenToScene(width / 2, height / 2, 500, pt);
			if (renderViewEntity != null)
				renderViewEntity.setPos(v.x, v.y, v.z);
		}

		public Vec3 screenToScene(double x, double y, int depth, float pt) {
			refreshMatrix(pt);
			Vec3 vec = new Vec3(x, y, depth);

			vec = vec.subtract(width / 2, height / 2, 200 + offset);
			vec = VecHelper.rotate(vec, 35, Axis.X);
			vec = VecHelper.rotate(vec, -55, Axis.Y);
			vec = vec.subtract(offset, 0, 0);
			vec = VecHelper.rotate(vec, 55, Axis.Y);
			vec = VecHelper.rotate(vec, -35, Axis.X);
			vec = VecHelper.rotate(vec, -xRotation.getValue(pt), Axis.X);
			vec = VecHelper.rotate(vec, -yRotation.getValue(pt), Axis.Y);

			float f = 1f / (30 * scaleFactor);

			vec = vec.multiply(f, -f, f);
			vec = vec.subtract((basePlateSize) / -2f - basePlateOffsetX, -1f + yOffset,
				(basePlateSize) / -2f - basePlateOffsetZ);

			return vec;
		}

		public Vec2 sceneToScreen(Vec3 vec, float pt) {
			refreshMatrix(pt);
			Vector4f vec4 = new Vector4f((float) vec.x, (float) vec.y, (float) vec.z, 1);
			vec4.transform(cachedMat);
			return new Vec2(vec4.x(), vec4.y());
		}

		protected void refreshMatrix(float pt) {
			if (cachedMat != null)
				return;
			cachedMat = apply(new PoseStack(), pt).last()
				.pose();
		}

	}

	public class SceneCamera extends Camera {

		public void set(float xRotation, float yRotation) {
			setRotation(yRotation, xRotation);
		}

	}

}
