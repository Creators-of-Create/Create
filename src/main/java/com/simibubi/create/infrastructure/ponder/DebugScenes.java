package com.simibubi.create.infrastructure.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.RotationIndicatorParticleData;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.PonderStoryBoardEntry.PonderStoryBoard;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement.DancePose;
import com.simibubi.create.foundation.ponder.element.ParrotElement.FacePointOfInterestPose;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instruction.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.utility.Pointing;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DebugScenes {

	private static int index;

	public static void registerAll() {
		index = 1;
		add(DebugScenes::coordinateScene);
		add(DebugScenes::blocksScene);
		add(DebugScenes::fluidsScene);
		add(DebugScenes::offScreenScene);
		add(DebugScenes::particleScene);
		add(DebugScenes::controlsScene);
		add(DebugScenes::birbScene);
		add(DebugScenes::sectionsScene);
		add(DebugScenes::itemScene);
		add(DebugScenes::pipeScene);
	}

	private static void add(PonderStoryBoard sb) {
		ItemEntry<Item> item = AllItems.BRASS_HAND;
		String schematicPath = "debug/scene_" + index;
		PonderIndex.HELPER.addStoryBoard(item, schematicPath, sb)
			.highlightAllTags()
			.chapter(PonderIndex.HELPER.getOrCreateChapter("debug"));
		index++;
	}

	public static void empty(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("debug_empty", "Missing Content");
		scene.showBasePlate();
		scene.idle(5);
	}

	public static void coordinateScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("debug_coords", "Coordinate Space");
		scene.showBasePlate();
		scene.idle(10);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);

		Selection xAxis = util.select.fromTo(2, 1, 1, 4, 1, 1);
		Selection yAxis = util.select.fromTo(1, 2, 1, 1, 4, 1);
		Selection zAxis = util.select.fromTo(1, 1, 2, 1, 1, 4);

		scene.idle(10);
		scene.overlay.showSelectionWithText(xAxis, 20)
			.colored(PonderPalette.RED)
			.text("Das X axis");
		scene.idle(20);
		scene.overlay.showSelectionWithText(yAxis, 20)
			.colored(PonderPalette.GREEN)
			.text("Das Y axis");
		scene.idle(20);
		scene.overlay.showSelectionWithText(zAxis, 20)
			.colored(PonderPalette.BLUE)
			.text("Das Z axis");
	}

	public static void blocksScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("debug_blocks", "Changing Blocks");
		scene.showBasePlate();
		scene.idle(10);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		scene.idle(10);
		scene.overlay.showText(1000)
			.independent(10)
			.text("Blocks can be modified");
		scene.idle(20);
		scene.world.replaceBlocks(util.select.fromTo(1, 1, 3, 2, 2, 4),
			AllBlocks.REFINED_RADIANCE_CASING.getDefaultState(), true);
		scene.idle(10);
		scene.world.replaceBlocks(util.select.position(3, 1, 1), Blocks.GOLD_BLOCK.defaultBlockState(), true);
		scene.rotateCameraY(180);
		scene.markAsFinished();
	}

	public static void fluidsScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("debug_fluids", "Showing Fluids");
		scene.showBasePlate();
		scene.idle(10);
		Vec3 parrotPos = util.vector.topOf(1, 0, 1);
		scene.special.createBirb(parrotPos, FacePointOfInterestPose::new);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		scene.overlay.showText(1000)
			.text("Fluid rendering test.")
			.pointAt(new Vec3(1, 2.5, 4.5));
		scene.markAsFinished();

		Object outlineSlot = new Object();

		Vec3 vec1 = util.vector.topOf(1, 0, 0);
		Vec3 vec2 = util.vector.topOf(0, 0, 1);
		AABB boundingBox1 = new AABB(vec1, vec1).expandTowards(0, 2.5, 0)
			.inflate(.15, 0, .15);
		AABB boundingBox2 = new AABB(vec2, vec2).expandTowards(0, .125, 0)
			.inflate(.45, 0, .45);
		Vec3 poi1 = boundingBox1.getCenter();
		Vec3 poi2 = boundingBox2.getCenter();

		for (int i = 0; i < 10; i++) {
			scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, outlineSlot,
				i % 2 == 0 ? boundingBox1 : boundingBox2, 15);
			scene.idle(3);
			scene.special.movePointOfInterest(i % 2 == 0 ? poi1 : poi2);
			scene.idle(12);
		}

		scene.idle(12);
		scene.special.movePointOfInterest(util.grid.at(-4, 5, 4));
		scene.overlay.showText(40)
			.colored(PonderPalette.RED)
			.text("wut?")
			.pointAt(parrotPos.add(-.25f, 0.25f, .25f));

	}

	public static void offScreenScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("debug_baseplate", "Out of bounds / configureBasePlate");
		scene.configureBasePlate(1, 0, 6);
		scene.showBasePlate();

		Selection out1 = util.select.fromTo(7, 0, 0, 8, 0, 5);
		Selection out2 = util.select.fromTo(0, 0, 0, 0, 0, 5);
		Selection blocksExceptBasePlate = util.select.layersFrom(1)
			.add(out1)
			.add(out2);

		scene.idle(10);
		scene.world.showSection(blocksExceptBasePlate, Direction.DOWN);
		scene.idle(10);

		scene.overlay.showSelectionWithText(out1, 100)
			.colored(PonderPalette.BLACK)
			.text("Blocks outside of the base plate do not affect scaling");
		scene.overlay.showSelectionWithText(out2, 100)
			.colored(PonderPalette.BLACK)
			.text("configureBasePlate() makes sure of that.");
		scene.markAsFinished();
	}

	public static void particleScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("debug_particles", "Emitting particles");
		scene.showBasePlate();
		scene.idle(10);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		scene.idle(10);

		Vec3 emitterPos = util.vector.of(2.5, 2.25, 2.5);
		Emitter emitter = Emitter.simple(ParticleTypes.LAVA, util.vector.of(0, .1, 0));
		Emitter rotation =
			Emitter.simple(new RotationIndicatorParticleData(SpeedLevel.MEDIUM.getColor(), 12, 1, 1, 20, 'Y'),
				util.vector.of(0, .1, 0));

		scene.overlay.showText(20)
			.text("Incoming...")
			.pointAt(emitterPos);
		scene.idle(30);
		scene.effects.emitParticles(emitterPos, emitter, 1, 60);
		scene.effects.emitParticles(emitterPos, rotation, 20, 1);
		scene.idle(30);
		scene.rotateCameraY(180);
	}

	public static void controlsScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("debug_controls", "Basic player interaction");
		scene.showBasePlate();
		scene.idle(10);
		scene.world.showSection(util.select.layer(1), Direction.DOWN);
		scene.idle(4);
		scene.world.showSection(util.select.layer(2), Direction.DOWN);
		scene.idle(4);
		scene.world.showSection(util.select.layer(3), Direction.DOWN);
		scene.idle(10);

		BlockPos shaftPos = util.grid.at(3, 1, 1);
		Selection shaftSelection = util.select.position(shaftPos);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(shaftPos), Pointing.DOWN).rightClick()
			.whileSneaking()
			.withWrench(), 40);
		scene.idle(20);
		scene.world.replaceBlocks(shaftSelection, AllBlocks.SHAFT.getDefaultState(), true);

		scene.idle(20);
		scene.world.hideSection(shaftSelection, Direction.UP);

		scene.idle(20);

		scene.overlay.showControls(new InputWindowElement(util.vector.of(1, 4.5, 3.5), Pointing.LEFT).rightClick()
			.withItem(new ItemStack(Blocks.POLISHED_ANDESITE)), 20);
		scene.world.showSection(util.select.layer(4), Direction.DOWN);

		scene.idle(40);

		BlockPos chassis = util.grid.at(1, 1, 3);
		Vec3 chassisSurface = util.vector.blockSurface(chassis, Direction.NORTH);

		Object chassisValueBoxHighlight = new Object();
		Object chassisEffectHighlight = new Object();

		AABB point = new AABB(chassisSurface, chassisSurface);
		AABB expanded = point.inflate(1 / 4f, 1 / 4f, 1 / 16f);

		Selection singleBlock = util.select.position(1, 2, 3);
		Selection twoBlocks = util.select.fromTo(1, 2, 3, 1, 3, 3);
		Selection threeBlocks = util.select.fromTo(1, 2, 3, 1, 4, 3);

		Selection singleRow = util.select.fromTo(1, 2, 3, 3, 2, 3);
		Selection twoRows = util.select.fromTo(1, 2, 3, 3, 3, 3);
		Selection threeRows = twoRows.copy()
			.add(threeBlocks);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, chassisValueBoxHighlight, point, 1);
		scene.idle(1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, chassisValueBoxHighlight, expanded, 120);
		scene.overlay.showControls(new InputWindowElement(chassisSurface, Pointing.UP).scroll()
			.withWrench(), 40);

		PonderPalette white = PonderPalette.WHITE;
		scene.overlay.showOutline(white, chassisEffectHighlight, singleBlock, 10);
		scene.idle(10);
		scene.overlay.showOutline(white, chassisEffectHighlight, twoBlocks, 10);
		scene.idle(10);
		scene.overlay.showOutline(white, chassisEffectHighlight, threeBlocks, 10);
		scene.idle(10);
		scene.overlay.showOutline(white, chassisEffectHighlight, twoBlocks, 10);
		scene.idle(10);
		scene.overlay.showOutline(white, chassisEffectHighlight, singleBlock, 10);
		scene.idle(10);

		scene.idle(30);
		scene.overlay.showControls(new InputWindowElement(chassisSurface, Pointing.UP).whileCTRL()
			.scroll()
			.withWrench(), 40);

		scene.overlay.showOutline(white, chassisEffectHighlight, singleRow, 10);
		scene.idle(10);
		scene.overlay.showOutline(white, chassisEffectHighlight, twoRows, 10);
		scene.idle(10);
		scene.overlay.showOutline(white, chassisEffectHighlight, threeRows, 10);
		scene.idle(10);
		scene.overlay.showOutline(white, chassisEffectHighlight, twoRows, 10);
		scene.idle(10);
		scene.overlay.showOutline(white, chassisEffectHighlight, singleRow, 10);
		scene.idle(10);

		scene.markAsFinished();
	}

	public static void birbScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("debug_birbs", "Birbs");
		scene.showBasePlate();
		scene.idle(10);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		scene.idle(10);

		BlockPos pos = new BlockPos(1, 2, 3);
		scene.special.birbOnSpinnyShaft(pos);
		scene.overlay.showText(100)
			.colored(PonderPalette.GREEN)
			.text("More birbs = More interesting")
			.pointAt(util.vector.topOf(pos));

		scene.idle(10);
		scene.special.createBirb(util.vector.topOf(0, 1, 2), DancePose::new);
		scene.idle(10);

		scene.special.createBirb(util.vector.centerOf(3, 1, 3)
			.add(0, 0.25f, 0), FacePointOfInterestPose::new);
		scene.idle(20);

		BlockPos poi1 = util.grid.at(4, 1, 0);
		BlockPos poi2 = util.grid.at(0, 1, 4);

		scene.world.setBlock(poi1, Blocks.GOLD_BLOCK.defaultBlockState(), true);
		scene.special.movePointOfInterest(poi1);
		scene.idle(20);

		scene.world.setBlock(poi2, Blocks.GOLD_BLOCK.defaultBlockState(), true);
		scene.special.movePointOfInterest(poi2);
		scene.overlay.showText(20)
			.text("Point of Interest")
			.pointAt(util.vector.centerOf(poi2));
		scene.idle(20);

		scene.world.destroyBlock(poi1);
		scene.special.movePointOfInterest(poi1);
		scene.idle(20);

		scene.world.destroyBlock(poi2);
		scene.special.movePointOfInterest(poi2);
	}

	public static void sectionsScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("debug_sections", "Sections");
		scene.showBasePlate();
		scene.idle(10);
		scene.rotateCameraY(95);

		BlockPos mergePos = util.grid.at(1, 1, 1);
		BlockPos independentPos = util.grid.at(3, 1, 1);
		Selection toMerge = util.select.position(mergePos);
		Selection independent = util.select.position(independentPos);
		Selection start = util.select.layersFrom(1)
			.substract(toMerge)
			.substract(independent);

		scene.world.showSection(start, Direction.DOWN);
		scene.idle(20);

		scene.world.showSection(toMerge, Direction.DOWN);
		ElementLink<WorldSectionElement> link = scene.world.showIndependentSection(independent, Direction.DOWN);

		scene.idle(20);

		scene.overlay.showText(40)
			.colored(PonderPalette.GREEN)
			.text("This Section got merged to base.")
			.pointAt(util.vector.topOf(mergePos));
		scene.idle(10);
		scene.overlay.showText(40)
			.colored(PonderPalette.RED)
			.text("This Section renders independently.")
			.pointAt(util.vector.topOf(independentPos));

		scene.idle(40);

		scene.world.hideIndependentSection(link, Direction.DOWN);
		scene.world.hideSection(util.select.fromTo(mergePos, util.grid.at(1, 1, 4)), Direction.DOWN);

		scene.idle(20);

		Selection hiddenReplaceArea = util.select.fromTo(2, 1, 2, 4, 1, 4)
			.substract(util.select.position(4, 1, 3))
			.substract(util.select.position(2, 1, 3));

		scene.world.hideSection(hiddenReplaceArea, Direction.UP);
		scene.idle(20);
		scene.world.setBlocks(hiddenReplaceArea, AllBlocks.REFINED_RADIANCE_CASING.getDefaultState(), false);
		scene.world.showSection(hiddenReplaceArea, Direction.DOWN);
		scene.idle(20);
		scene.overlay.showSelectionWithText(hiddenReplaceArea, 30)
			.colored(PonderPalette.BLUE)
			.text("Seamless substitution of blocks");

		scene.idle(40);

		ElementLink<WorldSectionElement> helicopter = scene.world.makeSectionIndependent(hiddenReplaceArea);
		scene.world.rotateSection(helicopter, 50, 5 * 360, 0, 60);
		scene.world.moveSection(helicopter, util.vector.of(0, 4, 5), 50);
		scene.overlay.showText(30)
			.colored(PonderPalette.BLUE)
			.text("Up, up and away.")
			.independent(30);

		scene.idle(40);
		scene.world.hideIndependentSection(helicopter, Direction.UP);

	}

	public static void itemScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("debug_items", "Manipulating Items");
		scene.configureBasePlate(0, 0, 6);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);

		ItemStack brassItem = AllItems.BRASS_INGOT.asStack();
		ItemStack copperItem = new ItemStack(Items.COPPER_INGOT);

		for (int z = 4; z >= 2; z--) {
			scene.world.createItemEntity(util.vector.centerOf(0, 4, z), Vec3.ZERO, brassItem.copy());
			scene.idle(10);
		}

		BlockPos beltPos = util.grid.at(2, 1, 3);
		ElementLink<BeltItemElement> itemOnBelt =
			scene.world.createItemOnBelt(beltPos, Direction.EAST, copperItem.copy());

		scene.idle(10);
		scene.world.stallBeltItem(itemOnBelt, true);
		scene.idle(5);
		scene.overlay.showText(40)
			.colored(PonderPalette.FAST)
			.text("Belt Items can only be force-stalled on the belt they were created on.")
			.pointAt(util.vector.topOf(2, 1, 2));
		scene.idle(45);
		scene.world.stallBeltItem(itemOnBelt, false);
		scene.idle(20);

		scene.world.modifyEntities(ItemEntity.class, entity -> {
			if (copperItem.sameItem(entity.getItem()))
				entity.setNoGravity(true);
		});

		scene.idle(20);

		scene.world.modifyEntities(ItemEntity.class, entity -> {
			if (brassItem.sameItem(entity.getItem()))
				entity.setDeltaMovement(util.vector.of(-.15f, .5f, 0));
		});

		scene.idle(27);

		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
	}
	
	public static void pipeScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("debug_pipes", "Manipulating Items");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
	
		
		
		
	}

}
