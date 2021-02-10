package com.simibubi.create.foundation.metadoc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.metadoc.instructions.DelayInstruction;
import com.simibubi.create.foundation.metadoc.instructions.DisplayWorldSectionInstruction;
import com.simibubi.create.foundation.metadoc.instructions.HideAllInstruction;
import com.simibubi.create.foundation.metadoc.instructions.ShowCompleteSchematicInstruction;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.Vec3i;

public class MetaDocScene {

	List<MetaDocInstruction> schedule, activeSchedule;
	Set<MetaDocSceneElement> elements;
	Map<Object, Set<MetaDocSceneElement>> groups;
	MetaDocWorld world;

	public MetaDocScene(MetaDocWorld world) {
		this.world = world;
		elements = new HashSet<>();
		groups = new IdentityHashMap<>();
		schedule = new ArrayList<>();
		activeSchedule = new ArrayList<>();
	}

	public void reset() {
		activeSchedule.clear();
		schedule.forEach(mdi -> mdi.reset(this));
	}
	
	public void begin() {
		reset();
		activeSchedule.addAll(schedule);
	}

	public void fadeOut() {
		reset();
		activeSchedule.add(new HideAllInstruction(10, Direction.DOWN));
	}

	public void render(IRenderTypeBuffer buffer, MatrixStack ms) {
		ms.push();
		MutableBoundingBox bounds = world.getBounds();
		ms.translate(bounds.getXSize() / -2f, -.5f, bounds.getZSize() / -2f);
		elements.forEach(e -> {
			if (e.visible)
				e.render(world, buffer, ms);
		});
		ms.pop();
	}

	public void tick() {
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

	public void addElement(MetaDocSceneElement e) {
		elements.add(e);
	}

	public MetaDocWorld getWorld() {
		return world;
	}

	public Set<MetaDocSceneElement> getElements() {
		return elements;
	}

	public MutableBoundingBox getBounds() {
		return world.getBounds();
	}

	public SceneBuilder builder() {
		return new SceneBuilder();
	}

	public class SceneBuilder {

		public SceneBuilder showBasePlate() {
			Vec3i length = getBounds().getLength();
			return showSection(BlockPos.ZERO, new Vec3i(length.getX(), 0, length.getZ()), Direction.UP);
		}

		public SceneBuilder showSection(BlockPos origin, Vec3i size, Direction fadeInDirection) {
			return addInstruction(
				new DisplayWorldSectionInstruction(20, fadeInDirection, new WorldSectionElement.Cuboid(origin, size)));
		}

		public SceneBuilder showSection(WorldSectionElement element, Direction fadeInDirection) {
			return addInstruction(new DisplayWorldSectionInstruction(20, fadeInDirection, element));
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

		public SceneBuilder addInstruction(MetaDocInstruction instruction) {
			schedule.add(instruction);
			return this;
		}

	}

}