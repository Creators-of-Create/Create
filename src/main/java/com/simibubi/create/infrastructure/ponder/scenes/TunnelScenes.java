package com.simibubi.create.infrastructure.ponder.scenes;

import java.util.Vector;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pointing;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TunnelScenes {

	public static void andesite(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("andesite_tunnel", "Using Andesite Tunnels");
		scene.configureBasePlate(0, 0, 5);

		scene.world.cycleBlockProperty(util.grid.at(2, 1, 2), BeltBlock.CASING);

		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(4, 1, 5, 4, 1, 3), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(4, 1, 2, 0, 1, 2), Direction.SOUTH);
		scene.idle(10);

		Vector<ElementLink<WorldSectionElement>> tunnels = new Vector<>(3);
		for (int i = 0; i < 3; i++) {
			tunnels.add(scene.world.showIndependentSection(util.select.position(1 + i, 2, 4), Direction.DOWN));
			scene.world.moveSection(tunnels.get(i), util.vector.of(0, 0, -2), 0);
			scene.idle(4);
		}

		for (int i = 0; i < 3; i++) {
			scene.world.cycleBlockProperty(util.grid.at(1 + i, 1, 2), BeltBlock.CASING);
			scene.world.modifyBlockEntityNBT(util.select.position(1 + i, 1, 2), BeltBlockEntity.class,
				nbt -> NBTHelper.writeEnum(nbt, "Casing", BeltBlockEntity.CasingType.ANDESITE), true);
			scene.idle(4);
		}

		scene.overlay.showText(60)
			.attachKeyFrame()
			.pointAt(util.vector.topOf(util.grid.at(1, 2, 2)))
			.placeNearTarget()
			.text("Andesite Tunnels can be used to cover up your belts");
		scene.idle(70);

		for (int i = 0; i < 3; i++) {
			scene.world.cycleBlockProperty(util.grid.at(1 + i, 1, 2), BeltBlock.CASING);
			scene.world.hideIndependentSection(tunnels.get(i), Direction.UP);
			scene.idle(4);
		}
		scene.idle(10);
		scene.world.showSection(util.select.fromTo(2, 1, 0, 0, 1, 1), Direction.SOUTH);
		scene.idle(10);
		scene.world.showSection(util.select.position(2, 2, 2), Direction.DOWN);
		scene.idle(10);
		scene.world.cycleBlockProperty(util.grid.at(2, 1, 2), BeltBlock.CASING);

		scene.overlay.showText(60)
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 2, 2), Direction.NORTH))
			.placeNearTarget()
			.text("Whenever an Andesite Tunnel has connections to the sides...");
		scene.idle(70);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(util.grid.at(4, 1, 2)), Pointing.DOWN)
			.withItem(new ItemStack(Items.COPPER_INGOT)), 20);
		scene.idle(7);
		scene.world.createItemOnBelt(util.grid.at(4, 1, 2), Direction.UP, new ItemStack(Items.COPPER_INGOT, 64));
		scene.idle(40);
		scene.world.multiplyKineticSpeed(util.select.everywhere(), 1 / 16f);
		scene.overlay.showText(80)
			.attachKeyFrame()
			.text("...they will split exactly one item off of any passing stacks")
			.pointAt(util.vector.blockSurface(util.grid.at(2, 1, 0), Direction.WEST))
			.placeNearTarget();
		scene.idle(90);
		scene.overlay.showText(80)
			.text("The remainder will continue on its path")
			.pointAt(util.vector.blockSurface(util.grid.at(0, 1, 2), Direction.UP))
			.placeNearTarget();
		scene.idle(90);
		scene.world.multiplyKineticSpeed(util.select.everywhere(), 16f);
	}

	public static void brass(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("brass_tunnel", "Using Brass Tunnels");
		scene.configureBasePlate(1, 0, 5);
		scene.world.cycleBlockProperty(util.grid.at(3, 1, 2), BeltBlock.CASING);

		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(5, 1, 5, 5, 1, 3), Direction.DOWN);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(5, 1, 2, 1, 1, 2), Direction.SOUTH);
		scene.idle(10);

		Vector<ElementLink<WorldSectionElement>> tunnels = new Vector<>(3);
		for (int i = 0; i < 3; i++) {
			tunnels.add(scene.world.showIndependentSection(util.select.position(2 + i, 2, 4), Direction.DOWN));
			scene.world.moveSection(tunnels.get(i), util.vector.of(0, 0, -2), 0);
			scene.idle(4);
		}

		for (int i = 0; i < 3; i++) {
			scene.world.cycleBlockProperty(util.grid.at(2 + i, 1, 2), BeltBlock.CASING);
			scene.world.modifyBlockEntityNBT(util.select.position(2 + i, 1, 2), BeltBlockEntity.class,
				nbt -> NBTHelper.writeEnum(nbt, "Casing", BeltBlockEntity.CasingType.BRASS), true);
			scene.idle(4);
		}

		scene.overlay.showText(60)
			.attachKeyFrame()
			.pointAt(util.vector.topOf(util.grid.at(2, 2, 2)))
			.placeNearTarget()
			.text("Brass Tunnels can be used to cover up your belts");
		scene.idle(70);

		for (int i = 0; i < 3; i++) {
			scene.world.cycleBlockProperty(util.grid.at(2 + i, 1, 2), BeltBlock.CASING);
			scene.world.hideIndependentSection(tunnels.get(i), Direction.UP);
			scene.idle(4);
		}
		scene.idle(10);
		scene.world.showSection(util.select.fromTo(3, 1, 0, 1, 1, 1), Direction.SOUTH);
		scene.idle(10);
		scene.world.showSection(util.select.position(3, 2, 2), Direction.DOWN);
		scene.idle(10);
		scene.world.cycleBlockProperty(util.grid.at(3, 1, 2), BeltBlock.CASING);
		scene.idle(10);

		BlockPos tunnelPos = util.grid.at(3, 2, 2);
		for (Direction d : Iterate.horizontalDirections) {
			if (d == Direction.SOUTH)
				continue;
			Vec3 filter = getTunnelFilterVec(tunnelPos, d);
			scene.overlay.showFilterSlotInput(filter, d, 40);
			scene.idle(3);
		}

		scene.overlay.showText(60)
			.attachKeyFrame()
			.pointAt(getTunnelFilterVec(tunnelPos, Direction.WEST))
			.placeNearTarget()
			.text("Brass Tunnels have filter slots on each open side");
		scene.idle(70);

		scene.rotateCameraY(70);

		scene.idle(20);
		Vec3 tunnelFilterVec = getTunnelFilterVec(tunnelPos, Direction.EAST);
		scene.overlay.showFilterSlotInput(tunnelFilterVec, Direction.EAST, 10);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.pointAt(tunnelFilterVec)
			.placeNearTarget()
			.text("Filters on inbound connections simply block non-matching items");
		ItemStack copper = new ItemStack(Items.COPPER_INGOT);
		Class<BrassTunnelBlockEntity> tunnelClass = BrassTunnelBlockEntity.class;
		scene.world.modifyBlockEntity(tunnelPos, tunnelClass, be -> be.getBehaviour(SidedFilteringBehaviour.TYPE)
			.setFilter(Direction.EAST, copper));
		scene.overlay.showControls(new InputWindowElement(tunnelFilterVec, Pointing.DOWN).withItem(copper), 30);
		ItemStack zinc = AllItems.ZINC_INGOT.asStack();
		scene.world.createItemOnBelt(util.grid.at(5, 1, 2), Direction.EAST, zinc);
		scene.idle(70);
		scene.world.multiplyKineticSpeed(util.select.everywhere(), -2);
		scene.idle(20);
		scene.rotateCameraY(-70);
		scene.world.multiplyKineticSpeed(util.select.everywhere(), -.5f);
		scene.idle(20);
		scene.world.modifyBlockEntity(tunnelPos, tunnelClass, be -> be.getBehaviour(SidedFilteringBehaviour.TYPE)
			.setFilter(Direction.EAST, ItemStack.EMPTY));

		tunnelFilterVec = getTunnelFilterVec(tunnelPos, Direction.NORTH);
		scene.overlay.showFilterSlotInput(tunnelFilterVec, Direction.NORTH, 40);
		tunnelFilterVec = getTunnelFilterVec(tunnelPos, Direction.WEST);
		scene.overlay.showFilterSlotInput(tunnelFilterVec, Direction.WEST, 40);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.pointAt(tunnelFilterVec)
			.placeNearTarget()
			.text("Filters on outbound connections can be used to sort items by type");
		scene.idle(70);

		scene.overlay.showControls(new InputWindowElement(tunnelFilterVec, Pointing.LEFT).withItem(copper), 30);
		scene.world.modifyBlockEntity(tunnelPos, tunnelClass, be -> be.getBehaviour(SidedFilteringBehaviour.TYPE)
			.setFilter(Direction.WEST, copper));
		scene.idle(4);
		tunnelFilterVec = getTunnelFilterVec(tunnelPos, Direction.NORTH);
		scene.overlay.showControls(new InputWindowElement(tunnelFilterVec, Pointing.RIGHT).withItem(zinc), 30);
		scene.world.modifyBlockEntity(tunnelPos, tunnelClass, be -> be.getBehaviour(SidedFilteringBehaviour.TYPE)
			.setFilter(Direction.NORTH, zinc));

		scene.world.multiplyKineticSpeed(util.select.everywhere(), 1.5f);
		for (int i = 0; i < 6; i++) {
			scene.world.createItemOnBelt(util.grid.at(5, 1, 2), Direction.EAST, i % 2 == 0 ? zinc : copper);
			scene.idle(12);
		}

		scene.idle(30);
		scene.world.modifyBlockEntity(tunnelPos, tunnelClass, be -> be.getBehaviour(SidedFilteringBehaviour.TYPE)
			.setFilter(Direction.NORTH, ItemStack.EMPTY));
		scene.world.modifyBlockEntity(tunnelPos, tunnelClass, be -> be.getBehaviour(SidedFilteringBehaviour.TYPE)
			.setFilter(Direction.WEST, ItemStack.EMPTY));
		scene.idle(10);

		Vec3 tunnelTop = util.vector.topOf(tunnelPos);
		scene.overlay.showCenteredScrollInput(tunnelPos, Direction.UP, 120);
		scene.overlay.showText(120)
			.attachKeyFrame()
			.pointAt(tunnelTop)
			.placeNearTarget()
			.text(
				"Whenever a passing item has multiple valid exits, the distribution mode will decide how to handle it");
		for (int i = 0; i < 3; i++) {
			scene.idle(40);
			scene.world.createItemOnBelt(util.grid.at(5, 1, 2), Direction.EAST, AllItems.BRASS_INGOT.asStack(63));
		}
		scene.idle(30);

		scene.world.hideSection(util.select.position(3, 2, 2), Direction.UP);
		scene.idle(5);
		scene.world.hideSection(util.select.fromTo(5, 1, 2, 1, 1, 0), Direction.UP);
		scene.idle(15);

		ElementLink<WorldSectionElement> newBelt =
			scene.world.showIndependentSection(util.select.fromTo(3, 3, 2, 0, 3, 4)
				.add(util.select.fromTo(5, 3, 3, 4, 3, 3)), Direction.DOWN);
		scene.world.moveSection(newBelt, util.vector.of(0, -2, -1), 0);
		scene.idle(15);
		for (int i = 0; i < 3; i++) {
			scene.idle(4);
			scene.world.showSectionAndMerge(util.select.position(3, 4, 2 + i), Direction.DOWN, newBelt);
		}

		scene.overlay.showSelectionWithText(util.select.fromTo(3, 1, 1, 3, 2, 3), 80)
			.attachKeyFrame()
			.placeNearTarget()
			.text("Brass Tunnels on parallel belts will form a group");
		scene.idle(90);

		ItemStack item1 = new ItemStack(Items.CARROT);
		ItemStack item2 = new ItemStack(Items.HONEY_BOTTLE);
		ItemStack item3 = new ItemStack(Items.SWEET_BERRIES);

		tunnelFilterVec = getTunnelFilterVec(tunnelPos, Direction.WEST);
		BlockPos newTunnelPos = tunnelPos.above(2)
			.south();
		scene.overlay
			.showControls(new InputWindowElement(tunnelFilterVec.add(0, 0, -1), Pointing.RIGHT).withItem(item1), 20);
		scene.world.modifyBlockEntity(newTunnelPos.north(), tunnelClass,
			be -> be.getBehaviour(SidedFilteringBehaviour.TYPE)
				.setFilter(Direction.WEST, item1));
		scene.idle(4);
		scene.overlay.showControls(new InputWindowElement(tunnelFilterVec, Pointing.DOWN).withItem(item2), 20);
		scene.world.modifyBlockEntity(newTunnelPos, tunnelClass, be -> be.getBehaviour(SidedFilteringBehaviour.TYPE)
			.setFilter(Direction.WEST, item2));
		scene.idle(4);
		scene.overlay.showControls(new InputWindowElement(tunnelFilterVec.add(0, 0, 1), Pointing.LEFT).withItem(item3),
			20);
		scene.world.modifyBlockEntity(newTunnelPos.south(), tunnelClass,
			be -> be.getBehaviour(SidedFilteringBehaviour.TYPE)
				.setFilter(Direction.WEST, item3));
		scene.idle(30);

		scene.overlay.showText(80)
			.pointAt(tunnelTop)
			.placeNearTarget()
			.text("Incoming Items will now be distributed across all connected exits");
		scene.idle(90);

		BlockPos beltPos = util.grid.at(5, 3, 3);
		Vec3 m = util.vector.of(0, 0.1, 0);
		Vec3 spawn = util.vector.centerOf(util.grid.at(5, 3, 2));
		scene.world.createItemEntity(spawn, m, item1);
		scene.idle(12);
		scene.world.createItemOnBelt(beltPos, Direction.UP, item1);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		scene.world.createItemEntity(spawn, m, item2);
		scene.idle(12);
		scene.world.createItemOnBelt(beltPos, Direction.UP, item2);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		scene.world.createItemEntity(spawn, m, item3);
		scene.idle(12);
		scene.world.createItemOnBelt(beltPos, Direction.UP, item3);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		scene.idle(50);

		scene.world.showSectionAndMerge(util.select.position(3, 5, 2), Direction.DOWN, newBelt);

		scene.overlay.showText(80)
			.pointAt(util.vector.blockSurface(tunnelPos.above()
				.north(), Direction.WEST))
			.placeNearTarget()
			.text("For this, items can also be inserted into the Tunnel block directly");
		scene.idle(20);

		beltPos = util.grid.at(3, 3, 3);
		spawn = util.vector.centerOf(util.grid.at(3, 5, 1));
		scene.world.createItemEntity(spawn, m, item1);
		scene.idle(12);
		scene.world.createItemOnBelt(beltPos, Direction.EAST, item1);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		scene.world.createItemEntity(spawn, m, item2);
		scene.idle(12);
		scene.world.createItemOnBelt(beltPos, Direction.EAST, item2);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		scene.world.createItemEntity(spawn, m, item3);
		scene.idle(12);
		scene.world.createItemOnBelt(beltPos, Direction.EAST, item3);
		scene.world.modifyEntities(ItemEntity.class, Entity::discard);
		scene.idle(30);

	}

	protected static Vec3 getTunnelFilterVec(BlockPos pos, Direction d) {
		return VecHelper.getCenterOf(pos)
			.add(Vec3.atLowerCornerOf(d.getNormal())
				.scale(.5))
			.add(0, 0.3, 0);
	}

	public static void brassModes(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("brass_tunnel_modes", "Distribution Modes of the Brass Tunnel");
		scene.configureBasePlate(0, 1, 5);
		BlockState barrier = Blocks.BARRIER.defaultBlockState();
		scene.world.setBlock(util.grid.at(1, 1, 0), barrier, false);
		scene.world.showSection(util.select.layer(0), Direction.UP);
		scene.idle(5);
		scene.world.showSection(util.select.fromTo(1, 1, 1, 5, 1, 5)
			.add(util.select.fromTo(3, 2, 5, 1, 2, 5)), Direction.DOWN);
		scene.idle(10);
		for (int i = 0; i < 3; i++) {
			scene.world.showSection(util.select.position(3 - i, 2, 3), Direction.DOWN);
			scene.idle(4);
		}

		Vec3 tunnelTop = util.vector.topOf(util.grid.at(2, 2, 3));
		scene.overlay.showControls(new InputWindowElement(tunnelTop, Pointing.DOWN).rightClick(), 80);
		scene.idle(7);
		scene.overlay.showCenteredScrollInput(util.grid.at(2, 2, 3), Direction.UP, 120);
		scene.overlay.showText(120)
			.attachKeyFrame()
			.pointAt(tunnelTop)
			.placeNearTarget()
			.text("The distribution behaviour of Brass Tunnels can be configured");
		scene.idle(130);

		Class<BrassTunnelBlockEntity> tunnelClass = BrassTunnelBlockEntity.class;
		ElementLink<WorldSectionElement> blockage =
			scene.world.showIndependentSection(util.select.position(4, 1, 0), Direction.UP);
		scene.world.moveSection(blockage, util.vector.of(-3, 0, 0), 0);

		Vec3 modeVec = util.vector.of(4, 2.5, 3);
		scene.overlay.showControls(new InputWindowElement(modeVec, Pointing.RIGHT).showing(AllIcons.I_TUNNEL_SPLIT),
			140);

		ElementLink<WorldSectionElement> blockage2 = null;

		for (int i = 0; i < 32; i++) {
			if (i < 30)
				scene.world.createItemOnBelt(util.grid.at(1, 1, 5), Direction.EAST, new ItemStack(Items.SNOWBALL, 12));
			scene.idle(i > 8 ? 30 : 40);

			if (i == 0) {
				scene.overlay.showText(80)
					.attachKeyFrame()
					.pointAt(tunnelTop)
					.placeNearTarget()
					.text("'Split' will attempt to distribute the stack evenly between available outputs");
			}

			if (i == 2) {
				scene.overlay.showText(60)
					.text("If an output is unable to take more items, it will be skipped")
					.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.UP))
					.placeNearTarget()
					.colored(PonderPalette.GREEN);
			}

			if (i == 4) {
				scene.overlay.showControls(
					new InputWindowElement(modeVec, Pointing.RIGHT).showing(AllIcons.I_TUNNEL_FORCED_SPLIT), 140);
				scene.world.modifyBlockEntity(util.grid.at(1, 2, 3), tunnelClass,
					be -> be.getBehaviour(ScrollOptionBehaviour.TYPE)
						.setValue(BrassTunnelBlockEntity.SelectionMode.FORCED_SPLIT.ordinal()));
			}

			if (i == 5) {
				scene.overlay.showText(80)
					.attachKeyFrame()
					.text("'Forced Split' will never skip outputs, and instead wait until they are free")
					.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.UP))
					.placeNearTarget()
					.colored(PonderPalette.RED);
				scene.idle(60);
				scene.world.moveSection(blockage, util.vector.of(-1, 0, 0), 10);
				scene.world.setBlock(util.grid.at(1, 1, 0), Blocks.AIR.defaultBlockState(), false);
				scene.world.multiplyKineticSpeed(util.select.everywhere(), 1.5f);
			}

			if (i == 7) {
				scene.world.modifyBlockEntity(util.grid.at(1, 2, 3), tunnelClass,
					be -> be.getBehaviour(ScrollOptionBehaviour.TYPE)
						.setValue(BrassTunnelBlockEntity.SelectionMode.ROUND_ROBIN.ordinal()));
				scene.overlay.showControls(
					new InputWindowElement(modeVec, Pointing.RIGHT).showing(AllIcons.I_TUNNEL_ROUND_ROBIN), 140);
				scene.overlay.showText(80)
					.attachKeyFrame()
					.pointAt(tunnelTop)
					.placeNearTarget()
					.text("'Round Robin' keeps stacks whole, and cycles through outputs iteratively");
			}

			if (i == 7) {
				scene.world.moveSection(blockage, util.vector.of(1, 0, 0), 10);
				scene.world.setBlock(util.grid.at(1, 1, 0), barrier, false);
			}

			if (i == 13) {
				scene.overlay.showText(60)
					.text("Once Again, if an output is unable to take more items, it will be skipped")
					.placeNearTarget()
					.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.UP))
					.colored(PonderPalette.GREEN);
			}

			if (i == 15) {
				scene.overlay.showControls(
					new InputWindowElement(modeVec, Pointing.RIGHT).showing(AllIcons.I_TUNNEL_FORCED_ROUND_ROBIN), 140);
				scene.world.modifyBlockEntity(util.grid.at(1, 2, 3), tunnelClass,
					be -> be.getBehaviour(ScrollOptionBehaviour.TYPE)
						.setValue(BrassTunnelBlockEntity.SelectionMode.FORCED_ROUND_ROBIN.ordinal()));
			}

			if (i == 16) {
				scene.overlay.showText(50)
					.attachKeyFrame()
					.placeNearTarget()
					.text("'Forced Round Robin' never skips outputs")
					.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.UP))
					.colored(PonderPalette.RED);
				scene.idle(30);
				scene.world.moveSection(blockage, util.vector.of(-1, 0, 0), 10);
				scene.world.setBlock(util.grid.at(1, 1, 0), Blocks.AIR.defaultBlockState(), false);
			}

			if (i == 19) {
				scene.overlay.showControls(
					new InputWindowElement(modeVec, Pointing.RIGHT).showing(AllIcons.I_TUNNEL_PREFER_NEAREST), 140);
				scene.world.modifyBlockEntity(util.grid.at(1, 2, 3), tunnelClass,
					be -> be.getBehaviour(ScrollOptionBehaviour.TYPE)
						.setValue(BrassTunnelBlockEntity.SelectionMode.PREFER_NEAREST.ordinal()));
				scene.world.moveSection(blockage, util.vector.of(1, 0, 0), 10);
				scene.world.setBlock(util.grid.at(1, 1, 0), barrier, false);
				scene.overlay.showText(70)
					.attachKeyFrame()
					.text("'Prefer Nearest' prioritizes the outputs closest to the items' input location")
					.pointAt(util.vector.blockSurface(util.grid.at(1, 1, 2), Direction.UP))
					.placeNearTarget()
					.colored(PonderPalette.GREEN);
			}

			if (i == 21) {
				scene.world.setBlock(util.grid.at(2, 1, 0), Blocks.BARRIER.defaultBlockState(), false);
				blockage2 = scene.world.showIndependentSection(util.select.position(4, 1, 0), Direction.UP);
				scene.world.moveSection(blockage2, util.vector.of(-2, 0, 0), 0);
			}

			if (i == 25) {
				scene.world.hideIndependentSection(blockage, Direction.DOWN);
				scene.world.setBlock(util.grid.at(1, 1, 0), Blocks.AIR.defaultBlockState(), false);
				scene.world.hideIndependentSection(blockage2, Direction.DOWN);
				scene.world.setBlock(util.grid.at(2, 1, 0), Blocks.AIR.defaultBlockState(), false);
			}

			if (i == 26) {
				scene.overlay.showControls(
					new InputWindowElement(modeVec, Pointing.RIGHT).showing(AllIcons.I_TUNNEL_RANDOMIZE), 140);
				scene.world.modifyBlockEntity(util.grid.at(1, 2, 3), tunnelClass,
					be -> be.getBehaviour(ScrollOptionBehaviour.TYPE)
						.setValue(BrassTunnelBlockEntity.SelectionMode.RANDOMIZE.ordinal()));
			}

			if (i == 27) {
				scene.overlay.showText(70)
					.attachKeyFrame()
					.text("'Randomize' will distribute whole stacks to randomly picked outputs")
					.pointAt(tunnelTop)
					.placeNearTarget();
			}
		}

		scene.world.hideSection(util.select.fromTo(3, 2, 5, 1, 2, 5), Direction.UP);
		scene.idle(10);
		scene.overlay
			.showControls(new InputWindowElement(modeVec, Pointing.RIGHT).showing(AllIcons.I_TUNNEL_SYNCHRONIZE), 140);
		scene.world.modifyBlockEntity(util.grid.at(1, 2, 3), tunnelClass,
			be -> be.getBehaviour(ScrollOptionBehaviour.TYPE)
				.setValue(BrassTunnelBlockEntity.SelectionMode.SYNCHRONIZE.ordinal()));
		scene.idle(30);
		scene.overlay.showText(70)
			.attachKeyFrame()
			.text("'Synchronize Inputs' is a unique setting for Brass Tunnels")
			.pointAt(tunnelTop)
			.placeNearTarget();

		ItemStack item1 = new ItemStack(Items.CARROT);
		ItemStack item2 = new ItemStack(Items.HONEY_BOTTLE);
		ItemStack item3 = AllItems.POLISHED_ROSE_QUARTZ.asStack();

		scene.world.createItemOnBelt(util.grid.at(3, 1, 4), Direction.UP, item1);
		scene.world.createItemOnBelt(util.grid.at(2, 1, 4), Direction.UP, item2);
		scene.world.createItemOnBelt(util.grid.at(3, 1, 5), Direction.SOUTH, item1);
		scene.world.createItemOnBelt(util.grid.at(2, 1, 5), Direction.SOUTH, item2);

		scene.idle(80);
		scene.world.createItemOnBelt(util.grid.at(2, 1, 5), Direction.SOUTH, item2);
		scene.rotateCameraY(-90);
		scene.idle(20);
		scene.world.multiplyKineticSpeed(util.select.everywhere(), .5f);

		scene.overlay.showText(70)
			.text("Items are only allowed past if every tunnel in the group has one waiting")
			.pointAt(util.vector.blockSurface(util.grid.at(2, 1, 4), Direction.UP))
			.placeNearTarget()
			.colored(PonderPalette.OUTPUT);
		scene.idle(60);
		scene.world.createItemOnBelt(util.grid.at(1, 1, 5), Direction.SOUTH, item3);
		scene.idle(90);
		scene.rotateCameraY(90);

		scene.overlay.showText(100)
			.text("This ensures that all affected belts supply items at the same rate")
			.pointAt(util.vector.blockSurface(util.grid.at(1, 2, 3), Direction.WEST))
			.placeNearTarget()
			.colored(PonderPalette.GREEN);
	}

}
