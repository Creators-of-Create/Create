package com.simibubi.create.content.schematics.block;

import java.util.Arrays;
import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlockEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlockEntity.CasingType;
import com.simibubi.create.content.contraptions.relays.belt.BeltPart;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorItem;
import com.simibubi.create.content.contraptions.relays.elementary.AbstractSimpleShaftBlock;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class LaunchedItem {

	public int totalTicks;
	public int ticksRemaining;
	public BlockPos target;
	public ItemStack stack;

	private LaunchedItem(BlockPos start, BlockPos target, ItemStack stack) {
		this(target, stack, ticksForDistance(start, target), ticksForDistance(start, target));
	}

	private static int ticksForDistance(BlockPos start, BlockPos target) {
		return (int) (Math.max(10, Math.sqrt(Math.sqrt(target.distSqr(start))) * 4f));
	}

	LaunchedItem() {}

	private LaunchedItem(BlockPos target, ItemStack stack, int ticksLeft, int total) {
		this.target = target;
		this.stack = stack;
		this.totalTicks = total;
		this.ticksRemaining = ticksLeft;
	}

	public boolean update(Level world) {
		if (ticksRemaining > 0) {
			ticksRemaining--;
			return false;
		}
		if (world.isClientSide)
			return false;

		place(world);
		return true;
	}

	public CompoundTag serializeNBT() {
		CompoundTag c = new CompoundTag();
		c.putInt("TotalTicks", totalTicks);
		c.putInt("TicksLeft", ticksRemaining);
		c.put("Stack", stack.serializeNBT());
		c.put("Target", NbtUtils.writeBlockPos(target));
		return c;
	}

	public static LaunchedItem fromNBT(CompoundTag c) {
		LaunchedItem launched = c.contains("Length") ? new LaunchedItem.ForBelt()
			: c.contains("BlockState") ? new LaunchedItem.ForBlockState() : new LaunchedItem.ForEntity();
		launched.readNBT(c);
		return launched;
	}

	abstract void place(Level world);

	void readNBT(CompoundTag c) {
		target = NbtUtils.readBlockPos(c.getCompound("Target"));
		ticksRemaining = c.getInt("TicksLeft");
		totalTicks = c.getInt("TotalTicks");
		stack = ItemStack.of(c.getCompound("Stack"));
	}

	public static class ForBlockState extends LaunchedItem {
		public BlockState state;
		public CompoundTag data;

		ForBlockState() {}

		public ForBlockState(BlockPos start, BlockPos target, ItemStack stack, BlockState state, CompoundTag data) {
			super(start, target, stack);
			this.state = state;
			this.data = data;
		}

		@Override
		public CompoundTag serializeNBT() {
			CompoundTag serializeNBT = super.serializeNBT();
			serializeNBT.put("BlockState", NbtUtils.writeBlockState(state));
			if (data != null) {
				data.remove("x");
				data.remove("y");
				data.remove("z");
				data.remove("id");
				serializeNBT.put("Data", data);
			}
			return serializeNBT;
		}

		@Override
		void readNBT(CompoundTag nbt) {
			super.readNBT(nbt);
			state = NbtUtils.readBlockState(nbt.getCompound("BlockState"));
			if (nbt.contains("Data", Tag.TAG_COMPOUND)) {
				data = nbt.getCompound("Data");
			}
		}

		@Override
		void place(Level world) {
			BlockHelper.placeSchematicBlock(world, state, target, stack, data);
		}

	}

	public static class ForBelt extends ForBlockState {
		public int length;
		public CasingType[] casings;

		public ForBelt() {}

		@Override
		public CompoundTag serializeNBT() {
			CompoundTag serializeNBT = super.serializeNBT();
			serializeNBT.putInt("Length", length);
			serializeNBT.putIntArray("Casing", Arrays.stream(casings)
				.map(CasingType::ordinal)
				.toList());
			return serializeNBT;
		}

		@Override
		void readNBT(CompoundTag nbt) {
			length = nbt.getInt("Length");
			int[] intArray = nbt.getIntArray("Casing");
			casings = new CasingType[length];
			for (int i = 0; i < casings.length; i++)
				casings[i] = i >= intArray.length ? CasingType.NONE
					: CasingType.values()[Mth.clamp(intArray[i], 0, CasingType.values().length - 1)];
			super.readNBT(nbt);
		}

		public ForBelt(BlockPos start, BlockPos target, ItemStack stack, BlockState state, CasingType[] casings) {
			super(start, target, stack, state, null);
			this.casings = casings;
			this.length = casings.length;
		}

		@Override
		void place(Level world) {
			boolean isStart = state.getValue(BeltBlock.PART) == BeltPart.START;
			BlockPos offset = BeltBlock.nextSegmentPosition(state, BlockPos.ZERO, isStart);
			int i = length - 1;
			Axis axis = state.getValue(BeltBlock.SLOPE) == BeltSlope.SIDEWAYS ? Axis.Y
				: state.getValue(BeltBlock.HORIZONTAL_FACING)
					.getClockWise()
					.getAxis();
			world.setBlockAndUpdate(target, AllBlocks.SHAFT.getDefaultState()
				.setValue(AbstractSimpleShaftBlock.AXIS, axis));
			BeltConnectorItem.createBelts(world, target,
				target.offset(offset.getX() * i, offset.getY() * i, offset.getZ() * i));

			for (int segment = 0; segment < length; segment++) {
				if (casings[segment] == CasingType.NONE)
					continue;
				BlockPos casingTarget =
					target.offset(offset.getX() * segment, offset.getY() * segment, offset.getZ() * segment);
				if (world.getBlockEntity(casingTarget) instanceof BeltBlockEntity bbe)
					bbe.setCasingType(casings[segment]);
			}
		}

	}

	public static class ForEntity extends LaunchedItem {
		public Entity entity;
		private CompoundTag deferredTag;

		ForEntity() {}

		public ForEntity(BlockPos start, BlockPos target, ItemStack stack, Entity entity) {
			super(start, target, stack);
			this.entity = entity;
		}

		@Override
		public boolean update(Level world) {
			if (deferredTag != null && entity == null) {
				try {
					Optional<Entity> loadEntityUnchecked = EntityType.create(deferredTag, world);
					if (!loadEntityUnchecked.isPresent())
						return true;
					entity = loadEntityUnchecked.get();
				} catch (Exception var3) {
					return true;
				}
				deferredTag = null;
			}
			return super.update(world);
		}

		@Override
		public CompoundTag serializeNBT() {
			CompoundTag serializeNBT = super.serializeNBT();
			if (entity != null)
				serializeNBT.put("Entity", entity.serializeNBT());
			return serializeNBT;
		}

		@Override
		void readNBT(CompoundTag nbt) {
			super.readNBT(nbt);
			if (nbt.contains("Entity"))
				deferredTag = nbt.getCompound("Entity");
		}

		@Override
		void place(Level world) {
			if (entity != null)
				world.addFreshEntity(entity);
		}

	}

}