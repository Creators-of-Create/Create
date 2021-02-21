package com.simibubi.create.foundation.ponder;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeTileEntity;
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.foundation.ponder.content.PonderPalette;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.ParrotElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instructions.AnimateWorldSectionInstruction;
import com.simibubi.create.foundation.ponder.instructions.ChaseAABBInstruction;
import com.simibubi.create.foundation.ponder.instructions.CreateParrotInstruction;
import com.simibubi.create.foundation.ponder.instructions.DelayInstruction;
import com.simibubi.create.foundation.ponder.instructions.DisplayWorldSectionInstruction;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.ponder.instructions.FadeOutOfSceneInstruction;
import com.simibubi.create.foundation.ponder.instructions.MarkAsFinishedInstruction;
import com.simibubi.create.foundation.ponder.instructions.MovePoiInstruction;
import com.simibubi.create.foundation.ponder.instructions.OutlineSelectionInstruction;
import com.simibubi.create.foundation.ponder.instructions.ReplaceBlocksInstruction;
import com.simibubi.create.foundation.ponder.instructions.RotateSceneInstruction;
import com.simibubi.create.foundation.ponder.instructions.ShowCompleteSchematicInstruction;
import com.simibubi.create.foundation.ponder.instructions.ShowInputInstruction;
import com.simibubi.create.foundation.ponder.instructions.TextInstruction;
import com.simibubi.create.foundation.ponder.instructions.TileEntityDataInstruction;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class SceneBuilder {

	public final OverlayInstructions overlay;
	public final SpecialInstructions special;
	public final WorldInstructions world;
	public final DebugInstructions debug;
	public final EffectInstructions effects;

	private final PonderScene scene;

	public SceneBuilder(PonderScene ponderScene) {
		scene = ponderScene;
		overlay = new OverlayInstructions();
		special = new SpecialInstructions();
		world = new WorldInstructions();
		debug = new DebugInstructions();
		effects = new EffectInstructions();
	}

	// General

	public void title(String title) {
		PonderLocalization.registerSpecific(scene.component, scene.sceneIndex, "title", title);
	}

	public void configureBasePlate(int xOffset, int zOffset, int basePlateSize) {
		scene.offsetX = xOffset;
		scene.offsetZ = zOffset;
		scene.size = basePlateSize;
	}

	public void showBasePlate() {
		world.showSection(scene.getSceneBuildingUtil().select.cuboid(new BlockPos(scene.offsetX, 0, scene.offsetZ),
			new Vec3i(scene.size, 0, scene.size)), Direction.UP);
	}

	public void idle(int ticks) {
		addInstruction(new DelayInstruction(ticks));
	}

	public void idleSeconds(int seconds) {
		idle(seconds * 20);
	}

	public void markAsFinished() {
		addInstruction(new MarkAsFinishedInstruction());
	}

	public void rotateCameraY(float degrees) {
		addInstruction(new RotateSceneInstruction(0, degrees, true));
	}

	public class EffectInstructions {

		public void emitParticles(Vec3d location, Emitter emitter, float amountPerCycle, int cycles) {
			addInstruction(new EmitParticlesInstruction(location, emitter, amountPerCycle, cycles));
		}

		public void indicateSuccess(BlockPos pos) {
			addInstruction(new EmitParticlesInstruction(VecHelper.getCenterOf(pos),
				Emitter.withinBlockSpace(new RedstoneParticleData(.5f, 1, .7f, 1), new Vec3d(0, 0, 0)), 20, 2));
		}

	}

	public class OverlayInstructions {

		public void showTargetedText(PonderPalette color, Vec3d position, String key, String defaultText,
			int duration) {
			PonderLocalization.registerSpecific(scene.component, scene.sceneIndex, key, defaultText);
			addInstruction(new TextInstruction(color.getColor(), scene.textGetter(key), duration, position));
		}

		public void showSelectionWithText(PonderPalette color, Selection selection, String key, String defaultText,
			int duration) {
			PonderLocalization.registerSpecific(scene.component, scene.sceneIndex, key, defaultText);
			addInstruction(new TextInstruction(color.getColor(), scene.textGetter(key), duration, selection));
		}

		public void showText(PonderPalette color, int y, String key, String defaultText, int duration) {
			PonderLocalization.registerSpecific(scene.component, scene.sceneIndex, key, defaultText);
			addInstruction(new TextInstruction(color.getColor(), scene.textGetter(key), duration, y));
		}

		public void showControls(InputWindowElement element, int duration) {
			addInstruction(new ShowInputInstruction(element, duration));
		}
		
		public void chaseBoundingBoxOutline(PonderPalette color, Object slot, AxisAlignedBB boundingBox, int duration) {
			addInstruction(new ChaseAABBInstruction(color, slot, boundingBox, duration));
		}
		
		public void showOutline(PonderPalette color, Object slot, Selection selection, int duration) {
			addInstruction(new OutlineSelectionInstruction(color, slot, selection, duration));
		}

	}

	public class SpecialInstructions {

		public void birbOnTurntable(BlockPos pos) {
			addInstruction(new CreateParrotInstruction(10, Direction.DOWN,
				ParrotElement.spinOnComponent(VecHelper.getCenterOf(pos), pos)));
		}

		public void birbOnSpinnyShaft(BlockPos pos) {
			addInstruction(
				new CreateParrotInstruction(10, Direction.DOWN, ParrotElement.spinOnComponent(VecHelper.getCenterOf(pos)
					.add(0, 0.5, 0), pos)));
		}

		public void birbLookingAtPOI(Vec3d location) {
			addInstruction(new CreateParrotInstruction(10, Direction.DOWN, ParrotElement.lookAtPOI(location)));
		}

		public void birbPartying(Vec3d location) {
			addInstruction(new CreateParrotInstruction(10, Direction.DOWN, ParrotElement.dance(location)));
		}

		public void movePointOfInterest(Vec3d location) {
			addInstruction(new MovePoiInstruction(location));
		}

		public void movePointOfInterest(BlockPos location) {
			movePointOfInterest(VecHelper.getCenterOf(location));
		}

	}

	public class WorldInstructions {

		public void showSection(Selection selection, Direction fadeInDirection) {
			addInstruction(new DisplayWorldSectionInstruction(15, fadeInDirection, selection, true));
		}

		public ElementLink<WorldSectionElement> showIndependentSection(Selection selection, Direction fadeInDirection) {
			DisplayWorldSectionInstruction instruction =
				new DisplayWorldSectionInstruction(15, fadeInDirection, selection, false);
			addInstruction(instruction);
			return instruction.createLink(scene);
		}

		public void hideSection(Selection selection, Direction fadeOutDirection) {
			WorldSectionElement worldSectionElement = new WorldSectionElement(selection);
			ElementLink<WorldSectionElement> elementLink =
				new ElementLink<>(WorldSectionElement.class, UUID.randomUUID());

			addInstruction(scene -> {
				scene.getBaseWorldSection()
					.erase(selection);
				scene.linkElement(worldSectionElement, elementLink);
				scene.addElement(worldSectionElement);
				worldSectionElement.queueRedraw();
			});

			hideIndependentSection(elementLink, fadeOutDirection);
		}

		public void hideIndependentSection(ElementLink<WorldSectionElement> link, Direction fadeOutDirection) {
			addInstruction(new FadeOutOfSceneInstruction<>(15, fadeOutDirection, link));
		}

		public ElementLink<WorldSectionElement> makeSectionIndependent(Selection selection) {
			WorldSectionElement worldSectionElement = new WorldSectionElement(selection);
			ElementLink<WorldSectionElement> elementLink =
				new ElementLink<>(WorldSectionElement.class, UUID.randomUUID());

			addInstruction(scene -> {
				scene.getBaseWorldSection()
					.erase(selection);
				scene.linkElement(worldSectionElement, elementLink);
				scene.addElement(worldSectionElement);
				worldSectionElement.resetAnimatedTransform();
				worldSectionElement.setVisible(true);
				worldSectionElement.forceApplyFade(1);
			});

			return elementLink;
		}

		public void rotateSection(ElementLink<WorldSectionElement> link, double xRotation, double yRotation,
			double zRotation, int duration) {
			addInstruction(
				AnimateWorldSectionInstruction.rotate(link, new Vec3d(xRotation, yRotation, zRotation), duration));
		}

		public void moveSection(ElementLink<WorldSectionElement> link, Vec3d offset, int duration) {
			addInstruction(AnimateWorldSectionInstruction.move(link, offset, duration));
		}

		public void setBlocks(Selection selection, BlockState state, boolean spawnParticles) {
			addInstruction(new ReplaceBlocksInstruction(selection, state, true, spawnParticles));
		}

		public void setBlock(BlockPos pos, BlockState state) {
			setBlocks(scene.getSceneBuildingUtil().select.position(pos), state, true);
		}

		public void replaceBlocks(Selection selection, BlockState state, boolean spawnParticles) {
			addInstruction(new ReplaceBlocksInstruction(selection, state, false, spawnParticles));
		}

		public void setKineticSpeed(Selection selection, float speed) {
			modifyKineticSpeed(selection, f -> speed);
		}

		public void multiplyKineticSpeed(Selection selection, float modifier) {
			modifyKineticSpeed(selection, f -> f * modifier);
		}

		public void modifyKineticSpeed(Selection selection, UnaryOperator<Float> speedFunc) {
			addInstruction(new TileEntityDataInstruction(selection, SpeedGaugeTileEntity.class, nbt -> {
				float newSpeed = speedFunc.apply(nbt.getFloat("Speed"));
				nbt.putFloat("Value", SpeedGaugeTileEntity.getDialTarget(newSpeed));
				return nbt;
			}, false));
			addInstruction(new TileEntityDataInstruction(selection, KineticTileEntity.class, nbt -> {
				nbt.putFloat("Speed", speedFunc.apply(nbt.getFloat("Speed")));
				return nbt;
			}, false));
		}

		public void flapFunnels(Selection selection, boolean outward) {
			addInstruction(new TileEntityDataInstruction(selection, FunnelTileEntity.class, nbt -> {
				nbt.putInt("Flap", outward ? -1 : 1);
				return nbt;
			}, false));
		}

	}

	public class DebugInstructions {

		public void debugSchematic() {
			addInstruction(new ShowCompleteSchematicInstruction());
		}

		public void addInstructionInstance(PonderInstruction instruction) {
			addInstruction(instruction);
		}

	}

	private void addInstruction(PonderInstruction instruction) {
		scene.schedule.add(instruction);
	}

	private void addInstruction(Consumer<PonderScene> callback) {
		scene.schedule.add(PonderInstruction.simple(callback));
	}

}