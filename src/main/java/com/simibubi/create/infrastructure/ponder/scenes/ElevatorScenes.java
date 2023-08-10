package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlock;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlock;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.utility.Pointing;
import net.createmod.catnip.utility.lang.Components;
import net.createmod.ponder.foundation.ElementLink;
import net.createmod.ponder.foundation.PonderPalette;
import net.createmod.ponder.foundation.SceneBuilder;
import net.createmod.ponder.foundation.SceneBuildingUtil;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.ponder.foundation.element.WorldSectionElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public class ElevatorScenes {

	public static void elevator(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("elevator_pulley", "Using the Elevator Pulley");
		scene.configureBasePlate(1, 0, 5);
		scene.scaleSceneView(.85f);
		scene.removeShadow();
		scene.setSceneOffsetY(-1.5f);

		Selection topFloor = util.select.fromTo(5, 12, 0, 1, 12, 4);
		Selection midFloor = util.select.fromTo(5, 6, 0, 1, 6, 4);
		Selection botFloor = util.select.fromTo(5, 0, 0, 1, 0, 4);
		Selection topCutout = util.select.fromTo(4, 12, 3, 2, 12, 1);
		Selection midCutout = util.select.fromTo(4, 6, 3, 2, 6, 1);
		Selection botCutout = util.select.fromTo(4, 0, 3, 2, 0, 1);
		BlockPos topContact = util.grid.at(1, 13, 2);
		BlockPos midContact = util.grid.at(1, 7, 2);
		BlockPos botContact = util.grid.at(1, 1, 2);
		Selection outputRedstone = util.select.fromTo(0, 0, 2, 0, 1, 2);
		Selection topInput = util.select.fromTo(1, 13, 0, 1, 13, 1);
		Selection midInput = util.select.fromTo(1, 7, 0, 1, 7, 1);
		Selection botInput = util.select.fromTo(1, 1, 0, 1, 1, 1);
		Selection pole = util.select.fromTo(6, 0, 3, 6, 17, 3)
			.add(util.select.position(5, 17, 3));
		Selection cog = util.select.fromTo(5, 18, 2, 4, 18, 2);
		BlockPos nixiePos = util.grid.at(4, 13, 0);
		BlockPos linkPos = util.grid.at(1, 14, 2);
		BlockPos doorPos = util.grid.at(3, 14, 1);
		Selection controls = util.select.position(4, 14, 2);
		BlockPos pulleyPos = util.grid.at(3, 18, 2);

		ElementLink<WorldSectionElement> camLink = scene.world.showIndependentSection(topFloor, Direction.UP);
		scene.world.moveSection(camLink, util.vector.of(0, -12, 0), 0);
		scene.world.setKineticSpeed(util.select.position(pulleyPos), 0);
		scene.idle(5);
		ElementLink<WorldSectionElement> pulleyLink =
			scene.world.showIndependentSection(util.select.position(pulleyPos), Direction.DOWN);
		scene.world.moveSection(pulleyLink, util.vector.of(0, -16, 0), 0);
		scene.idle(15);

		scene.overlay.showText(60)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 2, 2), Direction.WEST))
			.text("Elevator Pulleys can move structures vertically between marked locations");
		scene.idle(60);
		scene.world.moveSection(pulleyLink, util.vector.of(0, 4, 0), 20);
		scene.world.setBlocks(topCutout, Blocks.AIR.defaultBlockState(), false);
		scene.idle(5);

		ElementLink<WorldSectionElement> elevatorLink =
			scene.world.showIndependentSection(util.select.fromTo(4, 13, 3, 2, 13, 1), Direction.DOWN);
		scene.world.moveSection(elevatorLink, util.vector.of(0, -13, 0), 0);
		scene.idle(10);
		scene.world.showSectionAndMerge(util.select.fromTo(4, 14, 1, 4, 16, 1), Direction.DOWN, elevatorLink);
		scene.idle(2);
		scene.world.showSectionAndMerge(util.select.fromTo(2, 14, 1, 2, 16, 1), Direction.DOWN, elevatorLink);
		scene.idle(2);
		scene.world.showSectionAndMerge(util.select.fromTo(4, 14, 3, 4, 16, 3), Direction.DOWN, elevatorLink);
		scene.idle(2);
		scene.world.showSectionAndMerge(util.select.fromTo(2, 14, 3, 2, 16, 3), Direction.DOWN, elevatorLink);
		scene.idle(10);
		scene.world.showSectionAndMerge(util.select.fromTo(4, 17, 1, 2, 17, 3), Direction.DOWN, elevatorLink);

		scene.overlay.showText(40)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 2, 1), Direction.UP))
			.text("Start by constructing a cabin");
		scene.idle(30);

		scene.world.showSectionAndMerge(util.select.position(2, 14, 2), Direction.WEST, elevatorLink);
		scene.idle(2);
		scene.world.showSectionAndMerge(util.select.position(1, 13, 2), Direction.EAST, camLink);
		scene.idle(15);
		scene.world.toggleRedstonePower(util.select.fromTo(2, 14, 2, 1, 13, 2));
		scene.idle(15);

		scene.overlay.showText(60)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 1), Direction.UP))
			.text("Place a pair of Redstone Contacts facing each other...");
		scene.idle(55);

		AABB glue1 = new AABB(util.grid.at(3, 4, 2));
		AABB glue2 = glue1.inflate(1, 0, 1)
			.expandTowards(0, -4, 0);

		scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(4, 3, 1), Pointing.RIGHT)
			.withItem(AllItems.SUPER_GLUE.asStack()), 60);
		scene.idle(7);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, glue1, glue1, 5);
		scene.idle(1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, glue1, glue2, 90);
		scene.idle(10);

		scene.overlay.showSelectionWithText(util.select.position(2, 1, 2), 80)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 1), Direction.UP))
			.text("...and glue one of them to your moving structure");
		scene.idle(70);

		scene.world.showSectionAndMerge(controls, Direction.DOWN, elevatorLink);
		scene.idle(15);
		scene.effects.superGlue(util.grid.at(4, 1, 2), Direction.DOWN, true);

		scene.overlay.showText(80)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(util.grid.at(4, 1, 2), Direction.UP))
			.text("Contraption Controls can be attached to make floor selection easier");
		scene.idle(70);

		scene.world.showSectionAndMerge(cog, Direction.DOWN, camLink);
		scene.world.showSectionAndMerge(pole, Direction.UP, camLink);
		scene.idle(10);
		scene.world.setKineticSpeed(util.select.position(pulleyPos), 64);
		scene.idle(5);

		scene.overlay.showText(60)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 6, 2), Direction.WEST))
			.text("Ensure that the pulley is supplied with Rotational Power");
		scene.idle(75);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(3, 6, 2), Direction.NORTH), Pointing.RIGHT)
				.rightClick(),
			60);
		scene.idle(7);
		scene.effects.indicateSuccess(util.grid.at(3, 6, 2));
		scene.world.toggleRedstonePower(util.select.position(1, 13, 2));
		scene.world.setBlock(topContact, AllBlocks.ELEVATOR_CONTACT.getDefaultState()
			.setValue(ElevatorContactBlock.FACING, Direction.EAST)
			.setValue(ElevatorContactBlock.POWERING, true), false);
		scene.world.movePulley(pulleyPos, 1, 0);

		scene.overlay.showText(50)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 6, 2), Direction.WEST))
			.text("Right-Clicking the pulley assembles the elevator");
		scene.idle(60);

		scene.overlay.showText(70)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.UP))
			.text("The stationary contact now turns into an Elevator Contact");
		scene.idle(80);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.UP), Pointing.DOWN)
				.rightClick(),
			60);
		scene.idle(7);
		scene.overlay.showSelectionWithText(util.select.position(1, 1, 2), 60)
			.placeNearTarget()
			.colored(PonderPalette.BLUE)
			.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.UP))
			.text("Elevator Contacts represent a 'floor' and can be configured");
		scene.idle(75);

		scene.world.moveSection(elevatorLink, util.vector.of(0, 7, 0), 15);
		scene.world.moveSection(camLink, util.vector.of(0, 7, 0), 15);
		scene.world.moveSection(pulleyLink, util.vector.of(0, 7, 0), 15);
		scene.addLazyKeyframe();
		scene.world.setBlocks(midCutout, Blocks.AIR.defaultBlockState(), false);
		scene.idle(15);
		scene.world.showSectionAndMerge(midFloor, Direction.EAST, camLink);
		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.position(midContact), Direction.DOWN, camLink);
		scene.idle(10);
		scene.effects.indicateSuccess(util.grid.at(1, 2, 2));
		scene.world.setBlock(midContact, AllBlocks.ELEVATOR_CONTACT.getDefaultState()
			.setValue(ElevatorContactBlock.FACING, Direction.EAST), false);
		scene.idle(15);

		AABB bb = new AABB(util.grid.at(1, 8, 2));
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, bb, bb, 5);
		scene.idle(1);
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, bb, bb.expandTowards(0, -6, 0), 90);
		scene.idle(10);

		scene.overlay.showText(60)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 2), Direction.UP))
			.text("Any redstone contact sharing this column will be converted");
		scene.idle(50);
		scene.world.showSectionAndMerge(midInput, Direction.SOUTH, camLink);
		scene.idle(15);

		scene.world.toggleRedstonePower(midInput);
		scene.effects.indicateRedstone(util.grid.at(1, 2, 0));
		scene.world.cycleBlockProperty(midContact, ElevatorContactBlock.CALLING);
		scene.world.cycleBlockProperty(topContact, ElevatorContactBlock.POWERING);
		scene.world.moveSection(elevatorLink, util.vector.of(0, -6, 0), 60);
		scene.world.movePulley(pulleyPos, 6, 60);
		scene.idle(20);
		scene.world.toggleRedstonePower(midInput);
		scene.idle(10);

		scene.overlay.showText(60)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 0), Direction.UP))
			.text("Supply a redstone pulse to call the elevator to the contact");

		scene.idle(30);
		scene.world.cycleBlockProperty(midContact, ElevatorContactBlock.CALLING);
		scene.world.cycleBlockProperty(midContact, ElevatorContactBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.position(1, 7, 1));
		scene.idle(40);

		scene.overlay.showText(70)
			.placeNearTarget()
			.pointAt(util.vector.centerOf(util.grid.at(2, 3, 3)))
			.text("The movement speed depends on the rotation input on the pulley");
		scene.idle(80);
		scene.addLazyKeyframe();
		scene.idle(10);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(4, 2, 2), Direction.UP), Pointing.DOWN)
				.scroll(),
			60);
		scene.idle(15);
		scene.overlay.showText(90)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(4, 2, 2), Direction.UP))
			.text("Scroll and click on the controls block to choose a floor while on-board");
		scene.idle(85);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(4, 2, 2), Direction.UP), Pointing.DOWN)
				.rightClick(),
			10);
		scene.idle(7);
		scene.world.cycleBlockProperty(midContact, ElevatorContactBlock.POWERING);
		scene.world.cycleBlockProperty(topContact, ElevatorContactBlock.CALLING);
		scene.world.toggleRedstonePower(util.select.position(1, 7, 1));
		scene.world.moveSection(camLink, util.vector.of(0, -7, 0), 60);
		scene.world.moveSection(pulleyLink, util.vector.of(0, -7, 0), 60);
		scene.world.moveSection(elevatorLink, util.vector.of(0, -1, 0), 60);
		scene.world.movePulley(pulleyPos, -6, 60);
		scene.idle(60);

		scene.world.cycleBlockProperty(topContact, ElevatorContactBlock.POWERING);
		scene.world.cycleBlockProperty(topContact, ElevatorContactBlock.CALLING);
		scene.idle(15);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(3, 6, 2), Direction.NORTH), Pointing.RIGHT)
				.rightClick(),
			60);
		scene.idle(7);
		scene.effects.indicateSuccess(util.grid.at(3, 6, 2));
		scene.world.movePulley(pulleyPos, -1, 0);

		scene.overlay.showText(80)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 6, 2), Direction.WEST))
			.text("Right-Clicking the assembled pulley will turn the cabin back into blocks");
		scene.idle(90);

		scene.world.showSectionAndMerge(util.select.fromTo(doorPos, doorPos.above()), Direction.DOWN, elevatorLink);
		scene.idle(20);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(3, 6, 2), Direction.NORTH), Pointing.RIGHT)
				.rightClick(),
			60);
		scene.idle(7);
		scene.effects.indicateSuccess(util.grid.at(3, 6, 2));
		scene.world.movePulley(pulleyPos, 1, 0);
		scene.world.cycleBlockProperty(doorPos, SlidingDoorBlock.OPEN);
		scene.world.cycleBlockProperty(doorPos, SlidingDoorBlock.VISIBLE);
		scene.world.cycleBlockProperty(doorPos.above(), SlidingDoorBlock.VISIBLE);

		scene.overlay.showText(80)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(util.grid.at(3, 1, 1), Direction.NORTH))
			.text("Sliding doors attached to the cabin will open and close automatically");
		scene.idle(90);

		scene.world.moveSection(elevatorLink, util.vector.of(0, 13, 0), 15);
		scene.world.moveSection(camLink, util.vector.of(0, 13, 0), 15);
		scene.world.moveSection(pulleyLink, util.vector.of(0, 13, 0), 15);
		scene.world.setBlocks(botCutout, Blocks.AIR.defaultBlockState(), false);
		scene.idle(15);
		scene.world.showSectionAndMerge(botFloor, Direction.EAST, camLink);
		scene.idle(5);
		scene.world.showSectionAndMerge(util.select.position(botContact), Direction.DOWN, camLink);
		scene.idle(10);
		scene.effects.indicateSuccess(util.grid.at(1, 2, 2));
		scene.world.setBlock(botContact, AllBlocks.ELEVATOR_CONTACT.getDefaultState()
			.setValue(ElevatorContactBlock.FACING, Direction.EAST), false);
		scene.idle(5);
		scene.world.showSectionAndMerge(botInput, Direction.SOUTH, camLink);
		scene.idle(15);

		scene.world.toggleRedstonePower(botInput);
		scene.effects.indicateRedstone(util.grid.at(1, 2, 0));
		scene.world.cycleBlockProperty(botContact, ElevatorContactBlock.CALLING);
		scene.world.cycleBlockProperty(topContact, ElevatorContactBlock.POWERING);
		scene.world.moveSection(elevatorLink, util.vector.of(0, -12, 0), 50);
		scene.world.movePulley(pulleyPos, 12, 50);
		scene.world.cycleBlockProperty(doorPos, SlidingDoorBlock.OPEN);
		scene.world.cycleBlockProperty(doorPos, SlidingDoorBlock.VISIBLE);
		scene.world.cycleBlockProperty(doorPos.above(), SlidingDoorBlock.VISIBLE);
		scene.idle(20);

		scene.world.toggleRedstonePower(botInput);
		scene.world.showSectionAndMerge(outputRedstone, Direction.EAST, camLink);
		scene.idle(30);

		scene.world.cycleBlockProperty(botContact, ElevatorContactBlock.CALLING);
		scene.world.cycleBlockProperty(botContact, ElevatorContactBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.position(1, 1, 1));
		scene.world.toggleRedstonePower(outputRedstone);
		scene.world.cycleBlockProperty(doorPos, SlidingDoorBlock.OPEN);
		scene.world.cycleBlockProperty(doorPos, SlidingDoorBlock.VISIBLE);
		scene.world.cycleBlockProperty(doorPos.above(), SlidingDoorBlock.VISIBLE);
		scene.idle(15);

		scene.overlay.showText(80)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector.topOf(0, 1, 2))
			.text("Elevator Contacts emit a signal while the cabin is on their floor");
		scene.idle(90);

		scene.overlay.showText(80)
			.placeNearTarget()
			.pointAt(util.vector.topOf(0, 1, 2))
			.text("This can be useful to trigger doors or special effects upon arrival");
		scene.idle(90);

		scene.world.setBlock(nixiePos, AllBlocks.NIXIE_TUBES.get(DyeColor.GREEN)
			.getDefaultState()
			.setValue(NixieTubeBlock.FACING, Direction.WEST), false);

		scene.world.moveSection(camLink, util.vector.of(0, -13, 0), 20);
		scene.world.moveSection(pulleyLink, util.vector.of(0, -13, 0), 20);
		scene.world.moveSection(elevatorLink, util.vector.of(0, -13, 0), 20);
		scene.idle(30);
		scene.world.showSectionAndMerge(util.select.position(nixiePos), Direction.DOWN, camLink);
		scene.idle(15);
		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(util.grid.at(4, 1, 0), Direction.UP), Pointing.DOWN)
				.rightClick()
				.withItem(AllBlocks.DISPLAY_LINK.asStack()),
			15);
		scene.world.toggleRedstonePower(util.select.position(1, 14, 2));
		scene.idle(15);
		scene.world.showSectionAndMerge(util.select.position(linkPos), Direction.DOWN, camLink);
		scene.world.flashDisplayLink(linkPos);
		scene.world.modifyBlockEntityNBT(util.select.position(nixiePos), NixieTubeBlockEntity.class, nbt -> {
			Component component = Components.literal("0F");
			nbt.putString("RawCustomText", component.getString());
			nbt.putString("CustomText", Component.Serializer.toJson(component));
		});

		scene.overlay.showText(90)
			.placeNearTarget()
			.attachKeyFrame()
			.pointAt(util.vector.centerOf(1, 2, 2))
			.text("Display Links on any of the contacts can show the current floor of the elevator");
		scene.idle(90);

		scene.world.showSectionAndMerge(topInput, Direction.SOUTH, camLink);
		scene.idle(15);

		scene.world.toggleRedstonePower(topInput);
		scene.effects.indicateRedstone(util.grid.at(1, 2, 0));
		scene.world.cycleBlockProperty(topContact, ElevatorContactBlock.CALLING);
		scene.world.cycleBlockProperty(botContact, ElevatorContactBlock.POWERING);
		scene.world.moveSection(elevatorLink, util.vector.of(0, 12, 0), 70);
		scene.world.movePulley(pulleyPos, -12, 70);
		scene.world.cycleBlockProperty(doorPos, SlidingDoorBlock.OPEN);
		scene.world.cycleBlockProperty(doorPos, SlidingDoorBlock.VISIBLE);
		scene.world.cycleBlockProperty(doorPos.above(), SlidingDoorBlock.VISIBLE);
		scene.idle(20);

		scene.world.toggleRedstonePower(topInput);
		scene.idle(10);

		scene.world.flashDisplayLink(linkPos);
		scene.world.modifyBlockEntityNBT(util.select.position(nixiePos), NixieTubeBlockEntity.class, nbt -> {
			Component component = Components.literal("1F");
			nbt.putString("RawCustomText", component.getString());
			nbt.putString("CustomText", Component.Serializer.toJson(component));
		});

		scene.idle(40);

		scene.world.cycleBlockProperty(topContact, ElevatorContactBlock.CALLING);
		scene.world.cycleBlockProperty(topContact, ElevatorContactBlock.POWERING);
		scene.world.toggleRedstonePower(util.select.position(1, 13, 1));
		scene.world.cycleBlockProperty(doorPos, SlidingDoorBlock.OPEN);
		scene.world.cycleBlockProperty(doorPos, SlidingDoorBlock.VISIBLE);
		scene.world.cycleBlockProperty(doorPos.above(), SlidingDoorBlock.VISIBLE);

		scene.world.flashDisplayLink(linkPos);
		scene.world.modifyBlockEntityNBT(util.select.position(nixiePos), NixieTubeBlockEntity.class, nbt -> {
			Component component = Components.literal("2F");
			nbt.putString("RawCustomText", component.getString());
			nbt.putString("CustomText", Component.Serializer.toJson(component));
		});
	}

	public static void multiRope(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("elevator_pulley_multi_rope", "Synchronised Pulley Movement");
		scene.configureBasePlate(0, 0, 5);
		scene.setSceneOffsetY(-1);
		scene.scaleSceneView(.95f);
		scene.showBasePlate();
		scene.idle(5);

		Selection mainPulley = util.select.fromTo(5, 0, 1, 5, 4, 1)
			.add(util.select.fromTo(4, 4, 1, 3, 4, 1));
		BlockPos pulley1 = util.grid.at(3, 4, 1);
		BlockPos pulley2 = util.grid.at(3, 4, 3);
		BlockPos pulley3 = util.grid.at(1, 4, 3);
		Selection contraption = util.select.fromTo(3, 1, 3, 1, 1, 1);

		ElementLink<WorldSectionElement> planksLink = scene.world.showIndependentSection(contraption, Direction.DOWN);
		scene.idle(10);
		scene.world.showSection(mainPulley, Direction.WEST);
		scene.idle(4);
		scene.world.showSection(util.select.position(pulley2), Direction.DOWN);
		scene.idle(4);
		scene.world.showSection(util.select.position(pulley3), Direction.DOWN);
		scene.idle(15);

		scene.world.movePulley(pulley1, 2, 20);
		scene.idle(20);

		scene.overlay.showText(60)
			.pointAt(util.vector.topOf(util.grid.at(3, 1, 1)))
			.placeNearTarget()
			.text("Whenever a pulley assembles a contraption...");
		scene.idle(70);

		scene.world.movePulley(pulley2, 2, 0);
		scene.world.movePulley(pulley3, 2, 0);
		scene.idle(1);
		scene.world.movePulley(pulley1, -2, 20);
		scene.world.movePulley(pulley2, -2, 20);
		scene.world.movePulley(pulley3, -2, 20);
		scene.world.moveSection(planksLink, util.vector.of(0, 2, 0), 20);
		scene.idle(20);

		scene.overlay.showText(80)
			.pointAt(util.vector.blockSurface(util.grid.at(1, 4, 3), Direction.WEST))
			.placeNearTarget()
			.text("...other pulleys on the same layer will connect to the structure");
		scene.idle(60);

		scene.world.movePulley(pulley1, 2, 20);
		scene.world.movePulley(pulley2, 2, 20);
		scene.world.movePulley(pulley3, 2, 20);
		scene.world.moveSection(planksLink, util.vector.of(0, -2, 0), 20);
		scene.idle(20);

		scene.idle(20);
		scene.world.movePulley(pulley1, -2, 20);
		scene.world.movePulley(pulley2, -2, 20);
		scene.world.movePulley(pulley3, -2, 20);
		scene.world.moveSection(planksLink, util.vector.of(0, 2, 0), 20);
		scene.idle(20);

		scene.overlay.showText(80)
			.pointAt(util.vector.blockSurface(util.grid.at(1, 4, 3), Direction.WEST))
			.placeNearTarget()
			.text("They do not require to be powered, the effect is purely cosmetic");
		scene.idle(60);

		scene.world.movePulley(pulley1, 2, 20);
		scene.world.movePulley(pulley2, 2, 20);
		scene.world.movePulley(pulley3, 2, 20);
		scene.world.moveSection(planksLink, util.vector.of(0, -2, 0), 20);
		scene.idle(20);

	}

}
