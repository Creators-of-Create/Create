package com.simibubi.create.content.contraptions.components.deployer;

import static net.minecraftforge.eventbus.api.Event.Result.DEFAULT;
import static net.minecraftforge.eventbus.api.Event.Result.DENY;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Multimap;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.create.content.curiosities.tools.SandPaperItem;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BucketItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.Vec3d;
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
			super(world);
			this.face = face;
			this.pos = pos;
		}

		@Override
		public BlockRayTraceResult rayTraceBlocks(RayTraceContext context) {
			rayMode = true;
			BlockRayTraceResult rayTraceBlocks = super.rayTraceBlocks(context);
			rayMode = false;
			return rayTraceBlocks;
		}

		@Override
		public BlockState getBlockState(BlockPos position) {
			if (rayMode && (pos.offset(face.getOpposite(), 3)
				.equals(position)
				|| pos.offset(face.getOpposite(), 1)
					.equals(position)))
				return Blocks.BEDROCK.getDefaultState();
			return world.getBlockState(position);
		}
	}

	static boolean shouldActivate(ItemStack held, World world, BlockPos targetPos) {
		if (held.getItem() instanceof BlockItem)
			if (world.getBlockState(targetPos).getBlock() == ((BlockItem) held.getItem()).getBlock())
				return false;

		if (held.getItem() instanceof BucketItem) {
			BucketItem bucketItem = (BucketItem) held.getItem();
			Fluid fluid = bucketItem.getFluid();
			if (fluid != Fluids.EMPTY && world.getFluidState(targetPos)
				.getFluid() == fluid)
				return false;
		}

		return true;
	}

	static void activate(DeployerFakePlayer player, Vec3d vec, BlockPos clickedPos, Vec3d extensionVector, Mode mode) {
		Multimap<String, AttributeModifier> attributeModifiers = player.getHeldItemMainhand()
			.getAttributeModifiers(EquipmentSlotType.MAINHAND);
		player.getAttributes()
			.applyAttributeModifiers(attributeModifiers);
		activateInner(player, vec, clickedPos, extensionVector, mode);
		player.getAttributes()
			.removeAttributeModifiers(attributeModifiers);
	}

	private static void activateInner(DeployerFakePlayer player, Vec3d vec, BlockPos clickedPos, Vec3d extensionVector,
		Mode mode) {

		Vec3d rayOrigin = vec.add(extensionVector.scale(3 / 2f + 1 / 64f));
		Vec3d rayTarget = vec.add(extensionVector.scale(5 / 2f - 1 / 64f));
		player.setPosition(rayOrigin.x, rayOrigin.y, rayOrigin.z);
		BlockPos pos = new BlockPos(vec);
		ItemStack stack = player.getHeldItemMainhand();
		Item item = stack.getItem();

		// Check for entities
		final ServerWorld world = player.getServerWorld();
		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(clickedPos));
		Hand hand = Hand.MAIN_HAND;
		if (!entities.isEmpty()) {
			Entity entity = entities.get(world.rand.nextInt(entities.size()));
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
					if (entity.processInitialInteract(player, hand))
						success = true;
					else if (entity instanceof LivingEntity
						&& stack.interactWithEntity(player, (LivingEntity) entity, hand))
						success = true;
				}
				if (!success && stack.isFood() && entity instanceof PlayerEntity) {
					PlayerEntity playerEntity = (PlayerEntity) entity;
					if (playerEntity.canEat(item.getFood().canEatWhenFull())) {
						playerEntity.onFoodEaten(world, stack);
						player.spawnedItemEffects = stack.copy();
						success = true;
					}
				}
			}

			// Punch entity
			if (mode == Mode.PUNCH) {
				player.resetCooldown();
				player.attackTargetEntityWithCurrentItem(entity);
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
		BlockRayTraceResult result = world.rayTraceBlocks(rayTraceContext);
		if (result.getPos() != clickedPos)
			result = new BlockRayTraceResult(result.getHitVec(), result.getFace(), clickedPos, result.isInside());
		BlockState clickedState = world.getBlockState(clickedPos);
		Direction face = result.getFace();
		if (face == null)
			face = Direction.getFacingFromVector(extensionVector.x, extensionVector.y, extensionVector.z)
				.getOpposite();

		// Left click
		if (mode == Mode.PUNCH) {
			if (!world.isBlockModifiable(player, clickedPos))
				return;
			if (clickedState.getRenderShape(world, clickedPos)
				.isEmpty()) {
				player.blockBreakingProgress = null;
				return;
			}
			LeftClickBlock event = ForgeHooks.onLeftClickBlock(player, clickedPos, face);
			if (event.isCanceled())
				return;
			if (world.extinguishFire(player, clickedPos, face))
				return;
			if (event.getUseBlock() != DENY)
				clickedState.onBlockClicked(world, clickedPos, player);
			if (stack.isEmpty())
				return;

			float progress = clickedState.getPlayerRelativeBlockHardness(player, world, clickedPos) * 16;
			float before = 0;
			Pair<BlockPos, Float> blockBreakingProgress = player.blockBreakingProgress;
			if (blockBreakingProgress != null)
				before = blockBreakingProgress.getValue();
			progress += before;

			if (progress >= 1) {
				player.interactionManager.tryHarvestBlock(clickedPos);
				world.sendBlockBreakProgress(player.getEntityId(), clickedPos, -1);
				player.blockBreakingProgress = null;
				return;
			}
			if (progress <= 0) {
				player.blockBreakingProgress = null;
				return;
			}

			if ((int) (before * 10) != (int) (progress * 10))
				world.sendBlockBreakProgress(player.getEntityId(), clickedPos, (int) (progress * 10));
			player.blockBreakingProgress = Pair.of(clickedPos, progress);
			return;
		}

		// Right click
		ItemUseContext itemusecontext = new ItemUseContext(player, hand, result);
		Event.Result useBlock = DENY;
		Event.Result useItem = DEFAULT;
		if (!clickedState.getRenderShape(world, clickedPos)
			.isEmpty()) {
			RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, clickedPos, face);
			useBlock = event.getUseBlock();
			useItem = event.getUseItem();
		}

		// Item has custom active use
		if (useItem != DENY) {
			ActionResultType actionresult = stack.onItemUseFirst(itemusecontext);
			if (actionresult != ActionResultType.PASS)
				return;
		}

		boolean holdingSomething = !player.getHeldItemMainhand()
			.isEmpty();
		boolean flag1 =
			!(player.isSneaking() && holdingSomething) || (stack.doesSneakBypassUse(world, clickedPos, player));

		// Use on block
		if (useBlock != DENY && flag1 && clickedState.onUse(world, player, hand, result) == ActionResultType.SUCCESS)
			return;
		if (stack.isEmpty())
			return;
		if (useItem == DENY)
			return;
		if (item instanceof BlockItem && !clickedState.isReplaceable(new BlockItemUseContext(itemusecontext)))
			return;

		// Reposition fire placement for convenience
		if (item == Items.FLINT_AND_STEEL) {
			Direction newFace = result.getFace();
			BlockPos newPos = result.getPos();
			if (!FlintAndSteelItem.canSetFire(clickedState, world, clickedPos))
				newFace = Direction.UP;
			if (clickedState.getMaterial() == Material.AIR)
				newPos = newPos.offset(face.getOpposite());
			result = new BlockRayTraceResult(result.getHitVec(), newFace, newPos, result.isInside());
			itemusecontext = new ItemUseContext(player, hand, result);
		}

		// 'Inert' item use behaviour & block placement
		ActionResultType onItemUse = stack.onItemUse(itemusecontext);
		if (onItemUse == ActionResultType.SUCCESS)
			return;
		if (item == Items.ENDER_PEARL)
			return;

		// buckets create their own ray, We use a fake wall to contain the active area
		World itemUseWorld = world;
		if (item instanceof BucketItem || item instanceof SandPaperItem)
			itemUseWorld = new ItemUseWorld(world, face, pos);

		ActionResult<ItemStack> onItemRightClick = item.onItemRightClick(itemUseWorld, player, hand);
		player.setHeldItem(hand, onItemRightClick.getResult());

		CompoundNBT tag = stack.getTag();
		if (tag != null && stack.getItem() instanceof SandPaperItem && tag.contains("Polishing"))
			player.spawnedItemEffects = ItemStack.read(tag.getCompound("Polishing"));

		if (!player.getActiveItemStack()
			.isEmpty())
			player.setHeldItem(hand, stack.onItemUseFinish(world, player));

		player.resetActiveHand();
	}

}
