package com.simibubi.create.content.processing;

import javax.annotation.Nullable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class AssemblyOperatorUseContext extends BlockPlaceContext {
	public AssemblyOperatorUseContext(Level p_i50056_1_, @Nullable Player p_i50056_2_, InteractionHand p_i50056_3_, ItemStack p_i50056_4_, BlockHitResult p_i50056_5_) {
		super(p_i50056_1_, p_i50056_2_, p_i50056_3_, p_i50056_4_, p_i50056_5_);
	}
}
