package com.simibubi.create.modules.contraptions.receivers.constructs;

import static com.simibubi.create.AllBlocks.MECHANICAL_PISTON_HEAD;
import static com.simibubi.create.AllBlocks.PISTON_POLE;
import static com.simibubi.create.AllBlocks.STICKY_MECHANICAL_PISTON;
import static com.simibubi.create.CreateConfig.parameters;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateConfig;

import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.PistonType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class TranslationConstruct {

	protected Map<BlockPos, BlockInfo> blocks;
	protected List<BlockInfo> actors;

	protected AxisAlignedBB collisionBoxFront;
	protected AxisAlignedBB collisionBoxBack;

	protected Set<BlockPos> cachedColliders;
	protected Direction cachedColliderDirection;

	protected int extensionLength;
	protected int initialExtensionProgress;

	public TranslationConstruct() {
		blocks = new HashMap<>();
		actors = new ArrayList<>();
	}

	public Set<BlockPos> getColliders(World world, Direction movementDirection) {
		if (blocks == null)
			return null;
		if (cachedColliders == null || cachedColliderDirection != movementDirection) {
			cachedColliders = new HashSet<>();
			cachedColliderDirection = movementDirection;

			for (BlockInfo info : blocks.values()) {
				BlockPos offsetPos = info.pos.offset(movementDirection);
				boolean hasNext = false;
				for (BlockInfo otherInfo : blocks.values()) {
					if (!otherInfo.pos.equals(offsetPos))
						continue;
					hasNext = true;
					break;
				}
				if (!hasNext)
					cachedColliders.add(info.pos);
			}

		}
		return cachedColliders;
	}

	public static TranslationConstruct getAttachedForPushing(World world, BlockPos pos, Direction direction) {
		if (isFrozen())
			return null;
		
		TranslationConstruct construct = new TranslationConstruct();

		if (!construct.collectExtensions(world, pos, direction))
			return null;
		if (!construct.collectAttached(world, pos.offset(direction, construct.initialExtensionProgress), direction,
				direction, construct.initialExtensionProgress))
			return null;

		return construct;
	}

	public static TranslationConstruct getAttachedForPulling(World world, BlockPos pos, Direction direction) {
		if (isFrozen())
			return null;
		
		TranslationConstruct construct = new TranslationConstruct();

		if (!construct.collectExtensions(world, pos, direction))
			return null;
		if (STICKY_MECHANICAL_PISTON.typeOf(world.getBlockState(pos))) {
			if (!construct.collectAttached(world, pos.offset(direction, construct.initialExtensionProgress), direction,
					direction.getOpposite(), construct.initialExtensionProgress))
				return null;
		}

		return construct;
	}

	private boolean collectExtensions(World world, BlockPos pos, Direction direction) {
		List<BlockInfo> poles = new ArrayList<>();
		BlockPos actualStart = pos;
		BlockState nextBlock = world.getBlockState(actualStart.offset(direction));
		int extensionsInFront = 0;
		boolean sticky = STICKY_MECHANICAL_PISTON.typeOf(world.getBlockState(pos));

		while (PISTON_POLE.typeOf(nextBlock) && nextBlock.get(FACING).getAxis() == direction.getAxis()
				|| MECHANICAL_PISTON_HEAD.typeOf(nextBlock) && nextBlock.get(FACING) == direction) {

			actualStart = actualStart.offset(direction);
			poles.add(new BlockInfo(actualStart, nextBlock.with(FACING, direction), null));
			extensionsInFront++;
			nextBlock = world.getBlockState(actualStart.offset(direction));

			if (extensionsInFront > parameters.maxPistonPoles.get())
				return false;
		}

		if (extensionsInFront == 0)
			poles.add(
					new BlockInfo(pos,
							MECHANICAL_PISTON_HEAD.get().getDefaultState().with(FACING, direction).with(
									BlockStateProperties.PISTON_TYPE, sticky ? PistonType.STICKY : PistonType.DEFAULT),
							null));
		else
			poles.add(new BlockInfo(pos, PISTON_POLE.get().getDefaultState().with(FACING, direction), null));

		BlockPos end = pos;
		nextBlock = world.getBlockState(end.offset(direction.getOpposite()));
		int extensionsInBack = 0;

		while (PISTON_POLE.typeOf(nextBlock)) {
			end = end.offset(direction.getOpposite());
			poles.add(new BlockInfo(end, nextBlock.with(FACING, direction), null));
			extensionsInBack++;
			nextBlock = world.getBlockState(end.offset(direction.getOpposite()));

			if (extensionsInFront + extensionsInBack > parameters.maxPistonPoles.get())
				return false;
		}

		extensionLength = extensionsInBack + extensionsInFront;
		initialExtensionProgress = extensionsInFront;
		collisionBoxBack = new AxisAlignedBB(end.offset(direction, -extensionsInFront));

		for (BlockInfo pole : poles) {
			BlockPos polePos = pole.pos.offset(direction, -extensionsInFront);
			blocks.put(polePos, new BlockInfo(polePos, pole.state, null));
			collisionBoxBack = collisionBoxBack.union(new AxisAlignedBB(polePos));
		}

		return true;
	}

	protected boolean collectAttached(World world, BlockPos pos, Direction direction, Direction movementDirection,
			int offset) {

		// Find chassis
		List<BlockInfo> chassis = collectChassis(world, pos, direction, offset);
		if (chassis == null)
			return false;

		// Get single row of blocks
		if (chassis.isEmpty()) {
			if (movementDirection != direction) {
				BlockState state = world.getBlockState(pos.offset(direction));
				if (state.getMaterial().isReplaceable() || state.isAir(world, pos.offset(direction)))
					return true;
				if (state.getCollisionShape(world, pos.offset(direction)).isEmpty())
					return true;
				if (!canPull(world, pos.offset(direction), movementDirection))
					return false;

				BlockPos blockPos = pos.offset(direction).offset(direction, -offset);
				blocks.put(blockPos, new BlockInfo(blockPos, state, null));
				collisionBoxFront = new AxisAlignedBB(blockPos);

			} else {
				for (int distance = 1; distance <= parameters.maxChassisRange.get() + 1; distance++) {
					BlockPos currentPos = pos.offset(direction, distance);
					BlockState state = world.getBlockState(currentPos);

					// Ignore replaceable Blocks and Air-like
					if (state.getMaterial().isReplaceable() || state.isAir(world, currentPos))
						break;
					if (state.getCollisionShape(world, currentPos).isEmpty())
						break;

					// Row is immobile
					if (!canPush(world, currentPos, direction))
						return false;

					// Too many blocks
					if (distance == parameters.maxChassisRange.get() + 1)
						return false;

					BlockPos blockPos = currentPos.offset(direction, -offset);
					blocks.put(blockPos, new BlockInfo(blockPos, state, null));

					if (collisionBoxFront == null)
						collisionBoxFront = new AxisAlignedBB(blockPos);
					else
						collisionBoxFront = collisionBoxFront.union(new AxisAlignedBB(blockPos));

					// Don't collect in front of drills
					if (AllBlocks.DRILL.typeOf(state) && state.get(FACING) == direction)
						break;
				}
			}
		}

		// Get attached blocks by chassis
		else {
			collisionBoxFront = new AxisAlignedBB(pos.offset(direction, -offset + 1));
			List<BlockInfo> attachedBlocksByChassis = getAttachedBlocksByChassis(world, direction, chassis,
					movementDirection, offset);
			if (attachedBlocksByChassis == null)
				return false;
			attachedBlocksByChassis.forEach(info -> {
				blocks.put(info.pos, info);
				collisionBoxFront = collisionBoxFront.union(new AxisAlignedBB(info.pos));
			});
		}

		// Find blocks with special movement behaviour
		blocks.values().forEach(block -> {
			if (block.state.getBlock() instanceof IHaveMovementBehavior)
				actors.add(block);
		});

		return true;
	}

	private static List<BlockInfo> getAttachedBlocksByChassis(World world, Direction direction, List<BlockInfo> chassis,
			Direction movementDirection, int offset) {
		Axis axis = direction.getAxis();

		List<BlockPos> frontier = new LinkedList<>();
		Set<BlockPos> visited = new HashSet<>();
		chassis.forEach(c -> frontier.add(c.pos.offset(direction, offset)));

		BlockPos chassisPos = chassis.get(0).pos.offset(direction, offset);
		int chassisCoord = direction.getAxis().getCoordinate(chassisPos.getX(), chassisPos.getY(), chassisPos.getZ());
		Function<BlockPos, BlockPos> getChassisPos = pos -> new BlockPos(axis == Axis.X ? chassisCoord : pos.getX(),
				axis == Axis.Y ? chassisCoord : pos.getY(), axis == Axis.Z ? chassisCoord : pos.getZ());

		List<BlockInfo> blocks = new ArrayList<>();

		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			BlockState state = world.getBlockState(currentPos);

			if (visited.contains(currentPos))
				continue;
			visited.add(currentPos);

			BlockPos currentChassisPos = getChassisPos.apply(currentPos);
			BlockState chassisState = world.getBlockState(currentChassisPos);

			// Not attached to a chassis
			if (!(chassisState.getBlock() instanceof TranslationChassisBlock))
				continue;

			int chassisRange = ((ChassisTileEntity) world.getTileEntity(currentChassisPos)).getRange();
			boolean chassisSticky = chassisState
					.get(((AbstractChassisBlock) chassisState.getBlock()).getGlueableSide(chassisState, direction));

			// Ignore replaceable Blocks and Air-like
			if (state.getMaterial().isReplaceable() || state.isAir(world, currentPos))
				continue;
			if (state.getCollisionShape(world, currentPos).isEmpty())
				continue;

			// Too many Blocks
			if (direction == movementDirection && !currentChassisPos.withinDistance(currentPos, chassisRange + 1))
				return null;
			if (direction != movementDirection && !currentChassisPos.withinDistance(currentPos, chassisRange + 1))
				continue;

			// Skip if pushed column ended already (Except for Relocating Chassis)
			if (!chassisSticky && !currentPos.equals(currentChassisPos)) {
				boolean skip = false;

				if (movementDirection != direction && !currentChassisPos.withinDistance(currentPos, 1))
					continue;

				for (BlockPos p = currentPos; !p.equals(currentChassisPos); p = p.offset(direction.getOpposite())) {
					if (world.getBlockState(p).getMaterial().isReplaceable()
							|| world.getBlockState(p).isAir(world, currentPos)) {
						skip = true;
						break;
					}
				}
				if (skip)
					continue;
			}

			// Ignore sand and co.
			if (chassisSticky && movementDirection != direction && state.getBlock() instanceof FallingBlock)
				continue;

			// Structure is immobile
			if (!canPush(world, currentPos, movementDirection))
				return null;

			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt("Range", chassisRange);

			blocks.add(new BlockInfo(currentPos.offset(direction, -offset), state,
					AllBlocks.TRANSLATION_CHASSIS.typeOf(state) ? nbt : null));
			for (Direction facing : Direction.values()) {
				if (currentChassisPos.equals(currentPos) && facing == direction.getOpposite())
					continue;
				if (AllBlocks.DRILL.typeOf(state) && facing == direction)
					continue;

				frontier.add(currentPos.offset(facing));
			}
		}

		return blocks;
	}

	private static boolean canPush(World world, BlockPos pos, Direction direction) {
		return PistonBlock.canPush(world.getBlockState(pos), world, pos, direction, true, direction)
				|| AllBlocks.TRANSLATION_CHASSIS.typeOf(world.getBlockState(pos));
	}

	private static boolean canPull(World world, BlockPos pos, Direction direction) {
		return PistonBlock.canPush(world.getBlockState(pos), world, pos, direction, true, direction.getOpposite());
	}

	private static List<BlockInfo> collectChassis(World world, BlockPos pos, Direction direction, int offset2) {
		List<BlockPos> search = new LinkedList<>();
		Set<BlockPos> visited = new HashSet<>();
		List<BlockInfo> chassis = new LinkedList<>();
		search.add(pos.offset(direction));
		while (!search.isEmpty()) {
			if (chassis.size() > parameters.maxChassisForTranslation.get())
				return null;

			BlockPos current = search.remove(0);
			if (visited.contains(current))
				continue;

			BlockState blockState = world.getBlockState(current);
			if (!(blockState.getBlock() instanceof TranslationChassisBlock))
				continue;
			if (blockState.get(BlockStateProperties.AXIS) != direction.getAxis())
				continue;

			visited.add(current);
			chassis.add(new BlockInfo(current.offset(direction, -offset2), blockState, null));

			for (Direction offset : Direction.values()) {
				if (offset.getAxis() == direction.getAxis())
					continue;
				search.add(current.offset(offset));
			}
		}
		return chassis;
	}

	public AxisAlignedBB getCollisionBoxFront() {
		return collisionBoxFront;
	}

	public AxisAlignedBB getCollisionBoxBack() {
		return collisionBoxBack;
	}

	public CompoundNBT writeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		ListNBT blocks = new ListNBT();
		for (BlockInfo block : this.blocks.values()) {
			CompoundNBT c = new CompoundNBT();
			c.put("Block", NBTUtil.writeBlockState(block.state));
			c.put("Pos", NBTUtil.writeBlockPos(block.pos));
			if (block.nbt != null)
				c.put("Data", block.nbt);
			blocks.add(c);
		}

		if (collisionBoxFront != null) {
			ListNBT bb = writeAABB(collisionBoxFront);
			nbt.put("BoundsFront", bb);
		}

		if (collisionBoxBack != null) {
			ListNBT bb = writeAABB(collisionBoxBack);
			nbt.put("BoundsBack", bb);
		}

		nbt.put("Blocks", blocks);
		nbt.putInt("ExtensionLength", extensionLength);
		return nbt;
	}

	public ListNBT writeAABB(AxisAlignedBB bb) {
		ListNBT bbtag = new ListNBT();
		bbtag.add(new FloatNBT((float) bb.minX));
		bbtag.add(new FloatNBT((float) bb.minY));
		bbtag.add(new FloatNBT((float) bb.minZ));
		bbtag.add(new FloatNBT((float) bb.maxX));
		bbtag.add(new FloatNBT((float) bb.maxY));
		bbtag.add(new FloatNBT((float) bb.maxZ));
		return bbtag;
	}

	public AxisAlignedBB readAABB(ListNBT bbtag) {
		if (bbtag == null || bbtag.isEmpty())
			return null;
		return new AxisAlignedBB(bbtag.getFloat(0), bbtag.getFloat(1), bbtag.getFloat(2), bbtag.getFloat(3),
				bbtag.getFloat(4), bbtag.getFloat(5));

	}

	public static TranslationConstruct fromNBT(CompoundNBT nbt) {
		TranslationConstruct construct = new TranslationConstruct();
		nbt.getList("Blocks", 10).forEach(c -> {
			CompoundNBT comp = (CompoundNBT) c;
			BlockInfo info = new BlockInfo(NBTUtil.readBlockPos(comp.getCompound("Pos")),
					NBTUtil.readBlockState(comp.getCompound("Block")),
					comp.contains("Data") ? comp.getCompound("Data") : null);
			construct.blocks.put(info.pos, info);
		});
		construct.extensionLength = nbt.getInt("ExtensionLength");

		if (nbt.contains("BoundsFront"))
			construct.collisionBoxFront = construct.readAABB(nbt.getList("BoundsFront", 5));
		if (nbt.contains("BoundsBack"))
			construct.collisionBoxBack = construct.readAABB(nbt.getList("BoundsBack", 5));

		// Find blocks with special movement behaviour
		construct.blocks.values().forEach(block -> {
			if (block.state.getBlock() instanceof IHaveMovementBehavior)
				construct.actors.add(block);
		});

		return construct;
	}
	
	public static boolean isFrozen() {
		return CreateConfig.parameters.freezePistonConstructs.get();
	}
}
