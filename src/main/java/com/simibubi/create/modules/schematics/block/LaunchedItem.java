package com.simibubi.create.modules.schematics.block;

import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Part;
import com.simibubi.create.modules.contraptions.relays.belt.item.BeltConnectorItem;
import com.simibubi.create.modules.contraptions.relays.elementary.ShaftBlock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class LaunchedItem {

	public int totalTicks;
	public int ticksRemaining;
	public BlockPos target;
	public ItemStack stack;

	private LaunchedItem(BlockPos start, BlockPos target, ItemStack stack) {
		this(target, stack, ticksForDistance(start, target), ticksForDistance(start, target));
	}

	private static int ticksForDistance(BlockPos start, BlockPos target) {
		return (int) (Math.max(10, MathHelper.sqrt(MathHelper.sqrt(target.distanceSq(start))) * 4f));
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
		if (world.isRemote)
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
		stack = ItemStack.read(c.getCompound("Stack"));
	}

	public static class ForBlockState extends LaunchedItem {
		public BlockState state;

		ForBlockState() {}

		public ForBlockState(BlockPos start, BlockPos target, ItemStack stack, BlockState state) {
			super(start, target, stack);
			this.state = state;
		}

		@Override
		public CompoundNBT serializeNBT() {
			CompoundNBT serializeNBT = super.serializeNBT();
			serializeNBT.put("BlockState", NBTUtil.writeBlockState(state));
			return serializeNBT;
		}

		@Override
		void readNBT(CompoundNBT nbt) {
			super.readNBT(nbt);
			state = NBTUtil.readBlockState(nbt.getCompound("BlockState"));
		}

		@Override
		void place(World world) {
			// Piston
			if (state.has(BlockStateProperties.EXTENDED))
				state = state.with(BlockStateProperties.EXTENDED, false);

			if (AllBlocks.BELT.typeOf(state)) {
				world.setBlockState(target, state, 2);
				return;
			}

			world.setBlockState(target, state, 18);
			state.getBlock().onBlockPlacedBy(world, target, state, null, stack);
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
			super(start, target, stack, state);
			this.length = length;
		}

		@Override
		void place(World world) {
			// todo place belt
			boolean isStart = state.get(BeltBlock.PART) == Part.START;
			BlockPos offset = BeltBlock.nextSegmentPosition(state, BlockPos.ZERO, isStart);
			int i = length - 1;
			Axis axis = state.get(BeltBlock.HORIZONTAL_FACING).rotateY().getAxis();
			world.setBlockState(target, AllBlocks.SHAFT.getDefault().with(ShaftBlock.AXIS, axis));
			BeltConnectorItem
					.createBelts(world, target, target.add(offset.getX() * i, offset.getY() * i, offset.getZ() * i));
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
					Optional<Entity> loadEntityUnchecked = EntityType.loadEntityUnchecked(deferredTag, world);
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
			world.addEntity(entity);
		}

	}

}