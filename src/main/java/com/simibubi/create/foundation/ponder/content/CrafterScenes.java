package com.simibubi.create.foundation.ponder.content;

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterBlock;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.EntityElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;

public class CrafterScenes {

	public static void setup(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_crafter", "Setting up Mechanical Crafters");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.world.modifyKineticSpeed(util.select.everywhere(), f -> 1.5f * f);

		Selection redstone = util.select.fromTo(3, 1, 0, 3, 1, 1);
		Selection kinetics = util.select.fromTo(4, 1, 2, 4, 1, 5);
		BlockPos depotPos = util.grid.at(0, 1, 2);
		Selection crafters = util.select.fromTo(1, 1, 2, 3, 3, 2);

		scene.world.modifyBlocks(crafters, s -> s.setValue(MechanicalCrafterBlock.POINTING, Pointing.DOWN), false);
		scene.world.setKineticSpeed(crafters, 0);

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				scene.world.showSection(util.select.position(y == 1 ? x + 1 : 3 - x, y + 1, 2), Direction.DOWN);
				scene.idle(2);
			}
		}

		scene.overlay.showText(70)
			.text("An array of Mechanical Crafters can be used to automate any Crafting Recipe")
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 2), Direction.WEST))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(80);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 3, 2), Direction.NORTH), Pointing.RIGHT)
				.rightClick()
				.withWrench(),
			40);
		scene.idle(7);
		scene.world.cycleBlockProperty(util.grid.at(2, 3, 2), MechanicalCrafterBlock.POINTING);
		scene.idle(10);
		scene.overlay.showText(50)
			.text("Using a Wrench, the Crafters' paths can be arranged")
			.pointAt(util.vector.blockSurface(util.grid.at(2, 3, 2), Direction.NORTH))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(60);

		BlockPos[] positions = new BlockPos[] { util.grid.at(3, 1, 2), util.grid.at(2, 1, 2), util.grid.at(1, 1, 2) };

		for (BlockPos pos : positions) {
			scene.overlay.showControls(
				new InputWindowElement(util.vector.blockSurface(pos, Direction.NORTH), Pointing.RIGHT).rightClick()
					.withWrench(),
				10);
			scene.idle(7);
			scene.world.cycleBlockProperty(pos, MechanicalCrafterBlock.POINTING);
			scene.idle(15);
		}

		scene.overlay.showText(100)
			.text("For a valid setup, all paths have to converge into one exit at any side")
			.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.WEST)
				.add(0, 0, -.5f))
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(60);

		Collection<Couple<BlockPos>> couples =
			ImmutableList.of(Couple.create(util.grid.at(3, 3, 2), util.grid.at(3, 2, 2)),
				Couple.create(util.grid.at(3, 2, 2), util.grid.at(3, 1, 2)),
				Couple.create(util.grid.at(2, 3, 2), util.grid.at(1, 3, 2)),
				Couple.create(util.grid.at(3, 1, 2), util.grid.at(2, 1, 2)),
				Couple.create(util.grid.at(1, 3, 2), util.grid.at(1, 2, 2)),
				Couple.create(util.grid.at(2, 2, 2), util.grid.at(2, 1, 2)),
				Couple.create(util.grid.at(1, 2, 2), util.grid.at(1, 1, 2)),
				Couple.create(util.grid.at(2, 1, 2), util.grid.at(1, 1, 2)),
				Couple.create(util.grid.at(1, 1, 2), util.grid.at(0, 1, 2)));

		for (Couple<BlockPos> c : couples) {
			scene.idle(5);
			Vec3 p1 = util.vector.blockSurface(c.getFirst(), Direction.NORTH)
				.add(0, 0, -0.125);
			Vec3 p2 = util.vector.blockSurface(c.getSecond(), Direction.NORTH)
				.add(0, 0, -0.125);
			AABB point = new AABB(p1, p1);
			AABB line = new AABB(p1, p2);
			scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, p1, point, 2);
			scene.idle(1);
			scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, p1, line, 30);
		}

		scene.world.showSection(util.select.position(depotPos), Direction.EAST);
		scene.idle(20);
		scene.overlay.showText(60)
			.text("The outputs will be placed into the inventory at the exit")
			.pointAt(util.vector.blockSurface(util.grid.at(0, 1, 2), Direction.NORTH))
			.placeNearTarget();
		scene.idle(70);

		scene.rotateCameraY(60);
		scene.idle(20);
		scene.world.showSection(kinetics, Direction.NORTH);
		scene.overlay.showText(60)
			.text("Mechanical Crafters require Rotational Force to operate")
			.pointAt(util.vector.blockSurface(util.grid.at(4, 1, 2), Direction.NORTH))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(8);
		scene.world.setKineticSpeed(crafters, -48);
		scene.world.multiplyKineticSpeed(util.select.position(3, 2, 2)
			.add(util.select.position(2, 3, 2))
			.add(util.select.position(1, 2, 2))
			.add(util.select.position(2, 1, 2)), -1);
		scene.idle(55);
		scene.rotateCameraY(-60);

		scene.idle(40);
		ItemStack planks = new ItemStack(Items.OAK_PLANKS);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(1, 3, 2), Direction.NORTH), Pointing.RIGHT)
				.rightClick()
				.withItem(planks),
			40);
		scene.idle(7);
		Class<MechanicalCrafterBlockEntity> type = MechanicalCrafterBlockEntity.class;
		scene.world.modifyBlockEntity(util.grid.at(1, 3, 2), type, mct -> mct.getInventory()
			.insertItem(0, planks.copy(), false));

		scene.idle(10);
		scene.overlay.showText(50)
			.text("Right-Click the front to insert Items manually")
			.pointAt(util.vector.blockSurface(util.grid.at(1, 3, 2), Direction.NORTH))
			.attachKeyFrame()
			.placeNearTarget();
		scene.idle(60);

		ItemStack redstoneDust = new ItemStack(Items.REDSTONE);
		ItemStack iron = new ItemStack(Items.IRON_INGOT);
		ItemStack cobble = new ItemStack(Items.COBBLESTONE);

		scene.world.setCraftingResult(util.grid.at(1, 1, 2), new ItemStack(Items.PISTON));

		scene.world.modifyBlockEntity(util.grid.at(2, 3, 2), type, mct -> mct.getInventory()
			.insertItem(0, planks.copy(), false));
		scene.idle(5);
		scene.world.modifyBlockEntity(util.grid.at(3, 3, 2), type, mct -> mct.getInventory()
			.insertItem(0, planks.copy(), false));
		scene.idle(5);
		scene.world.modifyBlockEntity(util.grid.at(3, 2, 2), type, mct -> mct.getInventory()
			.insertItem(0, cobble.copy(), false));
		scene.idle(5);
		scene.world.modifyBlockEntity(util.grid.at(2, 2, 2), type, mct -> mct.getInventory()
			.insertItem(0, iron.copy(), false));
		scene.idle(5);
		scene.world.modifyBlockEntity(util.grid.at(1, 2, 2), type, mct -> mct.getInventory()
			.insertItem(0, cobble.copy(), false));
		scene.idle(5);
		scene.world.modifyBlockEntity(util.grid.at(1, 1, 2), type, mct -> mct.getInventory()
			.insertItem(0, cobble.copy(), false));
		scene.idle(5);
		scene.world.modifyBlockEntity(util.grid.at(2, 1, 2), type, mct -> mct.getInventory()
			.insertItem(0, redstoneDust.copy(), false));
		scene.idle(5);
		scene.world.modifyBlockEntity(util.grid.at(3, 1, 2), type, mct -> mct.getInventory()
			.insertItem(0, cobble.copy(), false));

		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("Once every slot of a path contains an Item, the crafting process will begin")
			.pointAt(util.vector.blockSurface(util.grid.at(1, 3, 2), Direction.WEST))
			.placeNearTarget();
		scene.idle(180);

		scene.world.removeItemsFromBelt(depotPos);

		ItemStack stick = new ItemStack(Items.STICK);

		scene.world.setCraftingResult(util.grid.at(1, 1, 2), new ItemStack(Items.IRON_PICKAXE));

		scene.world.modifyBlockEntity(util.grid.at(1, 3, 2), type, mct -> mct.getInventory()
			.insertItem(0, iron.copy(), false));
		scene.idle(2);
		scene.world.modifyBlockEntity(util.grid.at(2, 3, 2), type, mct -> mct.getInventory()
			.insertItem(0, iron.copy(), false));
		scene.idle(2);
		scene.world.modifyBlockEntity(util.grid.at(3, 3, 2), type, mct -> mct.getInventory()
			.insertItem(0, iron.copy(), false));
		scene.idle(2);
		scene.world.modifyBlockEntity(util.grid.at(2, 2, 2), type, mct -> mct.getInventory()
			.insertItem(0, stick.copy(), false));
		scene.idle(2);
		scene.world.modifyBlockEntity(util.grid.at(2, 1, 2), type, mct -> mct.getInventory()
			.insertItem(0, stick.copy(), false));
		scene.world.showSection(redstone, Direction.SOUTH);
		scene.idle(10);

		scene.overlay.showText(90)
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.text("For recipes not fully occupying the crafter setup, the start can be forced using a Redstone Pulse")
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 2), Direction.NORTH))
			.placeNearTarget();
		scene.idle(100);
		scene.effects.indicateRedstone(util.grid.at(3, 1, 0));
		scene.world.toggleRedstonePower(redstone);
		scene.idle(20);
		scene.world.toggleRedstonePower(redstone);
	}

	public static void connect(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_crafter_connect", "Connecting Inventories of Crafters");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 2; x++) {
				scene.world.showSection(util.select.position(y == 1 ? x + 1 : 2 - x, y + 1, 2), Direction.DOWN);
				scene.idle(2);
			}
		}

		Class<MechanicalCrafterBlockEntity> type = MechanicalCrafterBlockEntity.class;
		BlockPos depotPos = util.grid.at(0, 1, 2);
		Selection funnel = util.select.fromTo(4, 1, 5, 4, 1, 2)
			.add(util.select.fromTo(3, 2, 2, 3, 1, 2));
		Selection kinetics = util.select.position(3, 3, 2)
			.add(util.select.fromTo(3, 3, 3, 3, 1, 3));
		scene.idle(5);

		scene.world.showSection(kinetics, Direction.NORTH);
		scene.idle(5);
		scene.world.showSection(util.select.position(depotPos), Direction.EAST);
		scene.idle(10);
		scene.world.showSection(funnel, Direction.WEST);
		scene.rotateCameraY(60);
		ItemStack planks = new ItemStack(Items.OAK_PLANKS);
		scene.world.createItemOnBelt(util.grid.at(4, 1, 2), Direction.EAST, planks.copy());
		scene.idle(22);

		scene.world.modifyBlockEntity(util.grid.at(2, 2, 2), type, mct -> mct.getInventory()
			.insertItem(0, planks.copy(), false));
		scene.world.removeItemsFromBelt(util.grid.at(3, 1, 2));
		scene.world.flapFunnel(util.grid.at(3, 2, 2), false);

		scene.overlay.showSelectionWithText(util.select.position(2, 2, 2), 70)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 2, 2), Direction.NORTH))
			.text("Items can be inserted to Crafters automatically");
		scene.idle(80);

		scene.rotateCameraY(-60 - 90 - 30);
		scene.idle(40);

		Vec3 v = util.vector.blockSurface(util.grid.at(2, 2, 2), Direction.WEST);
		AABB bb = new AABB(v, v).inflate(.125f, .5, .5);
		v = v.add(0, 0, .5);

		scene.overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, new Object(), bb, 45);
		scene.overlay.showControls(new InputWindowElement(v, Pointing.LEFT).rightClick()
			.withWrench(), 40);
		scene.idle(7);
		scene.world.connectCrafterInvs(util.grid.at(2, 2, 2), util.grid.at(1, 2, 2));
		scene.idle(40);
		scene.overlay.showSelectionWithText(util.select.fromTo(2, 2, 2, 1, 2, 2), 70)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(v)
			.text("Using the Wrench at their backs, Mechanical Crafter inputs can be combined");
		scene.idle(80);
		scene.overlay.showControls(new InputWindowElement(v.add(0, 1, 0), Pointing.LEFT).rightClick()
			.withWrench(), 20);
		scene.idle(7);
		scene.world.connectCrafterInvs(util.grid.at(2, 3, 2), util.grid.at(1, 3, 2));
		scene.idle(20);
		scene.overlay.showControls(new InputWindowElement(v.add(0, -1, 0), Pointing.LEFT).rightClick()
			.withWrench(), 20);
		scene.idle(7);
		scene.world.connectCrafterInvs(util.grid.at(2, 1, 2), util.grid.at(1, 1, 2));
		scene.idle(20);
		scene.overlay.showControls(new InputWindowElement(v.add(.5, -.5, 0), Pointing.LEFT).rightClick()
			.withWrench(), 20);
		scene.idle(7);
		scene.world.connectCrafterInvs(util.grid.at(2, 1, 2), util.grid.at(2, 2, 2));
		scene.idle(10);
		scene.overlay.showControls(new InputWindowElement(v.add(.5, .5, 0), Pointing.LEFT).rightClick()
			.withWrench(), 20);
		scene.idle(7);
		scene.world.connectCrafterInvs(util.grid.at(2, 2, 2), util.grid.at(2, 3, 2));
		scene.idle(20);

		scene.rotateCameraY(90 + 30);
		scene.idle(40);
		scene.overlay.showSelectionWithText(util.select.fromTo(1, 1, 2, 2, 3, 2), 70)
			.attachKeyFrame()
			.placeNearTarget()
			.text("All connected Crafters can now be accessed by the same input location");
		scene.idle(60);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.centerOf(util.grid.at(4, 2, 2)), Pointing.DOWN).withItem(planks), 40);
		scene.idle(7);
		scene.world.createItemOnBelt(util.grid.at(4, 1, 2), Direction.EAST,
			ItemHandlerHelper.copyStackWithSize(planks, 16));
		scene.idle(22);

		scene.world.removeItemsFromBelt(util.grid.at(3, 1, 2));
		BlockPos[] positions = new BlockPos[] { util.grid.at(2, 3, 2), util.grid.at(1, 3, 2), util.grid.at(1, 2, 2),
			util.grid.at(2, 1, 2), util.grid.at(1, 1, 2) };

		scene.world.setCraftingResult(util.grid.at(1, 1, 2), new ItemStack(Items.OAK_DOOR, 3));
		for (BlockPos pos : positions) {
			scene.world.modifyBlockEntity(pos, type, mct -> mct.getInventory()
				.insertItem(0, planks.copy(), false));
			scene.idle(1);
		}

	}

	public static void covers(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("mechanical_crafter_covers", "Covering slots of Mechanical Crafters");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		scene.world.setBlock(util.grid.at(2, 2, 2), Blocks.AIR.defaultBlockState(), false);

		Selection kinetics = util.select.fromTo(3, 1, 2, 3, 1, 5);
		scene.world.setKineticSpeed(util.select.fromTo(1, 2, 2, 3, 1, 2), 0);

		scene.world.showSection(util.select.position(3, 2, 2), Direction.EAST);
		scene.idle(5);
		scene.world.showSection(util.select.position(2, 1, 2), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(1, 2, 2), Direction.WEST);
		scene.idle(5);

		ItemStack iron = new ItemStack(Items.IRON_INGOT);

		Class<MechanicalCrafterBlockEntity> type = MechanicalCrafterBlockEntity.class;
		scene.world.modifyBlockEntity(util.grid.at(3, 2, 2), type, mct -> mct.getInventory()
			.insertItem(0, iron.copy(), false));
		scene.idle(5);
		scene.world.modifyBlockEntity(util.grid.at(2, 1, 2), type, mct -> mct.getInventory()
			.insertItem(0, iron.copy(), false));
		scene.idle(5);
		scene.world.modifyBlockEntity(util.grid.at(1, 2, 2), type, mct -> mct.getInventory()
			.insertItem(0, iron.copy(), false));
		scene.idle(5);

		Selection emptyCrafter = util.select.position(2, 2, 2);
		scene.overlay.showSelectionWithText(emptyCrafter, 90)
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.text("Some recipes will require additional Crafters to bridge gaps in the path")
			.placeNearTarget();
		scene.idle(70);
		scene.world.restoreBlocks(emptyCrafter);
		scene.world.setCraftingResult(util.grid.at(2, 2, 2), new ItemStack(Items.BUCKET));
		scene.world.showSection(emptyCrafter, Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.position(2, 3, 2), Direction.DOWN);
		scene.world.showSection(kinetics, Direction.NORTH);
		scene.idle(5);
		scene.world.setKineticSpeed(util.select.fromTo(3, 1, 2, 1, 2, 2), -32);
		scene.world.setKineticSpeed(util.select.position(3, 1, 2)
			.add(emptyCrafter), 32);

		scene.idle(20);

		scene.overlay.showText(90)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.blockSurface(util.grid.at(2, 2, 2), Direction.NORTH))
			.text("Using Slot Covers, Crafters can be set to act as an Empty Slot in the arrangement")
			.placeNearTarget();
		scene.idle(100);
		scene.overlay
			.showControls(new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 2, 2), Direction.NORTH)
				.add(0.5, 0, 0), Pointing.RIGHT).withItem(AllItems.CRAFTER_SLOT_COVER.asStack())
					.rightClick(),
				50);
		scene.idle(7);
		scene.world.modifyBlockEntityNBT(emptyCrafter, type, compound -> compound.putBoolean("Cover", true));
		scene.idle(130);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(2, 3, 2), Direction.WEST), Pointing.LEFT)
				.withItem(new ItemStack(Items.BUCKET)),
			40);
		scene.idle(50);
		scene.world.showSection(util.select.position(4, 2, 2), Direction.DOWN);

		scene.world.connectCrafterInvs(util.grid.at(3, 2, 2), util.grid.at(2, 2, 2));
		scene.idle(5);
		scene.world.connectCrafterInvs(util.grid.at(2, 1, 2), util.grid.at(2, 2, 2));
		scene.idle(5);
		scene.world.connectCrafterInvs(util.grid.at(1, 2, 2), util.grid.at(2, 2, 2));
		scene.idle(10);

		scene.overlay.showSelectionWithText(util.select.fromTo(3, 2, 2, 1, 2, 2)
			.add(util.select.position(2, 1, 2)), 80)
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 2, 2), Direction.NORTH))
			.text("Shared Inputs created with the Wrench at the back can also reach across covered Crafters")
			.placeNearTarget();
		scene.idle(60);

		ElementLink<EntityElement> ingot =
			scene.world.createItemEntity(util.vector.centerOf(4, 4, 2), util.vector.of(0, 0.2, 0), iron);
		scene.idle(17);
		scene.world.modifyEntity(ingot, Entity::discard);
		scene.world.modifyBlockEntity(util.grid.at(3, 2, 2), type, mct -> mct.getInventory()
			.insertItem(0, iron.copy(), false));
		ingot = scene.world.createItemEntity(util.vector.centerOf(4, 4, 2), util.vector.of(0, 0.2, 0), iron);
		scene.idle(17);
		scene.world.modifyEntity(ingot, Entity::discard);
		scene.world.modifyBlockEntity(util.grid.at(2, 1, 2), type, mct -> mct.getInventory()
			.insertItem(0, iron.copy(), false));
		ingot = scene.world.createItemEntity(util.vector.centerOf(4, 4, 2), util.vector.of(0, 0.2, 0), iron);
		scene.idle(17);
		scene.world.modifyEntity(ingot, Entity::discard);
		scene.world.modifyBlockEntity(util.grid.at(1, 2, 2), type, mct -> mct.getInventory()
			.insertItem(0, iron.copy(), false));

	}

}
