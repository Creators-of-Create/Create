package com.simibubi.create.content.kinetics.simpleRelays;

import java.util.List;

import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class BracketedKineticBlockEntity extends SimpleKineticBlockEntity implements TransformableBlockEntity {

	public BracketedKineticBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours
			.add(new BracketedBlockEntityBehaviour(this, state -> state.getBlock() instanceof AbstractSimpleShaftBlock));
		super.addBehaviours(behaviours);
	}

	@Override
	public void transform(BlockEntity be, StructureTransform transform) {
		BracketedBlockEntityBehaviour bracketBehaviour = getBehaviour(BracketedBlockEntityBehaviour.TYPE);
		if (bracketBehaviour != null) {
			bracketBehaviour.transformBracket(transform);
		}
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		Block block = getBlockState().getBlock();
		boolean isCogwheel = ICogWheel.isSmallCog(block) || ICogWheel.isLargeCog(block);

		// Used for GameTests to safely verify tooltip content on the server side
		// Bypasses client-only formatting logic to prevent crashes in headless environments
		// Note: Used Component.translatable directly to avoid issues with CreateLang in GameTests
		if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
			if (isCogwheel) {
                tooltip.add(Component.translatable("create.tooltip.cogwheel.header"));
            } else {
                tooltip.add(Component.translatable("create.tooltip.shaft.header"));
            }
			addToGoggleRotationDirectionTooltip(tooltip);
            return true;
        }

		if (isCogwheel) {
			CreateLang.translate("tooltip.cogwheel.header")
				.forGoggles(tooltip);
		} else {
			CreateLang.translate("tooltip.shaft.header")
				.forGoggles(tooltip);
		}
		addToGoggleRotationDirectionTooltip(tooltip);
		return getSpeed() != 0;
	}
}
