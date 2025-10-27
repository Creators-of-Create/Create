package com.simibubi.create.content.contraptions.actors.harvester;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.contraptions.actors.AttachedActorBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

import org.jetbrains.annotations.NotNull;

public class HarvesterBlock extends AttachedActorBlock implements IBE<HarvesterBlockEntity> {

	public static final MapCodec<HarvesterBlock> CODEC = simpleCodec(HarvesterBlock::new);

	public HarvesterBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
	}

	@Override
	public Class<HarvesterBlockEntity> getBlockEntityClass() {
		return HarvesterBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends HarvesterBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.HARVESTER.get();
	}

	@Override
	protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}
}
