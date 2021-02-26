package com.simibubi.create.foundation.ponder.content;

import static com.simibubi.create.foundation.ponder.content.PonderPalette.WHITE;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.content.contraptions.particle.RotationIndicatorParticleData;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderStoryBoardEntry.PonderStoryBoard;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.BeltItemElement;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.utility.Pointing;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
	}

	private static void add(PonderStoryBoard sb) {
		ItemEntry<Item> item = AllItems.BRASS_HAND;
		String schematicPath = "debug/scene_" + index;
		PonderRegistry.addStoryBoard(item, schematicPath, sb);
		index++;
	}

	public static void coordinateScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Coordinate Space");
		scene.showBasePlate();
		scene.idle(10);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		
		Selection xAxis = util.select.fromTo(2, 1, 1, 4, 1, 1);
		Selection yAxis = util.select.fromTo(1, 2, 1, 1, 4, 1);
		Selection zAxis = util.select.fromTo(1, 1, 2, 1, 1, 4);

		scene.idle(10);
		scene.overlay.showSelectionWithText(PonderPalette.RED, xAxis, "x", "Das X axis", 20);
		scene.idle(20);
		scene.overlay.showSelectionWithText(PonderPalette.GREEN, yAxis, "y", "Das Y axis", 20);
		scene.idle(20);
		scene.overlay.showSelectionWithText(PonderPalette.BLUE, zAxis, "z", "Das Z axis", 20);
		scene.idle(10);
	}

	public static void blocksScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Changing Blocks");
		scene.showBasePlate();
		scene.idle(10);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		scene.idle(10);
		scene.overlay.showText(WHITE, 10, "change_blocks", "Blocks can be modified", 1000);
		scene.idle(20);
		scene.world.replaceBlocks(util.select.fromTo(1, 1, 3, 2, 2, 4),
			AllBlocks.REFINED_RADIANCE_CASING.getDefaultState(), true);
		scene.idle(10);
		scene.world.replaceBlocks(util.select.position(3, 1, 1), Blocks.GOLD_BLOCK.getDefaultState(), true);
		scene.rotateCameraY(180);
		scene.markAsFinished();
	}

	public static void fluidsScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Showing Fluids");
		scene.showBasePlate();
		scene.idle(10);
		Vec3d parrotPos = util.vector.topOf(1, 0, 1);
		scene.special.birbLookingAtPOI(parrotPos);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		scene.overlay.showTargetedText(WHITE, new Vec3d(1, 2.5, 4.5), "fluids", "Fluid rendering test.", 1000);
		scene.markAsFinished();

		Object outlineSlot = new Object();

		Vec3d vec1 = util.vector.topOf(1, 0, 0);
		Vec3d vec2 = util.vector.topOf(0, 0, 1);
		AxisAlignedBB boundingBox1 = new AxisAlignedBB(vec1, vec1).expand(0, 2.5, 0)
			.grow(.15, 0, .15);
		AxisAlignedBB boundingBox2 = new AxisAlignedBB(vec2, vec2).expand(0, .125, 0)
			.grow(.45, 0, .45);
		Vec3d poi1 = boundingBox1.getCenter();
		Vec3d poi2 = boundingBox2.getCenter();

		for (int i = 0; i < 10; i++) {
			scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, outlineSlot,
				i % 2 == 0 ? boundingBox1 : boundingBox2, 15);
			scene.idle(3);
			scene.special.movePointOfInterest(i % 2 == 0 ? poi1 : poi2);
			scene.idle(12);
		}

		scene.idle(12);
		scene.special.movePointOfInterest(util.grid.at(-4, 5, 4));
		scene.overlay.showTargetedText(PonderPalette.RED, parrotPos.add(-.25f, 0.25f, .25f), "wut", "wut?", 40);

	}

	public static void offScreenScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Out of bounds / configureBasePlate");
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

		scene.overlay.showSelectionWithText(PonderPalette.BLACK, out1, "outofbounds",
			"Blocks outside of the base plate do not affect scaling", 100);
		scene.overlay.showSelectionWithText(PonderPalette.BLACK, out2, "thanks_to_configureBasePlate",
			"configureBasePlate() makes sure of that.", 100);
		scene.markAsFinished();
	}

	public static void particleScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Emitting particles");
		scene.showBasePlate();
		scene.idle(10);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		scene.idle(10);

		Vec3d emitterPos = util.vector.of(2.5, 2.25, 2.5);
		Emitter emitter = Emitter.simple(ParticleTypes.LAVA, util.vector.of(0, .1, 0));
		Emitter rotation =
			Emitter.simple(new RotationIndicatorParticleData(SpeedLevel.MEDIUM.getColor(), 12, 1, 1, 20, 'Y'),
				util.vector.of(0, .1, 0));

		scene.overlay.showTargetedText(WHITE, emitterPos, "incoming", "Incoming...", 20);
		scene.idle(30);
		scene.effects.emitParticles(emitterPos, emitter, 1, 60);
		scene.effects.emitParticles(emitterPos, rotation, 20, 1);
		scene.idle(30);
		scene.rotateCameraY(180);
	}

	public static void controlsScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Basic player interaction");
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
		Vec3d chassisSurface = util.vector.blockSurface(chassis, Direction.NORTH);

		Object chassisValueBoxHighlight = new Object();
		Object chassisEffectHighlight = new Object();

		AxisAlignedBB point = new AxisAlignedBB(chassisSurface, chassisSurface);
		AxisAlignedBB expanded = point.grow(1 / 4f, 1 / 4f, 1 / 16f);

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
		scene.title("Birbs");
		scene.showBasePlate();
		scene.idle(10);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		scene.idle(10);

		BlockPos pos = new BlockPos(1, 2, 3);
		scene.special.birbOnSpinnyShaft(pos);
		scene.overlay.showTargetedText(PonderPalette.GREEN, util.vector.topOf(pos), "birbs_interesting",
			"More birbs = More interesting", 100);

		scene.idle(10);
		scene.special.birbPartying(util.vector.topOf(0, 1, 2));
		scene.idle(10);

		scene.special.birbLookingAtPOI(util.vector.centerOf(3, 1, 3)
			.add(0, 0.25f, 0));
		scene.idle(20);

		BlockPos poi1 = util.grid.at(4, 1, 0);
		BlockPos poi2 = util.grid.at(0, 1, 4);

		scene.world.setBlock(poi1, Blocks.GOLD_BLOCK.getDefaultState());
		scene.special.movePointOfInterest(poi1);
		scene.idle(20);

		scene.world.setBlock(poi2, Blocks.GOLD_BLOCK.getDefaultState());
		scene.special.movePointOfInterest(poi2);
		scene.overlay.showTargetedText(PonderPalette.FAST, util.vector.centerOf(poi2), "poi", "Point of Interest", 20);
		scene.idle(20);

		scene.world.setBlock(poi1, Blocks.AIR.getDefaultState());
		scene.special.movePointOfInterest(poi1);
		scene.idle(20);

		scene.world.setBlock(poi2, Blocks.AIR.getDefaultState());
		scene.special.movePointOfInterest(poi2);
	}

	public static void sectionsScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("Sections");
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

		scene.overlay.showTargetedText(PonderPalette.GREEN, util.vector.topOf(mergePos), "merged",
			"This Section got merged to base.", 40);
		scene.idle(10);
		scene.overlay.showTargetedText(PonderPalette.RED, util.vector.topOf(independentPos), "independent",
			"This Section renders independently.", 40);

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
		scene.overlay.showSelectionWithText(PonderPalette.BLUE, hiddenReplaceArea, "seamless",
			"Seamless substitution of blocks", 30);

		scene.idle(40);

		ElementLink<WorldSectionElement> helicopter = scene.world.makeSectionIndependent(hiddenReplaceArea);
		scene.world.rotateSection(helicopter, 50, 5 * 360, 0, 60);
		scene.world.moveSection(helicopter, util.vector.of(0, 4, 5), 50);
		scene.overlay.showText(PonderPalette.BLUE, 30, "blast_off", "Up, up and away.", 30);

		scene.idle(40);
		scene.world.hideIndependentSection(helicopter, Direction.UP);

	}

	public static void itemScene(SceneBuilder scene, SceneBuildingUtil util) {
		scene.configureBasePlate(0, 0, 6);
		scene.title("Manipulating Items");
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);

		ItemStack brassItem = AllItems.BRASS_INGOT.asStack();
		ItemStack copperItem = AllItems.COPPER_INGOT.asStack();

		for (int z = 4; z >= 2; z--) {
			scene.world.createItemEntity(util.vector.centerOf(0, 4, z), Vec3d.ZERO, brassItem.copy());
			scene.idle(10);
		}

		BlockPos beltPos = util.grid.at(2, 1, 3);
		ElementLink<BeltItemElement> itemOnBelt =
			scene.world.createItemOnBelt(beltPos, Direction.EAST, copperItem.copy());

		scene.idle(10);
		scene.world.stallBeltItem(itemOnBelt, true);
		scene.idle(5);
		scene.overlay.showTargetedText(PonderPalette.FAST, util.vector.topOf(2, 1, 2), "stalling",
			"Belt Items can only be force-stalled on the belt they were created on.", 40);
		scene.idle(45);
		scene.world.stallBeltItem(itemOnBelt, false);
		scene.idle(20);

		scene.world.modifyEntities(ItemEntity.class, entity -> {
			if (copperItem.isItemEqual(entity.getItem()))
				entity.setNoGravity(true);
		});

		scene.idle(20);

		scene.world.modifyEntities(ItemEntity.class, entity -> {
			if (brassItem.isItemEqual(entity.getItem()))
				entity.setMotion(util.vector.of(-.15f, .5f, 0));
		});

		scene.idle(27);

		scene.world.modifyEntities(ItemEntity.class, Entity::remove);
	}

}
