package com.simibubi.create.content.kinetics.deployer;

import static net.minecraftforge.eventbus.api.Event.Result.DEFAULT;
import static net.minecraftforge.eventbus.api.Event.Result.DENY;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Multimap;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.mounted.CartAssemblerBlockItem;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItem;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.extensions.IForgeBaseRailBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event;

public class DeployerHandler {

	private static final class ItemUseWorld extends WrappedWorld {
		private final Direction face;
		private final BlockPos pos;
		boolean rayMode = false;

		private ItemUseWorld(Level world, Direction face, BlockPos pos) {
			super(world);
			this.face = face;
			this.pos = pos;
		}

		@Override
		public BlockHitResult clip(ClipContext context) {
			rayMode = true;
			BlockHitResult rayTraceBlocks = super.clip(context);
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

	static boolean shouldActivate(ItemStack held, Level world, BlockPos targetPos, @Nullable Direction facing) {
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
			&& BlockEntityBehaviour.get(world, targetPos, TransportedItemStackHandlerBehaviour.TYPE) != null)
			return false;

		return true;
	}

	static void activate(DeployerFakePlayer player, Vec3 vec, BlockPos clickedPos, Vec3 extensionVector, Mode mode) {
		Multimap<Attribute, AttributeModifier> attributeModifiers = player.getMainHandItem()
			.getAttributeModifiers(EquipmentSlot.MAINHAND);
		player.getAttributes()
			.addTransientAttributeModifiers(attributeModifiers);
		activateInner(player, vec, clickedPos, extensionVector, mode);
		player.getAttributes()
			.addTransientAttributeModifiers(attributeModifiers);
	}

	private static void activateInner(DeployerFakePlayer player, Vec3 vec, BlockPos clickedPos, Vec3 extensionVector,
		Mode mode) {

		Vec3 rayOrigin = vec.add(extensionVector.scale(3 / 2f + 1 / 64f));
		Vec3 rayTarget = vec.add(extensionVector.scale(5 / 2f - 1 / 64f));
		player.setPos(rayOrigin.x, rayOrigin.y, rayOrigin.z);
		BlockPos pos = new BlockPos(vec);
		ItemStack stack = player.getMainHandItem();
		Item item = stack.getItem();

		// Check for entities
		final ServerLevel world = player.getLevel();
		List<Entity> entities = world.getEntitiesOfClass(Entity.class, new AABB(clickedPos))
			.stream()
			.filter(e -> !(e instanceof AbstractContraptionEntity))
			.collect(Collectors.toList());
		InteractionHand hand = InteractionHand.MAIN_HAND;
		if (!entities.isEmpty()) {
			Entity entity = entities.get(world.random.nextInt(entities.size()));
			List<ItemEntity> capturedDrops = new ArrayList<>();
			boolean success = false;
			entity.captureDrops(capturedDrops);

			// Use on entity
			if (mode == Mode.USE) {
				InteractionResult cancelResult = ForgeHooks.onInteractEntity(player, entity, hand);
				if (cancelResult == InteractionResult.FAIL) {
					entity.captureDrops(null);
					return;
				}
				if (cancelResult == null) {
					if (entity.interact(player, hand)
						.consumesAction()) {
						if (entity instanceof AbstractVillager) {
							AbstractVillager villager = ((AbstractVillager) entity);
							if (villager.getTradingPlayer() instanceof DeployerFakePlayer)
								villager.setTradingPlayer(null);
						}
						success = true;
					} else if (entity instanceof LivingEntity
						&& stack.interactLivingEntity(player, (LivingEntity) entity, hand)
							.consumesAction())
						success = true;
				}
				if (!success && entity instanceof Player playerEntity) {
					if (stack.isEdible()) {
						FoodProperties foodProperties = item.getFoodProperties(stack, player);
						if (playerEntity.canEat(foodProperties.canAlwaysEat())) {
							playerEntity.eat(world, stack);
							player.spawnedItemEffects = stack.copy();
							success = true;
						}
					}
					if (AllItemTags.DEPLOYABLE_DRINK.matches(stack)) {
						player.spawnedItemEffects = stack.copy();
						player.setItemInHand(hand, stack.finishUsingItem(world, playerEntity));
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
			capturedDrops.forEach(e -> player.getInventory()
				.placeItemBackInInventory(e.getItem()));
			if (success)
				return;
		}

		// Shoot ray
		ClipContext rayTraceContext =
			new ClipContext(rayOrigin, rayTarget, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
		BlockHitResult result = world.clip(rayTraceContext);
		if (result.getBlockPos() != clickedPos)
			result = new BlockHitResult(result.getLocation(), result.getDirection(), clickedPos, result.isInside());
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
			if (BlockHelper.extinguishFire(world, player, clickedPos, face))
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
				.getHitSound(), SoundSource.NEUTRAL, .25f, 1);

			if (progress >= 1) {
				tryHarvestBlock(player, player.gameMode, clickedPos);
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
		UseOnContext itemusecontext = new UseOnContext(player, hand, result);
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
			InteractionResult actionresult = stack.onItemUseFirst(itemusecontext);
			if (actionresult != InteractionResult.PASS)
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
		if (item instanceof BlockItem && !(item instanceof CartAssemblerBlockItem)
			&& !clickedState.canBeReplaced(new BlockPlaceContext(itemusecontext)))
			return;

		// Reposition fire placement for convenience
		if (item == Items.FLINT_AND_STEEL) {
			Direction newFace = result.getDirection();
			BlockPos newPos = result.getBlockPos();
			if (!BaseFireBlock.canBePlacedAt(world, clickedPos, newFace))
				newFace = Direction.UP;
			if (clickedState.getMaterial() == Material.AIR)
				newPos = newPos.relative(face.getOpposite());
			result = new BlockHitResult(result.getLocation(), newFace, newPos, result.isInside());
			itemusecontext = new UseOnContext(player, hand, result);
		}

		// 'Inert' item use behaviour & block placement
		InteractionResult onItemUse = stack.useOn(itemusecontext);
		if (onItemUse.consumesAction()) {
			if (stack.getItem() instanceof BlockItem bi
				&& (bi.getBlock() instanceof IForgeBaseRailBlock || bi.getBlock() instanceof ITrackBlock))
				player.placedTracks = true;
			return;
		}
		if (item == Items.ENDER_PEARL)
			return;
		if (AllItemTags.DEPLOYABLE_DRINK.matches(item))
			return;

		// buckets create their own ray, We use a fake wall to contain the active area
		Level itemUseWorld = world;
		if (item instanceof BucketItem || item instanceof SandPaperItem)
			itemUseWorld = new ItemUseWorld(world, face, pos);

		InteractionResultHolder<ItemStack> onItemRightClick = item.use(itemUseWorld, player, hand);
		ItemStack resultStack = onItemRightClick.getObject();
		if (resultStack != stack || resultStack.getCount() != stack.getCount() || resultStack.getUseDuration() > 0
			|| resultStack.getDamageValue() != stack.getDamageValue()) {
			player.setItemInHand(hand, onItemRightClick.getObject());
		}

		CompoundTag tag = stack.getTag();
		if (tag != null && stack.getItem() instanceof SandPaperItem && tag.contains("Polishing")) {
			player.spawnedItemEffects = ItemStack.of(tag.getCompound("Polishing"));
			AllSoundEvents.SANDING_SHORT.playOnServer(world, pos, .25f, 1f);
		}

		if (!player.getUseItem()
			.isEmpty())
			player.setItemInHand(hand, stack.finishUsingItem(world, player));

		player.stopUsingItem();
	}

	public static boolean tryHarvestBlock(ServerPlayer player, ServerPlayerGameMode interactionManager, BlockPos pos) {
		// <> PlayerInteractionManager#tryHarvestBlock

		ServerLevel world = player.getLevel();
		BlockState blockstate = world.getBlockState(pos);
		GameType gameType = interactionManager.getGameModeForPlayer();

		if (net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, gameType, player, pos) == -1)
			return false;

		BlockEntity blockEntity = world.getBlockEntity(pos);
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
			net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, heldItem, InteractionHand.MAIN_HAND);

		BlockPos posUp = pos.above();
		BlockState stateUp = world.getBlockState(posUp);
		if (blockstate.getBlock() instanceof DoublePlantBlock
			&& blockstate.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER
			&& stateUp.getBlock() == blockstate.getBlock()
			&& stateUp.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
			// hack to prevent DoublePlantBlock from dropping a duplicate item
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
			world.setBlock(posUp, Blocks.AIR.defaultBlockState(), 35);
		} else {
			if (!blockstate.onDestroyedByPlayer(world, pos, player, canHarvest, world.getFluidState(pos)))
				return true;
		}

		blockstate.getBlock()
			.destroy(world, pos, blockstate);
		if (!canHarvest)
			return true;

		Block.getDrops(blockstate, world, pos, blockEntity, player, prevHeldItem)
			.forEach(item -> player.getInventory().placeItemBackInInventory(item));
		blockstate.spawnAfterBreak(world, pos, prevHeldItem, true);
		return true;
	}

	public static InteractionResult safeOnUse(BlockState state, Level world, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult ray) {
		if (state.getBlock() instanceof BeehiveBlock)
			return safeOnBeehiveUse(state, world, pos, player, hand);
		return state.use(world, player, hand, ray);
	}

	protected static InteractionResult safeOnBeehiveUse(BlockState state, Level world, BlockPos pos, Player player,
		InteractionHand hand) {
		// <> BeehiveBlock#onUse

		BeehiveBlock block = (BeehiveBlock) state.getBlock();
		ItemStack prevHeldItem = player.getItemInHand(hand);
		int honeyLevel = state.getValue(BeehiveBlock.HONEY_LEVEL);
		boolean success = false;
		if (honeyLevel < 5)
			return InteractionResult.PASS;

		if (prevHeldItem.getItem() == Items.SHEARS) {
			world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_SHEAR,
				SoundSource.NEUTRAL, 1.0F, 1.0F);
			// <> BeehiveBlock#dropHoneycomb
			player.getInventory().placeItemBackInInventory(new ItemStack(Items.HONEYCOMB, 3));
			prevHeldItem.hurtAndBreak(1, player, s -> s.broadcastBreakEvent(hand));
			success = true;
		}

		if (prevHeldItem.getItem() == Items.GLASS_BOTTLE) {
			prevHeldItem.shrink(1);
			world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL,
				SoundSource.NEUTRAL, 1.0F, 1.0F);
			ItemStack honeyBottle = new ItemStack(Items.HONEY_BOTTLE);
			if (prevHeldItem.isEmpty())
				player.setItemInHand(hand, honeyBottle);
			else
				player.getInventory().placeItemBackInInventory(honeyBottle);
			success = true;
		}

		if (!success)
			return InteractionResult.PASS;

		block.resetHoneyLevel(world, state, pos);
		return InteractionResult.SUCCESS;
	}

}
