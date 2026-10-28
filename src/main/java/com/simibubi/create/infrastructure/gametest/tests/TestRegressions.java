package com.simibubi.create.infrastructure.gametest.tests;

import static com.simibubi.create.infrastructure.gametest.CreateGameTestHelper.TEN_SECONDS;

import java.util.ArrayList;
import java.util.HashMap;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.content.kinetics.belt.transport.BeltMovementHandler;
import com.simibubi.create.content.kinetics.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

@GameTestGroup(path = "regressions")
public class TestRegressions {
	@GameTest(template = "issue9615_efficient_deployers", timeoutTicks = TEN_SECONDS)
	public static void issue9615_efficientDeployers(CreateGameTestHelper helper) {
		final BlockPos lever = new BlockPos(2, 5, 0);
		final BlockPos goal = new BlockPos(1, 3, 4);
		helper.unpowerLever(lever);
		helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.LIME_STAINED_GLASS, goal));
	}

	@GameTest(template = "issue9615_efficient_deployers")
	public static void issue909_diagonalSidewaysBelts(CreateGameTestHelper helper) {
		BlockPos start = new BlockPos(0, 2, 0);
		BlockPos middle = new BlockPos(1, 2, 1);
		BlockPos end = new BlockPos(2, 2, 2);
		BlockState verticalShaft = AllBlocks.SHAFT.getDefaultState()
			.setValue(AbstractSimpleShaftBlock.AXIS, Axis.Y);

		helper.setBlock(start, verticalShaft);
		helper.setBlock(middle, Blocks.AIR);
		helper.setBlock(end, verticalShaft);

		BlockPos absoluteStart = helper.absolutePos(start);
		BlockPos absoluteEnd = helper.absolutePos(end);
		helper.assertTrue(BeltConnectorItem.canConnect(helper.getLevel(), absoluteStart, absoluteEnd),
			"Vertical shafts should accept a diagonal belt connection");
		BeltConnectorItem.createBelts(helper.getLevel(), absoluteStart, absoluteEnd);
		BeltBlock.initBelt(helper.getLevel(), absoluteStart);

		helper.assertBlockProperty(start, BeltBlock.SLOPE, BeltSlope.DIAGONAL_SIDEWAYS);
		helper.assertBlockProperty(middle, BeltBlock.SLOPE, BeltSlope.DIAGONAL_SIDEWAYS);
		helper.assertBlockProperty(end, BeltBlock.SLOPE, BeltSlope.DIAGONAL_SIDEWAYS);
		helper.assertBlockProperty(start, BeltBlock.HORIZONTAL_FACING, Direction.EAST);
		helper.assertBlockProperty(middle, BeltBlock.HORIZONTAL_FACING, Direction.EAST);
		helper.assertBlockProperty(end, BeltBlock.HORIZONTAL_FACING, Direction.EAST);
		helper.assertBlockProperty(start, BeltBlock.PART, BeltPart.START);
		helper.assertBlockProperty(middle, BeltBlock.PART, BeltPart.MIDDLE);
		helper.assertBlockProperty(end, BeltBlock.PART, BeltPart.END);
		BeltBlockEntity startBelt = helper.getBlockEntity(AllBlockEntityTypes.BELT.get(), start);
		BlockState startState = startBelt.getBlockState();
		helper.assertTrue(BeltBlock.canTransportEntities(startState),
			"Diagonal sideways belts should transport players and mobs");
		helper.assertTrue(!BeltBlock.canTransportObjects(startState),
			"Diagonal sideways belts should not transport items");
		var passenger = helper.spawn(EntityType.ZOMBIE, start.above());
		passenger.setNoAi(true);
		startBelt.getControllerBE().passengers = new HashMap<>();
		helper.assertTrue(!startState.getCollisionShape(helper.getLevel(), absoluteStart, CollisionContext.of(passenger))
			.isEmpty(), "Diagonal sideways belts should support entities");

		Vec3 beltCenter = Vec3.atCenterOf(absoluteStart);
		passenger.setPos(beltCenter.x, absoluteStart.getY() + 1, beltCenter.z);
		Vec3 forwardStart = passenger.position();
		startBelt.setSpeed(48);
		BeltMovementHandler.transportEntity(startBelt, passenger, new TransportedEntityInfo(absoluteStart, startState));
		Vec3 forwardMovement = passenger.position()
			.subtract(forwardStart);
		helper.assertTrue(forwardMovement.x * forwardMovement.z > 0,
			"Diagonal belts should move entities along both ground axes; movement was " + forwardMovement);

		passenger.setPos(beltCenter.x + .2, absoluteStart.getY() + 1, beltCenter.z - .2);
		double initialCenterOffset = Math.abs((passenger.getX() - beltCenter.x) - (passenger.getZ() - beltCenter.z));
		BeltMovementHandler.transportEntity(startBelt, passenger, new TransportedEntityInfo(absoluteStart, startState));
		double centeredOffset = Math.abs((passenger.getX() - beltCenter.x) - (passenger.getZ() - beltCenter.z));
		helper.assertTrue(centeredOffset < initialCenterOffset,
			"Diagonal belts should center entities toward the belt line");

		passenger.setPos(beltCenter.x, absoluteStart.getY() + 1, beltCenter.z);
		Vec3 reverseStart = passenger.position();
		startBelt.setSpeed(-48);
		BeltMovementHandler.transportEntity(startBelt, passenger, new TransportedEntityInfo(absoluteStart, startState));
		Vec3 reverseMovement = passenger.position()
			.subtract(reverseStart);
		helper.assertTrue(forwardMovement.x * reverseMovement.x < 0 && forwardMovement.z * reverseMovement.z < 0,
			"Reversing belt speed should reverse both movement axes");

		ItemEntity droppedItem = helper.spawnItem(start.above(), new ItemStack(Items.IRON_INGOT));
		Vec3 droppedItemPosition = droppedItem.position();
		startState.entityInside(helper.getLevel(), absoluteStart, droppedItem);
		helper.assertTrue(droppedItem.position()
			.equals(droppedItemPosition) && !startBelt.getControllerBE().passengers.containsKey(droppedItem),
			"Diagonal sideways belts should leave dropped items stationary");

		ArrayList<BlockPos> propagationLocations = new ArrayList<>();
		startBelt.addPropagationLocations((IRotate) startState.getBlock(), startState, propagationLocations);
		helper.assertTrue(propagationLocations.contains(absoluteStart.offset(1, 0, 1)),
			"Diagonal belt segments should propagate rotation to one another");

		assertDiagonalDirection(helper, 1, 1, Direction.EAST);
		assertDiagonalDirection(helper, 1, -1, Direction.NORTH);
		assertDiagonalDirection(helper, -1, 1, Direction.SOUTH);
		assertDiagonalDirection(helper, -1, -1, Direction.WEST);
		helper.succeed();
	}

	private static void assertDiagonalDirection(CreateGameTestHelper helper, int x, int z, Direction expectedFacing) {
		Direction facing = BeltHelper.getDiagonalFacing(x, z);
		helper.assertTrue(facing == expectedFacing, "Diagonal offset should select the expected facing");
		helper.assertTrue(BeltHelper.getBeltVector(facing, BeltSlope.DIAGONAL_SIDEWAYS)
			.equals(new BlockPos(x, 0, z)), "Diagonal facing should reproduce the original offset");
		helper.assertTrue(BeltHelper.getBeltPartForRendering(BeltSlope.DIAGONAL_SIDEWAYS, BeltPart.START)
			== BeltPart.END, "Diagonal starts should render toward the adjoining belt segment");
		helper.assertTrue(BeltHelper.getBeltPartForRendering(BeltSlope.DIAGONAL_SIDEWAYS, BeltPart.END)
			== BeltPart.START, "Diagonal ends should render toward the adjoining belt segment");
	}
}
