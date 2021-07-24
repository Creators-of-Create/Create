package com.simibubi.create.content.schematics.block;

import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltPart;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorItem;
import com.simibubi.create.content.contraptions.relays.elementary.AbstractShaftBlock;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public abstract class LaunchedItem {

	public int totalTicks;
	public int ticksRemaining;
	public BlockPos target;
	public ItemStack stack;

	private LaunchedItem(BlockPos start, BlockPos target, ItemStack stack) {
		this(target, stack, ticksForDistance(start, target), ticksForDistance(start, target));
	}

	private static int ticksForDistance(BlockPos start, BlockPos target) {
		return (int) (Math.max(10, MathHelper.sqrt(MathHelper.sqrt(target.distSqr(start))) * 4f));
	}

	LaunchedItem() {}

	private LaunchedItem(BlockPos target, ItemStack stack, int ticksLeft, int total) {
		this.target = target;
		this.stack = stack;
		this.totalTicks = total;
		this.ticksRemaining = ticksLeft;
	}

	public boolean update(World world) {
		if (ticksRemaining > 0) {
			ticksRemaining--;
			return false;
		}
		if (world.isClientSide)
			return false;

		place(world);
		return true;
	}

	public CompoundNBT serializeNBT() {
		CompoundNBT c = new CompoundNBT();
		c.putInt("TotalTicks", totalTicks);
		c.putInt("TicksLeft", ticksRemaining);
		c.put("Stack", stack.serializeNBT());
		c.put("Target", NBTUtil.writeBlockPos(target));
		return c;
	}

	public static LaunchedItem fromNBT(CompoundNBT c) {
		LaunchedItem launched = c.contains("Length") ? new LaunchedItem.ForBelt()
				: c.contains("BlockState") ? new LaunchedItem.ForBlockState() : new LaunchedItem.ForEntity();
		launched.readNBT(c);
		return launched;
	}

	abstract void place(World world);

	void readNBT(CompoundNBT c) {
		target = NBTUtil.readBlockPos(c.getCompound("Target"));
		ticksRemaining = c.getInt("TicksLeft");
		totalTicks = c.getInt("TotalTicks");
		stack = ItemStack.of(c.getCompound("Stack"));
	}

	public static class ForBlockState extends LaunchedItem {
		public BlockState state;
		public CompoundNBT data;

		ForBlockState() {}

		public ForBlockState(BlockPos start, BlockPos target, ItemStack stack, BlockState state, CompoundNBT data) {
			super(start, target, stack);
			this.state = state;
			this.data = data;
		}

		@Override
		public CompoundNBT serializeNBT() {
			CompoundNBT serializeNBT = super.serializeNBT();
			serializeNBT.put("BlockState", NBTUtil.writeBlockState(state));
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
		void readNBT(CompoundNBT nbt) {
			super.readNBT(nbt);
			state = NBTUtil.readBlockState(nbt.getCompound("BlockState"));
			if (nbt.contains("Data", Constants.NBT.TAG_COMPOUND)) {
				data = nbt.getCompound("Data");
			}
		}

		@Override
		void place(World world) {
			BlockHelper.placeSchematicBlock(world, state, target, stack, data);
		}

	}

	public static class ForBelt extends ForBlockState {
		public int length;

		public ForBelt() {}

		@Override
		public CompoundNBT serializeNBT() {
			CompoundNBT serializeNBT = super.serializeNBT();
			serializeNBT.putInt("Length", length);
			return serializeNBT;
		}

		@Override
		void readNBT(CompoundNBT nbt) {
			length = nbt.getInt("Length");
			super.readNBT(nbt);
		}

		public ForBelt(BlockPos start, BlockPos target, ItemStack stack, BlockState state, int length) {
			super(start, target, stack, state, null);
			this.length = length;
		}

		@Override
		void place(World world) {
			// todo place belt
			boolean isStart = state.getValue(BeltBlock.PART) == BeltPart.START;
			BlockPos offset = BeltBlock.nextSegmentPosition(state, BlockPos.ZERO, isStart);
			int i = length - 1;
			Axis axis = state.getValue(BeltBlock.HORIZONTAL_FACING).getClockWise().getAxis();
			world.setBlockAndUpdate(target, AllBlocks.SHAFT.getDefaultState().setValue(AbstractShaftBlock.AXIS, axis));
			BeltConnectorItem
					.createBelts(world, target, target.offset(offset.getX() * i, offset.getY() * i, offset.getZ() * i));
		}

	}

	public static class ForEntity extends LaunchedItem {
		public Entity entity;
		private CompoundNBT deferredTag;

		ForEntity() {}

		public ForEntity(BlockPos start, BlockPos target, ItemStack stack, Entity entity) {
			super(start, target, stack);
			this.entity = entity;
		}

		@Override
		public boolean update(World world) {
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
		public CompoundNBT serializeNBT() {
			CompoundNBT serializeNBT = super.serializeNBT();
			if (entity != null)
				serializeNBT.put("Entity", entity.serializeNBT());
			return serializeNBT;
		}

		@Override
		void readNBT(CompoundNBT nbt) {
			super.readNBT(nbt);
			if (nbt.contains("Entity"))
				deferredTag = nbt.getCompound("Entity");
		}

		@Override
		void place(World world) {
			world.addFreshEntity(entity);
		}

	}

}