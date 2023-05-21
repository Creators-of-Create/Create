package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity.Phase;
import com.simibubi.create.content.logistics.funnel.FunnelBlockEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ArmScenes {

	public static void setup(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_arm", "Setting up Mechanical Arms");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		ItemStack armItem = AllBlocks.MECHANICAL_ARM.asStack();
		BlockPos armPos = util.grid.at(2, 1, 2);
		Selection armSel = util.select.position(armPos);
		BlockPos inputDepot = util.grid.at(4, 2, 1);
		Vec3 depotSurface = util.vector.blockSurface(inputDepot, Direction.NORTH);
		Vec3 armSurface = util.vector.blockSurface(armPos, Direction.WEST);

		scene.idle(20);

		scene.world.setKineticSpeed(armSel, 0);
		scene.world.showSection(armSel, Direction.DOWN);
		scene.idle(10);
		scene.effects.indicateRedstone(armPos);
		scene.overlay.showSelectionWithText(armSel, 70)
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.text("Mechanical Arms have to be assigned their in- and outputs before they are placed")
			.pointAt(armSurface)
			.placeNearTarget();
		scene.idle(80);
		scene.world.showSection(util.select.fromTo(4, 1, 1, 4, 2, 1), Direction.DOWN);
		scene.world.showSection(util.select.fromTo(0, 1, 1, 0, 2, 1), Direction.DOWN);
		scene.world.hideSection(armSel, Direction.UP);
		scene.idle(20);
		scene.overlay.showControls(new InputWindowElement(depotSurface, Pointing.RIGHT).rightClick()
			.withItem(armItem), 50);
		scene.idle(7);
		AABB depotBounds = AllShapes.CASING_13PX.get(Direction.UP)
			.bounds();
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.INPUT, new Object(), depotBounds.move(4, 2, 1), 400);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.colored(PonderPalette.INPUT)
			.text("Right-Click inventories while holding the Arm to assign them as Targets")
			.pointAt(util.vector.blockSurface(inputDepot, Direction.WEST))
			.placeNearTarget();
		scene.idle(80);

		BlockPos outputDepot = util.grid.at(0, 2, 1);
		InputWindowElement input =
			new InputWindowElement(util.vector.blockSurface(outputDepot, Direction.NORTH), Pointing.RIGHT).rightClick()
				.withItem(armItem);
		scene.overlay.showControls(input, 20);
		scene.idle(7);
		Object second = new Object();
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.INPUT, second, depotBounds.move(0, 2, 1), 100);
		scene.idle(25);
		scene.overlay.showControls(input, 30);
		scene.idle(7);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, second, depotBounds.move(0, 2, 1), 280);
		scene.overlay.showText(70)
			.colored(PonderPalette.OUTPUT)
			.text("Right-Click again to toggle between Input (Blue) and Output (Orange)")
			.pointAt(util.vector.blockSurface(outputDepot, Direction.WEST))
			.placeNearTarget();

		scene.idle(80);
		scene.world.showSection(util.select.position(1, 1, 0), Direction.DOWN);
		scene.idle(15);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.INPUT, new Object(), depotBounds.move(1, 1, 0), 43);

		scene.overlay.showText(50)
			.colored(PonderPalette.WHITE)
			.text("Left-Click components to remove their Selection")
			.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 0), Direction.WEST))
			.placeNearTarget();

		scene.idle(35);
		scene.overlay
			.showControls(new InputWindowElement(util.vector.topOf(util.grid.at(1, 1, 0)), Pointing.DOWN).leftClick()
				.withItem(armItem), 30);
		scene.idle(50);

		scene.world.showSection(armSel, Direction.DOWN);
		scene.idle(10);
		Vec3 armTop = armSurface.add(0.5, 1.5, 0);
		scene.overlay.showText(70)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("Once placed, the Mechanical Arm will target the blocks selected previously")
			.pointAt(armTop)
			.placeNearTarget();
		scene.idle(80);

		scene.effects.indicateSuccess(armPos);
		scene.world.showSection(util.select.fromTo(2, 1, 5, 2, 1, 3)
			.add(util.select.position(2, 0, 5)), Direction.DOWN);
		ItemStack copper = new ItemStack(Items.COPPER_INGOT);
		scene.world.createItemOnBeltLike(inputDepot, Direction.SOUTH, copper);
		scene.idle(10);

		scene.world.setKineticSpeed(armSel, -48);
		scene.idle(20);
		scene.world.instructArm(armPos, Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 1);
		scene.idle(24);
		scene.world.removeItemsFromBelt(inputDepot);
		scene.world.instructArm(armPos, Phase.SEARCH_OUTPUTS, copper, -1);
		scene.idle(20);
		scene.world.instructArm(armPos, Phase.MOVE_TO_OUTPUT, copper, 1);
		scene.idle(24);
		scene.world.createItemOnBeltLike(outputDepot, Direction.UP, copper);
		scene.world.instructArm(armPos, Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
		scene.idle(44);

		scene.world.showSection(util.select.fromTo(1, 1, 4, 1, 3, 4), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(4, 1, 2), Direction.DOWN);
		scene.idle(5);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, new Object(), depotBounds.move(0, 2, 1), 60);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.INPUT, new Object(), depotBounds.move(4, 2, 1), 60);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, new Object(), depotBounds.move(1, 1, 0), 60);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.INPUT, new Object(), depotBounds.move(1, 3, 4), 60);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.INPUT, new Object(), depotBounds.move(4, 1, 2), 60);
		scene.idle(5);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("They can have any amount of in- and outputs within their range")
			.pointAt(util.vector.blockSurface(util.grid.at(1, 3, 4), Direction.WEST))
			.placeNearTarget();

		inputDepot = util.grid.at(1, 3, 4);
		outputDepot = util.grid.at(1, 1, 0);
		copper = new ItemStack(Items.COPPER_BLOCK);
		scene.world.createItemOnBeltLike(inputDepot, Direction.SOUTH, copper);
		scene.idle(20);
		scene.world.instructArm(armPos, Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 2);
		scene.idle(24);
		scene.world.removeItemsFromBelt(inputDepot);
		scene.world.instructArm(armPos, Phase.SEARCH_OUTPUTS, copper, -1);
		scene.idle(20);
		scene.world.instructArm(armPos, Phase.MOVE_TO_OUTPUT, copper, 0);
		scene.idle(24);
		scene.world.createItemOnBeltLike(outputDepot, Direction.UP, copper);
		scene.world.instructArm(armPos, Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);

		scene.world.hideSection(util.select.fromTo(4, 2, 1, 4, 1, 1), Direction.UP);
		scene.idle(2);
		scene.world.hideSection(util.select.fromTo(1, 1, 4, 1, 3, 4), Direction.UP);
		scene.idle(5);
		scene.world.hideSection(util.select.fromTo(0, 1, 1, 0, 2, 1), Direction.UP);
		scene.idle(2);
		scene.world.hideSection(util.select.position(1, 1, 0), Direction.UP);
		scene.idle(5);
		scene.world.hideSection(util.select.position(4, 1, 2), Direction.UP);
		scene.idle(15);

		scene.world.showSection(util.select.fromTo(4, 1, 3, 4, 2, 3), Direction.NORTH);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(0, 1, 3, 0, 2, 3), Direction.NORTH);
		scene.idle(15);

		Object in = new Object();
		Object out = new Object();
		AABB chestBounds = new AABB(1 / 16f, 0, 1 / 16f, 15 / 16f, 14 / 16f, 15 / 16f);
		AABB funnelBounds = new AABB(0, 0, 8 / 16f, 16 / 16f, 16 / 16f, 16 / 16f);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, in, chestBounds.move(4, 2, 3), 120);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, out, chestBounds.move(0, 2, 3), 120);
		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("However, not every type of Inventory can be interacted with directly")
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(0, 2, 3), Direction.WEST));
		scene.idle(90);

		scene.world.showSection(util.select.fromTo(4, 1, 2, 4, 2, 2), Direction.SOUTH);
		scene.idle(5);
		scene.world.showSection(util.select.position(0, 2, 2), Direction.SOUTH);
		scene.idle(10);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.INPUT, in, depotBounds.move(4, 1, 2), 80);
		scene.idle(5);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, out, funnelBounds.move(0, 2, 2), 80);
		scene.idle(5);

		scene.overlay.showText(60)
			.text("Funnels and Depots can help to Bridge that gap")
			.colored(PonderPalette.OUTPUT)
			.placeNearTarget()
			.pointAt(util.vector.topOf(util.grid.at(0, 2, 2))
				.add(0, 0, 0.25));
		scene.idle(70);
		ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
		inputDepot = util.grid.at(4, 1, 2);
		scene.overlay
			.showControls(new InputWindowElement(util.vector.topOf(inputDepot), Pointing.RIGHT).withItem(sword), 30);
		scene.world.createItemOnBeltLike(inputDepot, Direction.SOUTH, sword);

		scene.idle(20);
		scene.world.instructArm(armPos, Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
		scene.idle(24);
		scene.world.removeItemsFromBelt(inputDepot);
		scene.world.instructArm(armPos, Phase.SEARCH_OUTPUTS, sword, -1);
		scene.idle(20);
		scene.world.instructArm(armPos, Phase.MOVE_TO_OUTPUT, sword, 2);
		scene.idle(24);
		scene.world.flapFunnel(util.grid.at(0, 2, 2), false);
		scene.world.instructArm(armPos, Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
		scene.idle(5);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(0, 2, 3), Direction.WEST), Pointing.LEFT)
				.withItem(sword),
			30);

	}

	public static void filtering(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_arm_filtering", "Filtering Outputs of the Mechanical Arm");
		scene.configureBasePlate(0, 0, 6);
		scene.scaleSceneView(0.9f);
		scene.world.setKineticSpeed(util.select.fromTo(4, 1, 4, 6, 0, 5), 0);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.fromTo(4, 1, 4, 5, 1, 5), Direction.DOWN);
		scene.idle(10);

		for (int x = 0; x < 2; x++) {
			scene.idle(3);
			scene.world.showSection(util.select.position(x + 1, 1, 4), Direction.DOWN);
		}

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				scene.world.showSection(util.select.position(y == 1 ? x + 3 : 5 - x, y + 1, 1), Direction.DOWN);
				scene.idle(2);
			}
		}
		
		scene.world.showSection(util.select.position(6, 1, 1), Direction.WEST);
		scene.world.showSection(util.select.position(2, 1, 1), Direction.EAST);

		ItemStack sand = new ItemStack(Items.SAND, 64);
		ItemStack sulphur = new ItemStack(Items.GUNPOWDER, 64);
		scene.world.createItemOnBeltLike(util.grid.at(2, 1, 4), Direction.SOUTH, sand);
		scene.world.createItemOnBeltLike(util.grid.at(1, 1, 4), Direction.SOUTH, sulphur);

		scene.overlay.showSelectionWithText(util.select.fromTo(2, 1, 4, 1, 1, 4), 60)
			.text("Inputs")
			.placeNearTarget()
			.colored(PonderPalette.INPUT);
		scene.idle(50);
		scene.overlay.showSelectionWithText(util.select.fromTo(5, 3, 1, 3, 1, 1), 40)
			.text("Outputs")
			.placeNearTarget()
			.colored(PonderPalette.OUTPUT);
		scene.idle(50);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("Sometimes it is desirable to restrict targets of the Arm by matching a filter")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 3, 1), Direction.WEST));

		scene.idle(90);
		scene.rotateCameraY(-90 - 30);
		scene.idle(20);

		scene.overlay.showSelectionWithText(util.select.position(4, 1, 4), 80)
			.colored(PonderPalette.RED)
			.text("Mechanical Arms by themselves do not provide any options for filtering")
			.placeNearTarget();
		scene.idle(90);

		for (int y = 0; y < 3; y++) {
			scene.world.showSection(util.select.fromTo(5, y + 1, 2, 3, y + 1, 2), Direction.NORTH);
			scene.idle(2);
		}

		Vec3 filterSlot = util.vector.of(3.5, 3.75, 2.6);
		scene.overlay.showFilterSlotInput(filterSlot, Direction.NORTH, 80);
		scene.idle(10);
		scene.overlay.showText(80)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.pointAt(filterSlot)
			.text("Brass Funnels as Targets do however communicate their own filter to the Arm")
			.placeNearTarget();
		scene.idle(90);

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				ItemStack item = (x + y) % 2 == 0 ? sulphur : sand;
				scene.overlay
					.showControls(new InputWindowElement(filterSlot.add(2 - x, -y, 0), Pointing.LEFT).rightClick()
						.withItem(item), 5);
				scene.idle(7);
				scene.world.setFilterData(util.select.position(5 - x, 3 - y, 2), FunnelBlockEntity.class, item);
				scene.idle(4);
			}
		}

		scene.world.setKineticSpeed(util.select.fromTo(4, 1, 4, 6, 0, 5), 24);
		scene.world.multiplyKineticSpeed(util.select.position(5, 1, 5), -1);
		scene.world.multiplyKineticSpeed(util.select.position(4, 1, 4), 2);
		scene.idle(10);

		BlockPos armPos = util.grid.at(4, 1, 4);
		scene.world.instructArm(armPos, Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 1);
		scene.idle(24);
		scene.world.instructArm(armPos, Phase.SEARCH_OUTPUTS, sand, -1);
		scene.idle(20);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.pointAt(util.vector.topOf(2, 1, 4))
			.text("The Arm is smart enough not to pick up items it couldn't distribute")
			.placeNearTarget();
		scene.idle(90);

		for (int i = 0; i < 4; i++) {
			int index = i * 2 + 1;
			scene.world.instructArm(armPos, Phase.MOVE_TO_OUTPUT, sand, index);
			scene.idle(24);
			BlockPos funnelPos = util.grid.at(5 - index % 3, 1 + index / 3, 2);
			scene.world.flapFunnel(funnelPos, false);
			scene.world.instructArm(armPos, Phase.SEARCH_INPUTS, i == 3 ? ItemStack.EMPTY : sand, -1);
			scene.world.modifyBlockEntity(funnelPos.north(), MechanicalCrafterBlockEntity.class, mct -> mct.getInventory()
				.insertItem(0, sand.copy(), false));
			scene.idle(10);
		}

		scene.world.instructArm(armPos, Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
		scene.idle(24);
		scene.world.instructArm(armPos, Phase.SEARCH_OUTPUTS, sulphur, -1);
		scene.idle(20);

		scene.rotateCameraY(120);
		scene.world.setCraftingResult(util.grid.at(3, 1, 1), new ItemStack(Blocks.TNT));

		for (int i = 0; i < 5; i++) {
			int index = i * 2;
			scene.world.instructArm(armPos, Phase.MOVE_TO_OUTPUT, sulphur, index);
			scene.idle(24);
			BlockPos funnelPos = util.grid.at(3 + index % 3, 1 + index / 3, 2);
			scene.world.flapFunnel(funnelPos, false);
			scene.world.instructArm(armPos, Phase.SEARCH_INPUTS, i == 4 ? ItemStack.EMPTY : sulphur, -1);
			scene.world.modifyBlockEntity(funnelPos.north(), MechanicalCrafterBlockEntity.class, mct -> mct.getInventory()
				.insertItem(0, sulphur.copy(), false));
			scene.idle(10);
		}

		scene.idle(120);
	}

	public static void modes(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_arm_modes", "Distribution modes of the Mechanical Arm");
		scene.configureBasePlate(0, 1, 5);
		scene.world.setBlock(util.grid.at(3, 1, 0), Blocks.BARRIER.defaultBlockState(), false);

		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(3, 1, 4, 4, 1, 5), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(1, 1, 4, 1, 2, 5), Direction.NORTH);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(1, 1, 1, 5, 1, 2), Direction.SOUTH);
		scene.idle(10);

		AABB depotBox = AllShapes.CASING_13PX.get(Direction.UP)
			.bounds();
		AABB beltBox = depotBox.contract(0, -3 / 16f, 0)
			.inflate(1, 0, 0);
		BlockPos depotPos = util.grid.at(1, 1, 4);
		BlockPos armPos = util.grid.at(3, 1, 4);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.INPUT, depotBox, depotBox.move(1, 1, 4), 60);
		scene.overlay.showText(30)
			.text("Input")
			.pointAt(util.vector.blockSurface(depotPos, Direction.WEST))
			.placeNearTarget()
			.colored(PonderPalette.INPUT);
		scene.idle(40);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, depotBox, beltBox.move(2, 1, 2), 40);
		scene.overlay.showText(40)
			.text("Outputs")
			.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.WEST))
			.placeNearTarget()
			.colored(PonderPalette.OUTPUT);
		scene.idle(50);

		ItemStack item = new ItemStack(Items.SNOWBALL);

		scene.world.createItemOnBeltLike(depotPos, Direction.SOUTH, item);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("Whenever an Arm has to choose between multiple valid outputs...")
			.pointAt(util.vector.blockSurface(util.grid.at(2, 1, 2), Direction.UP))
			.placeNearTarget()
			.colored(PonderPalette.OUTPUT);
		scene.idle(70);

		Vec3 scrollSlot = util.vector.of(3.5, 1 + 3 / 16f, 4);
		scene.overlay.showFilterSlotInput(scrollSlot, Direction.NORTH, 120);
		scene.overlay.showText(50)
			.text("...it will act according to its setting")
			.pointAt(scrollSlot)
			.placeNearTarget();
		scene.idle(60);

		scene.overlay.showControls(new InputWindowElement(scrollSlot, Pointing.RIGHT).rightClick(), 40);
		scene.idle(10);
		scene.overlay.showText(50)
			.text("The value panel will allow you to configure it")
			.pointAt(scrollSlot)
			.placeNearTarget();
		scene.idle(60);

		ElementLink<WorldSectionElement> blockage =
			scene.world.showIndependentSection(util.select.position(4, 1, 0), Direction.UP);
		scene.world.moveSection(blockage, util.vector.of(-1, 0, 0), 0);

		for (int i = 0; i < 20; i++) {

			if (i == 2) {
				scene.overlay.showText(60)
					.attachKeyFrame()
					.text("Round Robin mode simply cycles through all outputs that are available")
					.pointAt(util.vector.blockSurface(util.grid.at(2, 1, 2), Direction.UP))
					.placeNearTarget()
					.colored(PonderPalette.OUTPUT);
			}
			if (i == 6)
				continue;
			if (i == 7) {
				scene.overlay.showText(60)
					.attachKeyFrame()
					.text("If an output is unable to take more items, it will be skipped")
					.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 2), Direction.UP))
					.placeNearTarget()
					.colored(PonderPalette.GREEN);
			}

			if (i == 12) {
				scene.world.moveSection(blockage, util.vector.of(-1, 0, 0), 10);
				scene.world.setBlock(util.grid.at(3, 1, 0), Blocks.BARRIER.defaultBlockState(), false);
			}

			int index = i % 3;

			if (i == 13) {
				scene.world.setBlock(util.grid.at(2, 1, 0), Blocks.BARRIER.defaultBlockState(), false);
				ElementLink<WorldSectionElement> blockage2 =
					scene.world.showIndependentSection(util.select.position(4, 1, 0), Direction.UP);
				scene.world.moveSection(blockage2, util.vector.of(-2, 0, 0), 0);
				scene.overlay.showText(60)
					.attachKeyFrame()
					.text("Prefer First prioritizes the outputs selected earliest when configuring this Arm")
					.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 2), Direction.UP))
					.placeNearTarget()
					.colored(PonderPalette.GREEN);
				index = 0;
			}

			if (i == 14)
				index = 1;
			if (i == 15)
				index = 1;
			if (i >= 16)
				index = 2;

			scene.idle(5);
			scene.world.instructArm(armPos, Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
			scene.idle(12);
			scene.world.instructArm(armPos, Phase.SEARCH_OUTPUTS, item, -1);
			scene.world.removeItemsFromBelt(depotPos);
			scene.idle(5);

			if (i == 9) {
				scene.overlay.showText(80)
					.attachKeyFrame()
					.text("Forced Round Robin mode will never skip outputs, and instead wait until they are free")
					.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 2), Direction.UP))
					.placeNearTarget()
					.colored(PonderPalette.RED);
				scene.idle(40);
				scene.world.moveSection(blockage, util.vector.of(1, 0, 0), 10);
				scene.world.setBlock(util.grid.at(3, 1, 0), Blocks.AIR.defaultBlockState(), false);
				scene.idle(50);
				scene.world.multiplyKineticSpeed(util.select.fromTo(1, 1, 1, 5, 0, 3), 2);
			}

			scene.world.instructArm(armPos, Phase.MOVE_TO_OUTPUT, item, index);
			scene.world.createItemOnBeltLike(depotPos, Direction.SOUTH, item);
			scene.idle(12);
			scene.world.instructArm(armPos, Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
			scene.world.createItemOnBelt(util.grid.at(3 - index, 1, 2), Direction.UP, item);
		}

	}

	public static void redstone(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_arm_redstone", "Controlling Mechanical Arms with Redstone");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(1, 1, 3, 2, 1, 4), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(3, 1, 5, 4, 1, 3), Direction.WEST);
		scene.idle(5);
		scene.world.showSection(util.select.position(4, 1, 2), Direction.SOUTH);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(2, 1, 1, 4, 1, 1), Direction.EAST);
		scene.idle(10);
		Selection redstone = util.select.fromTo(1, 1, 0, 1, 1, 2);
		scene.world.showSection(redstone, Direction.SOUTH);

		BlockPos armPos = util.grid.at(1, 1, 3);
		BlockPos leverPos = util.grid.at(1, 1, 0);
		ItemStack item = new ItemStack(Items.REDSTONE_ORE);

		scene.world.createItemOnBeltLike(util.grid.at(4, 1, 1), Direction.SOUTH, item);

		for (int i = 0; i < 3; i++) {
			scene.idle(12);

			if (i == 1) {
				scene.world.toggleRedstonePower(redstone);
				scene.effects.indicateRedstone(leverPos);
				scene.idle(10);

				scene.overlay.showText(60)
					.colored(PonderPalette.RED)
					.attachKeyFrame()
					.pointAt(util.vector.topOf(armPos))
					.placeNearTarget()
					.text("When powered by Redstone, Mechanical Arms will not activate");
				scene.idle(70);
				scene.world.toggleRedstonePower(redstone);
			}

			if (i == 2) {
				scene.idle(60);
				scene.world.toggleRedstonePower(redstone);
				scene.idle(3);
				scene.world.toggleRedstonePower(redstone);
				scene.effects.indicateRedstone(leverPos);
			}

			scene.world.instructArm(armPos, Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
			scene.idle(18);
			scene.world.instructArm(armPos, Phase.SEARCH_OUTPUTS, item, -1);
			scene.world.removeItemsFromBelt(util.grid.at(3, 1, 1));
			scene.idle(5);

			if (i == 1) {
				scene.world.toggleRedstonePower(redstone);
				scene.effects.indicateRedstone(leverPos);
				scene.overlay.showText(60)
					.pointAt(util.vector.topOf(armPos))
					.placeNearTarget()
					.text("Before stopping, it will finish any started cycles");
			}

			scene.idle(10);

			if (i == 2) {
				scene.overlay.showText(100)
					.colored(PonderPalette.GREEN)
					.attachKeyFrame()
					.pointAt(util.vector.topOf(armPos))
					.placeNearTarget()
					.text("Thus, a negative pulse can be used to trigger exactly one activation cycle");
			}

			scene.world.instructArm(armPos, Phase.MOVE_TO_OUTPUT, item, 0);
			scene.world.createItemOnBeltLike(util.grid.at(4, 1, 1), Direction.SOUTH, item);
			scene.idle(18);
			scene.world.instructArm(armPos, Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
			scene.world.createItemOnBelt(util.grid.at(3, 1, 3), Direction.UP, item);
		}

		scene.idle(5);
	}

}
