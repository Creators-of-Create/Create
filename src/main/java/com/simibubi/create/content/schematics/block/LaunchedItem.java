package com.simibubi.create.content.schematics.block;

import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltPart;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorItem;
import com.simibubi.create.content.contraptions.relays.elementary.AbstractShaftBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
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
			// Piston
			if (state.has(BlockStateProperties.EXTENDED))
				state = state.with(BlockStateProperties.EXTENDED, Boolean.FALSE);
			if (state.has(BlockStateProperties.WATERLOGGED))
				state = state.with(BlockStateProperties.WATERLOGGED, Boolean.FALSE);

			if (AllBlocks.BELT.has(state)) {
				world.setBlockState(target, state, 2);
				return;
			}
			else if (state.getBlock() == Blocks.COMPOSTER)
				state = Blocks.COMPOSTER.getDefaultState();
			else if (state.getBlock() != Blocks.SEA_PICKLE && state.getBlock() instanceof IPlantable)
				state = ((IPlantable) state.getBlock()).getPlant(world, target);

			if (world.dimension.doesWaterVaporize() && state.getFluidState().getFluid().isIn(FluidTags.WATER)) {
				int i = target.getX();
				int j = target.getY();
				int k = target.getZ();
				world.playSound(null, target, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

				for (int l = 0; l < 8; ++l) {
					world.addParticle(ParticleTypes.LARGE_SMOKE, i + Math.random(), j + Math.random(), k + Math.random(), 0.0D, 0.0D, 0.0D);
				}
				Block.spawnDrops(state, world, target);
				return;
			}
			world.setBlockState(target, state, 18);
			if (data != null) {
				TileEntity tile = world.getTileEntity(target);
				if (tile != null) {
					data.putInt("x", target.getX());
					data.putInt("y", target.getY());
					data.putInt("z", target.getZ());
					tile.read(data);
				}
			}
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
			super(start, target, stack, state, null);
			this.length = length;
		}

		@Override
		void place(World world) {
			// todo place belt
			boolean isStart = state.get(BeltBlock.PART) == BeltPart.START;
			BlockPos offset = BeltBlock.nextSegmentPosition(state, BlockPos.ZERO, isStart);
			int i = length - 1;
			Axis axis = state.get(BeltBlock.HORIZONTAL_FACING).rotateY().getAxis();
			world.setBlockState(target, AllBlocks.SHAFT.getDefaultState().with(AbstractShaftBlock.AXIS, axis));
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