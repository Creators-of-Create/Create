package com.simibubi.create.infrastructure.gametest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovingInteraction;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.gauge.SpeedGaugeBlockEntity;

import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;

import net.minecraftforge.registries.ForgeRegistries;

import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity.SelectionMode;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.mixin.accessor.GameTestHelperAccessor;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * A helper class expanding the functionality of {@link GameTestHelper}.
 * This class may replace the default helper parameter if a test is registered through {@link CreateTestFunction}.
 */
public class CreateGameTestHelper extends GameTestHelper {
	public static final int TICKS_PER_SECOND = 20;
	public static final int TEN_SECONDS = 10 * TICKS_PER_SECOND;
	public static final int FIFTEEN_SECONDS = 15 * TICKS_PER_SECOND;
	public static final int TWENTY_SECONDS = 20 * TICKS_PER_SECOND;

	private CreateGameTestHelper(GameTestInfo testInfo) {
		super(testInfo);
	}

	public static CreateGameTestHelper of(GameTestHelper original) {
		GameTestHelperAccessor access = (GameTestHelperAccessor) original;
		CreateGameTestHelper helper = new CreateGameTestHelper(access.getTestInfo());
		//noinspection DataFlowIssue // accessor applied at runtime
		GameTestHelperAccessor newAccess = (GameTestHelperAccessor) helper;
		newAccess.setFinalCheckAdded(access.getFinalCheckAdded());
		return helper;
	}

	// blocks

	/**
	 * Flip the direction of any block with the {@link BlockStateProperties#FACING} property.
	 */
	public void flipBlock(BlockPos pos) {
		BlockState original = getBlockState(pos);
		if (!original.hasProperty(BlockStateProperties.FACING))
			fail("FACING property not in block: " + ForgeRegistries.BLOCKS.getKey(original.getBlock()));
		Direction facing = original.getValue(BlockStateProperties.FACING);
		BlockState reversed = original.setValue(BlockStateProperties.FACING, facing.getOpposite());
		setBlock(pos, reversed);
	}

	public void assertNixiePower(BlockPos pos, int strength) {
		NixieTubeBlockEntity nixie = getBlockEntity(AllBlockEntityTypes.NIXIE_TUBE.get(), pos);
		int actualStrength = nixie.getRedstoneStrength();
		if (actualStrength != strength)
			fail("Expected nixie tube at %s to have power of %s, got %s".formatted(pos, strength, actualStrength));
	}

	/**
	 * Turn off a lever.
	 */
	public void powerLever(BlockPos pos) {
		assertBlockPresent(Blocks.LEVER, pos);
		if (!getBlockState(pos).getValue(LeverBlock.POWERED)) {
			pullLever(pos);
		}
	}

	/**
	 * Turn on a lever.
	 */
	public void unpowerLever(BlockPos pos) {
		assertBlockPresent(Blocks.LEVER, pos);
		if (getBlockState(pos).getValue(LeverBlock.POWERED)) {
			pullLever(pos);
		}
	}

	/**
	 * Set the {@link SelectionMode} of a belt tunnel at the given position.
	 * @param pos
	 * @param mode
	 */
	public void setTunnelMode(BlockPos pos, SelectionMode mode) {
		ScrollValueBehaviour behavior = getBehavior(pos, ScrollOptionBehaviour.TYPE);
		behavior.setValue(mode.ordinal());
	}

	public void assertSpeedometerSpeed(BlockPos speedometer, float value) {
		SpeedGaugeBlockEntity be = getBlockEntity(AllBlockEntityTypes.SPEEDOMETER.get(), speedometer);
		assertInRange(be.getSpeed(), value - 0.01, value + 0.01);
	}

	public void assertStressometerCapacity(BlockPos stressometer, float value) {
		StressGaugeBlockEntity be = getBlockEntity(AllBlockEntityTypes.STRESSOMETER.get(), stressometer);
		assertInRange(be.getNetworkCapacity(), value - 0.01, value + 0.01);
	}

	public void toggleActorsOfType(Contraption contraption, ItemLike item) {
		AtomicBoolean toggled = new AtomicBoolean(false);
		contraption.getInteractors().forEach((localPos, behavior) -> {
			if (toggled.get() || !(behavior instanceof ContraptionControlsMovingInteraction controls))
				return;
			MutablePair<StructureBlockInfo, MovementContext> actor = contraption.getActorAt(localPos);
			if (actor == null)
				return;
			ItemStack filter = ContraptionControlsMovement.getFilter(actor.right);
			if (filter != null && filter.is(item.asItem())) {
				controls.handlePlayerInteraction(
						makeMockPlayer(), InteractionHand.MAIN_HAND, localPos, contraption.entity
				);
				toggled.set(true);
			}
		});
	}

	// block entities

	/**
	 * Get the block entity of the expected type. If the type does not match, this fails the test.
	 */
	public <T extends BlockEntity> T getBlockEntity(BlockEntityType<T> type, BlockPos pos) {
		BlockEntity be = getBlockEntity(pos);
		BlockEntityType<?> actualType = be == null ? null : be.getType();
		if (actualType != type) {
			String actualId = actualType == null ? "null" : RegisteredObjects.getKeyOrThrow(actualType).toString();
			String error = "Expected block entity at pos [%s] with type [%s], got [%s]".formatted(
					pos, RegisteredObjects.getKeyOrThrow(type), actualId
			);
			fail(error);
		}
		return (T) be;
	}

	/**
	 * Given any segment of an {@link IMultiBlockEntityContainer}, get the controller for it.
	 */
	public <T extends BlockEntity & IMultiBlockEntityContainer> T getControllerBlockEntity(BlockEntityType<T> type, BlockPos anySegment) {
		T be = getBlockEntity(type, anySegment).getControllerBE();
		if (be == null)
			fail("Could not get block entity controller with type [%s] from pos [%s]".formatted(RegisteredObjects.getKeyOrThrow(type), anySegment));
		return be;
	}

	/**
	 * Get the expected {@link BlockEntityBehaviour} from the given position, failing if not present.
	 */
	public <T extends BlockEntityBehaviour> T getBehavior(BlockPos pos, BehaviourType<T> type) {
		T behavior = BlockEntityBehaviour.get(getLevel(), absolutePos(pos), type);
		if (behavior == null)
			fail("Behavior at " + pos + " missing, expected " + type.getName());
		return behavior;
	}

	// entities

	/**
	 * Spawn an item entity at the given position with no velocity.
	 */
	public ItemEntity spawnItem(BlockPos pos, ItemStack stack) {
		Vec3 spawn = Vec3.atCenterOf(absolutePos(pos));
		ServerLevel level = getLevel();
		ItemEntity item = new ItemEntity(level, spawn.x, spawn.y, spawn.z, stack, 0, 0, 0);
		level.addFreshEntity(item);
		return item;
	}

	/**
	 * Spawn item entities given an item and amount. The amount will be split into multiple entities if
	 * larger than the item's max stack size.
	 */
	public void spawnItems(BlockPos pos, Item item, int amount) {
		while (amount > 0) {
			int toSpawn = Math.min(amount, item.getMaxStackSize());
			amount -= toSpawn;
			ItemStack stack = new ItemStack(item, toSpawn);
			spawnItem(pos, stack);
		}
	}

	/**
	 * Get the first entity found at the given position.
	 */
	public <T extends Entity> T getFirstEntity(EntityType<T> type, BlockPos pos) {
		List<T> list = getEntitiesBetween(type, pos.north().east().above(), pos.south().west().below());
		if (list.isEmpty())
			fail("No entities at pos: " + pos);
		return list.get(0);
	}

	/**
	 * Get a list of all entities between two positions, inclusive.
	 */
	public <T extends Entity> List<T> getEntitiesBetween(EntityType<T> type, BlockPos pos1, BlockPos pos2) {
		BoundingBox box = BoundingBox.fromCorners(absolutePos(pos1), absolutePos(pos2));
		List<? extends T> entities = getLevel().getEntities(type, e -> box.isInside(e.blockPosition()));
		return (List<T>) entities;
	}


	// transfer - fluids

	public IFluidHandler fluidStorageAt(BlockPos pos) {
		BlockEntity be = getBlockEntity(pos);
		if (be == null)
			fail("BlockEntity not present");
		Optional<IFluidHandler> handler = be.getCapability(ForgeCapabilities.FLUID_HANDLER).resolve();
		if (handler.isEmpty())
			fail("handler not present");
		return handler.get();
	}

	/**
	 * Get the content of the tank at the pos.
	 * content is determined by what the tank allows to be extracted.
	 */
	public FluidStack getTankContents(BlockPos tank) {
		IFluidHandler handler = fluidStorageAt(tank);
		return handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
	}

	/**
	 * Get the total capacity of a tank at the given position.
	 */
	public long getTankCapacity(BlockPos pos) {
		IFluidHandler handler = fluidStorageAt(pos);
		long total = 0;
		for (int i = 0; i < handler.getTanks(); i++) {
			total += handler.getTankCapacity(i);
		}
		return total;
	}

	/**
	 * Get the total fluid amount across all fluid tanks at the given positions.
	 */
	public long getFluidInTanks(BlockPos... tanks) {
		long total = 0;
		for (BlockPos tank : tanks) {
			total += getTankContents(tank).getAmount();
		}
		return total;
	}

	/**
	 * Assert that the given fluid stack is present in the given tank. The tank might also hold more than the fluid.
	 */
	public void assertFluidPresent(FluidStack fluid, BlockPos pos) {
		FluidStack contained = getTankContents(pos);
		if (!fluid.isFluidEqual(contained))
			fail("Different fluids");
		if (fluid.getAmount() != contained.getAmount())
			fail("Different amounts");
	}

	/**
	 * Assert that the given tank holds no fluid.
	 */
	public void assertTankEmpty(BlockPos pos) {
		assertFluidPresent(FluidStack.EMPTY, pos);
	}

	public void assertTanksEmpty(BlockPos... tanks) {
		for (BlockPos tank : tanks) {
			assertTankEmpty(tank);
		}
	}

	// transfer - items

	public IItemHandler itemStorageAt(BlockPos pos) {
		BlockEntity be = getBlockEntity(pos);
		if (be == null)
			fail("BlockEntity not present");
		Optional<IItemHandler> handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
		if (handler.isEmpty())
			fail("handler not present");
		return handler.get();
	}

	/**
	 * Get a map of contained items to their amounts. This is not safe for NBT!
	 */
	public Object2LongMap<Item> getItemContent(BlockPos pos) {
		IItemHandler handler = itemStorageAt(pos);
		Object2LongMap<Item> map = new Object2LongArrayMap<>();
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			Item item = stack.getItem();
			long amount = map.getLong(item);
			amount += stack.getCount();
			map.put(item, amount);
		}
		return map;
	}

	/**
	 * Get the combined total of all ItemStacks inside the inventory.
	 */
	public long getTotalItems(BlockPos pos) {
		IItemHandler storage = itemStorageAt(pos);
		long total = 0;
		for (int i = 0; i < storage.getSlots(); i++) {
			total += storage.getStackInSlot(i).getCount();
		}
		return total;
	}

	/**
	 * Of the provided items, assert that at least one is present in the given inventory.
	 */
	public void assertAnyContained(BlockPos pos, Item... items) {
		IItemHandler handler = itemStorageAt(pos);
		boolean noneFound = true;
		for (int i = 0; i < handler.getSlots(); i++) {
			for (Item item : items) {
				if (handler.getStackInSlot(i).is(item)) {
					noneFound = false;
					break;
				}
			}
		}
		if (noneFound)
			fail("No matching items " + Arrays.toString(items) + " found in handler at pos: " + pos);
	}

	/**
	 * Assert that the inventory contains all the provided content.
	 */
	public void assertContentPresent(Object2LongMap<Item> content, BlockPos pos) {
		IItemHandler handler = itemStorageAt(pos);
		Object2LongMap<Item> map = new Object2LongArrayMap<>(content);
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			Item item = stack.getItem();
			long amount = map.getLong(item);
			amount -= stack.getCount();
			if (amount == 0)
				map.removeLong(item);
			else map.put(item, amount);
		}
		if (!map.isEmpty())
			fail("Storage missing content: " + map);
	}

	/**
	 * Assert that all the given inventories hold no items.
	 */
	public void assertContainersEmpty(List<BlockPos> positions) {
		for (BlockPos pos : positions) {
			assertContainerEmpty(pos);
		}
	}

	/**
	 * Assert that the given inventory holds no items.
	 */
	@Override
	public void assertContainerEmpty(@NotNull BlockPos pos) {
		IItemHandler storage = itemStorageAt(pos);
		for (int i = 0; i < storage.getSlots(); i++) {
			if (!storage.getStackInSlot(i).isEmpty())
				fail("Storage not empty");
		}
	}

	/** @see CreateGameTestHelper#assertContainerContains(BlockPos, ItemStack) */
	public void assertContainerContains(BlockPos pos, ItemLike item) {
		assertContainerContains(pos, item.asItem());
	}

	/** @see CreateGameTestHelper#assertContainerContains(BlockPos, ItemStack) */
	@Override
	public void assertContainerContains(@NotNull BlockPos pos, @NotNull Item item) {
		assertContainerContains(pos, new ItemStack(item));
	}

	/**
	 * Assert that the inventory holds at least the given ItemStack. It may also hold more than the stack.
	 */
	public void assertContainerContains(BlockPos pos, ItemStack item) {
		IItemHandler storage = itemStorageAt(pos);
		ItemStack extracted = ItemHelper.extract(storage, stack -> ItemHandlerHelper.canItemStacksStack(stack, item), item.getCount(), true);
		if (extracted.isEmpty())
			fail("item not present: " + item);
	}

	// time

	/**
	 * Fail unless the desired number seconds have passed since test start.
	 */
	public void assertSecondsPassed(int seconds) {
		if (getTick() < (long) seconds * TICKS_PER_SECOND)
			fail("Waiting for %s seconds to pass".formatted(seconds));
	}

	/**
	 * Get the total number of seconds that have passed since test start.
	 */
	public long secondsPassed() {
		return getTick() % 20;
	}

	/**
	 * Run an action later, once enough time has passed.
	 */
	public void whenSecondsPassed(int seconds, Runnable run) {
		runAfterDelay((long) seconds * TICKS_PER_SECOND, run);
	}

	// numbers

	/**
	 * Assert that a number is less than 1 away from its expected value
	 */
	public void assertCloseEnoughTo(double value, double expected) {
		assertInRange(value, expected - 1, expected + 1);
	}

	public void assertInRange(double value, double min, double max) {
		if (value < min)
			fail("Value %s below expected min of %s".formatted(value, min));
		if (value > max)
			fail("Value %s greater than expected max of %s".formatted(value, max));
	}

	// misc

	@Contract("_->fail") // make IDEA happier
	@Override
	public void fail(@NotNull String exceptionMessage) {
		super.fail(exceptionMessage);
	}
}
