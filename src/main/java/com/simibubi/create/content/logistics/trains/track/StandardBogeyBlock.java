package com.simibubi.create.content.logistics.trains.track;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllBogeyStyles;
import com.simibubi.create.content.logistics.trains.AbstractBogeyBlock;
import com.simibubi.create.content.logistics.trains.BogeySizes;
import com.simibubi.create.content.logistics.trains.TrackMaterial;
import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class StandardBogeyBlock extends AbstractBogeyBlock<StandardBogeyBlockEntity>
	implements IBE<StandardBogeyBlockEntity>, ProperWaterloggedBlock, ISpecialBlockItemRequirement {

	public StandardBogeyBlock(Properties props, BogeySizes.BogeySize size) {
		super(props, size);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	public TrackMaterial.TrackType getTrackType(BogeyStyle style) {
		return TrackMaterial.TrackType.STANDARD;
	}

	@Override
	public double getWheelPointSpacing() {
		return 2;
	}

	@Override
	public double getWheelRadius() {
		return (size == BogeySizes.LARGE ? 12.5 : 6.5) / 16d;
	}

	@Override
	public Vec3 getConnectorAnchorOffset() {
		return new Vec3(0, 7 / 32f, 1);
	}

	@Override
	public BogeyStyle getDefaultStyle() {
		return AllBogeyStyles.STANDARD;
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos,
		Player player) {
		return AllBlocks.RAILWAY_CASING.asStack();
	}

	@Override
	public Class<StandardBogeyBlockEntity> getBlockEntityClass() {
		return StandardBogeyBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends StandardBogeyBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.BOGEY.get();
	}

}
