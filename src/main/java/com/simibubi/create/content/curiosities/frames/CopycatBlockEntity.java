package com.simibubi.create.content.curiosities.frames;

import java.util.List;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.ITransformableTE;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.logistics.block.redstone.RoseQuartzLampBlock;
import com.simibubi.create.content.schematics.ISpecialBlockEntityItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.items.ItemHandlerHelper;

public class CopycatBlockEntity extends SmartBlockEntity implements ISpecialBlockEntityItemRequirement, ITransformableTE {

	ItemStack consumedItem;
	BlockState baseBlock;

	public CopycatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		baseBlock = AllBlocks.COPYCAT_BASE.getDefaultState();
		consumedItem = ItemStack.EMPTY;
	}

	public void setItem(ItemStack item) {
		consumedItem = ItemHandlerHelper.copyStackWithSize(item, 1);
		setChanged();
	}

	public ItemStack getConsumedItem() {
		return consumedItem;
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state) {
		if (consumedItem.isEmpty())
			return ItemRequirement.NONE;
		return new ItemRequirement(ItemUseType.CONSUME, consumedItem);
	}

	public void setMaterial(BlockState blockState) {
		BlockState wrapperState = getBlockState();

		if (!baseBlock.is(blockState.getBlock()))
			for (Direction side : Iterate.directions) {
				BlockPos neighbour = worldPosition.relative(side);
				BlockState neighbourState = level.getBlockState(neighbour);
				if (neighbourState != wrapperState)
					continue;
				if (!(level.getBlockEntity(neighbour)instanceof CopycatBlockEntity ufte))
					continue;
				BlockState otherMaterial = ufte.getMaterial();
				if (!otherMaterial.is(blockState.getBlock()))
					continue;
				blockState = otherMaterial;
				break;
			}

		baseBlock = blockState;
		if (!level.isClientSide()) {
			notifyUpdate();
			return;
		}
		redraw();
	}

	public boolean hasCustomMaterial() {
		return !AllBlocks.COPYCAT_BASE.has(getMaterial());
	}

	public BlockState getMaterial() {
		return baseBlock;
	}

	public boolean cycleMaterial() {
		if (baseBlock.hasProperty(BlockStateProperties.FACING))
			setMaterial(baseBlock.cycle(BlockStateProperties.FACING));
		else if (baseBlock.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
			setMaterial(baseBlock.cycle(BlockStateProperties.HORIZONTAL_FACING));
		else if (baseBlock.hasProperty(BlockStateProperties.AXIS))
			setMaterial(baseBlock.cycle(BlockStateProperties.AXIS));
		else if (baseBlock.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
			setMaterial(baseBlock.cycle(BlockStateProperties.HORIZONTAL_AXIS));
		else if (baseBlock.hasProperty(BlockStateProperties.LIT))
			setMaterial(baseBlock.cycle(BlockStateProperties.LIT));
		else if (baseBlock.hasProperty(RoseQuartzLampBlock.POWERING))
			setMaterial(baseBlock.cycle(RoseQuartzLampBlock.POWERING));
		else
			return false;

		return true;
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);

		consumedItem = ItemStack.of(tag.getCompound("Item"));

		BlockState prevMaterial = baseBlock;
		if (!tag.contains("Material"))
			return;

		JsonOps ops = JsonOps.INSTANCE;
		BlockState.CODEC.decode(ops, JsonParser.parseString(tag.getString("Material")))
			.result()
			.ifPresent(p -> baseBlock = p.getFirst());

		if (clientPacket && prevMaterial != baseBlock)
			redraw();
	}

	private void redraw() {
		if (!isVirtual())
			requestModelDataUpdate();
		if (hasLevel()) {
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
			level.getChunkSource()
				.getLightEngine()
				.checkBlock(worldPosition);
		}
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);

		tag.put("Item", consumedItem.serializeNBT());

		JsonOps ops = JsonOps.INSTANCE;
		BlockState.CODEC.encode(baseBlock, ops, ops.empty())
			.result()
			.map(je -> je.toString())
			.ifPresent(s -> tag.putString("Material", s));
	}

	public static final ModelProperty<BlockState> MATERIAL_PROPERTY = new ModelProperty<>();

	@Override
	public IModelData getModelData() {
		return new ModelDataMap.Builder().withInitial(MATERIAL_PROPERTY, baseBlock)
			.build();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override
	public void transform(StructureTransform transform) {
		baseBlock = transform.apply(baseBlock);
		notifyUpdate();
	}

}
