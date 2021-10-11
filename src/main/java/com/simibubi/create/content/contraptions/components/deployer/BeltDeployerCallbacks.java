package com.simibubi.create.content.contraptions.components.deployer;

import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity.State;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.curiosities.tools.SandPaperPolishingRecipe;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class BeltDeployerCallbacks {

	public static ProcessingResult onItemReceived(TransportedItemStack s, TransportedItemStackHandlerBehaviour i,
		DeployerTileEntity deployerTileEntity) {

		if (deployerTileEntity.getSpeed() == 0)
			return ProcessingResult.PASS;
		if (deployerTileEntity.mode == Mode.PUNCH)
			return ProcessingResult.PASS;
		BlockState blockState = deployerTileEntity.getBlockState();
		if (!blockState.hasProperty(FACING) || blockState.getValue(FACING) != Direction.DOWN)
			return ProcessingResult.PASS;
		if (deployerTileEntity.state != State.WAITING)
			return ProcessingResult.HOLD;
		if (deployerTileEntity.redstoneLocked)
			return ProcessingResult.PASS;

		DeployerFakePlayer player = deployerTileEntity.getPlayer();
		ItemStack held = player == null ? ItemStack.EMPTY : player.getMainHandItem();

		if (held.isEmpty())
			return ProcessingResult.HOLD;
		if (deployerTileEntity.getRecipe(s.stack) == null)
			return ProcessingResult.PASS;

		deployerTileEntity.start();
		return ProcessingResult.HOLD;
	}

	public static ProcessingResult whenItemHeld(TransportedItemStack s, TransportedItemStackHandlerBehaviour i,
		DeployerTileEntity deployerTileEntity) {

		if (deployerTileEntity.getSpeed() == 0)
			return ProcessingResult.PASS;
		BlockState blockState = deployerTileEntity.getBlockState();
		if (!blockState.hasProperty(FACING) || blockState.getValue(FACING) != Direction.DOWN)
			return ProcessingResult.PASS;

		DeployerFakePlayer player = deployerTileEntity.getPlayer();
		ItemStack held = player == null ? ItemStack.EMPTY : player.getMainHandItem();
		if (held.isEmpty())
			return ProcessingResult.HOLD;

		IRecipe<?> recipe = deployerTileEntity.getRecipe(s.stack);
		if (recipe == null)
			return ProcessingResult.PASS;

		if (deployerTileEntity.state == State.RETRACTING && deployerTileEntity.timer == 1000) {
			activate(s, i, deployerTileEntity, recipe);
			return ProcessingResult.HOLD;
		}

		if (deployerTileEntity.state == State.WAITING) {
			if (deployerTileEntity.redstoneLocked)
				return ProcessingResult.PASS;
			deployerTileEntity.start();
		}

		return ProcessingResult.HOLD;
	}

	public static void activate(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler,
		DeployerTileEntity deployerTileEntity, IRecipe<?> recipe) {

		List<TransportedItemStack> collect =
			InWorldProcessing.applyRecipeOn(ItemHandlerHelper.copyStackWithSize(transported.stack, 1), recipe)
				.stream()
				.map(stack -> {
					TransportedItemStack copy = transported.copy();
					boolean centered = BeltHelper.isItemUpright(stack);
					copy.stack = stack;
					copy.locked = true;
					copy.angle = centered ? 180 : Create.RANDOM.nextInt(360);
					return copy;
				})
				.map(t -> {
					t.locked = false;
					return t;
				})
				.collect(Collectors.toList());

		TransportedItemStack left = transported.copy();
		deployerTileEntity.player.spawnedItemEffects = transported.stack.copy();
		left.stack.shrink(1);

		if (collect.isEmpty())
			handler.handleProcessingOnItem(transported, TransportedResult.convertTo(left));
		else
			handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(collect, left));

		ItemStack heldItem = deployerTileEntity.player.getMainHandItem();
		boolean unbreakable = heldItem.hasTag() && heldItem.getTag()
			.getBoolean("Unbreakable");
		boolean keepHeld =
			recipe instanceof DeployerApplicationRecipe && ((DeployerApplicationRecipe) recipe).shouldKeepHeldItem();

		if (!unbreakable && !keepHeld) {
			if (heldItem.isDamageableItem())
				heldItem.hurtAndBreak(1, deployerTileEntity.player, s -> s.broadcastBreakEvent(Hand.MAIN_HAND));
			else
				heldItem.shrink(1);
		}

		BlockPos pos = deployerTileEntity.getBlockPos();
		World world = deployerTileEntity.getLevel();
		if (heldItem.isEmpty())
			world.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundCategory.BLOCKS, .25f, 1);
		world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundCategory.BLOCKS, .25f, .75f);
		if (recipe instanceof SandPaperPolishingRecipe)
			AllSoundEvents.AUTO_POLISH.playOnServer(world, pos, .25f, 1f);

		deployerTileEntity.sendData();
	}

}
