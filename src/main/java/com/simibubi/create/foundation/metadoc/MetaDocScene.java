package com.simibubi.create.foundation.metadoc;

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
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.foundation.metadoc.elements.MetaDocOverlayElement;
import com.simibubi.create.foundation.metadoc.elements.MetaDocSceneElement;
import com.simibubi.create.foundation.metadoc.elements.ParrotElement;
import com.simibubi.create.foundation.metadoc.elements.WorldSectionElement;
import com.simibubi.create.foundation.metadoc.instructions.CreateParrotInstruction;
import com.simibubi.create.foundation.metadoc.instructions.DelayInstruction;
import com.simibubi.create.foundation.metadoc.instructions.DisplayWorldSectionInstruction;
import com.simibubi.create.foundation.metadoc.instructions.HideAllInstruction;
import com.simibubi.create.foundation.metadoc.instructions.ReplaceBlocksInstruction;
import com.simibubi.create.foundation.metadoc.instructions.RotateSceneInstruction;
import com.simibubi.create.foundation.metadoc.instructions.ShowCompleteSchematicInstruction;
import com.simibubi.create.foundation.metadoc.instructions.TextWindowInstruction;
import com.simibubi.create.foundation.metadoc.instructions.TileEntityDataInstruction;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class MetaDocScene {

	List<MetaDocInstruction> schedule, activeSchedule;
	Set<MetaDocElement> elements;
	MetaDocWorld world;
	ResourceLocation component;
	int sceneIndex;
	SceneTransform transform;

	public MetaDocScene(MetaDocWorld world, ResourceLocation component, int sceneIndex) {
		this.world = world;
		this.component = component;
		this.sceneIndex = sceneIndex;
		elements = new HashSet<>();
		schedule = new ArrayList<>();
		activeSchedule = new ArrayList<>();
		transform = new SceneTransform();
	}

	public String getTitle() {
		return getString("title");
	}

	public String getString(String key) {
		return MetaDocLocalization.getSpecific(component, sceneIndex, key);
	}

	public void reset() {
		activeSchedule.clear();
		schedule.forEach(mdi -> mdi.reset(this));
	}

	public void begin() {
		reset();
		world.restore();
		transform = new SceneTransform();
		forEach(WorldSectionElement.class, wse -> wse.queueRedraw(world));
		elements.clear();
		activeSchedule.addAll(schedule);
	}

	public void fadeOut() {
		reset();
		activeSchedule.add(new HideAllInstruction(10, null));
	}

	public void renderScene(IRenderTypeBuffer buffer, MatrixStack ms) {
		ms.push();
		forEach(MetaDocSceneElement.class, e -> {
			if (e.isVisible())
				e.render(world, buffer, ms);
		});
		ms.pop();
	}

	public void renderOverlay(MetaDocScreen screen, MatrixStack ms, float partialTicks) {
		ms.push();
		forEach(MetaDocOverlayElement.class, e -> {
			if (e.isVisible())
				e.render(this, screen, ms, partialTicks);
		});
		ms.pop();
	}

	public void tick() {
		transform.tick();
		forEach(MetaDocElement::tick);
		for (Iterator<MetaDocInstruction> iterator = activeSchedule.iterator(); iterator.hasNext();) {
			MetaDocInstruction metaDocInstruction = iterator.next();
			metaDocInstruction.tick(this);
			if (metaDocInstruction.isComplete()) {
				iterator.remove();
				continue;
			}
			if (metaDocInstruction.isBlocking())
				break;
		}
	}

	public void addElement(MetaDocElement e) {
		elements.add(e);
	}

	public MetaDocWorld getWorld() {
		return world;
	}

	public Set<MetaDocElement> getElements() {
		return elements;
	}

	public void forEach(Consumer<? super MetaDocElement> function) {
		for (MetaDocElement metaDocElement : elements)
			function.accept(metaDocElement);
	}

	public <T extends MetaDocElement> void forEach(Class<T> type, Consumer<T> function) {
		for (MetaDocElement metaDocElement : elements)
			if (type.isInstance(metaDocElement))
				function.accept(type.cast(metaDocElement));
	}

	public MutableBoundingBox getBounds() {
		return world == null ? new MutableBoundingBox() : world.getBounds();
	}

	public SceneBuilder builder() {
		return new SceneBuilder();
	}

	private Supplier<String> textGetter(String key) {
		return () -> MetaDocLocalization.getSpecific(component, sceneIndex, key);
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
				.startWithValue(55);
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

			MutableBoundingBox bounds = getBounds();
			ms.translate(bounds.getXSize() / -2f, -.5f, bounds.getZSize() / -2f);

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
			MatrixStack ms = apply(new MatrixStack());
//			MatrixStacker.of(ms)
//				.rotateY(180);
			cachedMat = ms.peek()
				.getModel();
		}

	}

	public class SceneBuilder {

		public SceneBuilder showBasePlate() {
			Vec3i length = getBounds().getLength();
			return showSection(Select.cuboid(BlockPos.ZERO, new Vec3i(length.getX(), 0, length.getZ())), Direction.UP);
		}

		public SceneBuilder showText(Vec3d position, String key, String defaultText, int fadeTime, int duration) {
			MetaDocLocalization.registerSpecific(component, sceneIndex, key, defaultText);
			return addInstruction(new TextWindowInstruction(textGetter(key), fadeTime, duration, position));
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

		public SceneBuilder rotateCameraY(float degrees) {
			return addInstruction(new RotateSceneInstruction(0, degrees, true));
		}

		public SceneBuilder setBlocks(Select selection, BlockState state) {
			return addInstruction(new ReplaceBlocksInstruction(selection, state, true));
		}

		public SceneBuilder replaceBlocks(Select selection, BlockState state) {
			return addInstruction(new ReplaceBlocksInstruction(selection, state, false));
		}

		public SceneBuilder setKineticSpeed(Select selection, float speed) {
			return modifyKineticSpeed(selection, f -> speed);
		}

		public SceneBuilder multiplyKineticSpeed(Select selection, float modifier) {
			return modifyKineticSpeed(selection, f -> f * modifier);
		}

		public SceneBuilder modifyKineticSpeed(Select selection, UnaryOperator<Float> speedFunc) {
			return addInstruction(new TileEntityDataInstruction(selection, KineticTileEntity.class, nbt -> {
				if (!nbt.contains("Speed"))
					return nbt;
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

		public SceneBuilder createParrotOn(BlockPos pos, Direction fadeInDirection) {
			return addInstruction(
				new CreateParrotInstruction(15, fadeInDirection, new ParrotElement(new Vec3d(pos).add(.5, 0, .5))));
		}

		public SceneBuilder createParrot(Vec3d location, Direction fadeInDirection) {
			return addInstruction(new CreateParrotInstruction(15, fadeInDirection, new ParrotElement(location)));
		}

		public SceneBuilder addInstruction(MetaDocInstruction instruction) {
			schedule.add(instruction);
			return this;
		}

		//

		public Select everywhere() {
			return Select.cuboid(BlockPos.ZERO, getBounds().getLength());
		}

	}

}