package com.simibubi.create.content.schematics;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.foundation.blockEntity.IMergeableBE;
import com.simibubi.create.foundation.utility.BBHelper;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SchematicPrinter {

	public enum PrintStage {
		BLOCKS, DEFERRED_BLOCKS, ENTITIES
	}

	private boolean schematicLoaded;
	private boolean isErrored;
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

	public void fromTag(CompoundTag compound, boolean clientPacket) {
		if (compound.contains("CurrentPos"))
			currentPos = NbtUtils.readBlockPos(compound.getCompound("CurrentPos"));
		if (clientPacket) {
			schematicLoaded = false;
			if (compound.contains("Anchor")) {
				schematicAnchor = NbtUtils.readBlockPos(compound.getCompound("Anchor"));
				schematicLoaded = true;
			}
		}
		
		printingEntityIndex = compound.getInt("EntityProgress");
		printStage = PrintStage.valueOf(compound.getString("PrintStage"));
		compound.getList("DeferredBlocks", 10).stream()
			.map(p -> NbtUtils.readBlockPos((CompoundTag) p))
			.collect(Collectors.toCollection(() -> deferredBlocks));
	}

	public void write(CompoundTag compound) {
		if (currentPos != null)
			compound.put("CurrentPos", NbtUtils.writeBlockPos(currentPos));
		if (schematicAnchor != null)
			compound.put("Anchor", NbtUtils.writeBlockPos(schematicAnchor));
		
		compound.putInt("EntityProgress", printingEntityIndex);
		compound.putString("PrintStage", printStage.name());
		ListTag tagDeferredBlocks = new ListTag();
		for (BlockPos p : deferredBlocks)
			tagDeferredBlocks.add(NbtUtils.writeBlockPos(p));
		compound.put("DeferredBlocks", tagDeferredBlocks);
	}

	public void loadSchematic(ItemStack blueprint, Level originalWorld, boolean processNBT) {
		if (!blueprint.hasTag() || !blueprint.getTag().getBoolean("Deployed"))
			return;

		StructureTemplate activeTemplate = SchematicItem.loadSchematic(blueprint);
		StructurePlaceSettings settings = SchematicItem.getSettings(blueprint, processNBT);

		schematicAnchor = NbtUtils.readBlockPos(blueprint.getTag()
			.getCompound("Anchor"));
		blockReader = new SchematicWorld(schematicAnchor, originalWorld);

		try {
			activeTemplate.placeInWorld(blockReader, schematicAnchor, schematicAnchor, settings,
				blockReader.getRandom(), Block.UPDATE_CLIENTS);
		} catch (Exception e) {
			Create.LOGGER.error("Failed to load Schematic for Printing", e);
			schematicLoaded = true;
			isErrored = true;
			return;
		}

		BlockPos extraBounds = StructureTemplate.calculateRelativePosition(settings, new BlockPos(activeTemplate.getSize())
			.offset(-1, -1, -1));
		blockReader.bounds = BBHelper.encapsulate(blockReader.bounds, extraBounds);

		StructureTransform transform = new StructureTransform(settings.getRotationPivot(), Direction.Axis.Y,
			settings.getRotation(), settings.getMirror());
		for (BlockEntity be : blockReader.getBlockEntities())
			transform.apply(be);

		printingEntityIndex = -1;
		printStage = PrintStage.BLOCKS;
		deferredBlocks.clear();
		BoundingBox bounds = blockReader.getBounds();
		currentPos = new BlockPos(bounds.minX() - 1, bounds.minY(), bounds.minZ());
		schematicLoaded = true;
	}

	public void resetSchematic() {
		schematicLoaded = false;
		schematicAnchor = null;
		isErrored = false;
		currentPos = null;
		blockReader = null;
		printingEntityIndex = -1;
		printStage = PrintStage.BLOCKS;
		deferredBlocks.clear();
	}

	public boolean isLoaded() {
		return schematicLoaded;
	}
	
	public boolean isErrored() {
		return isErrored;
	}

	public BlockPos getCurrentTarget() {
		if (!isLoaded() || isErrored())
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
		void handle(BlockPos target, BlockState blockState, BlockEntity blockEntity);
	}
	@FunctionalInterface
	public interface EntityTargetHandler {
		void handle(BlockPos target, Entity entity);
	}

	public void handleCurrentTarget(BlockTargetHandler blockHandler, EntityTargetHandler entityHandler) {
		BlockPos target = getCurrentTarget();

		if (printStage == PrintStage.ENTITIES) {
			Entity entity = blockReader.getEntityStream()
				.collect(Collectors.toList())
				.get(printingEntityIndex);
			entityHandler.handle(target, entity);
		} else {
			BlockState blockState = BlockHelper.setZeroAge(blockReader.getBlockState(target));
			BlockEntity blockEntity = blockReader.getBlockEntity(target);
			blockHandler.handle(target, blockState, blockEntity);
		}
	}

	@FunctionalInterface
	public interface PlacementPredicate {
		boolean shouldPlace(BlockPos target, BlockState blockState, BlockEntity blockEntity,
							BlockState toReplace, BlockState toReplaceOther, boolean isNormalCube);
	}

	public boolean shouldPlaceCurrent(Level world) { return shouldPlaceCurrent(world, (a,b,c,d,e,f) -> true); }

	public boolean shouldPlaceCurrent(Level world, PlacementPredicate predicate) {
		if (world == null)
			return false;

		if (printStage == PrintStage.ENTITIES)
			return true;

		return shouldPlaceBlock(world, predicate, getCurrentTarget());
	}

	public boolean shouldPlaceBlock(Level world, PlacementPredicate predicate, BlockPos pos) {
		BlockState state = BlockHelper.setZeroAge(blockReader.getBlockState(pos));
		BlockEntity blockEntity = blockReader.getBlockEntity(pos);

		BlockState toReplace = world.getBlockState(pos);
		BlockEntity toReplaceBE = world.getBlockEntity(pos);
		BlockState toReplaceOther = null;
		
		if (state.hasProperty(BlockStateProperties.BED_PART) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
				&& state.getValue(BlockStateProperties.BED_PART) == BedPart.FOOT)
			toReplaceOther = world.getBlockState(pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
		if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
				&& state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
			toReplaceOther = world.getBlockState(pos.above());

		boolean mergeTEs = blockEntity != null && toReplaceBE instanceof IMergeableBE mergeBE && toReplaceBE.getType()
			.equals(blockEntity.getType());

		if (!world.isLoaded(pos))
			return false;
		if (!world.getWorldBorder().isWithinBounds(pos))
			return false;
		if (toReplace == state && !mergeTEs)
			return false;
		if (toReplace.getDestroySpeed(world, pos) == -1
				|| (toReplaceOther != null && toReplaceOther.getDestroySpeed(world, pos) == -1))
			return false;

		boolean isNormalCube = state.isRedstoneConductor(blockReader, currentPos);
		return predicate.shouldPlace(pos, state, blockEntity, toReplace, toReplaceOther, isNormalCube);
	}

	public ItemRequirement getCurrentRequirement() {
		if (printStage == PrintStage.ENTITIES)
			return ItemRequirement.of(blockReader.getEntityStream()
				.collect(Collectors.toList())
				.get(printingEntityIndex));

		BlockPos target = getCurrentTarget();
		BlockState blockState = BlockHelper.setZeroAge(blockReader.getBlockState(target));
		BlockEntity blockEntity = blockReader.getBlockEntity(target);
		return ItemRequirement.of(blockState, blockEntity);
	}

	public int markAllBlockRequirements(MaterialChecklist checklist, Level world, PlacementPredicate predicate) {
		int blocksToPlace = 0;
		for (BlockPos pos : blockReader.getAllPositions()) {
			BlockPos relPos = pos.offset(schematicAnchor);
			BlockState required = blockReader.getBlockState(relPos);
			BlockEntity requiredBE = blockReader.getBlockEntity(relPos);

			if (!world.isLoaded(pos.offset(schematicAnchor))) {
				checklist.warnBlockNotLoaded();
				continue;
			}
			if (!shouldPlaceBlock(world, predicate, relPos))
				continue;
			ItemRequirement requirement = ItemRequirement.of(required, requiredBE);
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
		blockReader.getEntityStream()
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
		List<Entity> entities = blockReader.getEntityStream().collect(Collectors.toList());

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
		BoundingBox bounds = blockReader.getBounds();
		BlockPos posInBounds = currentPos.offset(-bounds.minX(), -bounds.minY(), -bounds.minZ());

		if (posInBounds.getX() > bounds.getXSpan())
			currentPos = new BlockPos(bounds.minX(), currentPos.getY(), currentPos.getZ() + 1).west();
		if (posInBounds.getZ() > bounds.getZSpan())
			currentPos = new BlockPos(currentPos.getX(), currentPos.getY() + 1, bounds.minZ()).west();

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
