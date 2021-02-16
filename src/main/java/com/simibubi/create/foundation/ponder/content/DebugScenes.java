package com.simibubi.create.foundation.ponder.content;

import static com.simibubi.create.foundation.ponder.content.PonderPalette.WHITE;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.content.contraptions.particle.RotationIndicatorParticleData;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder;
import com.simibubi.create.foundation.ponder.PonderScene.SceneBuilder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.PonderStoryBoard;
import com.simibubi.create.foundation.ponder.Select;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction.Emitter;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;

public abstract class DebugScenes extends PonderStoryBoard {

	private int index;

	public static void registerAll() {
		ItemEntry<Item> item = AllItems.BRASS_HAND;
		int i = 0;
		PonderRegistry.addStoryBoard(item, new CoordinateScene(++i));
		PonderRegistry.addStoryBoard(item, new BlocksScene(++i));
		PonderRegistry.addStoryBoard(item, new FluidsScene(++i));
		PonderRegistry.addStoryBoard(item, new OffScreenScene(++i));
		PonderRegistry.addStoryBoard(item, new ParticlesScene(++i));
	}

	public DebugScenes(int index) {
		this.index = index;
	}

	@Override
	public final String getSchematicName() {
		return "debug/scene_" + index;
	}

	@Override
	public String getStoryTitle() {
		return "Debug Scene " + index + (getTitle().isEmpty() ? "" : ": " + getTitle());
	}

	protected String getTitle() {
		return "";
	}

	static class CoordinateScene extends DebugScenes {

		public CoordinateScene(int index) {
			super(index);
		}

		@Override
		public void program(SceneBuilder scene, SceneBuildingUtil util) {
			scene.showBasePlate();
			scene.idle(10);
			scene.showSection(util.layersFrom(1), Direction.DOWN);
//			scene.showTargetedText(WHITE, new Vec3d(1.5, 1.5, 1.5), "coordinate", "Schematic orientation: ", 40);

			scene.idle(10);
			scene.showSelectionWithText(PonderPalette.RED, Select.fromTo(2, 1, 1, 4, 1, 1), "x", "Das X axis", 20);
			scene.idle(20);
			scene.showSelectionWithText(PonderPalette.GREEN, Select.fromTo(1, 2, 1, 1, 4, 1), "y", "Das Y axis", 20);
			scene.idle(20);
			scene.showSelectionWithText(PonderPalette.BLUE, Select.fromTo(1, 1, 2, 1, 1, 4), "z", "Das Z axis", 20);
			scene.idle(10);
		}

		@Override
		protected String getTitle() {
			return "Coordinate Space";
		}

	}

	static class BlocksScene extends DebugScenes {

		public BlocksScene(int index) {
			super(index);
		}

		@Override
		public void program(SceneBuilder scene, SceneBuildingUtil util) {
			scene.showBasePlate();
			scene.idle(10);
			scene.showSection(util.layersFrom(1), Direction.DOWN);
			scene.idle(10);
			scene.showText(WHITE, 10, "change_blocks", "Blocks can be modified", 1000);
			scene.idle(20);
			scene.replaceBlocks(Select.fromTo(1, 1, 2, 2, 2, 4), AllBlocks.REFINED_RADIANCE_CASING.getDefaultState());
			scene.idle(10);
			scene.replaceBlocks(Select.pos(3, 1, 1), Blocks.GOLD_BLOCK.getDefaultState());
			scene.markAsFinished();
		}

		@Override
		protected String getTitle() {
			return "Changing Blocks";
		}

	}

	static class FluidsScene extends DebugScenes {

		public FluidsScene(int index) {
			super(index);
		}

		@Override
		public void program(SceneBuilder scene, SceneBuildingUtil util) {
			scene.showBasePlate();
			scene.idle(10);
			scene.showSection(util.layersFrom(1), Direction.DOWN);
			scene.showTargetedText(WHITE, new Vec3d(1, 2.5, 4.5), "fluids", "Fluid rendering test.", 1000);
			scene.markAsFinished();
		}

		@Override
		protected String getTitle() {
			return "Showing Fluids";
		}

	}

	static class ParticlesScene extends DebugScenes {

		public ParticlesScene(int index) {
			super(index);
		}

		@Override
		public void program(SceneBuilder scene, SceneBuildingUtil util) {
			scene.showBasePlate();
			scene.idle(10);
			scene.showSection(util.layersFrom(1), Direction.DOWN);
			scene.idle(10);

			Vec3d emitterPos = util.vector(2.5, 2.25, 2.5);
			Emitter emitter = Emitter.simple(ParticleTypes.LAVA, util.vector(0, .1, 0));
			Emitter rotation =
				Emitter.simple(new RotationIndicatorParticleData(SpeedLevel.MEDIUM.getColor(), 12, 1, 1, 20, 'Y'),
					util.vector(0, .1, 0));

			scene.showTargetedText(WHITE, emitterPos, "incoming", "Incoming...", 20);
			scene.idle(30);
			scene.addInstruction(new EmitParticlesInstruction(emitterPos, emitter, 1, 60));
			scene.addInstruction(new EmitParticlesInstruction(emitterPos, rotation, 20, 1));
			scene.idle(30);
			scene.rotateCameraY(180);
		}

		@Override
		protected String getTitle() {
			return "Emitting particles";
		}

	}

	static class OffScreenScene extends DebugScenes {

		public OffScreenScene(int index) {
			super(index);
		}

		@Override
		public void program(SceneBuilder scene, SceneBuildingUtil util) {
			scene.configureBasePlate(1, 0, 6);
			scene.showBasePlate();
			Select out1 = Select.fromTo(7, 0, 0, 8, 0, 5);
			Select out2 = Select.fromTo(0, 0, 0, 0, 0, 5);
			scene.idle(10);
			scene.showSection(Select.compound(util.layersFrom(1), out1, out2), Direction.DOWN);
			scene.idle(10);

			scene.showSelectionWithText(PonderPalette.BLACK, out1, "outofbounds",
				"Blocks outside of the base plate do not affect scaling", 100);
			scene.showSelectionWithText(PonderPalette.BLACK, out2, "thanks_to_configureBasePlate",
				"configureBasePlate() makes sure of that.", 100);
			scene.markAsFinished();
		}

		@Override
		protected String getTitle() {
			return "Out of bounds / configureBasePlate";
		}

	}

}
