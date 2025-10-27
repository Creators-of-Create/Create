package com.simibubi.create.infrastructure.ponder.scenes.highLogistics;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.redstoneRequester.AutoRequestData;
import com.simibubi.create.content.logistics.redstoneRequester.AutoRequestData.Mutable;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlock;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ParrotPose.FacePointOfInterestPose;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TableClothScenes {

	public static void tableCloth(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("table_cloth", "Selling items with Table Cloths");
		scene.configureBasePlate(0, 0, 7);
		scene.scaleSceneView(0.925f);
		scene.setSceneOffsetY(-0.5f);
		scene.showBasePlate();

		Selection initialCloth = util.select()
			.fromTo(3, 1, 3, 3, 2, 3);
		Selection scaff1 = util.select()
			.position(5, 1, 1);
		Selection cloth1 = util.select()
			.position(5, 2, 1);
		Selection vault = util.select()
			.fromTo(4, 1, 4, 3, 3, 5);
		Selection packScaff = util.select()
			.position(2, 1, 4);
		BlockPos pack = util.grid()
			.at(2, 2, 4);
		Selection packager = util.select()
			.position(2, 2, 4);
		BlockPos link = util.grid()
			.at(2, 3, 4);
		Selection linkS = util.select()
			.position(2, 3, 4);
		Selection funnel = util.select()
			.position(1, 2, 4);
		Selection belt = util.select()
			.fromTo(1, 1, 5, 1, 1, 0);
		Selection largeCog = util.select()
			.position(2, 0, 7);
		Selection cogs = util.select()
			.fromTo(2, 1, 5, 2, 1, 6);
		Selection ticker = util.select()
			.position(3, 1, 1);
		Selection seat = util.select()
			.position(3, 1, 2);

		scene.idle(10);

		scene.world()
			.showSection(initialCloth, Direction.DOWN);
		scene.idle(15);

		ItemStack grass = new ItemStack(Items.OAK_LOG);
		scene.overlay()
			.showControls(util.vector()
				.centerOf(3, 2, 3), Pointing.DOWN, 50)
			.rightClick()
			.withItem(grass);

		scene.idle(7);
		scene.effects()
			.indicateSuccess(util.grid()
				.at(3, 2, 3));
		scene.world()
			.cycleBlockProperty(util.grid()
				.at(3, 2, 3), TableClothBlock.HAS_BE);
		scene.world()
			.modifyBlockEntity(util.grid()
				.at(3, 2, 3), TableClothBlockEntity.class, be -> be.manuallyAddedItems.add(grass));
		scene.idle(10);

		scene.overlay()
			.showText(60)
			.text("Table cloths can be used to display items")
			.attachKeyFrame()
			.pointAt(util.vector()
				.topOf(3, 1, 3))
			.placeNearTarget();
		scene.idle(70);

		scene.effects()
			.indicateSuccess(util.grid()
				.at(3, 2, 3));
		scene.world()
			.modifyBlockEntity(util.grid()
				.at(3, 2, 3), TableClothBlockEntity.class, be -> {
					AutoRequestData.Mutable mutable = new Mutable();
					mutable.encodedRequest = PackageOrderWithCrafts.simple(List.of(new BigItemStack(grass)));
					mutable.isValid = true;
					be.requestData = mutable.toImmutable();
					be.priceTag.setFilter(new ItemStack(Items.DIAMOND));
					be.priceTag.count = 1;
					be.facing = Direction.NORTH;
				});

		scene.overlay()
			.showText(100)
			.text("With the help of a logistics system, they can also be used to sell items")
			.attachKeyFrame()
			.pointAt(util.vector()
				.of(3, 1.75, 3))
			.placeNearTarget();
		scene.idle(110);

		scene.world()
			.hideSection(initialCloth, Direction.UP);
		scene.idle(20);
		scene.world()
			.showSection(vault, Direction.NORTH);
		scene.world()
			.showSection(packScaff, Direction.NORTH);
		scene.idle(10);
		scene.world()
			.showSection(packager, Direction.EAST);
		scene.idle(10);
		scene.world()
			.showSection(linkS, Direction.DOWN);

		scene.overlay()
			.showOutlineWithText(util.select()
				.fromTo(3, 2, 4, 4, 3, 5), 100)
			.text("Start with the shop's inventory, a Packager and Stock link")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.pointAt(util.vector()
				.of(2, 3, 4))
			.placeNearTarget();
		scene.idle(110);

		ItemStack tickerItem = AllBlocks.STOCK_TICKER.asStack();
		scene.overlay()
			.showControls(util.vector()
				.centerOf(2, 3, 4), Pointing.DOWN, 80)
			.rightClick()
			.withItem(tickerItem);
		scene.idle(5);

		AABB bb1 = new AABB(link);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, link, bb1.deflate(0.45), 10);
		scene.idle(1);
		bb1 = bb1.deflate(1 / 16f)
			.contract(0, 8 / 16f, 0);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.BLUE, link, bb1, 50);
		scene.idle(26);

		scene.overlay()
			.showText(80)
			.text("Bind a Stock ticker to the link and place it in the shop")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.pointAt(util.vector()
				.centerOf(link))
			.placeNearTarget();
		scene.idle(70);

		scene.world()
			.showSection(ticker, Direction.DOWN);
		scene.idle(10);
		scene.world()
			.showSection(seat, Direction.NORTH);
		scene.idle(10);
		scene.special()
			.createBirb(util.vector()
				.centerOf(util.grid()
					.at(3, 1, 2)),
				FacePointOfInterestPose::new);
		scene.idle(20);

		scene.overlay()
			.showText(90)
			.text("Employ a mob or blaze burner as the shop keeper")
			.attachKeyFrame()
			.pointAt(util.vector()
				.of(2.5, 2.75, 1.5))
			.placeNearTarget();
		scene.idle(100);

		ItemStack logItem1 = new ItemStack(Items.OAK_LOG);
		ItemStack logItem2 = new ItemStack(Items.BIRCH_LOG);
		scene.overlay()
			.showControls(util.vector()
				.of(5, 3.5, 4), Pointing.RIGHT, 80)
			.withItem(logItem1);
		scene.overlay()
			.showControls(util.vector()
				.of(5, 2, 4), Pointing.RIGHT, 80)
			.withItem(logItem2);
		scene.idle(10);

		scene.overlay()
			.showText(70)
			.text("Fill the shop inventory with items to be sold")
			.attachKeyFrame()
			.pointAt(util.vector()
				.of(4, 3, 4))
			.placeNearTarget();
		scene.idle(80);

		ItemStack clothItem = AllBlocks.TABLE_CLOTHS.get(DyeColor.LIGHT_GRAY)
			.asStack();
		scene.overlay()
			.showControls(util.vector()
				.of(3, 3, 1.75), Pointing.DOWN, 120)
			.rightClick()
			.withItem(clothItem);
		scene.idle(30);

		scene.overlay()
			.showText(100)
			.text("To create a new trade, interact with the shop keeper while holding a table cloth")
			.attachKeyFrame()
			.pointAt(util.vector()
				.of(2.5, 2.75, 1.5))
			.placeNearTarget();
		scene.idle(100);

		scene.world()
			.cycleBlockProperty(util.grid()
				.at(5, 2, 1), TableClothBlock.HAS_BE);
		scene.world()
			.modifyBlockEntity(util.grid()
				.at(5, 2, 1), TableClothBlockEntity.class, be -> {
					AutoRequestData.Mutable mutable = new Mutable();
					mutable.encodedRequest = PackageOrderWithCrafts.simple(List.of(new BigItemStack(logItem1)));
					mutable.isValid = true;
					be.requestData = mutable.toImmutable();
					be.facing = Direction.NORTH;
				});

		scene.world()
			.showSection(scaff1, Direction.DOWN);
		scene.idle(10);
		scene.world()
			.showSection(cloth1, Direction.DOWN);
		scene.idle(20);

		ItemStack diamondItem = new ItemStack(Items.DIAMOND);
		Vec3 filterSlot = util.vector()
			.of(5.25, 1.825, 1);
		scene.overlay()
			.showControls(filterSlot, Pointing.DOWN, 120)
			.rightClick()
			.withItem(diamondItem);
		scene.idle(5);
		scene.world()
			.setFilterData(util.select()
				.position(5, 2, 1), TableClothBlockEntity.class, diamondItem);
		scene.idle(15);

		scene.overlay()
			.showText(90)
			.text("Once placed, set a price in the item slot on the side")
			.attachKeyFrame()
			.pointAt(filterSlot)
			.placeNearTarget();
		scene.idle(120);

		scene.overlay()
			.showControls(util.vector()
				.centerOf(util.grid()
					.at(5, 2, 1)),
				Pointing.DOWN, 90)
			.rightClick();
		scene.idle(10);

		scene.overlay()
			.showText(90)
			.text("Other players can now interact with the shop")
			.attachKeyFrame()
			.pointAt(util.vector()
				.of(5, 2, 1.5))
			.placeNearTarget();
		scene.idle(100);

		ItemStack listItem = AllItems.SHOPPING_LIST.asStack();
		scene.overlay()
			.showControls(util.vector()
				.of(3, 3, 1.75), Pointing.DOWN, 90)
			.rightClick()
			.withItem(listItem);
		scene.idle(5);
		scene.effects()
			.indicateSuccess(util.grid()
				.at(3, 1, 1));
		PonderHilo.linkEffect(scene, link);
		ItemStack box = PackageItem.containing(List.of());
		PonderHilo.packagerCreate(scene, pack, box);
		scene.idle(30);

		scene.overlay()
			.showText(120)
			.text("When checking out at the cashier, the bought items will be placed into a package")
			.attachKeyFrame()
			.pointAt(util.vector()
				.of(2.5, 2.5, 4))
			.placeNearTarget();
		scene.idle(100);

		scene.world()
			.showSection(largeCog, Direction.UP);
		scene.idle(3);
		scene.world()
			.showSection(cogs, Direction.DOWN);
		scene.idle(3);
		scene.world()
			.showSection(belt, Direction.EAST);
		scene.idle(5);
		scene.world()
			.showSection(funnel, Direction.DOWN);
		scene.idle(15);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(1, 1, 4), Direction.EAST, box);
		PonderHilo.packagerClear(scene, pack);
		scene.idle(45);

		scene.overlay()
			.showText(90)
			.text("From there, they can be transported to the shop front")
			.attachKeyFrame()
			.pointAt(util.vector()
				.of(1, 2.5, 1.5))
			.placeNearTarget();
		scene.idle(100);

		scene.overlay()
			.showControls(util.vector()
				.topOf(util.grid()
					.at(3, 1, 1)),
				Pointing.DOWN, 100)
			.rightClick()
			.withItem(diamondItem);
		scene.idle(10);

		scene.overlay()
			.showText(90)
			.text("The payments will be stored inside the stock ticker block")
			.attachKeyFrame()
			.pointAt(util.vector()
				.centerOf(util.grid()
					.at(3, 1, 1)))
			.placeNearTarget();
		scene.idle(100);

	}

}
