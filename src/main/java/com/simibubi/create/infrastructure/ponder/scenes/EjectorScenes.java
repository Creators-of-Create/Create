package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.depot.EjectorBlockEntity;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.Pointing;
import net.createmod.ponder.foundation.ElementLink;
import net.createmod.ponder.foundation.PonderPalette;
import net.createmod.ponder.foundation.SceneBuilder;
import net.createmod.ponder.foundation.SceneBuildingUtil;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.ponder.foundation.element.ParrotElement;
import net.createmod.ponder.foundation.element.WorldSectionElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;

public class EjectorScenes {

	public static void ejector(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("weighted_ejector", "Using Weighted Ejectors");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		BlockPos ejectorPos = util.grid.at(4, 1, 2);
		Selection ejectorS = util.select.position(ejectorPos);
		BlockPos targetPos = util.grid.at(0, 1, 2);
		Selection targetS = util.select.position(targetPos);

		scene.world.setBlock(targetPos, AllBlocks.ANDESITE_CASING.getDefaultState(), false);
		scene.idle(5);
		scene.world.showSection(targetS, Direction.DOWN);

		scene.idle(10);
		ItemStack asStack = AllBlocks.WEIGHTED_EJECTOR.asStack();
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(targetPos), Pointing.DOWN).rightClick()
			.whileSneaking()
			.withItem(asStack), 50);
		scene.idle(7);
		Object slot = new Object();
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, slot, new AABB(targetPos), 160);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.colored(PonderPalette.OUTPUT)
			.text("Sneak and Right-Click holding an Ejector to select its target location")
			.pointAt(util.vector.blockSurface(targetPos, Direction.WEST))
			.placeNearTarget();
		scene.idle(80);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(ejectorPos), Pointing.DOWN).rightClick()
			.withItem(asStack), 50);
		scene.idle(7);
		scene.world.setKineticSpeed(ejectorS, 0);
		scene.world.modifyBlockEntityNBT(ejectorS, EjectorBlockEntity.class, nbt -> {
			NBTHelper.writeEnum(nbt, "State", EjectorBlockEntity.State.RETRACTING);
			nbt.putFloat("ForceAngle", 1);
		});
		scene.world.showSection(ejectorS, Direction.DOWN);
		scene.idle(10);

		scene.overlay.showText(60)
			.colored(PonderPalette.OUTPUT)
			.text("The placed ejector will now launch objects to the marked location")
			.pointAt(util.vector.blockSurface(ejectorPos, Direction.WEST))
			.placeNearTarget();
		scene.idle(70);

		slot = new Object();
		AABB bb = new AABB(ejectorPos.west());
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, slot, bb, 20);
		scene.idle(10);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, slot, bb.expandTowards(-15, 15, 0), 100);
		scene.idle(10);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("A valid target can be at any height or distance within range")
			.pointAt(util.vector.blockSurface(targetPos, Direction.WEST))
			.placeNearTarget();
		scene.idle(70);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, new Object(), bb.move(-2, 0, -1), 60);
		scene.idle(10);
		scene.overlay.showText(50)
			.colored(PonderPalette.RED)
			.text("They cannot however be off to a side")
			.pointAt(util.vector.blockSurface(targetPos.north()
				.east(), Direction.WEST))
			.placeNearTarget();
		scene.idle(70);
		scene.overlay.showSelectionWithText(util.select.position(ejectorPos.west()), 70)
			.colored(PonderPalette.OUTPUT)
			.text("If no valid Target was selected, it will simply target the block directly in front")
			.placeNearTarget();
		scene.idle(80);

		scene.world.showSection(util.select.position(3, 0, 5), Direction.UP);
		scene.world.showSection(util.select.fromTo(4, 1, 5, 4, 1, 3), Direction.DOWN);
		scene.idle(12);
		scene.world.setKineticSpeed(ejectorS, 32);
		scene.idle(10);
		scene.overlay.showText(50)
			.attachKeyFrame()
			.text("Supply Rotational Force in order to charge it up")
			.pointAt(util.vector.topOf(4, 1, 3))
			.placeNearTarget();
		scene.idle(60);

		ItemStack copperBlock = new ItemStack(Items.COPPER_BLOCK);
		ItemStack copperIngot = new ItemStack(Items.COPPER_INGOT);
		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(ejectorPos)
			.add(0.5, 0, 0), Pointing.RIGHT).withItem(copperBlock), 30);
		scene.idle(7);
		scene.world.createItemOnBeltLike(ejectorPos, Direction.NORTH, copperBlock);
		scene.idle(20);
		scene.overlay.showText(50)
			.text("Items placed on the ejector cause it to trigger")
			.pointAt(util.vector.topOf(ejectorPos))
			.placeNearTarget();
		scene.idle(60);

		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		scene.world.hideSection(targetS, Direction.SOUTH);
		scene.idle(15);
		scene.world.restoreBlocks(targetS);
		scene.world.showSection(targetS, Direction.SOUTH);
		scene.idle(10);
		scene.world.createItemOnBeltLike(targetPos, Direction.SOUTH, copperIngot);
		scene.idle(20);
		scene.world.createItemOnBeltLike(ejectorPos, Direction.SOUTH, copperBlock);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("If Inventories are targeted, the ejector will wait until there is space")
			.pointAt(util.vector.topOf(targetPos))
			.placeNearTarget();
		scene.idle(70);
		scene.effects.indicateSuccess(targetPos);
		scene.world.removeItemsFromBelt(targetPos);
		scene.idle(40);
		scene.world.hideSection(targetS, Direction.NORTH);
		scene.idle(15);
		scene.world.setBlock(targetPos, AllBlocks.ANDESITE_CASING.getDefaultState(), false);
		scene.world.showSection(targetS, Direction.NORTH);

		Vec3 input = util.vector.blockSurface(ejectorPos, Direction.WEST)
			.add(0, -2 / 16f, 0);
		Vec3 topOfSlot = input.add(0, 2 / 16f, 0);
		scene.overlay.showControls(new InputWindowElement(topOfSlot, Pointing.DOWN).rightClick(), 60);
		scene.overlay.showFilterSlotInput(input, Direction.WEST, 80);
		scene.idle(10);
		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("Using the value panel, a required Stack Size can be configured")
			.pointAt(input.add(0, 0, 0.125))
			.placeNearTarget();
		scene.world.modifyBlockEntityNBT(ejectorS, EjectorBlockEntity.class, nbt -> {
			nbt.putInt("ScrollValue", 10);
		});
		scene.idle(90);

		scene.world.showSection(util.select.fromTo(5, 1, 0, 4, 1, 1), Direction.DOWN);
		scene.world.showSection(util.select.position(5, 0, 1), Direction.UP);
		scene.idle(15);

		BlockPos beltPos = util.grid.at(4, 1, 0);
		scene.world.createItemOnBeltLike(beltPos, Direction.UP, copperBlock);
		scene.overlay.showText(100)
			.text("It is now limited to this stack size, and only activates when its held stack reaches this amount")
			.pointAt(util.vector.topOf(ejectorPos))
			.placeNearTarget();
		for (int i = 0; i < 4; i++) {
			scene.idle(20);
			scene.world.createItemOnBeltLike(beltPos, Direction.UP, copperBlock);
		}
		scene.idle(20);
		scene.world.createItemOnBeltLike(beltPos, Direction.UP, ItemHandlerHelper.copyStackWithSize(copperBlock, 15));
		scene.idle(80);

		scene.world.hideSection(util.select.fromTo(5, 1, 0, 4, 1, 1), Direction.UP);
		scene.world.hideSection(util.select.position(5, 0, 1), Direction.DOWN);
		scene.idle(30);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);

		scene.addKeyframe();
		ElementLink<ParrotElement> birb = scene.special.createBirb(util.vector.topOf(ejectorPos)
			.add(0, -3 / 16f, 0), ParrotElement.FlappyPose::new);
		scene.idle(15);
		scene.world.modifyBlockEntity(ejectorPos, EjectorBlockEntity.class, ejector -> ejector.activateDeferred());
		scene.special.moveParrot(birb, util.vector.of(-2, 3, 0), 5);
		scene.special.rotateParrot(birb, 0, 360 * 2, 0, 21);
		scene.idle(5);
		scene.special.moveParrot(birb, util.vector.of(-1, 0, 0), 3);
		scene.idle(3);
		scene.special.moveParrot(birb, util.vector.of(-0.75, -1, 0), 6);
		scene.idle(6);
		scene.special.moveParrot(birb, util.vector.of(-0.25, -2 + 3 / 16f, 0), 12);
		scene.idle(15);
		scene.special.changeBirbPose(birb, ParrotElement.FaceCursorPose::new);
		scene.overlay.showText(80)
			.text("Mobs and Players will always trigger an Ejector when stepping on it")
			.pointAt(util.vector.topOf(targetPos))
			.placeNearTarget();
		scene.idle(50);

	}

	public static void splitY(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("weighted_ejector_tunnel", "Splitting item stacks using Weighted Ejectors");
		Selection coverbelt = util.select.fromTo(3, 1, 1, 2, 1, 0);
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(4, 1, 5, 0, 1, 3), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.position(2, 2, 3), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.position(2, 1, 2), Direction.SOUTH);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(4, 1, 2, 3, 1, 2), Direction.SOUTH);
		scene.idle(10);

		BlockPos ejectorPos = util.grid.at(2, 1, 2);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("Combined with Brass Tunnels, Ejectors can split item stacks by specific amounts")
			.pointAt(util.vector.topOf(ejectorPos))
			.placeNearTarget();
		scene.idle(90);

		BlockPos tunnel = util.grid.at(2, 2, 3);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.topOf(tunnel), Pointing.DOWN).showing(AllIcons.I_TUNNEL_PREFER_NEAREST),
			80);
		scene.idle(10);
		scene.overlay.showCenteredScrollInput(tunnel, Direction.UP, 100);
		scene.idle(10);
		scene.overlay.showText(100)
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.text("First, configure the Brass Tunnel to 'Prefer Nearest', in order to prioritize its side output")
			.pointAt(util.vector.topOf(tunnel))
			.placeNearTarget();
		scene.idle(110);

		Vec3 input = util.vector.blockSurface(ejectorPos, Direction.NORTH)
			.subtract(0, 2 / 16f, 0);
		Vec3 topOfSlot = input.add(0, 2 / 16f, 0);
		scene.overlay.showFilterSlotInput(input, Direction.NORTH, 80);
		scene.idle(10);
		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("The Stack Size set on the Ejector now determines the amount to be split off")
			.pointAt(topOfSlot)
			.placeNearTarget();
		scene.world.modifyBlockEntityNBT(util.select.position(2, 1, 2), EjectorBlockEntity.class, nbt -> {
			nbt.putInt("ScrollValue", 10);
		});
		scene.idle(90);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(util.grid.at(4, 1, 3)), Pointing.DOWN)
			.withItem(new ItemStack(Items.COPPER_INGOT)), 20);
		scene.world.showSection(coverbelt, Direction.SOUTH);

		scene.idle(7);
		scene.world.createItemOnBelt(util.grid.at(4, 1, 3), Direction.UP, new ItemStack(Items.COPPER_INGOT, 64));
		scene.idle(40);
		scene.world.multiplyKineticSpeed(util.select.everywhere(), 1 / 16f);
		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("While a new stack of the configured size exits the side output...")
			.pointAt(util.vector.blockSurface(util.grid.at(2, 1, 1), Direction.WEST))
			.placeNearTarget();
		scene.idle(90);
		scene.overlay.showText(80)
			.text("...the remainder will continue on its path")
			.pointAt(util.vector.blockSurface(util.grid.at(0, 1, 3), Direction.UP))
			.placeNearTarget();
		scene.idle(90);
		scene.world.multiplyKineticSpeed(util.select.everywhere(), 16f);
	}

	public static void redstone(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("weighted_ejector_redstone", "Controlling Weighted Ejectors with Redstone");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(4, 1, 3, 4, 1, 5), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(0, 1, 2, 0, 2, 2), Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(util.select.position(4, 1, 2), Direction.SOUTH);
		scene.idle(5);
		Selection redstone = util.select.fromTo(3, 1, 2, 2, 1, 2);
		scene.world.showSection(redstone, Direction.EAST);

		BlockPos ejectorPos = util.grid.at(4, 1, 2);
		Vec3 topOf = util.vector.topOf(ejectorPos.above(2));
		ItemStack copper = new ItemStack(Items.COPPER_INGOT);

		for (int i = 0; i < 3; i++) {
			scene.world.createItemEntity(topOf, util.vector.of(0, 0.1, 0), copper);
			scene.idle(12);
			scene.world.modifyEntities(ItemEntity.class, Entity::discard);
			scene.world.createItemOnBeltLike(ejectorPos, Direction.UP, copper);
			scene.idle(20);
			if (i == 1) {
				scene.world.toggleRedstonePower(redstone);
				scene.effects.indicateRedstone(util.grid.at(2, 1, 2));
				scene.world.modifyBlockEntityNBT(util.select.position(4, 1, 2), EjectorBlockEntity.class,
					nbt -> nbt.putBoolean("Powered", true));
			}
		}

		scene.idle(10);
		scene.overlay.showText(60)
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.pointAt(util.vector.topOf(ejectorPos))
			.placeNearTarget()
			.text("When powered by Redstone, Ejectors will not activate");
		scene.idle(70);

		scene.world.toggleRedstonePower(redstone);
		scene.idle(2);
		scene.world.modifyBlockEntityNBT(util.select.position(4, 1, 2), EjectorBlockEntity.class,
			nbt -> nbt.putBoolean("Powered", false));
		scene.idle(5);
		scene.world.hideSection(redstone, Direction.WEST);
		scene.idle(30);
		ElementLink<WorldSectionElement> observer =
			scene.world.showIndependentSection(util.select.position(4, 1, 1), Direction.SOUTH);
		scene.world.moveSection(observer, util.vector.of(0.5, 1.5, -0.5), 0);
		scene.world.rotateSection(observer, 0, 30 - 180, 0, 0);
		scene.idle(20);
		scene.world.moveSection(observer, util.vector.of(-0.5, -1.5, 0.5), 10);
		scene.world.rotateSection(observer, 0, -30 + 180, 0, 10);
		scene.world.showSection(util.select.position(4, 1, 0), Direction.SOUTH);

		Selection observerRedstone = util.select.fromTo(4, 1, 1, 4, 1, 0);
		for (int i = 0; i < 6; i++) {
			scene.world.createItemEntity(topOf, util.vector.of(0, 0.1, 0), copper);
			scene.idle(12);
			scene.world.modifyEntities(ItemEntity.class, Entity::discard);
			scene.world.createItemOnBeltLike(ejectorPos, Direction.UP, copper);
			scene.idle(1);
			scene.world.toggleRedstonePower(observerRedstone);
			scene.effects.indicateRedstone(util.grid.at(4, 1, 1));
			scene.idle(3);
			scene.world.toggleRedstonePower(observerRedstone);
			scene.idle(16);
			if (i == 3)
				scene.markAsFinished();
			if (i == 1) {
				scene.overlay.showText(60)
					.attachKeyFrame()
					.pointAt(util.vector.blockSurface(util.grid.at(4, 1, 1), Direction.NORTH))
					.placeNearTarget()
					.text("Observers can detect when Ejectors activate");
			}
		}

	}

}
