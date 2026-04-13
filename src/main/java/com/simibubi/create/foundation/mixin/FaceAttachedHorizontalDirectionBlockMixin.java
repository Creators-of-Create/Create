package com.simibubi.create.foundation.mixin;

import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlock;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

@Mixin(FaceAttachedHorizontalDirectionalBlock.class)
public class FaceAttachedHorizontalDirectionBlockMixin {

	@Inject(method = "canAttach", at = @At("HEAD"), cancellable = true)
	private static void create$canAttach(LevelReader level, BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		BlockState state = level.getBlockState(pos.relative(direction));
		Block block = state.getBlock();
		if (block instanceof MechanicalMixerBlock || block instanceof MechanicalPressBlock)
			cir.setReturnValue(!(direction == Direction.UP || direction == Direction.DOWN));
		if (block instanceof DeployerBlock) {
			Direction facing = state.getValue(FACING);
			cir.setReturnValue(!(direction == facing || direction == facing.getOpposite()));
		}
	}
}
