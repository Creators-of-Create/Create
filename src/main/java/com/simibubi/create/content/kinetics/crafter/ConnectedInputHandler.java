package com.simibubi.create.content.kinetics.crafter;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class ConnectedInputHandler {

	public static boolean shouldConnect(Level world, BlockPos pos, Direction face, Direction direction) {
		BlockState refState = world.getBlockState(pos);
		if (!refState.hasProperty(HORIZONTAL_FACING))
			return false;
		Direction refDirection = refState.getValue(HORIZONTAL_FACING);
		if (direction.getAxis() == refDirection.getAxis())
			return false;
		if (face == refDirection)
			return false;
		BlockState neighbour = world.getBlockState(pos.relative(direction));
		if (!AllBlocks.MECHANICAL_CRAFTER.has(neighbour))
			return false;
		if (refDirection != neighbour.getValue(HORIZONTAL_FACING))
			return false;
		return true;
	}

	public static void toggleConnection(Level world, BlockPos pos, BlockPos pos2) {
		MechanicalCrafterBlockEntity crafter1 = CrafterHelper.getCrafter(world, pos);
		MechanicalCrafterBlockEntity crafter2 = CrafterHelper.getCrafter(world, pos2);

		if (crafter1 == null || crafter2 == null)
			return;

		BlockPos controllerPos1 = crafter1.getBlockPos()
			.offset(crafter1.input.data.get(0));
		BlockPos controllerPos2 = crafter2.getBlockPos()
			.offset(crafter2.input.data.get(0));

		if (controllerPos1.equals(controllerPos2)) {
			MechanicalCrafterBlockEntity controller = CrafterHelper.getCrafter(world, controllerPos1);

			Set<BlockPos> positions = controller.input.data.stream()
				.map(controllerPos1::offset)
				.collect(Collectors.toSet());
			List<BlockPos> frontier = new LinkedList<>();
			List<BlockPos> splitGroup = new ArrayList<>();

			frontier.add(pos2);
			positions.remove(pos2);
			positions.remove(pos);
			while (!frontier.isEmpty()) {
				BlockPos current = frontier.remove(0);
				for (Direction direction : Iterate.directions) {
					BlockPos next = current.relative(direction);
					if (!positions.remove(next))
						continue;
					splitGroup.add(next);
					frontier.add(next);
				}
			}

			initAndAddAll(world, crafter1, positions);
			initAndAddAll(world, crafter2, splitGroup);

			crafter1.setChanged();
			crafter1.connectivityChanged();
			crafter2.setChanged();
			crafter2.connectivityChanged();
			return;
		}

		if (!crafter1.input.isController)
			crafter1 = CrafterHelper.getCrafter(world, controllerPos1);
		if (!crafter2.input.isController)
			crafter2 = CrafterHelper.getCrafter(world, controllerPos2);
		if (crafter1 == null || crafter2 == null)
			return;

		connectControllers(world, crafter1, crafter2);

		world.setBlock(crafter1.getBlockPos(), crafter1.getBlockState(), 3);

		crafter1.setChanged();
		crafter1.connectivityChanged();
		crafter2.setChanged();
		crafter2.connectivityChanged();
	}

	public static void initAndAddAll(Level world, MechanicalCrafterBlockEntity crafter, Collection<BlockPos> positions) {
		crafter.input = new ConnectedInput();
		positions.forEach(splitPos -> {
			modifyAndUpdate(world, splitPos, input -> {
				input.attachTo(crafter.getBlockPos(), splitPos);
				crafter.input.data.add(splitPos.subtract(crafter.getBlockPos()));
			});
		});
	}

	public static void connectControllers(Level world, MechanicalCrafterBlockEntity crafter1,
		MechanicalCrafterBlockEntity crafter2) {

		crafter1.input.data.forEach(offset -> {
			BlockPos connectedPos = crafter1.getBlockPos()
				.offset(offset);
			modifyAndUpdate(world, connectedPos, input -> {
			});
		});

		crafter2.input.data.forEach(offset -> {
			if (offset.equals(BlockPos.ZERO))
				return;
			BlockPos connectedPos = crafter2.getBlockPos()
				.offset(offset);
			modifyAndUpdate(world, connectedPos, input -> {
				input.attachTo(crafter1.getBlockPos(), connectedPos);
				crafter1.input.data.add(BlockPos.ZERO.subtract(input.data.get(0)));
			});
		});

		crafter2.input.attachTo(crafter1.getBlockPos(), crafter2.getBlockPos());
		crafter1.input.data.add(BlockPos.ZERO.subtract(crafter2.input.data.get(0)));
	}

	private static void modifyAndUpdate(Level world, BlockPos pos, Consumer<ConnectedInput> callback) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (!(blockEntity instanceof MechanicalCrafterBlockEntity))
			return;

		MechanicalCrafterBlockEntity crafter = (MechanicalCrafterBlockEntity) blockEntity;
		callback.accept(crafter.input);
		crafter.setChanged();
		crafter.connectivityChanged();
	}

	public static class ConnectedInput {
		boolean isController;
		List<BlockPos> data = Collections.synchronizedList(new ArrayList<>());

		public ConnectedInput() {
			isController = true;
			data.add(BlockPos.ZERO);
		}

		public void attachTo(BlockPos controllerPos, BlockPos myPos) {
			isController = false;
			data.clear();
			data.add(controllerPos.subtract(myPos));
		}

		public IItemHandler getItemHandler(Level world, BlockPos pos) {
			if (!isController) {
				BlockPos controllerPos = pos.offset(data.get(0));
				ConnectedInput input = CrafterHelper.getInput(world, controllerPos);
				if (input == this || input == null || !input.isController)
					return new ItemStackHandler();
				return input.getItemHandler(world, controllerPos);
			}

			Direction facing = Direction.SOUTH;
			BlockState blockState = world.getBlockState(pos);
			if (blockState.hasProperty(MechanicalCrafterBlock.HORIZONTAL_FACING))
				facing = blockState.getValue(MechanicalCrafterBlock.HORIZONTAL_FACING);
			AxisDirection axisDirection = facing.getAxisDirection();
			Axis compareAxis = facing.getClockWise()
				.getAxis();

			Comparator<BlockPos> invOrdering = (p1, p2) -> {
				int compareY = -Integer.compare(p1.getY(), p2.getY());
				int modifier = axisDirection.getStep() * (compareAxis == Axis.Z ? -1 : 1);
				int c1 = compareAxis.choose(p1.getX(), p1.getY(), p1.getZ());
				int c2 = compareAxis.choose(p2.getX(), p2.getY(), p2.getZ());
				return compareY != 0 ? compareY : modifier * Integer.compare(c1, c2);
			};

			List<IItemHandlerModifiable> list = data.stream()
				.sorted(invOrdering)
				.map(l -> CrafterHelper.getCrafter(world, pos.offset(l)))
				.filter(Objects::nonNull)
				.map(crafter -> crafter.getInventory())
				.collect(Collectors.toList());
			return new CombinedInvWrapper(Arrays.copyOf(list.toArray(), list.size(), IItemHandlerModifiable[].class));
		}

		public void write(CompoundTag nbt) {
			nbt.putBoolean("Controller", isController);
			ListTag list = new ListTag();
			data.forEach(pos -> list.add(NbtUtils.writeBlockPos(pos)));
			nbt.put("Data", list);
		}

		public void read(CompoundTag nbt) {
			isController = nbt.getBoolean("Controller");
			data.clear();
			nbt.getList("Data", Tag.TAG_COMPOUND)
				.forEach(inbt -> data.add(NbtUtils.readBlockPos((CompoundTag) inbt)));

			// nbt got wiped -> reset
			if (data.isEmpty()) {
				isController = true;
				data.add(BlockPos.ZERO);
			}
		}

	}

}
