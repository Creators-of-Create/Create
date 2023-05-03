package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerBlockEntity;
import com.simibubi.create.content.curiosities.clipboard.ClipboardOverrides;
import com.simibubi.create.content.curiosities.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.content.logistics.block.diodes.BrassDiodeBlock;
import com.simibubi.create.content.logistics.block.diodes.PoweredLatchBlock;
import com.simibubi.create.content.logistics.block.diodes.PulseExtenderBlockEntity;
import com.simibubi.create.content.logistics.block.diodes.PulseRepeaterBlockEntity;
import com.simibubi.create.content.logistics.block.diodes.ToggleLatchBlock;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverBlockEntity;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeBlock;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeBlockEntity;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkBlock;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkBlockEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RedstoneScenes {

	public static void sticker(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("sticker", "Attaching blocks using the Sticker");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		Selection redstone = util.select.fromTo(0, 2, 2, 2, 2, 2);
		BlockPos stickerPos = util.grid.at(2, 2, 2);
		Selection stickerSelect = util.select.position(stickerPos);
		BlockPos buttonPos = util.grid.at(0, 2, 2);
		BlockPos bearingPos = util.grid.at(2, 1, 2);

		scene.world.showSection(util.select.fromTo(2, 1, 2, 0, 2, 2)
			.substract(stickerSelect), Direction.DOWN);
		scene.idle(10);
		ElementLink<WorldSectionElement> sticker = scene.world.showIndependentSection(stickerSelect, Direction.DOWN);
		scene.idle(10);
		ElementLink<WorldSectionElement> plank =
			scene.world.showIndependentSection(util.select.position(2, 2, 1), Direction.SOUTH);
		scene.world.configureCenterOfRotation(sticker, util.vector.centerOf(stickerPos));
		scene.world.configureCenterOfRotation(plank, util.vector.centerOf(stickerPos));
		scene.overlay.showText(60)
			.text("Stickers are ideal for Redstone-controlled block attachment")
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(stickerPos, Direction.WEST))
			.placeNearTarget();
		scene.idle(70);

		scene.world.toggleRedstonePower(redstone);
		scene.world.modifyBlock(stickerPos, s -> s.setValue(StickerBlock.EXTENDED, true), false);
		scene.effects.indicateRedstone(buttonPos);
		scene.world.modifyBlockEntityNBT(stickerSelect, StickerBlockEntity.class, nbt -> {
		});
		scene.idle(20);

		scene.world.toggleRedstonePower(redstone);
		scene.idle(20);

		scene.overlay.showText(60)
			.text("Upon receiving a signal, it will toggle its state")
			.pointAt(util.vector.blockSurface(stickerPos, Direction.WEST))
			.placeNearTarget();
		scene.idle(70);

		scene.world.rotateBearing(bearingPos, 180 * 3, 80);
		scene.world.rotateSection(sticker, 0, 180 * 3, 0, 80);
		scene.world.rotateSection(plank, 0, 180 * 3, 0, 80);
		scene.overlay.showText(70)
			.text("If it is now moved in a contraption, the block will move with it")
			.pointAt(util.vector.topOf(stickerPos))
			.placeNearTarget();
		scene.idle(90);
		scene.addKeyframe();

		scene.world.toggleRedstonePower(redstone);
		scene.world.modifyBlock(stickerPos, s -> s.setValue(StickerBlock.EXTENDED, false), false);
		scene.effects.indicateRedstone(buttonPos);
		scene.world.modifyBlockEntityNBT(stickerSelect, StickerBlockEntity.class, nbt -> {
		});
		scene.idle(20);

		scene.world.toggleRedstonePower(redstone);
		scene.idle(20);

		scene.overlay.showText(60)
			.text("Toggled once again, the block is no longer attached")
			.pointAt(util.vector.blockSurface(stickerPos, Direction.WEST))
			.placeNearTarget();
		scene.idle(70);

		scene.world.rotateBearing(bearingPos, 180 * 3, 80);
		scene.world.rotateSection(sticker, 0, 180 * 3, 0, 80);
	}

	public static void contact(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("redstone_contact", "Redstone Contacts");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);
		Selection contactAndRedstone = util.select.fromTo(1, 1, 0, 1, 1, 2);
		Selection topContact = util.select.position(1, 2, 2);

		scene.world.toggleRedstonePower(contactAndRedstone);
		scene.world.toggleRedstonePower(topContact);
		scene.world.showSection(contactAndRedstone, Direction.DOWN);

		BlockPos bearingPos = util.grid.at(3, 1, 2);
		scene.idle(25);

		ElementLink<WorldSectionElement> contact = scene.world.showIndependentSection(topContact, Direction.DOWN);
		scene.idle(10);
		scene.world.toggleRedstonePower(topContact);
		scene.world.toggleRedstonePower(contactAndRedstone);
		scene.effects.indicateRedstone(util.grid.at(1, 1, 2));
		scene.idle(10);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.of(1, 2, 2.5))
			.text("Redstone Contacts facing each other will emit a redstone signal");
		scene.idle(70);

		scene.world.showSection(util.select.position(bearingPos), Direction.DOWN);
		scene.idle(10);
		scene.world.showSectionAndMerge(util.select.fromTo(2, 2, 2, 4, 2, 2), Direction.DOWN, contact);
		scene.idle(10);
		scene.effects.superGlue(util.grid.at(1, 2, 2), Direction.EAST, true);
		scene.world.configureCenterOfRotation(contact, util.vector.centerOf(bearingPos));

		int speed = 2;

		scene.idle(10);
		scene.world.rotateBearing(bearingPos, 10, speed);
		scene.world.rotateSection(contact, 0, 10, 0, speed);
		scene.idle(speed);

		scene.world.toggleRedstonePower(topContact);
		scene.world.toggleRedstonePower(contactAndRedstone);
		scene.effects.indicateRedstone(util.grid.at(1, 1, 2));
		scene.world.rotateBearing(bearingPos, 340, 34 * speed);
		scene.world.rotateSection(contact, 0, 340, 0, 34 * speed);
		scene.addKeyframe();
		scene.idle(34 * speed);

		scene.overlay.showText(100)
			.placeNearTarget()
			.pointAt(util.vector.of(1, 1.5, 2.5))
			.text("This still applies when one of them is part of a moving Contraption");

		for (int i = 0; i < 5; i++) {
			scene.world.toggleRedstonePower(topContact);
			scene.world.toggleRedstonePower(contactAndRedstone);
			scene.effects.indicateRedstone(util.grid.at(1, 1, 2));
			scene.world.rotateBearing(bearingPos, 20, 2 * speed);
			scene.world.rotateSection(contact, 0, 20, 0, 2 * speed);
			scene.idle(2 * speed);

			scene.world.toggleRedstonePower(topContact);
			scene.world.toggleRedstonePower(contactAndRedstone);
			scene.world.rotateBearing(bearingPos, 340, 34 * speed);
			scene.world.rotateSection(contact, 0, 340, 0, 34 * speed);
			scene.idle(34 * speed);

			if (i == 0)
				scene.markAsFinished();
		}

		scene.world.toggleRedstonePower(topContact);
		scene.world.toggleRedstonePower(contactAndRedstone);
		scene.world.rotateBearing(bearingPos, 10, speed);
		scene.world.rotateSection(contact, 0, 10, 0, speed);
	}

	public static void pulseExtender(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("pulse_extender", "Controlling signals using Pulse Extenders");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		BlockPos circuitPos = util.grid.at(2, 1, 2);
		BlockPos leverPos = util.grid.at(4, 1, 2);

		scene.world.modifyBlockEntityNBT(util.select.position(circuitPos), PulseExtenderBlockEntity.class,
			nbt -> nbt.putInt("ScrollValue", 30));
		scene.world.showSection(util.select.layersFrom(1)
			.substract(util.select.position(circuitPos)), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.position(circuitPos), Direction.DOWN);
		scene.idle(20);

		Vec3 circuitTop = util.vector.blockSurface(circuitPos, Direction.DOWN)
			.add(0, 3 / 16f, 0);
		scene.overlay.showText(70)
			.text("Pulse Extenders can lengthen a signal passing through")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.idle(60);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(2);
		scene.world.toggleRedstonePower(util.select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(15);

		scene.overlay.showText(60)
			.text("They activate after a short delay...")
			.placeNearTarget()
			.pointAt(util.vector.topOf(util.grid.at(0, 1, 2)));
		scene.idle(50);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(30);
		scene.world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.position(1, 1, 2));
		scene.idle(1);
		scene.world.toggleRedstonePower(util.select.position(0, 1, 2));
		scene.idle(15);

		scene.overlay.showText(40)
			.text("...and cool down for the configured duration")
			.placeNearTarget()
			.pointAt(util.vector.topOf(util.grid.at(0, 1, 2)));
		scene.idle(50);

		scene.overlay.showRepeaterScrollInput(circuitPos, 60);
		scene.overlay.showControls(new InputWindowElement(circuitTop, Pointing.DOWN).rightClick(), 60);
		scene.idle(10);
		scene.overlay.showText(60)
			.text("Using the value panel, the discharge time can be configured")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.world.modifyBlockEntityNBT(util.select.position(circuitPos), PulseExtenderBlockEntity.class,
			nbt -> nbt.putInt("ScrollValue", 120));
		scene.idle(70);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(2);
		scene.world.toggleRedstonePower(util.select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(20);
		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(15);
		scene.overlay.showText(50)
			.text("The configured duration can range up to an hour")
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.idle(70);
		scene.world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.position(1, 1, 2));
		scene.idle(1);
		scene.world.toggleRedstonePower(util.select.position(0, 1, 2));
		scene.idle(15);

	}

	public static void pulseRepeater(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("pulse_repeater", "Controlling signals using Pulse Repeaters");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		BlockPos circuitPos = util.grid.at(2, 1, 2);
		BlockPos leverPos = util.grid.at(4, 1, 2);

		scene.world.modifyBlockEntityNBT(util.select.position(circuitPos), PulseRepeaterBlockEntity.class,
			nbt -> nbt.putInt("ScrollValue", 30));
		scene.world.showSection(util.select.layersFrom(1)
			.substract(util.select.position(circuitPos)), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.position(circuitPos), Direction.DOWN);
		scene.idle(20);

		Vec3 circuitTop = util.vector.blockSurface(circuitPos, Direction.DOWN)
			.add(0, 3 / 16f, 0);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(30);
		scene.world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(2);
		scene.world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.position(1, 1, 2));
		scene.idle(1);
		scene.world.toggleRedstonePower(util.select.position(0, 1, 2));
		scene.idle(15);

		scene.overlay.showText(60)
			.text("Pulse Repeaters emit a short pulse after a delay")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(circuitTop);

		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(70);

		scene.overlay.showRepeaterScrollInput(circuitPos, 60);
		scene.overlay.showControls(new InputWindowElement(circuitTop, Pointing.DOWN).rightClick(), 60);
		scene.idle(10);
		scene.overlay.showText(60)
			.text("Using the value panel, the charge time can be configured")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.world.modifyBlockEntityNBT(util.select.position(circuitPos), PulseRepeaterBlockEntity.class,
			nbt -> nbt.putInt("ScrollValue", 120));
		scene.idle(70);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(60);
		scene.overlay.showText(50)
			.text("Configured delays can range up to an hour")
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.idle(60);
		scene.world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(2);
		scene.world.cycleBlockProperty(circuitPos, BrassDiodeBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.position(1, 1, 2));
		scene.idle(1);
		scene.world.toggleRedstonePower(util.select.position(0, 1, 2));
	}

	public static void poweredLatch(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("powered_latch", "Controlling signals using the Powered Latch");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		BlockPos circuitPos = util.grid.at(2, 1, 2);
		BlockPos buttonPos = util.grid.at(4, 1, 2);
		Vec3 circuitTop = util.vector.blockSurface(circuitPos, Direction.DOWN)
			.add(0, 3 / 16f, 0);

		scene.world.showSection(util.select.layersFrom(1)
			.substract(util.select.position(circuitPos)), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.position(circuitPos), Direction.DOWN);
		scene.idle(20);

		scene.overlay.showText(40)
			.attachKeyFrame()
			.text("Powered Latches are redstone controllable Levers")
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.idle(50);

		scene.effects.indicateRedstone(buttonPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 0, 1, 2));
		scene.world.cycleBlockProperty(circuitPos, PoweredLatchBlock.POWERING);
		scene.idle(30);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 3, 1, 2));

		AABB bb = new AABB(circuitPos).inflate(-.48f, -.45f, -.05f)
			.move(.575, -.45, 0);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb, 40);
		scene.overlay.showText(40)
			.colored(PonderPalette.GREEN)
			.text("Signals at the back switch it on")
			.placeNearTarget()
			.pointAt(bb.getCenter());
		scene.idle(60);

		scene.effects.indicateRedstone(util.grid.at(2, 1, 0));
		scene.world.toggleRedstonePower(util.select.fromTo(2, 1, 0, 2, 1, 1));
		scene.world.toggleRedstonePower(util.select.fromTo(2, 1, 2, 0, 1, 2));
		scene.world.cycleBlockProperty(circuitPos, PoweredLatchBlock.POWERING);
		scene.idle(30);
		scene.world.toggleRedstonePower(util.select.fromTo(2, 1, 0, 2, 1, 1));

		bb = new AABB(circuitPos).inflate(-.05f, -.45f, -.48f)
			.move(0, -.45, .575);
		AABB bb2 = new AABB(circuitPos).inflate(-.05f, -.45f, -.48f)
			.move(0, -.45, -.575);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, bb, bb, 40);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, bb2, bb2, 40);
		scene.overlay.showText(40)
			.colored(PonderPalette.RED)
			.text("Signals from the side switch it back off")
			.placeNearTarget()
			.pointAt(bb2.getCenter());
		scene.idle(50);

		scene.addKeyframe();
		scene.idle(10);
		scene.overlay.showControls(new InputWindowElement(circuitTop, Pointing.DOWN).rightClick(), 40);
		scene.idle(7);
		scene.world.toggleRedstonePower(util.select.fromTo(2, 1, 2, 0, 1, 2));
		scene.world.cycleBlockProperty(circuitPos, PoweredLatchBlock.POWERING);
		scene.idle(10);

		scene.overlay.showText(50)
			.text("Powered latches can also be toggled manually")
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.idle(60);

		scene.overlay.showControls(new InputWindowElement(circuitTop, Pointing.DOWN).rightClick(), 40);
		scene.idle(7);
		scene.world.toggleRedstonePower(util.select.fromTo(2, 1, 2, 0, 1, 2));
		scene.world.cycleBlockProperty(circuitPos, PoweredLatchBlock.POWERING);
		scene.idle(10);
	}

	public static void poweredToggleLatch(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("powered_toggle_latch", "Controlling signals using the Powered Toggle Latch");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		BlockPos circuitPos = util.grid.at(2, 1, 2);
		BlockPos buttonPos = util.grid.at(4, 1, 2);
		Vec3 circuitTop = util.vector.blockSurface(circuitPos, Direction.DOWN)
			.add(0, 3 / 16f, 0);

		scene.world.showSection(util.select.layersFrom(1)
			.substract(util.select.position(circuitPos)), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.position(circuitPos), Direction.DOWN);
		scene.idle(20);

		scene.overlay.showText(40)
			.attachKeyFrame()
			.text("Powered Toggle Latches are redstone controllable Levers")
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.idle(50);

		scene.effects.indicateRedstone(buttonPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 0, 1, 2));
		scene.world.cycleBlockProperty(circuitPos, ToggleLatchBlock.POWERING);
		scene.idle(30);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 3, 1, 2));

		AABB bb = new AABB(circuitPos).inflate(-.48f, -.45f, -.05f)
			.move(.575, -.45, 0);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb, 40);
		scene.overlay.showText(40)
			.colored(PonderPalette.GREEN)
			.text("Signals at the back will toggle its state")
			.placeNearTarget()
			.pointAt(bb.getCenter());
		scene.idle(60);

		scene.effects.indicateRedstone(buttonPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 0, 1, 2));
		scene.world.cycleBlockProperty(circuitPos, ToggleLatchBlock.POWERING);
		scene.idle(30);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 3, 1, 2));
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.RED, bb, bb, 40);
		scene.overlay.showText(30)
			.colored(PonderPalette.RED)
			.text("...on and back off")
			.placeNearTarget()
			.pointAt(bb.getCenter());
		scene.idle(50);

		scene.addKeyframe();
		scene.idle(10);
		scene.overlay.showControls(new InputWindowElement(circuitTop, Pointing.DOWN).rightClick(), 40);
		scene.idle(7);
		scene.world.toggleRedstonePower(util.select.fromTo(2, 1, 2, 0, 1, 2));
		scene.world.cycleBlockProperty(circuitPos, ToggleLatchBlock.POWERING);
		scene.idle(10);

		scene.overlay.showText(50)
			.text("Powered toggle latches can also be toggled manually")
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.idle(60);

		scene.overlay.showControls(new InputWindowElement(circuitTop, Pointing.DOWN).rightClick(), 40);
		scene.idle(7);
		scene.world.toggleRedstonePower(util.select.fromTo(2, 1, 2, 0, 1, 2));
		scene.world.cycleBlockProperty(circuitPos, ToggleLatchBlock.POWERING);
		scene.idle(10);
	}

	public static void analogLever(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("analog_lever", "Controlling signals using the Analog Lever");
		scene.configureBasePlate(0, 0, 5);

		BlockPos[] wireLocations = new BlockPos[] { util.grid.at(2, 1, 1), util.grid.at(2, 1, 0), util.grid.at(1, 1, 0),
			util.grid.at(0, 1, 0), util.grid.at(0, 1, 1), util.grid.at(0, 1, 2), util.grid.at(0, 1, 3),
			util.grid.at(0, 1, 4), util.grid.at(1, 1, 4), util.grid.at(2, 1, 4), util.grid.at(3, 1, 4),
			util.grid.at(4, 1, 4), util.grid.at(4, 1, 3), util.grid.at(4, 1, 2), util.grid.at(4, 1, 1) };

		Selection leverSelection = util.select.fromTo(2, 1, 2, 2, 2, 2);
		Selection lamp = util.select.position(4, 1, 0);
		BlockPos leverPos = util.grid.at(2, 2, 2);
		Vec3 leverVec = util.vector.centerOf(leverPos)
			.add(0, -.25, 0);

		scene.world.showSection(util.select.layersFrom(0)
			.substract(lamp)
			.substract(leverSelection), Direction.UP);
		scene.idle(5);
		scene.world.showSection(lamp, Direction.DOWN);
		scene.idle(10);

		scene.world.showSection(leverSelection, Direction.DOWN);
		scene.idle(20);

		scene.overlay.showText(60)
			.text("Analog Levers make for a compact and precise source of redstone power")
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(leverVec);
		scene.idle(70);

		IntegerProperty power = RedStoneWireBlock.POWER;
		scene.overlay.showControls(new InputWindowElement(leverVec, Pointing.DOWN).rightClick(), 40);
		scene.idle(7);
		for (int i = 0; i < 7; i++) {
			scene.idle(2);
			final int state = i + 1;
			scene.world.modifyBlockEntityNBT(leverSelection, AnalogLeverBlockEntity.class,
				nbt -> nbt.putInt("State", state));
			scene.world.modifyBlock(wireLocations[i], s -> s.setValue(power, 7 - state), false);
			scene.effects.indicateRedstone(wireLocations[i]);
		}
		scene.idle(20);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("Right-click to increase its analog power output")
			.placeNearTarget()
			.pointAt(leverVec);
		scene.idle(70);

		scene.overlay.showControls(new InputWindowElement(leverVec, Pointing.DOWN).rightClick()
			.whileSneaking(), 40);
		scene.idle(7);
		for (int i = 7; i > 0; i--) {
			scene.idle(2);
			final int state = i - 1;
			if (i > 3) {
				scene.world.modifyBlockEntityNBT(leverSelection, AnalogLeverBlockEntity.class,
					nbt -> nbt.putInt("State", state));
				scene.effects.indicateRedstone(wireLocations[i]);
			}
			scene.world.modifyBlock(wireLocations[i], s -> s.setValue(power, state > 2 ? 0 : 3 - state), false);
		}
		scene.world.modifyBlock(wireLocations[0], s -> s.setValue(power, 3), false);
		scene.idle(20);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("Right-click while Sneaking to decrease the power output again")
			.placeNearTarget()
			.pointAt(leverVec);
		scene.idle(70);

		scene.overlay.showControls(new InputWindowElement(leverVec, Pointing.DOWN).rightClick(), 40);
		scene.idle(7);
		for (int i = 0; i < 15; i++) {
			scene.idle(2);
			final int state = i + 1;
			if (i >= 4) {
				scene.world.modifyBlockEntityNBT(leverSelection, AnalogLeverBlockEntity.class,
					nbt -> nbt.putInt("State", state));
				scene.effects.indicateRedstone(wireLocations[i]);
			}
			scene.world.modifyBlock(wireLocations[i], s -> s.setValue(power, 15 - state), false);
		}

		scene.world.toggleRedstonePower(lamp);
		scene.effects.indicateRedstone(leverPos);
		scene.effects.indicateRedstone(util.grid.at(4, 1, 1));
		scene.idle(20);
	}

	public static void nixieTube(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("nixie_tube", "Using Nixie Tubes");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0)
			.add(util.select.fromTo(2, 1, 1, 2, 1, 2)), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.position(2, 1, 3), Direction.DOWN);
		scene.idle(20);

		Selection tubes = util.select.fromTo(3, 1, 3, 1, 1, 3);

		scene.effects.indicateRedstone(util.grid.at(2, 1, 1));
		scene.world.modifyBlockEntityNBT(util.select.position(2, 1, 1), AnalogLeverBlockEntity.class,
			nbt -> nbt.putInt("State", 11));
		scene.world.modifyBlock(util.grid.at(2, 1, 2), s -> s.setValue(RedStoneWireBlock.POWER, 11), false);
		scene.world.modifyBlockEntityNBT(tubes, NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 11));
		scene.idle(20);

		Vec3 centerTube = util.vector.centerOf(2, 1, 3);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("When powered by Redstone, Nixie Tubes will display the signal strength")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 1, 3), Direction.WEST));
		scene.idle(70);

		scene.world.hideSection(util.select.position(2, 1, 3), Direction.UP);
		scene.idle(5);
		scene.world.hideSection(util.select.fromTo(2, 1, 1, 2, 1, 2), Direction.NORTH);
		scene.idle(10);
		scene.world.modifyBlockEntityNBT(tubes, NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 0));
		scene.world.showSection(tubes, Direction.DOWN);
		scene.idle(20);

		ItemStack clipboard = AllBlocks.CLIPBOARD.asStack();
		ClipboardOverrides.switchTo(ClipboardType.WRITTEN, clipboard);
		scene.overlay.showControls(new InputWindowElement(centerTube.add(1, .35, 0), Pointing.DOWN).rightClick()
			.withItem(clipboard), 40);
		scene.idle(7);

		Component component = Components.literal("CREATE");
		for (int i = 0; i < 3; i++) {
			final int index = i;
			scene.world.modifyBlockEntityNBT(util.select.position(3 - i, 1, 3), NixieTubeBlockEntity.class, nbt -> {
				nbt.putString("RawCustomText", component.getString());
				nbt.putString("CustomText", Component.Serializer.toJson(component));
				nbt.putInt("CustomTextIndex", index);
			});
		}

		scene.idle(10);
		scene.world.showSection(util.select.position(4, 1, 3), Direction.DOWN);
		scene.idle(10);
		scene.special.createBirb(util.vector.topOf(util.grid.at(0, 0, 3)), ParrotElement.DancePose::new);

		scene.idle(20);
		scene.overlay.showText(80)
			.attachKeyFrame()
			.placeNearTarget()
			.text("Using written Clipboards, custom text can be displayed")
			.pointAt(util.vector.topOf(util.grid.at(3, 1, 3))
				.add(-.75, -.05f, 0));
		scene.idle(90);

		InputWindowElement input =
			new InputWindowElement(util.vector.blockSurface(util.grid.at(3, 1, 3), Direction.UP), Pointing.DOWN)
				.withItem(new ItemStack(Items.BLUE_DYE));
		scene.overlay.showControls(input, 30);
		scene.idle(7);
		scene.world.setBlocks(util.select.fromTo(1, 1, 3, 3, 1, 3), AllBlocks.NIXIE_TUBES.get(DyeColor.BLUE)
			.getDefaultState()
			.setValue(NixieTubeBlock.FACING, Direction.WEST), false);
		scene.idle(10);
		scene.overlay.showText(80)
			.colored(PonderPalette.BLUE)
			.text("Right-Click with Dye to change their display colour")
			.attachKeyFrame()
			.pointAt(util.vector.topOf(util.grid.at(3, 1, 3))
				.add(-.75, -.05f, 0))
			.placeNearTarget();
		scene.idle(60);
	}

	public static void redstoneLink(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("redstone_link", "Using Redstone Links");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0)
			.add(util.select.fromTo(3, 1, 1, 2, 1, 1)), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(4, 1, 3, 0, 2, 3), Direction.DOWN);
		scene.idle(10);

		Selection redstone = util.select.fromTo(3, 1, 1, 1, 1, 1);
		BlockPos leverPos = util.grid.at(3, 1, 1);
		BlockPos link1Pos = util.grid.at(1, 1, 1);
		BlockPos link2Pos = util.grid.at(1, 2, 2);
		BlockPos link3Pos = util.grid.at(3, 2, 2);
		Selection link1Select = util.select.position(link1Pos);
		Selection link2Select = util.select.position(link2Pos);
		Selection link3Select = util.select.position(link3Pos);
		Vec3 link1Vec = util.vector.blockSurface(link1Pos, Direction.DOWN)
			.add(0, 3 / 16f, 0);
		Vec3 link2Vec = util.vector.blockSurface(link2Pos, Direction.SOUTH)
			.add(0, 0, -3 / 16f);
		Vec3 link3Vec = util.vector.blockSurface(link3Pos, Direction.SOUTH)
			.add(0, 0, -3 / 16f);

		scene.world.showSection(link1Select, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(link2Select, Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(link3Select, Direction.DOWN);
		scene.idle(10);

		scene.overlay.showText(50)
			.attachKeyFrame()
			.text("Redstone Links can transmit redstone signals wirelessly")
			.placeNearTarget()
			.pointAt(link1Vec);
		scene.idle(60);

		scene.overlay.showControls(new InputWindowElement(link2Vec, Pointing.UP).rightClick()
			.whileSneaking(), 40);
		scene.idle(7);
		scene.world.modifyBlock(link2Pos, s -> s.cycle(RedstoneLinkBlock.RECEIVER), true);
		scene.idle(10);
		scene.overlay.showText(50)
			.text("Right-click while Sneaking to toggle receive mode")
			.placeNearTarget()
			.pointAt(link2Vec);
		scene.idle(60);

		scene.overlay.showControls(new InputWindowElement(link3Vec, Pointing.UP).rightClick()
			.withWrench(), 40);
		scene.idle(7);
		scene.world.modifyBlock(link3Pos, s -> s.cycle(RedstoneLinkBlock.RECEIVER), true);
		scene.idle(10);
		scene.overlay.showText(50)
			.text("A simple Right-click with a Wrench can do the same")
			.placeNearTarget()
			.pointAt(link3Vec);
		scene.idle(70);

		scene.addKeyframe();
		scene.idle(10);
		scene.world.toggleRedstonePower(redstone);
		scene.effects.indicateRedstone(leverPos);
		scene.idle(5);
		scene.world.toggleRedstonePower(util.select.fromTo(3, 2, 3, 1, 2, 2));
		scene.effects.indicateRedstone(link2Pos);
		scene.effects.indicateRedstone(link3Pos);

		scene.idle(10);
		scene.overlay.showText(70)
			.colored(PonderPalette.GREEN)
			.text("Receivers emit the redstone power of transmitters within 128 blocks")
			.placeNearTarget()
			.pointAt(link2Vec);
		scene.idle(80);
		scene.world.toggleRedstonePower(redstone);
		scene.idle(5);
		scene.world.toggleRedstonePower(util.select.fromTo(3, 2, 3, 1, 2, 2));
		scene.idle(20);

		Vec3 frontSlot = link1Vec.add(0, .025, -.15);
		Vec3 backSlot = link1Vec.add(0, .025, .15);
		Vec3 top2Slot = link2Vec.add(0, .15, 0);
		Vec3 bottom2Slot = link2Vec.add(0, -.2, 0);
		Vec3 top3Slot = link3Vec.add(0, .15, 0);
		Vec3 bottom3Slot = link3Vec.add(0, -.2, 0);

		scene.addKeyframe();
		scene.idle(10);
		scene.overlay.showFilterSlotInput(frontSlot, Direction.UP, 100);
		scene.overlay.showFilterSlotInput(backSlot, Direction.UP, 100);
		scene.idle(10);

		scene.overlay.showText(50)
			.text("Placing items in the two slots can specify a Frequency")
			.placeNearTarget()
			.pointAt(backSlot);
		scene.idle(60);

		ItemStack iron = new ItemStack(Items.IRON_INGOT);
		ItemStack gold = new ItemStack(Items.GOLD_INGOT);
		ItemStack sapling = new ItemStack(Items.OAK_SAPLING);

		scene.overlay.showControls(new InputWindowElement(frontSlot, Pointing.UP).withItem(iron), 30);
		scene.idle(7);
		scene.overlay.showControls(new InputWindowElement(backSlot, Pointing.DOWN).withItem(sapling), 30);
		scene.world.modifyBlockEntityNBT(link1Select, RedstoneLinkBlockEntity.class,
			nbt -> nbt.put("FrequencyLast", iron.save(new CompoundTag())));
		scene.idle(7);
		scene.world.modifyBlockEntityNBT(link1Select, RedstoneLinkBlockEntity.class,
			nbt -> nbt.put("FrequencyFirst", sapling.save(new CompoundTag())));
		scene.idle(20);

		scene.overlay.showControls(new InputWindowElement(bottom2Slot, Pointing.UP).withItem(iron), 30);
		scene.idle(7);
		scene.overlay.showControls(new InputWindowElement(top2Slot, Pointing.DOWN).withItem(sapling), 30);
		scene.world.modifyBlockEntityNBT(link2Select, RedstoneLinkBlockEntity.class,
			nbt -> nbt.put("FrequencyLast", iron.save(new CompoundTag())));
		scene.idle(7);
		scene.world.modifyBlockEntityNBT(link2Select, RedstoneLinkBlockEntity.class,
			nbt -> nbt.put("FrequencyFirst", sapling.save(new CompoundTag())));
		scene.idle(20);

		scene.overlay.showControls(new InputWindowElement(bottom3Slot, Pointing.UP).withItem(gold), 30);
		scene.idle(7);
		scene.overlay.showControls(new InputWindowElement(top3Slot, Pointing.DOWN).withItem(sapling), 30);
		scene.world.modifyBlockEntityNBT(link3Select, RedstoneLinkBlockEntity.class,
			nbt -> nbt.put("FrequencyLast", gold.save(new CompoundTag())));
		scene.idle(7);
		scene.world.modifyBlockEntityNBT(link3Select, RedstoneLinkBlockEntity.class,
			nbt -> nbt.put("FrequencyFirst", sapling.save(new CompoundTag())));
		scene.idle(20);

		scene.world.toggleRedstonePower(redstone);
		scene.effects.indicateRedstone(leverPos);
		scene.idle(2);
		scene.world.toggleRedstonePower(util.select.fromTo(1, 2, 2, 1, 2, 3));
		scene.overlay.showText(90)
			.attachKeyFrame()
			.text("Only the links with matching Frequencies will communicate")
			.placeNearTarget()
			.pointAt(link2Vec);

		scene.idle(30);
		for (int i = 0; i < 4; i++) {
			if (i % 2 == 1)
				scene.effects.indicateRedstone(leverPos);
			scene.world.toggleRedstonePower(redstone);
			scene.idle(2);
			scene.world.toggleRedstonePower(util.select.fromTo(1, 2, 2, 1, 2, 3));
			scene.idle(20);
		}
	}

}
