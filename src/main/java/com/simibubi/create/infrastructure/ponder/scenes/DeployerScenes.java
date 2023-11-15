package com.simibubi.create.infrastructure.ponder.scenes;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItem;
import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;

import net.createmod.catnip.utility.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class DeployerScenes {

	public static void filter(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("deployer", "Using the Deployer");
		scene.configureBasePlate(0, 0, 5);

		BlockPos potPosition = util.grid().at(1, 1, 2);
		BlockPos deployerPos = util.grid().at(3, 1, 2);
		Selection deployerSelection = util.select().position(deployerPos);

		scene.world().setBlock(potPosition, Blocks.AIR.defaultBlockState(), false);
		scene.world().showSection(util.select().layer(0)
			.add(util.select().position(1, 1, 2)), Direction.UP);
		scene.idle(5);
		scene.world().showSection(util.select().fromTo(3, 1, 3, 3, 1, 5), Direction.DOWN);
		scene.idle(10);

		scene.world().showSection(deployerSelection, Direction.SOUTH);
		scene.idle(10);

		scene.overlay().showText(60)
			.placeNearTarget()
			.pointAt(util.vector().topOf(deployerPos))
			.text("Given Rotational Force, a Deployer can imitate player interactions");
		scene.world().moveDeployer(deployerPos, 1, 25);
		scene.idle(26);
		scene.world().moveDeployer(deployerPos, -1, 25);
		scene.idle(44);

		scene.overlay().showOutlineWithText(util.select().position(deployerPos.west(2)), 60)
			.text("It will always interact with the position 2 blocks in front of itself")
			.attachKeyFrame()
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.attachKeyFrame();
		scene.world().moveDeployer(deployerPos, 1, 25);
		scene.idle(26);
		scene.world().moveDeployer(deployerPos, -1, 25);
		scene.idle(20);
		scene.world().showSection(util.select().fromTo(2, 1, 3, 2, 1, 1), Direction.DOWN);
		scene.idle(24);

		scene.overlay().showText(50)
			.pointAt(util.vector().topOf(deployerPos.west()))
			.text("Blocks directly in front will not obstruct it")
			.placeNearTarget();
		scene.world().moveDeployer(deployerPos, 1, 25);
		scene.idle(26);
		scene.world().moveDeployer(deployerPos, -1, 25);
		scene.idle(34);
		scene.world().hideSection(util.select().fromTo(2, 1, 3, 2, 1, 1), Direction.UP);
		scene.idle(20);

		String[] actions =
			new String[] { "Place Blocks,", "Use Items,", "Activate Blocks,", "Harvest blocks", "and Attack Mobs" };

		scene.overlay().showText(80)
			.attachKeyFrame()
			.independent(40)
			.placeNearTarget()
			.text("Deployers can:");

		int y = 60;
		for (String s : actions) {
			scene.idle(15);
			scene.overlay().showText(50)
				.colored(PonderPalette.MEDIUM)
				.placeNearTarget()
				.independent(y)
				.text(s);
			y += 16;
		}
		scene.idle(50);

		ItemStack pot = new ItemStack(Items.FLOWER_POT);
		Vec3 frontVec = util.vector().blockSurface(deployerPos, Direction.WEST)
			.add(-.125, 0, 0);

		scene.overlay().showControls(frontVec, Pointing.DOWN, 40).rightClick()
			.withItem(pot);
		scene.idle(7);
		Class<DeployerBlockEntity> teType = DeployerBlockEntity.class;
		scene.world().modifyBlockEntityNBT(deployerSelection, teType, nbt -> nbt.put("HeldItem", pot.serializeNBT()));
		scene.idle(10);

		scene.overlay().showText(40)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(frontVec)
			.text("Right-click the front to give it an Item to use");
		scene.idle(40);
		scene.world().moveDeployer(deployerPos, 1, 25);
		scene.idle(26);
		scene.world().restoreBlocks(util.select().position(potPosition));
		scene.world().modifyBlockEntityNBT(deployerSelection, teType,
			nbt -> nbt.put("HeldItem", ItemStack.EMPTY.serializeNBT()));
		scene.world().moveDeployer(deployerPos, -1, 25);
		scene.idle(20);

		scene.world().showSection(util.select().position(deployerPos.above()), Direction.DOWN);

		ItemStack tulip = new ItemStack(Items.RED_TULIP);
		Vec3 entitySpawn = util.vector().topOf(deployerPos.above(3));

		ElementLink<EntityElement> entity1 =
			scene.world().createItemEntity(entitySpawn, util.vector().of(0, 0.2, 0), tulip);
		scene.idle(17);
		scene.world().modifyEntity(entity1, Entity::discard);
		scene.world().modifyBlockEntityNBT(deployerSelection, teType, nbt -> nbt.put("HeldItem", tulip.serializeNBT()));
		scene.idle(10);
		scene.overlay().showText(40)
			.placeNearTarget()
			.pointAt(util.vector().of(3, 2.5, 3))
			.text("Items can also be inserted automatically");
		scene.idle(30);
		scene.world().moveDeployer(deployerPos, 1, 25);
		scene.idle(26);
		scene.world().setBlock(potPosition, Blocks.POTTED_RED_TULIP.defaultBlockState(), false);
		scene.world().modifyBlockEntityNBT(deployerSelection, teType,
			nbt -> nbt.put("HeldItem", ItemStack.EMPTY.serializeNBT()));
		scene.world().moveDeployer(deployerPos, -1, 25);
		scene.idle(25);
		scene.world().hideSection(util.select().position(potPosition), Direction.UP);
		scene.world().hideSection(util.select().position(deployerPos.above()), Direction.EAST);
		scene.idle(20);

		Vec3 filterSlot = util.vector().topOf(deployerPos)
			.add(2 / 16f, 0, 0);
		scene.overlay().showFilterSlotInput(filterSlot, Direction.UP, 80);
		scene.overlay().showText(40)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(filterSlot)
			.text("Deployers carry a filter slot");
		scene.idle(50);

		ItemStack shears = new ItemStack(Items.SHEARS);

		scene.overlay().showControls(filterSlot, Pointing.DOWN, 40).rightClick()
			.withItem(shears);
		scene.idle(7);
		scene.world().setFilterData(deployerSelection, teType, shears);
		scene.overlay().showText(60)
			.placeNearTarget()
			.pointAt(filterSlot)
			.text("When a filter is set, it activates only while holding a matching item");
		scene.idle(70);

		ElementLink<EntityElement> sheep = scene.world().createEntity(w -> {
			Sheep entity = EntityType.SHEEP.create(w);
			entity.setColor(DyeColor.PINK);
			Vec3 p = util.vector().topOf(util.grid().at(1, 0, 2));
			entity.setPos(p.x, p.y, p.z);
			entity.xo = p.x;
			entity.yo = p.y;
			entity.zo = p.z;
			WalkAnimationState animation = entity.walkAnimation;
			animation.update(-animation.position(), 1);
			animation.setSpeed(1);
			entity.yRotO = 210;
			entity.setYRot(210);
			entity.yHeadRotO = 210;
			entity.yHeadRot = 210;
			return entity;
		});
		scene.idle(20);
		scene.world().showSection(util.select().position(deployerPos.above()), Direction.WEST);
		entity1 = scene.world().createItemEntity(entitySpawn, util.vector().of(0, 0.2, 0), shears);
		scene.idle(17);
		scene.world().modifyEntity(entity1, Entity::discard);
		scene.world().modifyBlockEntityNBT(deployerSelection, teType, nbt -> nbt.put("HeldItem", shears.serializeNBT()));
		scene.idle(10);

		scene.overlay().showText(60)
			.placeNearTarget()
			.pointAt(util.vector().of(3, 2.5, 3))
			.text("Only items matching the filter can now be inserted...");

		scene.idle(70);
		scene.world().moveDeployer(deployerPos, 1, 25);
		scene.idle(26);
		scene.world().modifyEntity(sheep, e -> ((Sheep) e).setSheared(true));
		scene.effects().emitParticles(util.vector().topOf(deployerPos.west(2))
						.add(0, -.25, 0),
				scene.effects().particleEmitterWithinBlockSpace(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PINK_WOOL.defaultBlockState()),
						util.vector().of(0, 0, 0)),
				25, 1);
		scene.world().moveDeployer(deployerPos, -1, 25);
		scene.world().showSection(util.select().position(deployerPos.north()), Direction.SOUTH);
		scene.idle(25);

		scene.overlay().showText(80)
			.placeNearTarget()
			.pointAt(util.vector().of(3.5, 1.25, 1.25))
			.text("...and only non-matching items will be extracted");
		scene.world().flapFunnel(deployerPos.north(), true);
		scene.world().createItemEntity(util.vector().centerOf(deployerPos.north())
			.subtract(0, .45, 0), util.vector().of(0, 0, -0.1), new ItemStack(Items.PINK_WOOL));

		scene.markAsFinished();
		for (int i = 0; i < 10; i++) {
			scene.idle(26);
			scene.world().moveDeployer(deployerPos, 1, 25);
			scene.idle(26);
			scene.world().moveDeployer(deployerPos, -1, 25);
			scene.idle(26);
		}
	}

	public static void modes(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("deployer_modes", "Modes of the Deployer");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);
		scene.world().showSection(util.select().fromTo(3, 1, 3, 3, 1, 5), Direction.DOWN);
		scene.idle(10);

		BlockPos deployerPos = util.grid().at(3, 1, 2);
		Vec3 frontVec = util.vector().blockSurface(deployerPos, Direction.WEST)
			.add(-.125, 0, 0);
		Selection grassBlock = util.select().position(1, 1, 2);

		Selection deployerSelection = util.select().position(deployerPos);
		scene.world().showSection(deployerSelection, Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(grassBlock, Direction.DOWN);
		scene.idle(10);

		ItemStack tool = new ItemStack(Items.GOLDEN_HOE);
		scene.overlay().showControls(util.vector().topOf(deployerPos), Pointing.DOWN, 30).withItem(tool);
		scene.idle(7);
		scene.world().modifyBlockEntityNBT(deployerSelection, DeployerBlockEntity.class,
			nbt -> nbt.put("HeldItem", tool.serializeNBT()));
		scene.idle(45);

		scene.world().setKineticSpeed(util.select().position(2, 0, 5), 16);
		scene.world().setKineticSpeed(util.select().layer(1), -32);
		scene.world().moveDeployer(deployerPos, 1, 25);

		scene.overlay().showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().topOf(1, 1, 2))
			.text("By default, a Deployer imitates a Right-click interaction");

		scene.idle(26);
		scene.world().replaceBlocks(grassBlock, Blocks.FARMLAND.defaultBlockState(), false);
		scene.world().moveDeployer(deployerPos, -1, 25);
		scene.idle(46);

		scene.overlay().showControls(frontVec, Pointing.LEFT, 40).rightClick()
			.withItem(AllItems.WRENCH.asStack());
		scene.idle(7);
		scene.world().modifyBlockEntityNBT(deployerSelection, DeployerBlockEntity.class,
			nbt -> nbt.putString("Mode", "PUNCH"));
		scene.idle(45);

		scene.overlay().showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().topOf(1, 1, 2))
			.text("Using a Wrench, it can be set to imitate a Left-click instead");

		BlockPos breakingPos = deployerPos.west(2);
		for (int i = 0; i < 4; i++) {
			scene.idle(26);
			scene.world().moveDeployer(deployerPos, 1, 25);
			scene.idle(26);
			scene.world().incrementBlockBreakingProgress(breakingPos);
			scene.world().incrementBlockBreakingProgress(breakingPos);
			scene.world().incrementBlockBreakingProgress(breakingPos);
			scene.world().moveDeployer(deployerPos, -1, 25);
			if (i == 3)
				scene.world().createItemEntity(util.vector().centerOf(breakingPos), util.vector().of(0, 0, 0),
											   new ItemStack(Blocks.DIRT));
			scene.idle(26);

			if (i == 0)
				scene.markAsFinished();
		}
	}

	public static void processing(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("deployer_processing", "Processing Items using Deployers");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		ElementLink<WorldSectionElement> depot =
			scene.world().showIndependentSection(util.select().position(2, 1, 1), Direction.DOWN);
		scene.world().moveSection(depot, util.vector().of(0, 0, 1), 0);
		scene.idle(10);

		Selection pressS = util.select().position(2, 3, 2);
		BlockPos pressPos = util.grid().at(2, 3, 2);
		BlockPos depotPos = util.grid().at(2, 1, 1);
		scene.world().setKineticSpeed(pressS, 0);
		scene.world().showSection(pressS, Direction.DOWN);
		scene.idle(10);

		scene.world().showSection(util.select().fromTo(2, 1, 3, 2, 1, 5), Direction.NORTH);
		scene.idle(3);
		scene.world().showSection(util.select().position(2, 2, 3), Direction.SOUTH);
		scene.idle(3);
		scene.world().showSection(util.select().position(2, 3, 3), Direction.NORTH);
		scene.world().setKineticSpeed(pressS, -32);
		scene.effects().indicateSuccess(pressPos);
		scene.idle(10);

		ItemStack tool = AllItems.SAND_PAPER.asStack();
		scene.overlay().showControls(util.vector().blockSurface(pressPos.below(), Direction.EAST).add(0, 0.15, 0), Pointing.RIGHT, 30)
				.withItem(tool);
		scene.idle(7);
		scene.world().modifyBlockEntityNBT(pressS, DeployerBlockEntity.class,
			nbt -> nbt.put("HeldItem", tool.serializeNBT()));
		scene.idle(25);

		Vec3 pressSide = util.vector().blockSurface(pressPos, Direction.WEST);
		scene.overlay().showText(60)
			.pointAt(pressSide)
			.placeNearTarget()
			.attachKeyFrame()
			.text("With a fitting held item, Deployers can process items provided beneath them");
		scene.idle(80);

		scene.overlay().showText(60)
			.pointAt(pressSide.subtract(0, 2, 0))
			.placeNearTarget()
			.text("The Input items can be dropped or placed on a Depot under the Deployer");
		scene.idle(50);
		ItemStack quartz = AllItems.ROSE_QUARTZ.asStack();
		scene.world().createItemOnBeltLike(depotPos, Direction.NORTH, quartz);
		Vec3 depotCenter = util.vector().centerOf(depotPos.south());
		scene.overlay().showControls(depotCenter, Pointing.UP, 30).withItem(quartz);
		scene.idle(10);

		Vec3 targetV = util.vector().centerOf(pressPos)
			.subtract(0, 1.65, 0);

		scene.world().moveDeployer(pressPos, 1, 30);
		scene.idle(30);
		scene.world().moveDeployer(pressPos, -1, 30);
		scene.debug().enqueueCallback(s -> SandPaperItem.spawnParticles(targetV, quartz, s.getWorld()));
		// particle
		scene.world().removeItemsFromBelt(depotPos);
		ItemStack polished = AllItems.POLISHED_ROSE_QUARTZ.asStack();
		scene.world().createItemOnBeltLike(depotPos, Direction.UP, polished);
		scene.idle(10);
		scene.overlay().showControls(depotCenter, Pointing.UP, 50).withItem(polished);
		scene.idle(60);

		scene.world().hideIndependentSection(depot, Direction.NORTH);
		scene.idle(5);
		scene.world().showSection(util.select().fromTo(0, 1, 3, 0, 2, 3), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(util.select().fromTo(4, 1, 2, 0, 2, 2), Direction.SOUTH);
		scene.idle(20);
		BlockPos beltPos = util.grid().at(0, 1, 2);
		scene.overlay().showText(40)
			.pointAt(util.vector().blockSurface(beltPos, Direction.WEST))
			.placeNearTarget()
			.attachKeyFrame()
			.text("When items are provided on a belt...");
		scene.idle(30);

		ElementLink<BeltItemElement> ingot = scene.world().createItemOnBelt(beltPos, Direction.SOUTH, quartz);
		scene.idle(15);
		ElementLink<BeltItemElement> ingot2 = scene.world().createItemOnBelt(beltPos, Direction.SOUTH, quartz);
		scene.idle(15);
		scene.world().stallBeltItem(ingot, true);
		scene.world().moveDeployer(pressPos, 1, 30);

		scene.overlay().showText(50)
			.pointAt(pressSide)
			.placeNearTarget()
			.attachKeyFrame()
			.text("The Deployer will hold and process them automatically");

		scene.idle(30);
		scene.world().moveDeployer(pressPos, -1, 30);
		scene.debug().enqueueCallback(s -> SandPaperItem.spawnParticles(targetV, quartz, s.getWorld()));
		scene.world().removeItemsFromBelt(pressPos.below(2));
		ingot = scene.world().createItemOnBelt(pressPos.below(2), Direction.UP, polished);
		scene.world().stallBeltItem(ingot, true);
		scene.idle(15);
		scene.world().stallBeltItem(ingot, false);
		scene.idle(15);
		scene.world().stallBeltItem(ingot2, true);
		scene.world().moveDeployer(pressPos, 1, 30);
		scene.idle(30);
		scene.world().moveDeployer(pressPos, -1, 30);
		scene.debug().enqueueCallback(s -> SandPaperItem.spawnParticles(targetV, quartz, s.getWorld()));
		scene.world().removeItemsFromBelt(pressPos.below(2));
		ingot2 = scene.world().createItemOnBelt(pressPos.below(2), Direction.UP, polished);
		scene.world().stallBeltItem(ingot2, true);
		scene.idle(15);
		scene.world().stallBeltItem(ingot2, false);
	}

	public static void redstone(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("deployer_redstone", "Controlling Deployers with Redstone");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);
		scene.world().showSection(util.select().fromTo(3, 1, 5, 3, 1, 3), Direction.DOWN);

		BlockPos deployerPos = util.grid().at(3, 1, 3);
		Selection redstone = util.select().fromTo(3, 1, 1, 3, 1, 2);
		BlockPos leverPos = util.grid().at(3, 1, 1);

		scene.world().toggleRedstonePower(redstone);

		scene.idle(26);
		scene.world().moveDeployer(deployerPos, 1, 30);
		scene.idle(31);
		scene.world().moveDeployer(deployerPos, -1, 30);
		scene.world().showSection(redstone, Direction.SOUTH);
		scene.idle(31);

		scene.world().toggleRedstonePower(redstone);
		scene.effects().indicateRedstone(leverPos);
		scene.idle(10);

		scene.overlay().showText(60)
			.colored(PonderPalette.RED)
			.attachKeyFrame()
			.pointAt(util.vector().topOf(deployerPos))
			.placeNearTarget()
			.text("When powered by Redstone, Deployers will not activate");
		scene.idle(70);

		scene.world().toggleRedstonePower(redstone);
		scene.idle(10);
		scene.world().moveDeployer(deployerPos, 1f, 30);
		scene.idle(10);

		scene.world().toggleRedstonePower(redstone);
		scene.effects().indicateRedstone(leverPos);
		scene.idle(21);

		scene.overlay().showText(60)
			.pointAt(util.vector().topOf(deployerPos))
			.placeNearTarget()
			.text("Before stopping, the Deployer will finish any started cycles");

		scene.world().moveDeployer(deployerPos, -1f, 30);
		scene.idle(70);

		scene.world().toggleRedstonePower(redstone);
		scene.idle(3);
		scene.world().toggleRedstonePower(redstone);
		scene.effects().indicateRedstone(leverPos);
		scene.world().moveDeployer(deployerPos, 1, 30);
		scene.overlay().showText(100)
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.pointAt(util.vector().topOf(deployerPos))
			.placeNearTarget()
			.text("Thus, a negative pulse can be used to trigger exactly one activation cycle");
		scene.idle(31);
		scene.world().moveDeployer(deployerPos, -1, 30);
		scene.idle(40);

	}

	public static void contraption(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("deployer_contraption", "Using Deployers on Contraptions");
		scene.configureBasePlate(0, 0, 6);
		scene.scaleSceneView(.9f);
		Selection flowers = util.select().fromTo(4, 1, 1, 1, 1, 1);
		scene.world().replaceBlocks(flowers, Blocks.AIR.defaultBlockState(), false);

		Selection kinetics = util.select().fromTo(5, 1, 6, 5, 1, 3);
		BlockPos deployerPos = util.grid().at(4, 1, 3);
		Selection deployerSelection = util.select().position(deployerPos);

		scene.world().cycleBlockProperty(deployerPos, DeployerBlock.AXIS_ALONG_FIRST_COORDINATE);

		scene.world().showSection(util.select().layer(0)
			.add(flowers), Direction.UP);
		scene.idle(5);

		ElementLink<WorldSectionElement> pistonHead =
			scene.world().showIndependentSection(util.select().fromTo(5, 1, 2, 8, 1, 2), Direction.DOWN);
		scene.world().moveSection(pistonHead, util.vector().of(0, 0, 1), 0);
		scene.world().showSection(kinetics, Direction.DOWN);
		scene.idle(5);

		ElementLink<WorldSectionElement> contraption =
			scene.world().showIndependentSection(deployerSelection, Direction.DOWN);
		scene.idle(5);
		scene.world().glueBlockOnto(util.grid().at(4, 2, 3), Direction.DOWN, contraption);

		scene.overlay().showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().blockSurface(deployerPos, Direction.WEST))
			.text("Whenever Deployers are moved as part of an animated Contraption...");
		scene.idle(70);

		scene.world().setKineticSpeed(util.select().position(4, 0, 6), -8);
		scene.world().setKineticSpeed(kinetics, 16);
		scene.world().moveSection(pistonHead, util.vector().of(-3, 0, 0), 100);
		scene.world().moveSection(contraption, util.vector().of(-3, 0, 0), 100);

		for (int x = 0; x < 4; x++) {
			scene.world().moveDeployer(deployerPos, 1, 9);
			scene.idle(10);
			scene.world().moveDeployer(deployerPos, -1, 9);
			scene.world().restoreBlocks(util.select().position(4 - x, 1, 1));
			scene.idle(18);
		}

		scene.overlay().showOutlineWithText(flowers, 90)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("They activate at each visited location, using items from inventories anywhere on the contraption");
		scene.idle(100);

		scene.world().hideSection(flowers, Direction.UP);
		scene.idle(15);
		scene.world().replaceBlocks(flowers, Blocks.AIR.defaultBlockState(), false);
		scene.world().showSection(flowers, Direction.UP);

		Vec3 filterSlot = util.vector().blockSurface(deployerPos.west(3), Direction.WEST)
			.add(0, 0, 2 / 16f);
		scene.overlay().showFilterSlotInput(filterSlot, Direction.WEST, 80);
		scene.overlay().showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(filterSlot)
			.text("The Filter slot can be used to specify which items to pull");
		scene.idle(70);

		ItemStack poppy = new ItemStack(Items.POPPY);
		scene.overlay().showControls(filterSlot, Pointing.DOWN, 30).withItem(poppy);
		scene.idle(7);
		scene.world().setFilterData(deployerSelection, DeployerBlockEntity.class, poppy);
		scene.idle(25);

		scene.world().setKineticSpeed(util.select().position(4, 0, 6), 8);
		scene.world().setKineticSpeed(kinetics, -16);
		scene.world().moveSection(pistonHead, util.vector().of(3, 0, 0), 100);
		scene.world().moveSection(contraption, util.vector().of(3, 0, 0), 100);

		for (int x = 0; x < 4; x++) {
			scene.world().moveDeployer(deployerPos, 1, 9);
			scene.idle(10);
			scene.world().moveDeployer(deployerPos, -1, 9);
			scene.world().setBlock(util.grid().at(1 + x, 1, 1), Blocks.POPPY.defaultBlockState(), false);
			scene.idle(18);
		}

	}

}
