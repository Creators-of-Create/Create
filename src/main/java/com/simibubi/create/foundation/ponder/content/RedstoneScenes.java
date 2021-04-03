package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerTileEntity;
import com.simibubi.create.content.logistics.block.diodes.AdjustablePulseRepeaterTileEntity;
import com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterBlock;
import com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterTileEntity;
import com.simibubi.create.content.logistics.block.diodes.PoweredLatchBlock;
import com.simibubi.create.content.logistics.block.diodes.PulseRepeaterBlock;
import com.simibubi.create.content.logistics.block.diodes.ToggleLatchBlock;
import com.simibubi.create.content.logistics.block.redstone.AnalogLeverTileEntity;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeTileEntity;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkBlock;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkTileEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.ParrotElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

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
		scene.world.modifyBlock(stickerPos, s -> s.with(StickerBlock.EXTENDED, true), false);
		scene.effects.indicateRedstone(buttonPos);
		scene.world.modifyTileNBT(stickerSelect, StickerTileEntity.class, nbt -> {
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
		scene.world.modifyBlock(stickerPos, s -> s.with(StickerBlock.EXTENDED, false), false);
		scene.effects.indicateRedstone(buttonPos);
		scene.world.modifyTileNBT(stickerSelect, StickerTileEntity.class, nbt -> {
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

	public static void pulseRepeater(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("pulse_repeater", "Controlling signals using Pulse Repeaters");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		BlockPos circuitPos = util.grid.at(2, 1, 2);
		BlockPos leverPos = util.grid.at(4, 1, 2);

		scene.world.showSection(util.select.layersFrom(1)
			.substract(util.select.position(circuitPos)), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.position(circuitPos), Direction.DOWN);
		scene.idle(20);
		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 0, 1, 2));
		scene.world.cycleBlockProperty(circuitPos, PulseRepeaterBlock.PULSING);
		scene.idle(3);
		scene.world.cycleBlockProperty(circuitPos, PulseRepeaterBlock.PULSING);
		scene.world.toggleRedstonePower(util.select.position(1, 1, 2));
		scene.idle(2);
		scene.world.toggleRedstonePower(util.select.position(0, 1, 2));

		scene.idle(15);
		scene.overlay.showText(70)
			.text("Pulse Repeaters will shorten any redstone signal to a single pulse")
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector.topOf(util.grid.at(0, 1, 2)));
		scene.idle(60);

		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(20);
		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 0, 1, 2));
		scene.world.cycleBlockProperty(circuitPos, PulseRepeaterBlock.PULSING);
		scene.idle(3);
		scene.world.cycleBlockProperty(circuitPos, PulseRepeaterBlock.PULSING);
		scene.world.toggleRedstonePower(util.select.position(1, 1, 2));
		scene.idle(2);
		scene.world.toggleRedstonePower(util.select.position(0, 1, 2));
	}

	public static void adjustableRepeater(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("adjustable_repeater", "Controlling signals using Adjustable Repeaters");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		BlockPos circuitPos = util.grid.at(2, 1, 2);
		BlockPos leverPos = util.grid.at(4, 1, 2);

		scene.world.modifyTileNBT(util.select.position(circuitPos), AdjustableRepeaterTileEntity.class,
			nbt -> nbt.putInt("ScrollValue", 30));
		scene.world.showSection(util.select.layersFrom(1)
			.substract(util.select.position(circuitPos)), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.position(circuitPos), Direction.DOWN);
		scene.idle(20);

		Vec3d circuitTop = util.vector.blockSurface(circuitPos, Direction.DOWN)
			.add(0, 3 / 16f, 0);
		scene.overlay.showText(70)
			.text("Adjustable Repeaters behave similarly to regular Repeaters")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.idle(60);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(30);
		scene.world.cycleBlockProperty(circuitPos, AdjustableRepeaterBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(15);

		scene.overlay.showText(40)
			.text("They charge up for a set time...")
			.placeNearTarget()
			.pointAt(util.vector.topOf(util.grid.at(0, 1, 2)));
		scene.idle(50);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(30);
		scene.world.cycleBlockProperty(circuitPos, AdjustableRepeaterBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(15);

		scene.overlay.showText(40)
			.text("...and cool down for the same duration")
			.placeNearTarget()
			.pointAt(util.vector.topOf(util.grid.at(0, 1, 2)));
		scene.idle(50);

		scene.overlay.showRepeaterScrollInput(circuitPos, 60);
		scene.overlay.showControls(new InputWindowElement(circuitTop, Pointing.DOWN).scroll(), 60);
		scene.idle(10);
		scene.overlay.showText(60)
			.text("Using the mouse wheel, the charge time can be configured")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.world.modifyTileNBT(util.select.position(circuitPos), AdjustableRepeaterTileEntity.class,
			nbt -> nbt.putInt("ScrollValue", 120));
		scene.idle(70);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(60);
		scene.overlay.showText(50)
			.text("Configured delays can range up to 30 minutes")
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.idle(60);
		scene.world.cycleBlockProperty(circuitPos, AdjustableRepeaterBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(15);

	}

	public static void adjustablePulseRepeater(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("adjustable_pulse_repeater", "Controlling signals using Adjustable Pulse Repeaters");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		BlockPos circuitPos = util.grid.at(2, 1, 2);
		BlockPos leverPos = util.grid.at(4, 1, 2);

		scene.world.modifyTileNBT(util.select.position(circuitPos), AdjustablePulseRepeaterTileEntity.class,
			nbt -> nbt.putInt("ScrollValue", 30));
		scene.world.showSection(util.select.layersFrom(1)
			.substract(util.select.position(circuitPos)), Direction.UP);
		scene.idle(10);
		scene.world.showSection(util.select.position(circuitPos), Direction.DOWN);
		scene.idle(20);

		Vec3d circuitTop = util.vector.blockSurface(circuitPos, Direction.DOWN)
			.add(0, 3 / 16f, 0);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(30);
		scene.world.cycleBlockProperty(circuitPos, AdjustableRepeaterBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(3);
		scene.world.toggleRedstonePower(util.select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(15);

		scene.overlay.showText(60)
			.text("Adjustable Pulse Repeaters emit a short pulse at a delay")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(circuitTop);

		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(70);

		scene.overlay.showRepeaterScrollInput(circuitPos, 60);
		scene.overlay.showControls(new InputWindowElement(circuitTop, Pointing.DOWN).scroll(), 60);
		scene.idle(10);
		scene.overlay.showText(60)
			.text("Using the mouse wheel, the charge time can be configured")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.world.modifyTileNBT(util.select.position(circuitPos), AdjustablePulseRepeaterTileEntity.class,
			nbt -> nbt.putInt("ScrollValue", 120));
		scene.idle(70);

		scene.effects.indicateRedstone(leverPos);
		scene.world.toggleRedstonePower(util.select.fromTo(4, 1, 2, 2, 1, 2));
		scene.idle(60);
		scene.overlay.showText(50)
			.text("Configured delays can range up to 30 minutes")
			.placeNearTarget()
			.pointAt(circuitTop);
		scene.idle(60);
		scene.world.cycleBlockProperty(circuitPos, AdjustableRepeaterBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.fromTo(1, 1, 2, 0, 1, 2));
		scene.idle(3);
		scene.world.toggleRedstonePower(util.select.fromTo(1, 1, 2, 0, 1, 2));
	}

	public static void poweredLatch(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("powered_latch", "Controlling signals using the Powered Latch");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);

		BlockPos circuitPos = util.grid.at(2, 1, 2);
		BlockPos buttonPos = util.grid.at(4, 1, 2);
		Vec3d circuitTop = util.vector.blockSurface(circuitPos, Direction.DOWN)
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

		AxisAlignedBB bb = new AxisAlignedBB(circuitPos).grow(-.48f, -.45f, -.05f)
			.offset(.575, -.45, 0);
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

		bb = new AxisAlignedBB(circuitPos).grow(-.05f, -.45f, -.48f)
			.offset(0, -.45, .575);
		AxisAlignedBB bb2 = new AxisAlignedBB(circuitPos).grow(-.05f, -.45f, -.48f)
			.offset(0, -.45, -.575);
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
		Vec3d circuitTop = util.vector.blockSurface(circuitPos, Direction.DOWN)
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

		AxisAlignedBB bb = new AxisAlignedBB(circuitPos).grow(-.48f, -.45f, -.05f)
			.offset(.575, -.45, 0);
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
		Vec3d leverVec = util.vector.centerOf(leverPos)
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

		IntegerProperty power = RedstoneWireBlock.POWER;
		scene.overlay.showControls(new InputWindowElement(leverVec, Pointing.DOWN).rightClick(), 40);
		scene.idle(7);
		for (int i = 0; i < 7; i++) {
			scene.idle(2);
			final int state = i + 1;
			scene.world.modifyTileNBT(leverSelection, AnalogLeverTileEntity.class, nbt -> nbt.putInt("State", state));
			scene.world.modifyBlock(wireLocations[i], s -> s.with(power, 7 - state), false);
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
				scene.world.modifyTileNBT(leverSelection, AnalogLeverTileEntity.class,
					nbt -> nbt.putInt("State", state));
				scene.effects.indicateRedstone(wireLocations[i]);
			}
			scene.world.modifyBlock(wireLocations[i], s -> s.with(power, state > 2 ? 0 : 3 - state), false);
		}
		scene.world.modifyBlock(wireLocations[0], s -> s.with(power, 3), false);
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
				scene.world.modifyTileNBT(leverSelection, AnalogLeverTileEntity.class,
					nbt -> nbt.putInt("State", state));
				scene.effects.indicateRedstone(wireLocations[i]);
			}
			scene.world.modifyBlock(wireLocations[i], s -> s.with(power, 15 - state), false);
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
		scene.world.modifyTileNBT(util.select.position(2, 1, 1), AnalogLeverTileEntity.class,
			nbt -> nbt.putInt("State", 11));
		scene.world.modifyBlock(util.grid.at(2, 1, 2), s -> s.with(RedstoneWireBlock.POWER, 11), false);
		scene.world.modifyTileNBT(tubes, NixieTubeTileEntity.class, nbt -> nbt.putInt("RedstoneStrength", 11));
		scene.idle(20);

		Vec3d centerTube = util.vector.centerOf(2, 1, 3);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.text("When powered by Redstone, Nixie Tubes will display the redstone signals' strength")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 1, 3), Direction.WEST));
		scene.idle(70);

		scene.world.hideSection(util.select.position(2, 1, 3), Direction.UP);
		scene.idle(5);
		scene.world.hideSection(util.select.fromTo(2, 1, 1, 2, 1, 2), Direction.NORTH);
		scene.idle(10);
		scene.world.modifyTileNBT(tubes, NixieTubeTileEntity.class, nbt -> nbt.putInt("RedstoneStrength", 0));
		scene.world.showSection(tubes, Direction.DOWN);
		scene.idle(20);

		scene.overlay.showControls(new InputWindowElement(centerTube.add(1, .35, 0), Pointing.DOWN).rightClick()
			.withItem(new ItemStack(Items.NAME_TAG)), 40);
		scene.idle(7);

		ITextComponent component = new StringTextComponent("CREATE");
		for (int i = 0; i < 3; i++) {
			final int index = i;
			scene.world.modifyTileNBT(util.select.position(3 - i, 1, 3), NixieTubeTileEntity.class, nbt -> {
				nbt.putString("RawCustomText", component.getFormattedText());
				nbt.putString("CustomText", ITextComponent.Serializer.toJson(component));
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
			.text("Using name tags edited with an anvil, custom text can be displayed")
			.pointAt(util.vector.topOf(util.grid.at(3, 1, 3))
				.add(.25, -.05f, 0));
		scene.idle(70);
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
		Vec3d link1Vec = util.vector.blockSurface(link1Pos, Direction.DOWN)
			.add(0, 3 / 16f, 0);
		Vec3d link2Vec = util.vector.blockSurface(link2Pos, Direction.SOUTH)
			.add(0, 0, -3 / 16f);
		Vec3d link3Vec = util.vector.blockSurface(link3Pos, Direction.SOUTH)
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

		Vec3d frontSlot = link1Vec.add(.18, -.05, -.15);
		Vec3d backSlot = link1Vec.add(.18, -.05, .15);
		Vec3d top2Slot = link2Vec.add(-.09, .15, 0);
		Vec3d bottom2Slot = link2Vec.add(-.09, -.2, 0);
		Vec3d top3Slot = link3Vec.add(-.09, .15, 0);
		Vec3d bottom3Slot = link3Vec.add(-.09, -.2, 0);

		scene.addKeyframe();
		scene.idle(10);
		scene.overlay.showFilterSlotInput(frontSlot, 100);
		scene.overlay.showFilterSlotInput(backSlot, 100);
		scene.idle(10);

		scene.overlay.showText(50)
			.text("Placing items in the two slots can specify a Frequency")
			.placeNearTarget()
			.pointAt(backSlot);
		scene.idle(60);

		ItemStack iron = new ItemStack(Items.IRON_INGOT);
		ItemStack gold = new ItemStack(Items.GOLD_INGOT);
		ItemStack sapling = new ItemStack(Items.OAK_SAPLING);

		scene.overlay.showControls(new InputWindowElement(backSlot, Pointing.DOWN).withItem(iron), 40);
		scene.idle(7);
		scene.overlay.showControls(new InputWindowElement(frontSlot, Pointing.UP).withItem(sapling), 40);
		scene.world.modifyTileNBT(link1Select, RedstoneLinkTileEntity.class,
			nbt -> nbt.put("FrequencyLast", iron.write(new CompoundNBT())));
		scene.idle(7);
		scene.world.modifyTileNBT(link1Select, RedstoneLinkTileEntity.class,
			nbt -> nbt.put("FrequencyFirst", sapling.write(new CompoundNBT())));
		scene.idle(20);

		scene.overlay.showControls(new InputWindowElement(top2Slot, Pointing.DOWN).withItem(iron), 40);
		scene.idle(7);
		scene.overlay.showControls(new InputWindowElement(bottom2Slot, Pointing.UP).withItem(sapling), 40);
		scene.world.modifyTileNBT(link2Select, RedstoneLinkTileEntity.class,
			nbt -> nbt.put("FrequencyLast", iron.write(new CompoundNBT())));
		scene.idle(7);
		scene.world.modifyTileNBT(link2Select, RedstoneLinkTileEntity.class,
			nbt -> nbt.put("FrequencyFirst", sapling.write(new CompoundNBT())));
		scene.idle(20);

		scene.overlay.showControls(new InputWindowElement(top3Slot, Pointing.DOWN).withItem(gold), 40);
		scene.idle(7);
		scene.overlay.showControls(new InputWindowElement(bottom3Slot, Pointing.UP).withItem(sapling), 40);
		scene.world.modifyTileNBT(link3Select, RedstoneLinkTileEntity.class,
			nbt -> nbt.put("FrequencyLast", gold.write(new CompoundNBT())));
		scene.idle(7);
		scene.world.modifyTileNBT(link3Select, RedstoneLinkTileEntity.class,
			nbt -> nbt.put("FrequencyFirst", sapling.write(new CompoundNBT())));
		scene.idle(20);

		scene.world.toggleRedstonePower(redstone);
		scene.effects.indicateRedstone(leverPos);
		scene.idle(5);
		scene.world.toggleRedstonePower(util.select.fromTo(1, 2, 2, 1, 2, 3));
		scene.effects.indicateRedstone(link2Pos);
		scene.overlay.showText(90)
			.attachKeyFrame()
			.text("Only the links with matching Frequencies will communicate")
			.placeNearTarget()
			.pointAt(link2Vec);
		scene.idle(100);
	}

}
