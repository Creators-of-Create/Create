package com.simibubi.create.foundation.ponder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeTileEntity;
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.foundation.ponder.content.PonderPalette;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.ParrotElement;
import com.simibubi.create.foundation.ponder.elements.PonderOverlayElement;
import com.simibubi.create.foundation.ponder.elements.PonderSceneElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instructions.CreateParrotInstruction;
import com.simibubi.create.foundation.ponder.instructions.DelayInstruction;
import com.simibubi.create.foundation.ponder.instructions.DisplayWorldSectionInstruction;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.ponder.instructions.HideAllInstruction;
import com.simibubi.create.foundation.ponder.instructions.MarkAsFinishedInstruction;
import com.simibubi.create.foundation.ponder.instructions.MovePoiInstruction;
import com.simibubi.create.foundation.ponder.instructions.ReplaceBlocksInstruction;
import com.simibubi.create.foundation.ponder.instructions.RotateSceneInstruction;
import com.simibubi.create.foundation.ponder.instructions.ShowCompleteSchematicInstruction;
import com.simibubi.create.foundation.ponder.instructions.ShowInputInstruction;
import com.simibubi.create.foundation.ponder.instructions.TextInstruction;
import com.simibubi.create.foundation.ponder.instructions.TileEntityDataInstruction;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class PonderScene {

	List<PonderInstruction> schedule, activeSchedule;
	Set<PonderElement> elements;
	PonderWorld world;
	ResourceLocation component;
	int sceneIndex;
	SceneTransform transform;
	public boolean finished;
	SceneRenderInfo info;
	Outliner outliner;

	Vec3d pointOfInterest;
	Vec3d chasingPointOfInterest;

	private int offsetX;
	private int offsetZ;
	private int size;

	public PonderScene(PonderWorld world, ResourceLocation component, int sceneIndex) {
		pointOfInterest = Vec3d.ZERO;

		this.world = world;
		this.component = component;
		this.sceneIndex = sceneIndex;

		outliner = new Outliner();
		elements = new HashSet<>();
		schedule = new ArrayList<>();
		activeSchedule = new ArrayList<>();
		transform = new SceneTransform();
		size = getBounds().getXSize();
		info = new SceneRenderInfo();
	}

	public String getTitle() {
		return getString("title");
	}

	public String getString(String key) {
		return PonderLocalization.getSpecific(component, sceneIndex, key);
	}

	public void reset() {
		activeSchedule.clear();
		schedule.forEach(mdi -> mdi.reset(this));
	}

	public void begin() {
		reset();
		world.restore();
		transform = new SceneTransform();
		finished = false;
		forEach(WorldSectionElement.class, wse -> wse.queueRedraw(world));
		elements.clear();
		activeSchedule.addAll(schedule);
	}

	public void fadeOut() {
		reset();
		activeSchedule.add(new HideAllInstruction(10, null));
	}

	public void renderScene(SuperRenderTypeBuffer buffer, MatrixStack ms) {
		float pt = Minecraft.getInstance()
			.getRenderPartialTicks();

		ms.push();
		forEachVisible(PonderSceneElement.class, e -> e.renderFirst(world, buffer, ms));
		for (RenderType type : RenderType.getBlockLayers())
			forEachVisible(PonderSceneElement.class, e -> e.renderLayer(world, buffer, type, ms));
		forEachVisible(PonderSceneElement.class, e -> e.renderLast(world, buffer, ms));
		info.set(transform.xRotation.getValue(pt), transform.yRotation.getValue(pt));
		world.renderParticles(ms, buffer, info);
		outliner.renderOutlines(ms, buffer);
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
		world.tickParticles();
		transform.tick();
		forEach(e -> e.tick(this));

		for (Iterator<PonderInstruction> iterator = activeSchedule.iterator(); iterator.hasNext();) {
			PonderInstruction instruction = iterator.next();
			instruction.tick(this);
			if (instruction.isComplete()) {
				iterator.remove();
				continue;
			}
			if (instruction.isBlocking())
				break;
		}

		if (activeSchedule.isEmpty())
			finished = true;
	}

	public void addElement(PonderElement e) {
		elements.add(e);
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

	public SceneBuilder builder() {
		return new SceneBuilder();
	}

	private Supplier<String> textGetter(String key) {
		return () -> PonderLocalization.getSpecific(component, sceneIndex, key);
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
			float pt = Minecraft.getInstance()
				.getRenderPartialTicks();
			ms.translate(width / 2, height / 2, 200);

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
			ms.scale(30, -30, 30);
			ms.translate((size + offsetX) / -2f, -.5f, (size + offsetZ) / -2f);

			return ms;
		}

		public Vec3d screenToScene(float x, float y) {
			refreshMatrix();
			Vector4f vec = new Vector4f(x, y, 0, 1);
			cachedMat.invert();
			vec.transform(cachedMat);
			cachedMat.invert();
			MutableBoundingBox bounds = getBounds();
			return new Vec3d(vec.getX() + bounds.getXSize() / -2f, vec.getY(), vec.getZ() + bounds.getZSize() / -2f);
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

	public class SceneBuilder {

		private SceneBuildingUtil sceneBuildingUtil;

		public SceneBuilder() {
			sceneBuildingUtil = new SceneBuildingUtil();
		}

		public SceneBuildingUtil getSceneBuildingUtil() {
			return sceneBuildingUtil;
		}

		public SceneBuilder showBasePlate() {
			return showSection(Select.cuboid(new BlockPos(offsetX, 0, offsetZ), new Vec3i(size, 0, size)),
				Direction.UP);
		}

		public SceneBuilder showTargetedText(PonderPalette color, Vec3d position, String key, String defaultText,
			int duration) {
			PonderLocalization.registerSpecific(component, sceneIndex, key, defaultText);
			return addInstruction(new TextInstruction(color.getColor(), textGetter(key), duration, position));
		}

		public SceneBuilder showSelectionWithText(PonderPalette color, Select selection, String key, String defaultText,
			int duration) {
			PonderLocalization.registerSpecific(component, sceneIndex, key, defaultText);
			return addInstruction(new TextInstruction(color.getColor(), textGetter(key), duration, selection));
		}

		public SceneBuilder showText(PonderPalette color, int y, String key, String defaultText, int duration) {
			PonderLocalization.registerSpecific(component, sceneIndex, key, defaultText);
			return addInstruction(new TextInstruction(color.getColor(), textGetter(key), duration, y));
		}

		public SceneBuilder showSection(Select selection, Direction fadeInDirection) {
			return addInstruction(
				new DisplayWorldSectionInstruction(15, fadeInDirection, new WorldSectionElement(selection)));
		}

		public SceneBuilder debugSchematic() {
			return addInstruction(new ShowCompleteSchematicInstruction());
		}

		public SceneBuilder idle(int ticks) {
			return addInstruction(new DelayInstruction(ticks));
		}

		public SceneBuilder idleSeconds(int seconds) {
			return idle(seconds * 20);
		}

		public SceneBuilder markAsFinished() {
			return addInstruction(new MarkAsFinishedInstruction());
		}

		public SceneBuilder rotateCameraY(float degrees) {
			return addInstruction(new RotateSceneInstruction(0, degrees, true));
		}

		public SceneBuilder setBlocks(Select selection, BlockState state) {
			return addInstruction(new ReplaceBlocksInstruction(selection, state, true));
		}

		public SceneBuilder replaceBlocks(Select selection, BlockState state) {
			return addInstruction(new ReplaceBlocksInstruction(selection, state, true));
		}

		public SceneBuilder setKineticSpeed(Select selection, float speed) {
			return modifyKineticSpeed(selection, f -> speed);
		}

		public SceneBuilder multiplyKineticSpeed(Select selection, float modifier) {
			return modifyKineticSpeed(selection, f -> f * modifier);
		}

		public SceneBuilder modifyKineticSpeed(Select selection, UnaryOperator<Float> speedFunc) {
			addInstruction(new TileEntityDataInstruction(selection, SpeedGaugeTileEntity.class, nbt -> {
				float newSpeed = speedFunc.apply(nbt.getFloat("Speed"));
				nbt.putFloat("Value", SpeedGaugeTileEntity.getDialTarget(newSpeed));
				return nbt;
			}, false));
			return addInstruction(new TileEntityDataInstruction(selection, KineticTileEntity.class, nbt -> {
				nbt.putFloat("Speed", speedFunc.apply(nbt.getFloat("Speed")));
				return nbt;
			}, false));
		}

		public SceneBuilder flapFunnels(Select selection, boolean outward) {
			return addInstruction(new TileEntityDataInstruction(selection, FunnelTileEntity.class, nbt -> {
				nbt.putInt("Flap", outward ? -1 : 1);
				return nbt;
			}, false));
		}

		public SceneBuilder movePOI(Vec3d location) {
			return addInstruction(new MovePoiInstruction(location));
		}

		public SceneBuilder showControls(InputWindowElement element, int duration) {
			return addInstruction(new ShowInputInstruction(element, duration));
		}

		public SceneBuilder emitParticles(Vec3d location, Emitter emitter, float amountPerCycle, int cycles) {
			return addInstruction(new EmitParticlesInstruction(location, emitter, amountPerCycle, cycles));
		}

		public SceneBuilder indicateSuccess(BlockPos pos) {
			return addInstruction(new EmitParticlesInstruction(VecHelper.getCenterOf(pos),
				Emitter.withinBlockSpace(new RedstoneParticleData(.5f, 1, .7f, 1), new Vec3d(0, 0, 0)), 20, 2));
		}

		public SceneBuilder birbOnTurntable(BlockPos pos) {
			return addInstruction(new CreateParrotInstruction(10, Direction.DOWN,
				ParrotElement.spinOnComponent(VecHelper.getCenterOf(pos), pos)));
		}

		public SceneBuilder birbOnSpinnyShaft(BlockPos pos) {
			return addInstruction(
				new CreateParrotInstruction(10, Direction.DOWN, ParrotElement.spinOnComponent(VecHelper.getCenterOf(pos)
					.add(0, 0.5, 0), pos)));
		}

		public SceneBuilder birbLookingAtPOI(Vec3d location) {
			return addInstruction(new CreateParrotInstruction(10, Direction.DOWN, ParrotElement.lookAtPOI(location)));
		}

		public SceneBuilder birbPartying(Vec3d location) {
			return addInstruction(new CreateParrotInstruction(10, Direction.DOWN, ParrotElement.dance(location)));
		}

		public SceneBuilder addInstruction(PonderInstruction instruction) {
			schedule.add(instruction);
			return this;
		}

		public class SceneBuildingUtil {

			public Vec3d centerOf(int x, int y, int z) {
				return VecHelper.getCenterOf(new BlockPos(x, y, z));
			}

			public Vec3d topOf(int x, int y, int z) {
				return new Vec3d(x + .5, y + 1, z + .5);
			}

			public Vec3d vector(double x, double y, double z) {
				return new Vec3d(x, y, z);
			}

			public Select everywhere() {
				return Select.everything(getBounds());
			}

			public Select column(int x, int z) {
				return Select.column(getBounds(), x, z);
			}

			public Select layer(int y) {
				return layers(y, 1);
			}

			public Select layers(int y, int height) {
				return Select.layer(getBounds(), y, height);
			}

			public Select layersFrom(int y) {
				return Select.layer(getBounds(), y, getBounds().getYSize());
			}

		}

		public SceneBuilder configureBasePlate(int xOffset, int zOffset, int basePlateSize) {
			offsetX = xOffset;
			offsetZ = zOffset;
			size = basePlateSize;
			return this;
		}

	}

	public Outliner getOutliner() {
		return outliner;
	}

}