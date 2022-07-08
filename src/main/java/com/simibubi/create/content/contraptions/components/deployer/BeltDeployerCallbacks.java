package com.simibubi.create.content.contraptions.components.deployer;

import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity.State;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.content.contraptions.processing.ItemApplicationRecipe;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.curiosities.tools.SandPaperPolishingRecipe;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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

		Recipe<?> recipe = deployerTileEntity.getRecipe(s.stack);
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
		DeployerTileEntity deployerTileEntity, Recipe<?> recipe) {
		
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

		deployerTileEntity.award(AllAdvancements.DEPLOYER);
		
		TransportedItemStack left = transported.copy();
		deployerTileEntity.player.spawnedItemEffects = transported.stack.copy();
		left.stack.shrink(1);
		ItemStack resultItem = null;

		if (collect.isEmpty()) {
			resultItem = left.stack.copy();
			handler.handleProcessingOnItem(transported, TransportedResult.convertTo(left));
		} else {
			resultItem = collect.get(0).stack.copy();
			handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(collect, left));
		}

		ItemStack heldItem = deployerTileEntity.player.getMainHandItem();
		boolean unbreakable = heldItem.hasTag() && heldItem.getTag()
			.getBoolean("Unbreakable");
		boolean keepHeld =
			recipe instanceof ItemApplicationRecipe && ((ItemApplicationRecipe) recipe).shouldKeepHeldItem();

		if (!unbreakable && !keepHeld) {
			if (heldItem.isDamageableItem())
				heldItem.hurtAndBreak(1, deployerTileEntity.player,
					s -> s.broadcastBreakEvent(InteractionHand.MAIN_HAND));
			else
				heldItem.shrink(1);
		}

		if (resultItem != null && !resultItem.isEmpty())
			awardAdvancements(deployerTileEntity, resultItem);

		BlockPos pos = deployerTileEntity.getBlockPos();
		Level world = deployerTileEntity.getLevel();
		if (heldItem.isEmpty())
			world.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, .25f, 1);
		world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, .25f, .75f);
		if (recipe instanceof SandPaperPolishingRecipe)
			AllSoundEvents.SANDING_SHORT.playOnServer(world, pos, .35f, 1f);

		deployerTileEntity.sendData();
	}

	private static void awardAdvancements(DeployerTileEntity deployerTileEntity, ItemStack created) {
		CreateAdvancement advancement = null;

		if (AllBlocks.ANDESITE_CASING.isIn(created))
			advancement = AllAdvancements.ANDESITE_CASING;
		else if (AllBlocks.BRASS_CASING.isIn(created))
			advancement = AllAdvancements.BRASS_CASING;
		else if (AllBlocks.COPPER_CASING.isIn(created))
			advancement = AllAdvancements.COPPER_CASING;
		else if (AllBlocks.RAILWAY_CASING.isIn(created))
			advancement = AllAdvancements.TRAIN_CASING;
		else
			return;

		deployerTileEntity.award(advancement);
	}

}
