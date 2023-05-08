package com.simibubi.create.foundation.ponder.content;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.deployer.DeployerBlockEntity;
import com.simibubi.create.content.contraptions.components.millstone.MillstoneBlockEntity;
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour.Mode;
import com.simibubi.create.content.contraptions.processing.BasinBlock;
import com.simibubi.create.content.contraptions.processing.BasinBlockEntity;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.contraptions.processing.burner.LitBlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import com.simibubi.create.foundation.ponder.element.EntityElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instruction.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class ProcessingScenes {

	public static void millstone(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("millstone", "Processing Items in the Millstone");
		scene.configureBasePlate(0, 0, 5);

		Selection belt = util.select.fromTo(1, 1, 5, 0, 1, 2)
			.add(util.select.position(1, 2, 2));
		Selection beltCog = util.select.position(2, 0, 5);

		scene.world.showSection(util.select.layer(0)
			.substract(beltCog), Direction.UP);

		BlockPos millstone = util.grid.at(2, 2, 2);
		Selection millstoneSelect = util.select.position(2, 2, 2);
		Selection cogs = util.select.fromTo(3, 1, 2, 3, 2, 2);
		scene.world.setKineticSpeed(millstoneSelect, 0);

		scene.idle(5);
		scene.world.showSection(util.select.position(4, 1, 3), Direction.DOWN);
		scene.world.showSection(util.select.position(2, 1, 2), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.position(millstone), Direction.DOWN);
		scene.idle(10);
		Vec3 millstoneTop = util.vector.topOf(millstone);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("Millstones process items by grinding them")
			.pointAt(millstoneTop)
			.placeNearTarget();
		scene.idle(70);

		scene.world.showSection(cogs, Direction.DOWN);
		scene.idle(10);
		scene.world.setKineticSpeed(millstoneSelect, 32);
		scene.effects.indicateSuccess(millstone);
		scene.idle(10);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("They can be powered from the side using cogwheels")
			.pointAt(util.vector.topOf(millstone.east()))
			.placeNearTarget();
		scene.idle(70);

		ItemStack itemStack = new ItemStack(Items.WHEAT);
		Vec3 entitySpawn = util.vector.topOf(millstone.above(3));

		ElementLink<EntityElement> entity1 =
			scene.world.createItemEntity(entitySpawn, util.vector.of(0, 0.2, 0), itemStack);
		scene.idle(18);
		scene.world.modifyEntity(entity1, Entity::discard);
		scene.world.modifyBlockEntity(millstone, MillstoneBlockEntity.class,
			ms -> ms.inputInv.setStackInSlot(0, itemStack));
		scene.idle(10);
		scene.overlay.showControls(new InputWindowElement(millstoneTop, Pointing.DOWN).withItem(itemStack), 30);
		scene.idle(7);

		scene.overlay.showText(40)
			.attachKeyFrame()
			.text("Throw or Insert items at the top")
			.pointAt(millstoneTop)
			.placeNearTarget();
		scene.idle(60);

		scene.world.modifyBlockEntity(millstone, MillstoneBlockEntity.class,
			ms -> ms.inputInv.setStackInSlot(0, ItemStack.EMPTY));

		scene.overlay.showText(50)
			.text("After some time, the result can be obtained via Right-click")
			.pointAt(util.vector.blockSurface(millstone, Direction.WEST))
			.placeNearTarget();
		scene.idle(60);

		ItemStack flour = AllItems.WHEAT_FLOUR.asStack();
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(millstone, Direction.NORTH), Pointing.RIGHT).rightClick()
				.withItem(flour),
			40);
		scene.idle(50);

		scene.addKeyframe();
		scene.world.showSection(beltCog, Direction.UP);
		scene.world.showSection(belt, Direction.EAST);
		scene.idle(15);

		BlockPos beltPos = util.grid.at(1, 1, 2);
		scene.world.createItemOnBelt(beltPos, Direction.EAST, flour);
		scene.idle(15);
		scene.world.createItemOnBelt(beltPos, Direction.EAST, new ItemStack(Items.WHEAT_SEEDS));
		scene.idle(20);

		scene.overlay.showText(50)
			.text("The outputs can also be extracted by automation")
			.pointAt(util.vector.blockSurface(millstone, Direction.WEST)
				.add(-.5, .4, 0))
			.placeNearTarget();
		scene.idle(60);
	}

	public static void crushingWheels(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("crushing_wheels", "Processing Items with Crushing Wheels");
		scene.configureBasePlate(0, 0, 5);
		scene.scaleSceneView(.9f);

		Selection wheels = util.select.fromTo(3, 2, 2, 1, 2, 2);
		Selection kinetics = util.select.fromTo(0, 1, 5, 4, 1, 3);
		Selection kinetics2 = util.select.fromTo(0, 2, 5, 4, 2, 3);
		Selection beltCog = util.select.position(5, 0, 1);
		scene.world.setKineticSpeed(wheels, 0);
		scene.world.setBlock(util.grid.at(2, 3, 2), Blocks.AIR.defaultBlockState(), false);

		scene.world.showSection(util.select.layer(0)
			.substract(beltCog), Direction.UP);
		scene.idle(5);

		Selection belt = util.select.fromTo(4, 1, 2, 4, 4, 2)
			.add(util.select.fromTo(4, 3, 3, 4, 4, 3))
			.add(util.select.position(3, 3, 2))
			.add(util.select.position(2, 3, 2));
		Selection bottomBelt = util.select.fromTo(5, 1, 0, 2, 1, 0)
			.add(util.select.fromTo(2, 1, 2, 2, 1, 1));

		BlockPos center = util.grid.at(2, 2, 2);
		Selection wWheel = util.select.position(center.west());
		Selection eWheel = util.select.position(center.east());

		scene.world.showSection(wWheel, Direction.SOUTH);
		scene.idle(3);
		scene.world.showSection(eWheel, Direction.SOUTH);
		scene.idle(10);

		Vec3 centerTop = util.vector.topOf(center);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("A pair of Crushing Wheels can grind items very effectively")
			.pointAt(centerTop)
			.placeNearTarget();
		scene.idle(70);

		scene.world.showSection(kinetics, Direction.DOWN);
		scene.idle(3);
		scene.world.showSection(kinetics2, Direction.DOWN);
		scene.world.setKineticSpeed(wWheel, -16);
		scene.world.setKineticSpeed(eWheel, 16);
		scene.idle(5);
		scene.effects.rotationDirectionIndicator(center.west());
		scene.effects.rotationDirectionIndicator(center.east());
		scene.idle(10);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("Their Rotational Input has to make them spin into each other")
			.pointAt(util.vector.blockSurface(center.west(), Direction.NORTH))
			.placeNearTarget();
		scene.idle(40);
		scene.effects.rotationDirectionIndicator(center.west());
		scene.effects.rotationDirectionIndicator(center.east());
		scene.idle(30);

		ItemStack input = new ItemStack(Items.GOLD_ORE);
		ItemStack output = new ItemStack(Items.RAW_GOLD);
		Vec3 entitySpawn = util.vector.topOf(center.above(2));

		ElementLink<EntityElement> entity1 =
			scene.world.createItemEntity(entitySpawn, util.vector.of(0, 0.2, 0), input);
		scene.idle(18);
		scene.world.modifyEntity(entity1, Entity::discard);
		Emitter blockSpace =
			Emitter.withinBlockSpace(new ItemParticleOption(ParticleTypes.ITEM, input), util.vector.of(0, 0, 0));
		scene.effects.emitParticles(util.vector.centerOf(center)
			.add(0, -0.2, 0), blockSpace, 3, 40);
		scene.idle(10);
		scene.overlay.showControls(new InputWindowElement(centerTop, Pointing.DOWN).withItem(input), 30);
		scene.idle(7);

		scene.overlay.showText(50)
			.attachKeyFrame()
			.text("Items thrown or inserted into the top will get processed")
			.pointAt(centerTop)
			.placeNearTarget();
		scene.idle(60);

		scene.world.createItemEntity(centerTop.add(0, -1.4, 0), util.vector.of(0, 0, 0), output);
		scene.idle(10);
		scene.world.createItemEntity(centerTop.add(0, -1.4, 0), util.vector.of(0, 0, 0), output);
		scene.overlay.showControls(new InputWindowElement(centerTop.add(0, -2, 0), Pointing.UP).withItem(output), 30);
		scene.idle(40);

		scene.world.restoreBlocks(util.select.position(2, 3, 2));
		scene.world.showSection(belt, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(beltCog, Direction.UP);
		scene.idle(5);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		scene.world.showSection(bottomBelt, Direction.SOUTH);
		scene.idle(5);

		scene.overlay.showText(50)
			.attachKeyFrame()
			.text("Items can be inserted and picked up through automated means as well")
			.pointAt(centerTop.add(0, .5, 0))
			.placeNearTarget();
		scene.idle(40);

		for (int i = 0; i < 5; i++) {
			if (i < 4)
				scene.world.createItemOnBelt(util.grid.at(4, 4, 2), Direction.EAST, input);
			scene.idle(15);
			if (i < 3)
				scene.world.createItemOnBelt(util.grid.at(4, 4, 2), Direction.EAST, input);
			scene.idle(15);
			if (i > 0) {
				scene.world.createItemOnBelt(center.below(), Direction.UP, output);
				scene.idle(15);
				scene.world.createItemOnBelt(center.below(), Direction.UP, output);
			}
			scene.world.removeItemsFromBelt(util.grid.at(3, 3, 2));
			if (i < 4)
				scene.effects.emitParticles(util.vector.centerOf(center)
					.add(0, -0.2, 0), blockSpace, 3, 28);
			if (i == 0)
				scene.markAsFinished();
		}
	}

	public static void pressing(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_press", "Processing Items with the Mechanical Press");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		ElementLink<WorldSectionElement> depot =
			scene.world.showIndependentSection(util.select.position(2, 1, 1), Direction.DOWN);
		scene.world.moveSection(depot, util.vector.of(0, 0, 1), 0);
		scene.idle(10);

		Selection pressS = util.select.position(2, 3, 2);
		BlockPos pressPos = util.grid.at(2, 3, 2);
		BlockPos depotPos = util.grid.at(2, 1, 1);
		scene.world.setKineticSpeed(pressS, 0);
		scene.world.showSection(pressS, Direction.DOWN);
		scene.idle(10);

		scene.world.showSection(util.select.fromTo(2, 1, 3, 2, 1, 5), Direction.NORTH);
		scene.idle(3);
		scene.world.showSection(util.select.position(2, 2, 3), Direction.SOUTH);
		scene.idle(3);
		scene.world.showSection(util.select.position(2, 3, 3), Direction.NORTH);
		scene.world.setKineticSpeed(pressS, -32);
		scene.effects.indicateSuccess(pressPos);
		scene.idle(10);

		Vec3 pressSide = util.vector.blockSurface(pressPos, Direction.WEST);
		scene.overlay.showText(60)
			.pointAt(pressSide)
			.placeNearTarget()
			.attachKeyFrame()
			.text("The Mechanical Press can process items provided beneath it");
		scene.idle(70);
		scene.overlay.showText(60)
			.pointAt(pressSide.subtract(0, 2, 0))
			.placeNearTarget()
			.text("The Input items can be dropped or placed on a Depot under the Press");
		scene.idle(50);
		ItemStack copper = new ItemStack(Items.COPPER_INGOT);
		scene.world.createItemOnBeltLike(depotPos, Direction.NORTH, copper);
		Vec3 depotCenter = util.vector.centerOf(depotPos.south());
		scene.overlay.showControls(new InputWindowElement(depotCenter, Pointing.UP).withItem(copper), 30);
		scene.idle(10);

		Class<MechanicalPressBlockEntity> type = MechanicalPressBlockEntity.class;
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.start(Mode.BELT));
		scene.idle(30);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.makePressingParticleEffect(depotCenter.add(0, 8 / 16f, 0), copper));
		scene.world.removeItemsFromBelt(depotPos);
		ItemStack sheet = AllItems.COPPER_SHEET.asStack();
		scene.world.createItemOnBeltLike(depotPos, Direction.UP, sheet);
		scene.idle(10);
		scene.overlay.showControls(new InputWindowElement(depotCenter, Pointing.UP).withItem(sheet), 50);
		scene.idle(60);

		scene.world.hideIndependentSection(depot, Direction.NORTH);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(0, 1, 3, 0, 2, 3), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.fromTo(4, 1, 2, 0, 2, 2), Direction.SOUTH);
		scene.idle(20);
		BlockPos beltPos = util.grid.at(0, 1, 2);
		scene.overlay.showText(40)
			.pointAt(util.vector.blockSurface(beltPos, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("When items are provided on a belt...");
		scene.idle(30);

		ElementLink<BeltItemElement> ingot = scene.world.createItemOnBelt(beltPos, Direction.SOUTH, copper);
		scene.idle(15);
		ElementLink<BeltItemElement> ingot2 = scene.world.createItemOnBelt(beltPos, Direction.SOUTH, copper);
		scene.idle(15);
		scene.world.stallBeltItem(ingot, true);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.start(Mode.BELT));

		scene.overlay.showText(50)
			.pointAt(pressSide)
			.placeNearTarget()
			.attachKeyFrame()
			.text("The Press will hold and process them automatically");

		scene.idle(30);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.makePressingParticleEffect(depotCenter.add(0, 8 / 16f, 0), copper));
		scene.world.removeItemsFromBelt(pressPos.below(2));
		ingot = scene.world.createItemOnBelt(pressPos.below(2), Direction.UP, sheet);
		scene.world.stallBeltItem(ingot, true);
		scene.idle(15);
		scene.world.stallBeltItem(ingot, false);
		scene.idle(15);
		scene.world.stallBeltItem(ingot2, true);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.start(Mode.BELT));
		scene.idle(30);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.makePressingParticleEffect(depotCenter.add(0, 8 / 16f, 0), copper));
		scene.world.removeItemsFromBelt(pressPos.below(2));
		ingot2 = scene.world.createItemOnBelt(pressPos.below(2), Direction.UP, sheet);
		scene.world.stallBeltItem(ingot2, true);
		scene.idle(15);
		scene.world.stallBeltItem(ingot2, false);

	}

	public static void mixing(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_mixer", "Processing Items with the Mechanical Mixer");
		scene.configureBasePlate(0, 0, 5);
		scene.world.setBlock(util.grid.at(1, 1, 2), AllBlocks.ANDESITE_CASING.getDefaultState(), false);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(1, 4, 3, 1, 1, 5), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(1, 1, 2), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(1, 2, 2), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(1, 4, 2), Direction.SOUTH);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(3, 1, 1, 1, 1, 1), Direction.SOUTH);
		scene.world.showSection(util.select.fromTo(3, 1, 5, 3, 1, 2), Direction.SOUTH);
		scene.idle(20);

		BlockPos basin = util.grid.at(1, 2, 2);
		BlockPos pressPos = util.grid.at(1, 4, 2);
		Vec3 basinSide = util.vector.blockSurface(basin, Direction.WEST);

		ItemStack blue = new ItemStack(Items.BLUE_DYE);
		ItemStack red = new ItemStack(Items.RED_DYE);
		ItemStack purple = new ItemStack(Items.PURPLE_DYE);

		scene.overlay.showText(60)
			.pointAt(basinSide)
			.placeNearTarget()
			.attachKeyFrame()
			.text("With a Mixer and Basin, some Crafting Recipes can be automated");
		scene.idle(40);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(basin), Pointing.LEFT).withItem(blue), 30);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(basin), Pointing.RIGHT).withItem(red), 30);
		scene.idle(30);
		Class<MechanicalMixerBlockEntity> type = MechanicalMixerBlockEntity.class;
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.startProcessingBasin());
		scene.world.createItemOnBeltLike(basin, Direction.UP, red);
		scene.world.createItemOnBeltLike(basin, Direction.UP, blue);
		scene.idle(80);
		scene.world.modifyBlockEntityNBT(util.select.position(basin), BasinBlockEntity.class, nbt -> {
			nbt.put("VisualizedItems",
				NBTHelper.writeCompoundList(ImmutableList.of(IntAttached.with(1, purple)), ia -> ia.getValue()
					.serializeNBT()));
		});
		scene.idle(4);
		scene.world.createItemOnBelt(util.grid.at(1, 1, 1), Direction.UP, purple);
		scene.idle(30);

		scene.overlay.showText(80)
			.pointAt(basinSide)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Available recipes include any Shapeless Crafting Recipe, plus a couple extra ones");
		scene.idle(80);

		scene.rotateCameraY(-30);
		scene.idle(10);
		scene.world.setBlock(util.grid.at(1, 1, 2), AllBlocks.BLAZE_BURNER.getDefaultState()
			.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED), true);
		scene.idle(10);

		scene.overlay.showText(80)
			.pointAt(basinSide.subtract(0, 1, 0))
			.placeNearTarget()
			.text("Some of those recipes may require the heat of a Blaze Burner");
		scene.idle(40);

		scene.rotateCameraY(30);

		scene.idle(60);
		Vec3 filterPos = util.vector.of(1, 2.75f, 2.5f);
		scene.overlay.showFilterSlotInput(filterPos, Direction.WEST, 100);
		scene.overlay.showText(100)
			.pointAt(filterPos)
			.placeNearTarget()
			.attachKeyFrame()
			.text("The filter slot can be used in case two recipes are conflicting.");
		scene.idle(80);
	}

	public static void compacting(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_press_compacting", "Compacting items with the Mechanical Press");
		scene.configureBasePlate(0, 0, 5);
		scene.world.setBlock(util.grid.at(1, 1, 2), AllBlocks.ANDESITE_CASING.getDefaultState(), false);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(1, 4, 3, 1, 1, 5), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(1, 1, 2), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(1, 2, 2), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(1, 4, 2), Direction.SOUTH);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(3, 1, 1, 1, 1, 1), Direction.SOUTH);
		scene.world.showSection(util.select.fromTo(3, 1, 5, 3, 1, 2), Direction.SOUTH);
		scene.idle(20);

		BlockPos basin = util.grid.at(1, 2, 2);
		BlockPos pressPos = util.grid.at(1, 4, 2);
		Vec3 basinSide = util.vector.blockSurface(basin, Direction.WEST);

		ItemStack copper = new ItemStack(Items.COPPER_INGOT);
		ItemStack copperBlock = new ItemStack(Items.COPPER_BLOCK);

		scene.overlay.showText(60)
			.pointAt(basinSide)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Pressing items held in a Basin will cause them to be Compacted");
		scene.idle(40);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(basin), Pointing.DOWN).withItem(copper),
			30);
		scene.idle(30);
		Class<MechanicalPressBlockEntity> type = MechanicalPressBlockEntity.class;
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.start(Mode.BASIN));
		scene.idle(30);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.makeCompactingParticleEffect(util.vector.centerOf(basin), copper));
		scene.world.modifyBlockEntityNBT(util.select.position(basin), BasinBlockEntity.class, nbt -> {
			nbt.put("VisualizedItems",
				NBTHelper.writeCompoundList(ImmutableList.of(IntAttached.with(1, copperBlock)), ia -> ia.getValue()
					.serializeNBT()));
		});
		scene.idle(4);
		scene.world.createItemOnBelt(util.grid.at(1, 1, 1), Direction.UP, copperBlock);
		scene.idle(30);

		scene.overlay.showText(80)
			.pointAt(basinSide)
			.placeNearTarget()
			.attachKeyFrame()
			.text("Compacting includes any filled 2x2 or 3x3 Crafting Recipe, plus a couple extra ones");

		scene.idle(30);
		ItemStack log = new ItemStack(Items.OAK_LOG);
		ItemStack bark = new ItemStack(Items.OAK_WOOD);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(basin), Pointing.DOWN).withItem(log), 30);
		scene.idle(30);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.start(Mode.BASIN));
		scene.idle(30);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.makeCompactingParticleEffect(util.vector.centerOf(basin), log));
		scene.world.modifyBlockEntityNBT(util.select.position(basin), BasinBlockEntity.class, nbt -> {
			nbt.put("VisualizedItems",
				NBTHelper.writeCompoundList(ImmutableList.of(IntAttached.with(1, bark)), ia -> ia.getValue()
					.serializeNBT()));
		});
		scene.idle(4);
		scene.world.createItemOnBelt(util.grid.at(1, 1, 1), Direction.UP, bark);
		scene.idle(30);

		scene.rotateCameraY(-30);
		scene.idle(10);
		scene.world.setBlock(util.grid.at(1, 1, 2), AllBlocks.BLAZE_BURNER.getDefaultState()
			.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED), true);
		scene.idle(10);

		scene.overlay.showText(80)
			.pointAt(basinSide.subtract(0, 1, 0))
			.placeNearTarget()
			.text("Some of those recipes may require the heat of a Blaze Burner");
		scene.idle(40);

		scene.rotateCameraY(30);

		scene.idle(60);
		Vec3 filterPos = util.vector.of(1, 2.75f, 2.5f);
		scene.overlay.showFilterSlotInput(filterPos, Direction.WEST, 100);
		scene.overlay.showText(100)
			.pointAt(filterPos)
			.placeNearTarget()
			.attachKeyFrame()
			.text("The filter slot can be used in case two recipes are conflicting.");
		scene.idle(80);
	}

	public static void emptyBlazeBurner(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("empty_blaze_burner", "Using Empty Blaze Burners");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(10);
		BlockPos center = util.grid.at(2, 0, 2);

		scene.world.createEntity(w -> {
			Blaze blazeEntity = EntityType.BLAZE.create(w);
			Vec3 v = util.vector.topOf(center);
			blazeEntity.setPosRaw(v.x, v.y, v.z);
			blazeEntity.setYRot(blazeEntity.yRotO = 180);
			return blazeEntity;
		});

		scene.idle(20);
		scene.overlay
			.showControls(new InputWindowElement(util.vector.centerOf(center.above(2)), Pointing.DOWN).rightClick()
				.withItem(AllItems.EMPTY_BLAZE_BURNER.asStack()), 40);
		scene.idle(10);
		scene.overlay.showText(60)
			.text("Right-click a Blaze with the empty burner to capture it")
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(center.above(2), Direction.WEST))
			.placeNearTarget();
		scene.idle(50);

		scene.world.modifyEntities(Blaze.class, Entity::discard);
		scene.idle(20);

		scene.world.showSection(util.select.position(2, 1, 2), Direction.DOWN);
		scene.idle(20);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(center.above()), Pointing.DOWN).rightClick()
			.withItem(AllItems.EMPTY_BLAZE_BURNER.asStack()), 40);
		scene.idle(10);
		scene.overlay.showText(60)
			.text("Alternatively, Blazes can be collected from their Spawners directly")
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(center.above(), Direction.WEST))
			.placeNearTarget();
		scene.idle(50);
		scene.world.hideSection(util.select.position(2, 1, 2), Direction.UP);
		scene.idle(20);
		scene.world.showSection(util.select.position(1, 1, 2), Direction.DOWN);
		scene.idle(20);

		scene.world.modifyBlock(util.grid.at(1, 1, 2), s -> s.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED),
			false);
		scene.overlay.showText(70)
			.text("You now have an ideal heat source for various machines")
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(center.west()
				.above(), Direction.WEST))
			.placeNearTarget();
		scene.idle(80);

		scene.world.showSection(util.select.position(3, 1, 2), Direction.DOWN);
		scene.idle(20);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(center.east()
			.above()), Pointing.DOWN).rightClick()
				.withItem(new ItemStack(Items.FLINT_AND_STEEL)),
			40);
		scene.idle(7);
		scene.world.setBlock(util.grid.at(3, 1, 2), AllBlocks.LIT_BLAZE_BURNER.getDefaultState(), false);
		scene.idle(10);
		scene.overlay.showText(70)
			.text("For Aesthetic purposes, Empty Blaze Burners can also be lit using Flint and Steel")
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(center.east()
				.above(), Direction.UP))
			.placeNearTarget();
		scene.idle(80);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(center.east()
			.above()), Pointing.DOWN).rightClick()
				.withItem(new ItemStack(Items.SOUL_SAND)),
			40);
		scene.idle(7);
		scene.world.modifyBlock(util.grid.at(3, 1, 2),
			s -> s.setValue(LitBlazeBurnerBlock.FLAME_TYPE, LitBlazeBurnerBlock.FlameType.SOUL), false);
		scene.overlay.showText(60)
			.text("The flame can be transformed using a soul-infused item")
			.pointAt(util.vector.blockSurface(center.east()
				.above(), Direction.UP))
			.placeNearTarget();
		scene.idle(80);
		scene.overlay.showText(90)
			.colored(PonderPalette.RED)
			.text("However, without a blaze they are not suitable for industrial heating")
			.pointAt(util.vector.blockSurface(center.east()
				.above(), Direction.UP))
			.placeNearTarget();
		scene.idle(70);
	}

	public static void blazeBurner(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("blaze_burner", "Feeding Blaze Burners");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(10);

		BlockPos burner = util.grid.at(2, 1, 2);
		scene.world.showSection(util.select.position(burner), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.position(burner.above()), Direction.DOWN);
		scene.idle(10);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.text("Blaze Burners can provide Heat to Items processed in a Basin")
			.pointAt(util.vector.blockSurface(burner, Direction.WEST))
			.placeNearTarget();
		scene.idle(80);

		scene.world.hideSection(util.select.position(burner.above()), Direction.UP);
		scene.idle(20);
		scene.world.setBlock(burner.above(), Blocks.AIR.defaultBlockState(), false);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(burner), Pointing.DOWN).rightClick()
			.withItem(new ItemStack(Items.OAK_PLANKS)), 15);
		scene.idle(7);
		scene.world.modifyBlock(burner, s -> s.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED), false);
		scene.idle(20);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.text("For this, the Blaze has to be fed with flammable items")
			.pointAt(util.vector.blockSurface(burner, Direction.WEST))
			.placeNearTarget();
		scene.idle(80);

		scene.idle(20);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(burner), Pointing.DOWN).rightClick()
			.withItem(AllItems.BLAZE_CAKE.asStack()), 30);
		scene.idle(7);
		scene.world.modifyBlock(burner, s -> s.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.SEETHING), false);
		scene.idle(20);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.colored(PonderPalette.MEDIUM)
			.text("With a Blaze Cake, the Burner can reach an even stronger level of heat")
			.pointAt(util.vector.blockSurface(burner, Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		Class<DeployerBlockEntity> teType = DeployerBlockEntity.class;
		scene.world.modifyBlockEntityNBT(util.select.position(4, 1, 2), teType,
			nbt -> nbt.put("HeldItem", AllItems.BLAZE_CAKE.asStack()
				.serializeNBT()));

		scene.world.showSection(util.select.fromTo(3, 0, 5, 2, 0, 5), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(4, 1, 2, 4, 1, 5), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(2, 1, 4, 2, 1, 5), Direction.DOWN);
		scene.idle(10);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("The feeding process can be automated using Deployers or Mechanical Arms")
			.pointAt(util.vector.blockSurface(burner.east(2), Direction.UP));
		scene.idle(90);
	}

	public static void basin(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("basin", "Processing Items in the Basin");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		scene.world.showSection(util.select.position(1, 1, 2), Direction.DOWN);
		scene.idle(10);
		BlockPos basinPos = util.grid.at(1, 2, 2);
		scene.world.modifyBlock(basinPos, s -> s.setValue(BasinBlock.FACING, Direction.DOWN), false);
		scene.world.showSection(util.select.position(basinPos), Direction.DOWN);
		scene.idle(10);
		Vec3 basinSide = util.vector.blockSurface(basinPos, Direction.WEST);
		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("A Basin can hold Items and Fluids for Processing")
			.pointAt(basinSide)
			.placeNearTarget();
		scene.idle(10);

		ItemStack stack = new ItemStack(Items.BRICK);
		for (int i = 0; i < 4; i++) {
			scene.world.createItemEntity(util.vector.centerOf(basinPos.above(3)), util.vector.of(0, 0, 0), stack);
			scene.idle(10);
		}
		scene.idle(10);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(basinPos), Pointing.DOWN).withItem(stack),
			30);
		scene.idle(30);

		for (Direction d : Iterate.horizontalDirections) {
			scene.overlay.showOutline(PonderPalette.GREEN, new Object(), util.select.position(basinPos.below()
				.relative(d)), 60);
			scene.idle(4);
		}

		scene.overlay.showText(80)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("After a processing step, basins try to output below to the side of them")
			.pointAt(basinSide)
			.placeNearTarget();
		scene.idle(90);

		ElementLink<WorldSectionElement> depot =
			scene.world.showIndependentSection(util.select.position(3, 1, 1), Direction.EAST);
		scene.world.moveSection(depot, util.vector.of(-2, 0, 0), 0);
		scene.idle(10);
		scene.world.modifyBlock(basinPos, s -> s.setValue(BasinBlock.FACING, Direction.NORTH), false);
		scene.idle(10);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("When a valid component is present, the Basin will show an output faucet")
			.pointAt(basinSide.add(0.15, 0, -0.5))
			.placeNearTarget();
		scene.idle(90);

		scene.world.hideIndependentSection(depot, Direction.EAST);
		scene.idle(15);
		depot = scene.world.showIndependentSection(util.select.position(0, 1, 1), Direction.EAST);
		scene.world.moveSection(depot, util.vector.of(1, 0, 0), 0);
		scene.idle(20);
		scene.world.hideIndependentSection(depot, Direction.EAST);

		scene.overlay.showText(80)
			.text("A number of options are applicable here")
			.pointAt(util.vector.centerOf(util.grid.at(1, 1, 1)))
			.placeNearTarget();

		scene.idle(15);
		depot = scene.world.showIndependentSection(util.select.position(1, 1, 0), Direction.EAST);
		scene.world.moveSection(depot, util.vector.of(0, 0, 1), 0);
		scene.idle(20);
		scene.world.hideIndependentSection(depot, Direction.EAST);
		scene.idle(15);
		depot = scene.world.showIndependentSection(util.select.position(1, 1, 1), Direction.EAST);
		scene.idle(20);
		scene.world.hideIndependentSection(depot, Direction.EAST);
		scene.idle(15);
		depot = scene.world.showIndependentSection(util.select.fromTo(3, 1, 0, 2, 1, 0), Direction.EAST);
		scene.world.moveSection(depot, util.vector.of(-2, 0, 1), 0);
		scene.idle(20);
		scene.world.hideIndependentSection(depot, Direction.EAST);
		scene.idle(15);
		depot = scene.world.showIndependentSection(util.select.position(2, 1, 1), Direction.EAST);
		scene.world.moveSection(depot, util.vector.of(-1, 0, 0), 0);

		scene.idle(25);

		BlockPos pressPos = util.grid.at(1, 4, 2);
		scene.world.showSection(util.select.position(pressPos), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(1, 4, 3, 1, 1, 5), Direction.NORTH);
		scene.idle(10);

		Class<MechanicalPressBlockEntity> type = MechanicalPressBlockEntity.class;
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.start(Mode.BASIN));
		scene.idle(30);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.makeCompactingParticleEffect(util.vector.centerOf(basinPos), stack));
		scene.world.modifyBlockEntityNBT(util.select.position(basinPos), BasinBlockEntity.class, nbt -> {
			nbt.put("VisualizedItems",
				NBTHelper.writeCompoundList(ImmutableList.of(IntAttached.with(1, new ItemStack(Blocks.BRICKS))),
					ia -> ia.getValue()
						.serializeNBT()));
		});
		scene.idle(4);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(basinPos.below()
			.north()), Pointing.RIGHT).withItem(new ItemStack(Items.BRICKS)), 30);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("Outputs will be caught by the inventory below")
			.pointAt(basinSide.add(0, -1, -1))
			.placeNearTarget();
		scene.idle(70);

		scene.world.hideIndependentSection(depot, Direction.NORTH);
		scene.idle(10);
		scene.world.modifyBlock(basinPos, s -> s.setValue(BasinBlock.FACING, Direction.DOWN), false);
		scene.idle(20);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("Without output faucet, the Basin will retain items created in its processing")
			.pointAt(basinSide)
			.placeNearTarget();
		scene.idle(50);

		ItemStack nugget = AllItems.COPPER_NUGGET.asStack();
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(basinPos), Pointing.RIGHT).withItem(nugget),
			30);
		scene.idle(30);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.start(Mode.BASIN));
		scene.idle(30);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.makeCompactingParticleEffect(util.vector.centerOf(basinPos), nugget));

		ItemStack ingot = new ItemStack(Items.COPPER_INGOT);
		scene.idle(30);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(basinPos), Pointing.RIGHT).withItem(ingot),
			30);
		scene.idle(30);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.start(Mode.BASIN));
		scene.idle(30);
		scene.world.modifyBlockEntity(pressPos, type, pte -> pte.getPressingBehaviour()
			.makeCompactingParticleEffect(util.vector.centerOf(basinPos), ingot));

		ItemStack block = new ItemStack(Items.COPPER_BLOCK);
		scene.idle(30);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(basinPos), Pointing.RIGHT).withItem(block),
			30);
		scene.overlay.showText(70)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("This can be useful if outputs should be re-used as ingredients")
			.pointAt(basinSide)
			.placeNearTarget();
		scene.idle(80);

		scene.world.showSection(util.select.fromTo(2, 2, 5, 4, 1, 2), Direction.DOWN);
		scene.rotateCameraY(70);
		scene.world.createItemOnBelt(util.grid.at(2, 1, 2), Direction.WEST, block);
		scene.idle(40);
		scene.overlay.showText(70)
			.text("Desired outputs will then have to be extracted from the basin")
			.pointAt(util.vector.topOf(util.grid.at(3, 1, 2))
				.subtract(0, 3 / 16f, 0))
			.placeNearTarget();
		scene.idle(80);

		Vec3 filter = util.vector.of(2.5, 2.825, 2.5);
		scene.overlay.showFilterSlotInput(filter, Direction.EAST, 80);
		scene.overlay.showText(70)
			.text("A Filter might be necessary to avoid pulling out un-processed items")
			.pointAt(filter)
			.placeNearTarget();
		scene.idle(40);
		scene.markAsFinished();
	}

}
