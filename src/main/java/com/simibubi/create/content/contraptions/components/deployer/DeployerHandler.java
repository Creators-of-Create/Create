package com.simibubi.create.content.contraptions.components.deployer;

import static net.minecraftforge.eventbus.api.Event.Result.DEFAULT;
import static net.minecraftforge.eventbus.api.Event.Result.DENY;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Multimap;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlockItem;
import com.simibubi.create.content.curiosities.tools.SandPaperItem;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event;

public class DeployerHandler {

	private static final class ItemUseWorld extends WrappedWorld {
		private final Direction face;
		private final BlockPos pos;
		boolean rayMode = false;

		private ItemUseWorld(World world, Direction face, BlockPos pos) {
			super(world, world.getChunkSource());
			this.face = face;
			this.pos = pos;
		}

		@Override
		public BlockRayTraceResult clip(RayTraceContext context) {
			rayMode = true;
			BlockRayTraceResult rayTraceBlocks = super.clip(context);
			rayMode = false;
			return rayTraceBlocks;
		}

		@Override
		public BlockState getBlockState(BlockPos position) {
			if (rayMode && (pos.relative(face.getOpposite(), 3)
				.equals(position)
				|| pos.relative(face.getOpposite(), 1)
					.equals(position)))
				return Blocks.BEDROCK.defaultBlockState();
			return world.getBlockState(position);
		}
	}

	static boolean shouldActivate(ItemStack held, World world, BlockPos targetPos, @Nullable Direction facing) {
		if (held.getItem() instanceof BlockItem)
			if (world.getBlockState(targetPos)
				.getBlock() == ((BlockItem) held.getItem()).getBlock())
				return false;

		if (held.getItem() instanceof BucketItem) {
			BucketItem bucketItem = (BucketItem) held.getItem();
			Fluid fluid = bucketItem.getFluid();
			if (fluid != Fluids.EMPTY && world.getFluidState(targetPos)
				.getType() == fluid)
				return false;
		}

		if (!held.isEmpty() && facing == Direction.DOWN
			&& TileEntityBehaviour.get(world, targetPos, TransportedItemStackHandlerBehaviour.TYPE) != null)
			return false;

		return true;
	}

	static void activate(DeployerFakePlayer player, Vector3d vec, BlockPos clickedPos, Vector3d extensionVector,
		Mode mode) {
		Multimap<Attribute, AttributeModifier> attributeModifiers = player.getMainHandItem()
			.getAttributeModifiers(EquipmentSlotType.MAINHAND);
		player.getAttributes()
			.addTransientAttributeModifiers(attributeModifiers);
		activateInner(player, vec, clickedPos, extensionVector, mode);
		player.getAttributes()
			.addTransientAttributeModifiers(attributeModifiers);
	}

	private static void activateInner(DeployerFakePlayer player, Vector3d vec, BlockPos clickedPos,
		Vector3d extensionVector, Mode mode) {

		Vector3d rayOrigin = vec.add(extensionVector.scale(3 / 2f + 1 / 64f));
		Vector3d rayTarget = vec.add(extensionVector.scale(5 / 2f - 1 / 64f));
		player.setPos(rayOrigin.x, rayOrigin.y, rayOrigin.z);
		BlockPos pos = new BlockPos(vec);
		ItemStack stack = player.getMainHandItem();
		Item item = stack.getItem();

		// Check for entities
		final ServerWorld world = player.getLevel();
		List<Entity> entities = world.getEntitiesOfClass(Entity.class, new AxisAlignedBB(clickedPos)).stream()
			.filter(e -> !(e instanceof AbstractContraptionEntity))
			.collect(Collectors.toList());
		Hand hand = Hand.MAIN_HAND;
		if (!entities.isEmpty()) {
			Entity entity = entities.get(world.random.nextInt(entities.size()));
			List<ItemEntity> capturedDrops = new ArrayList<>();
			boolean success = false;
			entity.captureDrops(capturedDrops);

			// Use on entity
			if (mode == Mode.USE) {
				ActionResultType cancelResult = ForgeHooks.onInteractEntity(player, entity, hand);
				if (cancelResult == ActionResultType.FAIL) {
					entity.captureDrops(null);
					return;
				}
				if (cancelResult == null) {
					if (entity.interact(player, hand)
						.consumesAction()){
						if (entity instanceof AbstractVillagerEntity) {
							AbstractVillagerEntity villager = ((AbstractVillagerEntity) entity);
							if (villager.getTradingPlayer() instanceof DeployerFakePlayer)
								villager.setTradingPlayer(null);
						}
						success = true;
					}
					else if (entity instanceof LivingEntity && stack.interactLivingEntity(player, (LivingEntity) entity, hand)
						.consumesAction())
						success = true;
				}
				if (!success && stack.isEdible() && entity instanceof PlayerEntity) {
					PlayerEntity playerEntity = (PlayerEntity) entity;
					if (playerEntity.canEat(item.getFoodProperties()
						.canAlwaysEat())) {
						playerEntity.eat(world, stack);
						player.spawnedItemEffects = stack.copy();
						success = true;
					}
				}
			}

			// Punch entity
			if (mode == Mode.PUNCH) {
				player.resetAttackStrengthTicker();
				player.attack(entity);
				success = true;
			}

			entity.captureDrops(null);
			capturedDrops.forEach(e -> player.inventory.placeItemBackInInventory(world, e.getItem()));
			if (success)
				return;
		}

		// Shoot ray
		RayTraceContext rayTraceContext =
			new RayTraceContext(rayOrigin, rayTarget, BlockMode.OUTLINE, FluidMode.NONE, player);
		BlockRayTraceResult result = world.clip(rayTraceContext);
		if (result.getBlockPos() != clickedPos)
			result = new BlockRayTraceResult(result.getLocation(), result.getDirection(), clickedPos, result.isInside());
		BlockState clickedState = world.getBlockState(clickedPos);
		Direction face = result.getDirection();
		if (face == null)
			face = Direction.getNearest(extensionVector.x, extensionVector.y, extensionVector.z)
				.getOpposite();

		// Left click
		if (mode == Mode.PUNCH) {
			if (!world.mayInteract(player, clickedPos))
				return;
			if (clickedState.getShape(world, clickedPos)
				.isEmpty()) {
				player.blockBreakingProgress = null;
				return;
			}
			LeftClickBlock event = ForgeHooks.onLeftClickBlock(player, clickedPos, face);
			if (event.isCanceled())
				return;
			if (BlockHelper.extinguishFire(world, player, clickedPos, face)) // FIXME: is there an equivalent in world, as there was in 1.15?
				return;
			if (event.getUseBlock() != DENY)
				clickedState.attack(world, clickedPos, player);
			if (stack.isEmpty())
				return;

			float progress = clickedState.getDestroyProgress(player, world, clickedPos) * 16;
			float before = 0;
			Pair<BlockPos, Float> blockBreakingProgress = player.blockBreakingProgress;
			if (blockBreakingProgress != null)
				before = blockBreakingProgress.getValue();
			progress += before;
			world.playSound(null, clickedPos, clickedState.getSoundType()
				.getHitSound(), SoundCategory.NEUTRAL, .25f, 1);

			if (progress >= 1) {
				tryHarvestBlock(player.gameMode, clickedPos);
				world.destroyBlockProgress(player.getId(), clickedPos, -1);
				player.blockBreakingProgress = null;
				return;
			}
			if (progress <= 0) {
				player.blockBreakingProgress = null;
				return;
			}

			if ((int) (before * 10) != (int) (progress * 10))
				world.destroyBlockProgress(player.getId(), clickedPos, (int) (progress * 10));
			player.blockBreakingProgress = Pair.of(clickedPos, progress);
			return;
		}

		// Right click
		ItemUseContext itemusecontext = new ItemUseContext(player, hand, result);
		Event.Result useBlock = DEFAULT;
		Event.Result useItem = DEFAULT;
		if (!clickedState.getShape(world, clickedPos)
			.isEmpty()) {
			RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, clickedPos, result);
			useBlock = event.getUseBlock();
			useItem = event.getUseItem();
		}

		// Item has custom active use
		if (useItem != DENY) {
			ActionResultType actionresult = stack.onItemUseFirst(itemusecontext);
			if (actionresult != ActionResultType.PASS)
				return;
		}

		boolean holdingSomething = !player.getMainHandItem()
			.isEmpty();
		boolean flag1 =
			!(player.isShiftKeyDown() && holdingSomething) || (stack.doesSneakBypassUse(world, clickedPos, player));

		// Use on block
		if (useBlock != DENY && flag1
			&& safeOnUse(clickedState, world, clickedPos, player, hand, result).consumesAction())
			return;
		if (stack.isEmpty())
			return;
		if (useItem == DENY)
			return;
		if (item instanceof BlockItem
			&& !(item instanceof CartAssemblerBlockItem)
			&& !clickedState.canBeReplaced(new BlockItemUseContext(itemusecontext)))
			return;

		// Reposition fire placement for convenience
		if (item == Items.FLINT_AND_STEEL) {
			Direction newFace = result.getDirection();
			BlockPos newPos = result.getBlockPos();
			if (!AbstractFireBlock.canBePlacedAt(world, clickedPos, newFace))
				newFace = Direction.UP;
			if (clickedState.getMaterial() == Material.AIR)
				newPos = newPos.relative(face.getOpposite());
			result = new BlockRayTraceResult(result.getLocation(), newFace, newPos, result.isInside());
			itemusecontext = new ItemUseContext(player, hand, result);
		}

		// 'Inert' item use behaviour & block placement
		ActionResultType onItemUse = stack.useOn(itemusecontext);
		if (onItemUse.consumesAction())
			return;
		if (item == Items.ENDER_PEARL)
			return;

		// buckets create their own ray, We use a fake wall to contain the active area
		World itemUseWorld = world;
		if (item instanceof BucketItem || item instanceof SandPaperItem)
			itemUseWorld = new ItemUseWorld(world, face, pos);

		ActionResult<ItemStack> onItemRightClick = item.use(itemUseWorld, player, hand);
		ItemStack resultStack = onItemRightClick.getObject();
		if (resultStack != stack || resultStack.getCount() != stack.getCount() || resultStack.getUseDuration() > 0
			|| resultStack.getDamageValue() != stack.getDamageValue()) {
			player.setItemInHand(hand, onItemRightClick.getObject());
		}

		CompoundNBT tag = stack.getTag();
		if (tag != null && stack.getItem() instanceof SandPaperItem && tag.contains("Polishing")) {
			player.spawnedItemEffects = ItemStack.of(tag.getCompound("Polishing"));
			AllSoundEvents.AUTO_POLISH.playOnServer(world, pos, .25f, 1f);
		}

		if (!player.getUseItem()
			.isEmpty())
			player.setItemInHand(hand, stack.finishUsingItem(world, player));

		player.stopUsingItem();
	}

	public static boolean tryHarvestBlock(PlayerInteractionManager interactionManager, BlockPos pos) {
		// <> PlayerInteractionManager#tryHarvestBlock

		ServerWorld world = interactionManager.level;
		ServerPlayerEntity player = interactionManager.player;
		BlockState blockstate = world.getBlockState(pos);
		GameType gameType = interactionManager.getGameModeForPlayer();

		if (net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, gameType, player, pos) == -1)
			return false;

		TileEntity tileentity = world.getBlockEntity(pos);
		if (player.getMainHandItem()
			.onBlockStartBreak(pos, player))
			return false;
		if (player.blockActionRestricted(world, pos, gameType))
			return false;

		ItemStack prevHeldItem = player.getMainHandItem();
		ItemStack heldItem = prevHeldItem.copy();

		boolean canHarvest = blockstate.canHarvestBlock(world, pos, player);
		prevHeldItem.mineBlock(world, blockstate, pos, player);
		if (prevHeldItem.isEmpty() && !heldItem.isEmpty())
			net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, heldItem, Hand.MAIN_HAND);


		BlockPos posUp = pos.above();
		BlockState stateUp = world.getBlockState(posUp);
		if (blockstate.getBlock() instanceof DoublePlantBlock
			&& blockstate.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER
			&& stateUp.getBlock() == blockstate.getBlock()
			&& stateUp.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER
		) {
			// hack to prevent DoublePlantBlock from dropping a duplicate item
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
			world.setBlock(posUp, Blocks.AIR.defaultBlockState(), 35);
		} else {
			if (!blockstate.removedByPlayer(world, pos, player, canHarvest, world.getFluidState(pos)))
				return true;
		}

		blockstate.getBlock()
			.destroy(world, pos, blockstate);
		if (!canHarvest)
			return true;

		Block.getDrops(blockstate, world, pos, tileentity, player, prevHeldItem)
			.forEach(item -> player.inventory.placeItemBackInInventory(world, item));
		blockstate.spawnAfterBreak(world, pos, prevHeldItem);
		return true;
	}

	public static ActionResultType safeOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
		Hand hand, BlockRayTraceResult ray) {
		if (state.getBlock() instanceof BeehiveBlock)
			return safeOnBeehiveUse(state, world, pos, player, hand);
		return state.use(world, player, hand, ray);
	}

	protected static ActionResultType safeOnBeehiveUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
		Hand hand) {
		// <> BeehiveBlock#onUse

		BeehiveBlock block = (BeehiveBlock) state.getBlock();
		ItemStack prevHeldItem = player.getItemInHand(hand);
		int honeyLevel = state.getValue(BeehiveBlock.HONEY_LEVEL);
		boolean success = false;
		if (honeyLevel < 5)
			return ActionResultType.PASS;

		if (prevHeldItem.getItem() == Items.SHEARS) {
			world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_SHEAR,
				SoundCategory.NEUTRAL, 1.0F, 1.0F);
			// <> BeehiveBlock#dropHoneycomb
			player.inventory.placeItemBackInInventory(world, new ItemStack(Items.HONEYCOMB, 3));
			prevHeldItem.hurtAndBreak(1, player, s -> s.broadcastBreakEvent(hand));
			success = true;
		}

		if (prevHeldItem.getItem() == Items.GLASS_BOTTLE) {
			prevHeldItem.shrink(1);
			world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL,
				SoundCategory.NEUTRAL, 1.0F, 1.0F);
			ItemStack honeyBottle = new ItemStack(Items.HONEY_BOTTLE);
			if (prevHeldItem.isEmpty())
				player.setItemInHand(hand, honeyBottle);
			else
				player.inventory.placeItemBackInInventory(world, honeyBottle);
			success = true;
		}

		if (!success)
			return ActionResultType.PASS;

		block.resetHoneyLevel(world, state, pos);
		return ActionResultType.SUCCESS;
	}

}
