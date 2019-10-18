package com.simibubi.create.modules.contraptions.receivers.constructs;

import static com.simibubi.create.AllBlocks.MECHANICAL_PISTON_HEAD;
import static com.simibubi.create.AllBlocks.PISTON_POLE;
import static com.simibubi.create.AllBlocks.STICKY_MECHANICAL_PISTON;
import static com.simibubi.create.CreateConfig.parameters;
import static net.minecraft.state.properties.BlockStateProperties.AXIS;
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
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalPistonBlock.PistonState;

import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SlimeBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.PistonType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class TranslationConstruct {

	protected Map<BlockPos, BlockInfo> blocks;
	protected List<BlockInfo> actors;

	protected AxisAlignedBB constructCollisionBox;
	protected AxisAlignedBB pistonCollisionBox;

	protected Set<BlockPos> cachedColliders;
	protected Direction cachedColliderDirection;

	protected int extensionLength;
	protected int initialExtensionProgress;
	protected Direction orientation;

	public TranslationConstruct() {
		blocks = new HashMap<>();
		actors = new ArrayList<>();
	}
	
	public static TranslationConstruct movePistonAt(World world, BlockPos pos, Direction direction, boolean retract) {
		if (isFrozen())
			return null;
		TranslationConstruct construct = new TranslationConstruct();
		construct.orientation = direction;
		if (!construct.collectExtensions(world, pos, direction))
			return null;
		if (!construct.searchMovedStructure(world, pos.offset(direction, construct.initialExtensionProgress + 1),
				retract ? direction.getOpposite() : direction))
			return null;
		return construct;
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

	private boolean collectExtensions(World world, BlockPos pos, Direction direction) {
		List<BlockInfo> poles = new ArrayList<>();
		BlockPos actualStart = pos;
		BlockState nextBlock = world.getBlockState(actualStart.offset(direction));
		int extensionsInFront = 0;
		boolean sticky = STICKY_MECHANICAL_PISTON.typeOf(world.getBlockState(pos));

		if (world.getBlockState(pos).get(MechanicalPistonBlock.STATE) == PistonState.EXTENDED) {
			while (PISTON_POLE.typeOf(nextBlock) && nextBlock.get(FACING).getAxis() == direction.getAxis()
					|| MECHANICAL_PISTON_HEAD.typeOf(nextBlock) && nextBlock.get(FACING) == direction) {

				actualStart = actualStart.offset(direction);
				poles.add(new BlockInfo(actualStart, nextBlock.with(FACING, direction), null));
				extensionsInFront++;
				nextBlock = world.getBlockState(actualStart.offset(direction));

				if (extensionsInFront > parameters.maxPistonPoles.get())
					return false;
			}
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
		pistonCollisionBox = new AxisAlignedBB(end.offset(direction, -extensionsInFront));

		for (BlockInfo pole : poles) {
			BlockPos polePos = pole.pos.offset(direction, -extensionsInFront);
			blocks.put(polePos, new BlockInfo(polePos, pole.state, null));
			pistonCollisionBox = pistonCollisionBox.union(new AxisAlignedBB(polePos));
		}

		return true;
	}

	private boolean searchMovedStructure(World world, BlockPos pos, Direction direction) {
		List<BlockPos> frontier = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		constructCollisionBox = new AxisAlignedBB(pos);
		frontier.add(pos);

		for (int offset = 1; offset <= parameters.maxChassisRange.get(); offset++) {
			BlockPos currentPos = pos.offset(direction, offset);
			if (!world.isAreaLoaded(currentPos, 1))
				return false;
			if (!world.isBlockPresent(currentPos))
				break;
			BlockState state = world.getBlockState(currentPos);
			if (state.getMaterial().isReplaceable())
				break;
			if (state.getCollisionShape(world, currentPos).isEmpty())
				break;
			if (AllBlocks.MECHANICAL_PISTON_HEAD.typeOf(state) && state.get(FACING) == direction.getOpposite())
				break;
			if (!canPush(world, currentPos, direction))
				return false;
			frontier.add(currentPos);
		}

		for (int limit = 1000; limit > 0; limit--) {
			if (frontier.isEmpty())
				return true;
			if (!moveBlock(world, frontier.remove(0), direction, frontier, visited))
				return false;

		}
		return false;
	}

	private boolean moveBlock(World world, BlockPos pos, Direction direction, List<BlockPos> frontier,
			Set<BlockPos> visited) {
		visited.add(pos);
		frontier.remove(pos);

		if (!world.isBlockPresent(pos))
			return false;
		BlockState state = world.getBlockState(pos);
		if (state.getMaterial().isReplaceable())
			return true;
		if (state.getCollisionShape(world, pos).isEmpty())
			return true;
		if (!canPush(world, pos, direction))
			return false;
		if (isChassis(state) && !moveChassis(world, pos, direction, frontier, visited))
			return false;
		if (state.getBlock() instanceof SlimeBlock)
			for (Direction offset : Direction.values())
				frontier.add(pos.offset(offset));

		add(pos, capture(world, pos));
		return true;
	}

	private boolean moveChassis(World world, BlockPos pos, Direction movementDirection, List<BlockPos> frontier,
			Set<BlockPos> visited) {
		List<BlockInfo> cluster = getChassisClusterAt(world, pos);
		if (cluster == null)
			return false;
		if (cluster.isEmpty())
			return true;

		BlockInfo anchorChassis = cluster.get(0);
		Axis chassisAxis = anchorChassis.state.get(AXIS);
		int chassisCoord = chassisAxis.getCoordinate(anchorChassis.pos.getX(), anchorChassis.pos.getY(),
				anchorChassis.pos.getZ());

		Function<BlockPos, BlockPos> getChassisPos = position -> new BlockPos(
				chassisAxis == Axis.X ? chassisCoord : position.getX(),
				chassisAxis == Axis.Y ? chassisCoord : position.getY(),
				chassisAxis == Axis.Z ? chassisCoord : position.getZ());

		// Collect blocks on both sides
		for (AxisDirection axisDirection : AxisDirection.values()) {

			Direction chassisDirection = Direction.getFacingFromAxis(axisDirection, chassisAxis);
			List<BlockPos> chassisFrontier = new LinkedList<>();
			Set<BlockPos> chassisVisited = new HashSet<>();
			cluster.forEach(c -> chassisFrontier.add(c.pos));
			boolean pushing = chassisDirection == movementDirection;

			Search: while (!chassisFrontier.isEmpty()) {
				BlockPos currentPos = chassisFrontier.remove(0);
				if (!world.isAreaLoaded(currentPos, 1))
					return false;
				if (!world.isBlockPresent(currentPos))
					continue;
				if (chassisVisited.contains(currentPos))
					continue;
				chassisVisited.add(currentPos);

				BlockState state = world.getBlockState(currentPos);
				BlockPos currentChassisPos = getChassisPos.apply(currentPos);
				BlockState chassisState = world.getBlockState(currentChassisPos);

				// Not attached to a chassis
				if (!isChassis(chassisState) || chassisState.get(AXIS) != chassisAxis)
					continue;
				if (AllBlocks.MECHANICAL_PISTON_HEAD.typeOf(state)
						&& state.get(FACING) == chassisDirection.getOpposite())
					continue;

				int chassisRange = ((ChassisTileEntity) world.getTileEntity(currentChassisPos)).getRange();
				boolean chassisSticky = chassisState.get(((AbstractChassisBlock) chassisState.getBlock())
						.getGlueableSide(chassisState, chassisDirection));

				// Ignore replaceable Blocks and Air-like
				if (state.getMaterial().isReplaceable() || state.isAir(world, currentPos))
					continue;
				if (state.getCollisionShape(world, currentPos).isEmpty())
					continue;

				// Too many Blocks
				boolean notInRange = !currentChassisPos.withinDistance(currentPos, chassisRange + 1);
				if (pushing && notInRange)
					return false;
				if (!pushing && notInRange)
					continue;

				boolean isBaseChassis = currentPos.equals(currentChassisPos);
				if (!isBaseChassis) {
					// Don't pull if chassis not sticky
					if (!chassisSticky && !pushing)
						continue;

					// Skip if pushed column ended already
					for (BlockPos posInbetween = currentPos; !posInbetween.equals(
							currentChassisPos); posInbetween = posInbetween.offset(chassisDirection.getOpposite())) {
						BlockState blockState = world.getBlockState(posInbetween);

						if (!chassisSticky && (blockState.getMaterial().isReplaceable()))
							continue Search;
						if (!pushing && chassisSticky && !canPush(world, posInbetween, movementDirection))
							continue Search;
					}
				}

				// Ignore sand and co.
				if (chassisSticky && !pushing && state.getBlock() instanceof FallingBlock)
					continue;

				// Structure is immobile
				boolean cannotPush = !canPush(world, currentPos, movementDirection);
				if (pushing && cannotPush)
					return false;
				if (!pushing && cannotPush)
					continue;

				if (isBaseChassis) {
					add(currentPos, capture(world, currentPos));
					visited.add(currentPos);
				} else {
					frontier.add(currentPos);
				}

				// Expand search
				for (Direction facing : Direction.values()) {
					if (isBaseChassis && facing == chassisDirection.getOpposite())
						continue;
					if (notSupportive(world, pos, facing))
						continue;
					chassisFrontier.add(currentPos.offset(facing));
				}
			}
		}

		return true;
	}

	private static List<BlockInfo> getChassisClusterAt(World world, BlockPos pos) {
		List<BlockPos> search = new LinkedList<>();
		Set<BlockPos> visited = new HashSet<>();
		List<BlockInfo> chassis = new LinkedList<>();
		BlockState anchorChassis = world.getBlockState(pos);
		Axis axis = anchorChassis.get(AXIS);
		search.add(pos);

		while (!search.isEmpty()) {
			if (chassis.size() > parameters.maxChassisForTranslation.get())
				return null;

			BlockPos current = search.remove(0);
			if (visited.contains(current))
				continue;
			if (!world.isAreaLoaded(current, 1))
				return null;

			BlockState state = world.getBlockState(current);
			if (!isChassis(state))
				continue;
			if (!TranslationChassisBlock.sameKind(anchorChassis, state))
				continue;
			if (state.get(AXIS) != axis)
				continue;

			visited.add(current);
			chassis.add(capture(world, current));

			for (Direction offset : Direction.values()) {
				if (offset.getAxis() == axis)
					continue;
				search.add(current.offset(offset));
			}
		}
		return chassis;
	}

	private boolean notSupportive(World world, BlockPos pos, Direction facing) {
		BlockState state = world.getBlockState(pos);
		if (AllBlocks.DRILL.typeOf(state))
			return state.get(BlockStateProperties.FACING) == facing;
		if (AllBlocks.HARVESTER.typeOf(state))
			return state.get(BlockStateProperties.HORIZONTAL_FACING) == facing;
		return false;
	}
	
	private static boolean isChassis(BlockState state) {
		return TranslationChassisBlock.isChassis(state);
	}

	private static boolean canPush(World world, BlockPos pos, Direction direction) {
		BlockState blockState = world.getBlockState(pos);
		if (isChassis(blockState))
			return true;
		if (blockState.getBlock() instanceof ShulkerBoxBlock)
			return false;
		return PistonBlock.canPush(blockState, world, pos, direction, true, direction);
	}
	
	private void add(BlockPos pos, BlockInfo block) {
		BlockPos localPos = pos.offset(orientation, -initialExtensionProgress);
		BlockInfo blockInfo = new BlockInfo(localPos, block.state, block.nbt);
		blocks.put(localPos, blockInfo);
		if (block.state.getBlock() instanceof IHaveMovementBehavior)
			actors.add(blockInfo);
		constructCollisionBox.union(new AxisAlignedBB(localPos));
	}

	private static BlockInfo capture(World world, BlockPos pos) {
		BlockState blockstate = world.getBlockState(pos);
		TileEntity tileentity = world.getTileEntity(pos);
		CompoundNBT compoundnbt = null;
		if (tileentity != null) {
			compoundnbt = tileentity.write(new CompoundNBT());
			compoundnbt.remove("x");
			compoundnbt.remove("y");
			compoundnbt.remove("z");
		}
		return new BlockInfo(pos, blockstate, compoundnbt);
	}

	public AxisAlignedBB getCollisionBoxFront() {
		return constructCollisionBox;
	}

	public AxisAlignedBB getCollisionBoxBack() {
		return pistonCollisionBox;
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

		if (constructCollisionBox != null) {
			ListNBT bb = writeAABB(constructCollisionBox);
			nbt.put("BoundsFront", bb);
		}

		if (pistonCollisionBox != null) {
			ListNBT bb = writeAABB(pistonCollisionBox);
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
			construct.constructCollisionBox = construct.readAABB(nbt.getList("BoundsFront", 5));
		if (nbt.contains("BoundsBack"))
			construct.pistonCollisionBox = construct.readAABB(nbt.getList("BoundsBack", 5));

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
