package com.simibubi.create.content.schematics;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

public class SchematicPrinter {

	public enum PrintStage {
		BLOCKS, DEFERRED_BLOCKS, ENTITIES
	}

	private boolean schematicLoaded;
	private SchematicWorld blockReader;
	private BlockPos schematicAnchor;

	private BlockPos currentPos;
	private int printingEntityIndex;
	private PrintStage printStage;
	private List<BlockPos> deferredBlocks;

	public SchematicPrinter() {
		printingEntityIndex = -1;
		printStage = PrintStage.BLOCKS;
		deferredBlocks = new LinkedList<>();
	}

	public void fromTag(CompoundNBT compound, boolean clientPacket) {
		if (compound.contains("CurrentPos"))
			currentPos = NBTUtil.readBlockPos(compound.getCompound("CurrentPos"));

		printingEntityIndex = compound.getInt("EntityProgress");
		printStage = PrintStage.valueOf(compound.getString("PrintStage"));
		compound.getList("DeferredBlocks", 10).stream()
			.map(p -> NBTUtil.readBlockPos((CompoundNBT) p))
			.collect(Collectors.toCollection(() -> deferredBlocks));
	}

	public void write(CompoundNBT compound) {
		if (currentPos != null)
			compound.put("CurrentPos", NBTUtil.writeBlockPos(currentPos));

		compound.putInt("EntityProgress", printingEntityIndex);
		compound.putString("PrintStage", printStage.name());
		ListNBT tagDeferredBlocks = new ListNBT();
		for (BlockPos p : deferredBlocks)
			tagDeferredBlocks.add(NBTUtil.writeBlockPos(p));
		compound.put("DeferredBlocks", tagDeferredBlocks);
	}

	public void loadSchematic(ItemStack blueprint, World originalWorld, boolean processNBT) {
		if (!blueprint.hasTag() || !blueprint.getTag().getBoolean("Deployed"))
			return;

		Template activeTemplate = SchematicItem.loadSchematic(blueprint);
		PlacementSettings settings = SchematicItem.getSettings(blueprint, processNBT);

		schematicAnchor = NBTUtil.readBlockPos(blueprint.getTag().getCompound("Anchor"));
		blockReader = new SchematicWorld(schematicAnchor, originalWorld);
		activeTemplate.placeInWorldChunk(blockReader, schematicAnchor, settings, blockReader.getRandom());

		StructureTransform transform = new StructureTransform(settings.getRotationPivot(), Direction.Axis.Y,
			settings.getRotation(), settings.getMirror());
		for (TileEntity te : blockReader.tileEntities.values()) {
			transform.apply(te);
		}

		printingEntityIndex = -1;
		printStage = PrintStage.BLOCKS;
		deferredBlocks.clear();
		MutableBoundingBox bounds = blockReader.getBounds();
		currentPos = new BlockPos(bounds.x0 - 1, bounds.y0, bounds.z0);
		schematicLoaded = true;
	}

	public void resetSchematic() {
		schematicLoaded = false;
		schematicAnchor = null;
		currentPos = null;
		blockReader = null;
		printingEntityIndex = -1;
		printStage = PrintStage.BLOCKS;
		deferredBlocks.clear();
	}

	public boolean isLoaded() {
		return schematicLoaded;
	}

	public BlockPos getCurrentTarget() {
		if (!isLoaded())
			return null;
		return schematicAnchor.offset(currentPos);
	}

	public PrintStage getPrintStage() {
		return printStage;
	}

	public BlockPos getAnchor() {
		return schematicAnchor;
	}

	public boolean isWorldEmpty() {
		return blockReader.getAllPositions().isEmpty();
		//return blockReader.getBounds().getLength().equals(new Vector3i(0,0,0));
	}

	@FunctionalInterface
	public interface BlockTargetHandler {
		void handle(BlockPos target, BlockState blockState, TileEntity tileEntity);
	}
	@FunctionalInterface
	public interface EntityTargetHandler {
		void handle(BlockPos target, Entity entity);
	}

	public void handleCurrentTarget(BlockTargetHandler blockHandler, EntityTargetHandler entityHandler) {
		BlockPos target = getCurrentTarget();

		if (printStage == PrintStage.ENTITIES) {
			Entity entity = blockReader.getEntities()
				.collect(Collectors.toList())
				.get(printingEntityIndex);
			entityHandler.handle(target, entity);
		} else {
			BlockState blockState = BlockHelper.setZeroAge(blockReader.getBlockState(target));
			TileEntity tileEntity = blockReader.getBlockEntity(target);
			blockHandler.handle(target, blockState, tileEntity);
		}
	}

	@FunctionalInterface
	public interface PlacementPredicate {
		boolean shouldPlace(BlockPos target, BlockState blockState, TileEntity tileEntity,
							BlockState toReplace, BlockState toReplaceOther, boolean isNormalCube);
	}

	public boolean shouldPlaceCurrent(World world) { return shouldPlaceCurrent(world, (a,b,c,d,e,f) -> true); }

	public boolean shouldPlaceCurrent(World world, PlacementPredicate predicate) {
		if (world == null)
			return false;

		if (printStage == PrintStage.ENTITIES)
			return true;

		return shouldPlaceBlock(world, predicate, getCurrentTarget());
	}

	public boolean shouldPlaceBlock(World world, PlacementPredicate predicate, BlockPos pos) {
		BlockState state = BlockHelper.setZeroAge(blockReader.getBlockState(pos));
		TileEntity tileEntity = blockReader.getBlockEntity(pos);

		BlockState toReplace = world.getBlockState(pos);
		BlockState toReplaceOther = null;
		if (state.hasProperty(BlockStateProperties.BED_PART) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
				&& state.getValue(BlockStateProperties.BED_PART) == BedPart.FOOT)
			toReplaceOther = world.getBlockState(pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
		if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
				&& state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
			toReplaceOther = world.getBlockState(pos.above());

		if (!world.isLoaded(pos))
			return false;
		if (!world.getWorldBorder().isWithinBounds(pos))
			return false;
		if (toReplace == state)
			return false;
		if (toReplace.getDestroySpeed(world, pos) == -1
				|| (toReplaceOther != null && toReplaceOther.getDestroySpeed(world, pos) == -1))
			return false;

		boolean isNormalCube = state.isRedstoneConductor(blockReader, currentPos);
		return predicate.shouldPlace(pos, state, tileEntity, toReplace, toReplaceOther, isNormalCube);
	}

	public ItemRequirement getCurrentRequirement() {
		if (printStage == PrintStage.ENTITIES)
			return ItemRequirement.of(blockReader.getEntities()
				.collect(Collectors.toList())
				.get(printingEntityIndex));

		BlockPos target = getCurrentTarget();
		BlockState blockState = BlockHelper.setZeroAge(blockReader.getBlockState(target));
		TileEntity tileEntity = blockReader.getBlockEntity(target);
		return ItemRequirement.of(blockState, tileEntity);
	}

	public int markAllBlockRequirements(MaterialChecklist checklist, World world, PlacementPredicate predicate) {
		int blocksToPlace = 0;
		for (BlockPos pos : blockReader.getAllPositions()) {
			BlockPos relPos = pos.offset(schematicAnchor);
			BlockState required = blockReader.getBlockState(relPos);
			TileEntity requiredTE = blockReader.getBlockEntity(relPos);

			if (!world.isAreaLoaded(pos.offset(schematicAnchor), 0)) {
				checklist.warnBlockNotLoaded();
				continue;
			}
			if (!shouldPlaceBlock(world, predicate, relPos))
				continue;
			ItemRequirement requirement = ItemRequirement.of(required, requiredTE);
			if (requirement.isEmpty())
				continue;
			if (requirement.isInvalid())
				continue;
			checklist.require(requirement);
			blocksToPlace++;
		}
		return blocksToPlace;
	}

	public void markAllEntityRequirements(MaterialChecklist checklist) {
		blockReader.getEntities()
			.forEach(entity -> {
				ItemRequirement requirement = ItemRequirement.of(entity);
				if (requirement.isEmpty())
					return;
				if (requirement.isInvalid())
					return;
				checklist.require(requirement);
			});
	}

	public boolean advanceCurrentPos() {
		List<Entity> entities = blockReader.getEntities().collect(Collectors.toList());

		do {
			if (printStage == PrintStage.BLOCKS) {
				while (tryAdvanceCurrentPos()) {
					deferredBlocks.add(currentPos);
				}
			}

			if (printStage == PrintStage.DEFERRED_BLOCKS) {
				if (deferredBlocks.isEmpty()) {
					printStage = PrintStage.ENTITIES;
				} else {
					currentPos = deferredBlocks.remove(0);
				}
			}

			if (printStage == PrintStage.ENTITIES) {
				if (printingEntityIndex + 1 < entities.size()) {
					printingEntityIndex++;
					currentPos = entities.get(printingEntityIndex).blockPosition().subtract(schematicAnchor);
				} else {
					// Reached end of printing
					return false;
				}
			}
		} while (!blockReader.getBounds().isInside(currentPos));

		// More things available to print
		return true;
	}

	public boolean tryAdvanceCurrentPos() {
		currentPos = currentPos.relative(Direction.EAST);
		MutableBoundingBox bounds = blockReader.getBounds();
		BlockPos posInBounds = currentPos.offset(-bounds.x0, -bounds.y0, -bounds.z0);

		if (posInBounds.getX() > bounds.getXSpan())
			currentPos = new BlockPos(bounds.x0, currentPos.getY(), currentPos.getZ() + 1).west();
		if (posInBounds.getZ() > bounds.getZSpan())
			currentPos = new BlockPos(currentPos.getX(), currentPos.getY() + 1, bounds.z0).west();

		// End of blocks reached
		if (currentPos.getY() > bounds.getYSpan()) {
			printStage = PrintStage.DEFERRED_BLOCKS;
			return false;
		}

		return shouldDeferBlock(blockReader.getBlockState(getCurrentTarget()));
	}

	public static boolean shouldDeferBlock(BlockState state) {
		return AllBlocks.GANTRY_CARRIAGE.has(state) || AllBlocks.MECHANICAL_ARM.has(state)
			|| BlockMovementChecks.isBrittle(state);
	}

}
