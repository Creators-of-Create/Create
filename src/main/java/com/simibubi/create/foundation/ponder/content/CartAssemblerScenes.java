package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssembleRailType;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.EntityElement;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.MinecartElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class CartAssemblerScenes {

	public static void anchor(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("cart_assembler", "Moving Structures using Cart Assemblers");
		scene.configureBasePlate(0, 0, 5);
		scene.scaleSceneView(.9f);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		BlockPos assemblerPos = util.grid.at(2, 1, 2);
		scene.world.setBlock(assemblerPos, Blocks.RAIL.getDefaultState(), false);
		for (int z = 0; z < 5; z++) {
			scene.world.showSection(util.select.position(2, 1, z), Direction.DOWN);
			scene.idle(2);
		}

		BlockPos leverPos = util.grid.at(0, 1, 2);
		Selection toggle = util.select.fromTo(assemblerPos, leverPos);

		scene.idle(10);

		scene.overlay
			.showControls(new InputWindowElement(util.vector.centerOf(assemblerPos), Pointing.DOWN).rightClick()
				.withItem(AllBlocks.CART_ASSEMBLER.asStack()), 30);
		scene.idle(7);
		scene.world.setBlock(assemblerPos, AllBlocks.CART_ASSEMBLER.getDefaultState()
			.with(CartAssemblerBlock.RAIL_SHAPE, RailShape.NORTH_SOUTH)
			.with(CartAssemblerBlock.RAIL_TYPE, CartAssembleRailType.REGULAR), true);
		scene.idle(20);
		scene.world.showSection(util.select.fromTo(0, 1, 2, 1, 1, 2), Direction.EAST);
		scene.idle(20);
		scene.world.toggleRedstonePower(toggle);
		scene.effects.indicateRedstone(leverPos);
		scene.idle(10);

		scene.overlay.showText(70)
			.text("Powered Cart Assemblers mount attached structures to passing Minecarts")
			.attachKeyFrame()
			.pointAt(util.vector.topOf(assemblerPos))
			.placeNearTarget();
		scene.idle(80);

		ElementLink<MinecartElement> cart =
			scene.special.createCart(util.vector.topOf(2, 0, 4), 90, MinecartEntity::new);
		scene.world.showSection(util.select.position(assemblerPos.up()), Direction.DOWN);
		scene.idle(10);
		scene.special.moveCart(cart, util.vector.of(0, 0, -2), 20);
		scene.idle(20);
		ElementLink<WorldSectionElement> plank =
			scene.world.makeSectionIndependent(util.select.position(assemblerPos.up()));
		ElementLink<WorldSectionElement> anchor =
			scene.world.showIndependentSectionImmediately(util.select.position(assemblerPos.east()));
		scene.world.moveSection(anchor, util.vector.of(-1, 0, 0), 0);
		scene.effects.indicateSuccess(assemblerPos);
		scene.idle(1);
		scene.world.moveSection(anchor, util.vector.of(0, 0, -2), 20);
		scene.world.moveSection(plank, util.vector.of(0, 0, -2), 20);
		scene.special.moveCart(cart, util.vector.of(0, 0, -2), 20);
		scene.idle(20);

		scene.world.toggleRedstonePower(toggle);
		scene.idle(10);

		scene.overlay.showText(70)
			.text("Without a redstone signal, it disassembles passing cart contraptions back into blocks")
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.pointAt(util.vector.topOf(assemblerPos))
			.placeNearTarget();
		scene.idle(80);

		scene.world.rotateSection(anchor, 0, 180, 0, 6);
		scene.world.rotateSection(plank, 0, 180, 0, 6);
		scene.idle(3);

		scene.world.moveSection(anchor, util.vector.of(0, 0, 2), 20);
		scene.world.moveSection(plank, util.vector.of(0, 0, 2), 20);
		scene.special.moveCart(cart, util.vector.of(0, 0, 2), 20);
		scene.idle(21);
		scene.world.moveSection(anchor, util.vector.of(0, -2, 0), 0);
		scene.special.moveCart(cart, util.vector.of(0, 0, 2), 20);
		scene.idle(30);

		scene.world.destroyBlock(assemblerPos.up());
		scene.idle(5);
		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.fromTo(1, 4, 2, 3, 3, 2), Direction.DOWN);
		scene.world.moveSection(contraption, util.vector.of(0, -1, 0), 0);
		scene.idle(10);
		scene.world.showSectionAndMerge(util.select.position(3, 3, 1), Direction.SOUTH, contraption);
		scene.idle(15);
		scene.effects.superGlue(util.grid.at(3, 2, 1), Direction.SOUTH, true);
		scene.overlay.showText(80)
			.attachKeyFrame()
			.sharedText("movement_anchors")
			.pointAt(util.vector.blockSurface(util.grid.at(1, 3, 2), Direction.NORTH))
			.placeNearTarget();
		scene.idle(80);
		scene.world.toggleRedstonePower(toggle);
		scene.effects.indicateRedstone(leverPos);

		scene.special.moveCart(cart, util.vector.of(0, 0, -2), 20);
		scene.idle(20);
		scene.world.moveSection(anchor, util.vector.of(0, 2, 0), 0);
		scene.idle(1);
		scene.world.moveSection(anchor, util.vector.of(0, 0, -2), 20);
		scene.world.moveSection(contraption, util.vector.of(0, 0, -2), 20);
		scene.special.moveCart(cart, util.vector.of(0, 0, -2), 20);
		scene.idle(25);

		Vector3d cartCenter = util.vector.centerOf(assemblerPos.north(2));
		scene.overlay.showControls(new InputWindowElement(cartCenter, Pointing.LEFT).rightClick()
			.withWrench(), 40);
		scene.idle(7);
		scene.special.moveCart(cart, util.vector.of(0, -100, 4), 0);
		scene.world.moveSection(anchor, util.vector.of(0, -100, 4), 0);
		scene.world.moveSection(contraption, util.vector.of(0, -100, 4), 0);
		ItemStack asStack = AllItems.MINECART_CONTRAPTION.asStack();
		ElementLink<EntityElement> itemEntity =
			scene.world.createItemEntity(cartCenter, util.vector.of(0, .1, 0), asStack);
		scene.idle(40);
		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("Using a Wrench on the Minecart will let you carry the Contraption elsewhere")
			.pointAt(cartCenter)
			.placeNearTarget();
		scene.idle(80);
		scene.world.modifyEntity(itemEntity, Entity::remove);

		scene.overlay.showControls(new InputWindowElement(cartCenter.add(0, 0, 4), Pointing.DOWN).rightClick()
			.withItem(asStack), 20);
		scene.idle(20);
		scene.special.moveCart(cart, util.vector.of(0, 100.5, 0), 0);
		scene.world.moveSection(anchor, util.vector.of(0, 100.5, 0), 0);
		scene.world.moveSection(contraption, util.vector.of(0, 100.5, 0), 0);
		scene.idle(1);
		scene.special.moveCart(cart, util.vector.of(0, -.5, 0), 5);
		scene.world.moveSection(anchor, util.vector.of(0, -.5, 0), 5);
		scene.world.moveSection(contraption, util.vector.of(0, -.5, 0), 5);
	}

	public static void modes(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("cart_assembler_modes", "Orientation Settings for Minecart Contraptions");
		scene.configureBasePlate(0, 0, 5);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		for (int z = 0; z < 4; z++) {
			scene.world.showSection(util.select.position(1, 1, z), Direction.DOWN);
			scene.idle(2);
		}
		for (int x = 2; x < 5; x++) {
			scene.world.showSection(util.select.position(x, 1, 3), Direction.DOWN);
			scene.idle(2);
		}

		BlockPos assemblerPos = util.grid.at(3, 1, 3);
		scene.idle(5);
		scene.world.setBlock(assemblerPos, AllBlocks.CART_ASSEMBLER.getDefaultState()
			.with(CartAssemblerBlock.RAIL_SHAPE, RailShape.EAST_WEST)
			.with(CartAssemblerBlock.RAIL_TYPE, CartAssembleRailType.REGULAR), true);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(3, 1, 1, 3, 1, 2), Direction.SOUTH);
		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.position(3, 2, 3), Direction.DOWN);
		scene.idle(10);
		scene.world.glueBlockOnto(util.grid.at(2, 2, 3), Direction.EAST, contraption);
		scene.world.toggleRedstonePower(util.select.fromTo(3, 1, 1, 3, 1, 3));
		scene.effects.indicateRedstone(util.grid.at(3, 1, 1));
		scene.idle(10);

		ElementLink<MinecartElement> cart =
			scene.special.createCart(util.vector.topOf(util.grid.at(4, 0, 3)), 0, MinecartEntity::new);
		scene.idle(20);
		scene.special.moveCart(cart, util.vector.of(-1, 0, 0), 10);
		scene.idle(10);
		ElementLink<WorldSectionElement> anchor =
			scene.world.showIndependentSectionImmediately(util.select.position(assemblerPos.south()));
		scene.world.moveSection(anchor, util.vector.of(0, 0, -1), 0);
		scene.idle(1);

		scene.world.setKineticSpeed(util.select.position(2, 2, 3), 32);
		scene.special.moveCart(cart, util.vector.of(-1.5, 0, 0), 15);
		scene.world.moveSection(anchor, util.vector.of(-1.5, 0, 0), 15);
		scene.world.moveSection(contraption, util.vector.of(-1.5, 0, 0), 15);
		scene.idle(16);
		scene.special.rotateCart(cart, -45, 2);
		scene.special.moveCart(cart, util.vector.of(-.5, 0, -.5), 8);
		scene.world.moveSection(anchor, util.vector.of(-.5, 0, -.5), 8);
		scene.world.moveSection(contraption, util.vector.of(-.5, 0, -.5), 8);
		scene.world.rotateSection(anchor, 0, -90, 0, 12);
		scene.world.rotateSection(contraption, 0, -90, 0, 12);
		scene.idle(9);
		scene.special.rotateCart(cart, -45, 2);
		scene.special.moveCart(cart, util.vector.of(0, 0, -1.5), 15);
		scene.world.moveSection(anchor, util.vector.of(0, 0, -1.5), 15);
		scene.world.moveSection(contraption, util.vector.of(0, 0, -1.5), 15);
		scene.idle(15);
		scene.world.setKineticSpeed(util.select.position(2, 2, 3), 0);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("Cart Contraptions will rotate to face towards their carts' motion")
			.pointAt(util.vector.of(1.5, 2.5, 0))
			.placeNearTarget();
		scene.idle(90);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("This Arrow indicates which side of the Structure will be considered the front")
			.pointAt(util.vector.topOf(assemblerPos))
			.placeNearTarget();
		scene.idle(90);

		scene.world.hideIndependentSection(contraption, Direction.UP);
		scene.world.hideIndependentSection(anchor, Direction.UP);
		scene.special.hideElement(cart, Direction.UP);
		scene.idle(25);

		Vector3d blockSurface = util.vector.blockSurface(assemblerPos, Direction.NORTH)
			.add(0, 0, -2 / 16f);
		scene.overlay.showScrollInput(blockSurface, Direction.NORTH, 60);
		scene.overlay.showControls(new InputWindowElement(blockSurface, Pointing.DOWN).scroll()
			.withWrench(), 60);
		scene.idle(10);
		scene.overlay.showText(60)
			.pointAt(util.vector.of(3, 1.5, 3))
			.placeNearTarget()
			.sharedText("behaviour_modify_wrench");
		scene.idle(70);

		contraption = scene.world.showIndependentSection(util.select.fromTo(3, 2, 3, 2, 2, 3), Direction.DOWN);
		cart = scene.special.createCart(util.vector.topOf(util.grid.at(4, 0, 3)), 0, MinecartEntity::new);
		scene.idle(10);
		scene.special.moveCart(cart, util.vector.of(-1, 0, 0), 10);
		scene.idle(10);
		anchor = scene.world.showIndependentSectionImmediately(util.select.position(assemblerPos.south()));
		scene.world.moveSection(anchor, util.vector.of(0, 0, -1), 0);
		scene.idle(1);

		scene.world.setKineticSpeed(util.select.position(2, 2, 3), 32);
		scene.special.moveCart(cart, util.vector.of(-1.5, 0, 0), 15);
		scene.world.moveSection(anchor, util.vector.of(-1.5, 0, 0), 15);
		scene.world.moveSection(contraption, util.vector.of(-1.5, 0, 0), 15);
		scene.idle(16);
		scene.special.rotateCart(cart, -45, 2);
		scene.special.moveCart(cart, util.vector.of(-.5, 0, -.5), 8);
		scene.world.moveSection(anchor, util.vector.of(-.5, 0, -.5), 8);
		scene.world.moveSection(contraption, util.vector.of(-.5, 0, -.5), 8);
		scene.idle(9);
		scene.special.rotateCart(cart, -45, 2);
		scene.special.moveCart(cart, util.vector.of(0, 0, -1.5), 15);
		scene.world.moveSection(anchor, util.vector.of(0, 0, -1.5), 15);
		scene.world.moveSection(contraption, util.vector.of(0, 0, -1.5), 15);
		scene.idle(15);
		scene.world.setKineticSpeed(util.select.position(2, 2, 3), 0);

		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("If the Assembler is set to Lock Rotation, the contraptions' orientation will never change")
			.pointAt(util.vector.of(0, 2.5, 1.5))
			.placeNearTarget();
		scene.idle(90);
	}

	public static void dual(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("cart_assembler_dual", "Assembling Carriage Contraptions");
		scene.configureBasePlate(0, 0, 6);
		scene.scaleSceneView(.9f);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		for (int z = 0; z < 5; z++) {
			scene.world.showSection(util.select.position(1, 1, z), Direction.DOWN);
			scene.idle(2);
		}
		for (int x = 2; x < 6; x++) {
			scene.world.showSection(util.select.position(x, 1, 4), Direction.DOWN);
			scene.idle(2);
		}

		BlockPos assembler1 = util.grid.at(2, 1, 4);
		BlockPos assembler2 = util.grid.at(5, 1, 4);
		Selection chassis = util.select.fromTo(5, 2, 4, 2, 2, 4);

		scene.idle(5);
		scene.world.showSection(util.select.fromTo(2, 1, 3, 2, 1, 2), Direction.SOUTH);
		scene.idle(5);
		ElementLink<MinecartElement> cart =
			scene.special.createCart(util.vector.topOf(assembler1.down()), 0, MinecartEntity::new);
		ElementLink<MinecartElement> cart2 =
			scene.special.createCart(util.vector.topOf(assembler2.down()), 0, ChestMinecartEntity::new);
		scene.idle(15);
		scene.world.setBlock(assembler1, AllBlocks.CART_ASSEMBLER.getDefaultState()
			.with(CartAssemblerBlock.RAIL_SHAPE, RailShape.EAST_WEST)
			.with(CartAssemblerBlock.RAIL_TYPE, CartAssembleRailType.CONTROLLER_RAIL), true);
		scene.idle(5);
		scene.world.setBlock(assembler2, AllBlocks.CART_ASSEMBLER.getDefaultState()
			.with(CartAssemblerBlock.RAIL_SHAPE, RailShape.EAST_WEST)
			.with(CartAssemblerBlock.RAIL_TYPE, CartAssembleRailType.REGULAR), true);
		scene.idle(5);

		ElementLink<WorldSectionElement> contraption = scene.world.showIndependentSection(chassis, Direction.DOWN);
		scene.idle(15);
		scene.overlay.showOutline(PonderPalette.GREEN, new Object(), util.select.position(assembler2), 60);
		scene.overlay.showSelectionWithText(util.select.position(assembler1), 60)
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector.blockSurface(util.grid.at(2, 2, 4), Direction.NORTH))
			.placeNearTarget()
			.text("Whenever two Cart Assembers share an attached structure...")
			.attachKeyFrame();
		scene.idle(70);

		scene.overlay.showText(60)
			.pointAt(util.vector.blockSurface(util.grid.at(2, 1, 4), Direction.NORTH))
			.placeNearTarget()
			.text("Powering either of them will create a Carriage Contraption");
		scene.idle(70);

		scene.effects.indicateRedstone(util.grid.at(2, 1, 2));
		scene.world.toggleRedstonePower(util.select.fromTo(2, 1, 2, 2, 1, 4));
		ElementLink<WorldSectionElement> anchors =
			scene.world.showIndependentSectionImmediately(util.select.fromTo(assembler1.south(), assembler2.south()));
		scene.world.moveSection(anchors, util.vector.of(0, 0, -1), 0);
		scene.world.configureCenterOfRotation(anchors, util.vector.centerOf(util.grid.at(2, 2, 5)));
		scene.world.configureCenterOfRotation(contraption, util.vector.centerOf(util.grid.at(2, 2, 4)));
		scene.idle(5);

		Vector3d m = util.vector.of(-0.5, 0, 0);
		scene.special.moveCart(cart, m, 5);
		scene.special.moveCart(cart2, m, 5);
		scene.world.moveSection(contraption, m, 5);
		scene.world.moveSection(anchors, m, 5);
		scene.idle(5);
		scene.special.rotateCart(cart, -45, 2);
		scene.special.moveCart(cart2, util.vector.of(-.3, 0, 0), 8);
		m = util.vector.of(-.5, 0, -.5);
		scene.special.moveCart(cart, m, 8);
		scene.world.moveSection(anchors, m, 8);
		scene.world.moveSection(contraption, m, 8);
		scene.world.rotateSection(anchors, 0, -10, 0, 8);
		scene.world.rotateSection(contraption, 0, -10, 0, 8);
		scene.idle(8);
		scene.special.rotateCart(cart, -45, 2);
		scene.special.moveCart(cart2, util.vector.of(-.4, 0, 0), 5);
		m = util.vector.of(0, 0, -3.5);
		scene.special.moveCart(cart, m, 25);
		scene.world.moveSection(anchors, m, 25);
		scene.world.moveSection(contraption, m, 25);
		scene.world.rotateSection(anchors, 0, -33, 0, 10);
		scene.world.rotateSection(contraption, 0, -33, 0, 10);
		scene.idle(5);
		scene.special.moveCart(cart2, util.vector.of(-0.8, 0, 0), 5);
		scene.idle(5);
		scene.special.moveCart(cart2, util.vector.of(-1.5, 0, 0), 9);
		scene.world.rotateSection(anchors, 0, -42, 0, 9);
		scene.world.rotateSection(contraption, 0, -42, 0, 9);
		scene.idle(9);
		m = util.vector.of(-.5, 0, -.5);
		scene.special.moveCart(cart2, m, 2);
		scene.special.rotateCart(cart2, -45, 2);
		scene.world.rotateSection(anchors, 0, -5, 0, 5);
		scene.world.rotateSection(contraption, 0, -5, 0, 5);
		scene.idle(2);
		scene.special.moveCart(cart2, util.vector.of(0, 0, -.5), 5);
		scene.special.rotateCart(cart2, -45, 2);
		scene.idle(10);

		scene.overlay.showText(70)
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 3), Direction.WEST))
			.placeNearTarget()
			.text("The carts will behave like those connected via Minecart Coupling");
		scene.idle(80);

	}

	public static void rails(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("cart_assembler_rails", "Other types of Minecarts and Rails");
		scene.configureBasePlate(0, 0, 6);
		scene.scaleSceneView(.9f);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);

		for (int x = 0; x < 6; x++) {
			scene.world.showSection(util.select.position(x, 1, 3), Direction.DOWN);
			scene.idle(2);
		}

		BlockPos assembler = util.grid.at(3, 1, 3);

		Selection chassis = util.select.fromTo(4, 2, 3, 2, 2, 3);

		scene.idle(5);
		scene.overlay.showText(70)
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(assembler, Direction.DOWN))
			.placeNearTarget()
			.text("Cart Assemblers on Regular Tracks will not affect the passing carts' motion");
		scene.idle(10);
		scene.world.setBlock(assembler, AllBlocks.CART_ASSEMBLER.getDefaultState()
			.with(CartAssemblerBlock.RAIL_SHAPE, RailShape.EAST_WEST)
			.with(CartAssemblerBlock.RAIL_TYPE, CartAssembleRailType.REGULAR), true);
		scene.idle(70);

		ElementLink<MinecartElement> cart = scene.special.createCart(util.vector.topOf(assembler.east(2)
			.down()), 0, MinecartEntity::new);
		ElementLink<WorldSectionElement> anchor =
			scene.world.showIndependentSection(util.select.position(assembler.south()), Direction.DOWN);
		ElementLink<WorldSectionElement> contraption =
			scene.world.showIndependentSection(util.select.position(assembler.south()
				.up()), Direction.DOWN);
		scene.world.moveSection(contraption, util.vector.of(2, 0, -1), 0);
		scene.world.moveSection(anchor, util.vector.of(2, 0, -1), 0);
		scene.idle(10);
		scene.world.moveSection(contraption, util.vector.of(-2, 0, 0), 10);
		scene.world.moveSection(anchor, util.vector.of(-2, 0, 0), 10);
		scene.special.moveCart(cart, util.vector.of(-5, 0, 0), 25);
		scene.idle(30);
		scene.special.hideElement(cart, Direction.UP);
		scene.world.hideIndependentSection(contraption, Direction.UP);
		scene.world.moveSection(anchor, util.vector.of(0, -3, 0), 0);
		scene.idle(30);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(assembler), Pointing.DOWN)
			.withItem(new ItemStack(Items.POWERED_RAIL)), 50);
		scene.idle(7);
		scene.world.setBlock(assembler, AllBlocks.CART_ASSEMBLER.getDefaultState()
			.with(CartAssemblerBlock.RAIL_SHAPE, RailShape.EAST_WEST)
			.with(CartAssemblerBlock.RAIL_TYPE, CartAssembleRailType.POWERED_RAIL), true);
		scene.overlay.showText(100)
			.attachKeyFrame()
			.pointAt(util.vector.topOf(assembler))
			.placeNearTarget()
			.text("When on Powered or Controller Rail, the carts will be held in place until it's Powered");
		scene.idle(110);

		scene.world.hideIndependentSection(anchor, Direction.DOWN);
		cart = scene.special.createCart(util.vector.topOf(assembler.east(2)
			.down()), 0, MinecartEntity::new);
		anchor = scene.world.showIndependentSection(util.select.position(assembler.south()), Direction.DOWN);
		contraption = scene.world.showIndependentSection(util.select.position(assembler.south()
			.up()), Direction.DOWN);
		scene.world.moveSection(contraption, util.vector.of(2, 0, -1), 0);
		scene.world.moveSection(anchor, util.vector.of(2, 0, -1), 0);
		scene.idle(10);
		scene.world.moveSection(contraption, util.vector.of(-2, 0, 0), 10);
		scene.world.moveSection(anchor, util.vector.of(-2, 0, 0), 10);
		scene.special.moveCart(cart, util.vector.of(-2, 0, 0), 10);
		scene.world.showSection(util.select.fromTo(3, 1, 1, 3, 1, 2), Direction.SOUTH);
		scene.idle(30);

		scene.world.toggleRedstonePower(util.select.fromTo(3, 1, 1, 3, 1, 3));
		scene.effects.indicateRedstone(util.grid.at(3, 1, 1));
		scene.idle(5);

		scene.world.moveSection(contraption, util.vector.of(-3, 0, 0), 15);
		scene.world.moveSection(anchor, util.vector.of(-3, 0, 0), 15);
		scene.special.moveCart(cart, util.vector.of(-3, 0, 0), 15);

		scene.idle(30);
		scene.special.hideElement(cart, Direction.UP);
		scene.world.hideIndependentSection(anchor, Direction.UP);
		scene.world.hideIndependentSection(contraption, Direction.UP);
		scene.idle(20);

		cart = scene.special.createCart(util.vector.topOf(assembler.east(2)
			.down()), 0, FurnaceMinecartEntity::new);
		scene.idle(10);
		scene.overlay.showText(50)
			.attachKeyFrame()
			.pointAt(util.vector.topOf(assembler.east(2)))
			.placeNearTarget()
			.text("Other types of Minecarts can be used as the anchor");
		scene.idle(50);
		contraption = scene.world.showIndependentSection(chassis, Direction.DOWN);
		scene.idle(5);
		scene.world.glueBlockOnto(assembler.up(2), Direction.DOWN, contraption);
		scene.idle(15);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(assembler.up()), Pointing.UP)
			.withItem(new ItemStack(Items.CHARCOAL)), 40);
		scene.idle(7);
		scene.overlay.showText(80)
			.pointAt(util.vector.blockSurface(assembler.up(2), Direction.WEST))
			.placeNearTarget()
			.text("Furnace Carts will keep themselves powered, pulling fuel from any attached inventories");
		scene.idle(85);

		Emitter smoke = Emitter.simple(ParticleTypes.LARGE_SMOKE, util.vector.of(0, 0, 0));

		scene.special.moveCart(cart, util.vector.of(-5, 0, 0), 50);
		scene.idle(20);
		anchor = scene.world.showIndependentSectionImmediately(util.select.position(assembler.south()));
		scene.world.moveSection(anchor, util.vector.of(0, 0, -1), 0);
		scene.idle(1);
		scene.world.setKineticSpeed(util.select.position(2, 2, 3), 32);
		scene.world.moveSection(contraption, util.vector.of(-3, 0, 0), 30);
		scene.world.moveSection(anchor, util.vector.of(-3, 0, 0), 30);

		Vector3d vec = util.vector.centerOf(assembler)
			.add(.25, .25, -0.5);
		for (int i = 0; i < 7; i++) {
			scene.effects.emitParticles(vec = vec.add(-.5, 0, 0), smoke, 2, 1);
			scene.idle(5);
		}

		scene.world.setKineticSpeed(util.select.position(2, 2, 3), 0);
	}

}
