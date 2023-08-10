package com.simibubi.create.content.kinetics.deployer;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.mounted.MountedContraption;
import com.simibubi.create.content.contraptions.render.ActorInstance;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.schematics.SchematicInstances;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.worldWrappers.SchematicWorld;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandler;

public class DeployerMovementBehaviour implements MovementBehaviour {

	@Override
	public Vec3 getActiveAreaOffset(MovementContext context) {
		return Vec3.atLowerCornerOf(context.state.getValue(DeployerBlock.FACING)
			.getNormal())
			.scale(2);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		if (context.world.isClientSide)
			return;

		tryGrabbingItem(context);
		DeployerFakePlayer player = getPlayer(context);
		Mode mode = getMode(context);
		if (mode == Mode.USE && !DeployerHandler.shouldActivate(player.getMainHandItem(), context.world, pos, null))
			return;

		activate(context, pos, player, mode);
		tryDisposeOfExcess(context);
		context.stall = player.blockBreakingProgress != null;
	}

	public void activate(MovementContext context, BlockPos pos, DeployerFakePlayer player, Mode mode) {
		Level world = context.world;

		ItemStack filter = getFilter(context);
		if (AllItems.SCHEMATIC.isIn(filter))
			activateAsSchematicPrinter(context, pos, player, world, filter);

		Vec3 facingVec = Vec3.atLowerCornerOf(context.state.getValue(DeployerBlock.FACING)
			.getNormal());
		facingVec = context.rotation.apply(facingVec);
		Vec3 vec = context.position.subtract(facingVec.scale(2));

		float xRot = AbstractContraptionEntity.pitchFromVector(facingVec) - 90;
		if (Math.abs(xRot) > 89) {
			Vec3 initial = new Vec3(0, 0, 1);
			if (context.contraption.entity instanceof OrientedContraptionEntity oce)
				initial = VecHelper.rotate(initial, oce.getInitialYaw(), Axis.Y);
			if (context.contraption.entity instanceof CarriageContraptionEntity cce)
				initial = VecHelper.rotate(initial, 90, Axis.Y);
			facingVec = context.rotation.apply(initial);
		}

		player.setYRot(AbstractContraptionEntity.yawFromVector(facingVec));
		player.setXRot(xRot);
		player.placedTracks = false;

		DeployerHandler.activate(player, vec, pos, facingVec, mode);

		if ((context.contraption instanceof MountedContraption || context.contraption instanceof CarriageContraption)
			&& player.placedTracks && context.blockEntityData != null && context.blockEntityData.contains("Owner"))
			AllAdvancements.SELF_DEPLOYING.awardTo(world.getPlayerByUUID(context.blockEntityData.getUUID("Owner")));
	}

	protected void activateAsSchematicPrinter(MovementContext context, BlockPos pos, DeployerFakePlayer player,
		Level world, ItemStack filter) {
		if (!filter.hasTag())
			return;
		if (!world.getBlockState(pos)
			.getMaterial()
			.isReplaceable())
			return;

		CompoundTag tag = filter.getTag();
		if (!tag.getBoolean("Deployed"))
			return;
		SchematicWorld schematicWorld = SchematicInstances.get(world, filter);
		if (schematicWorld == null)
			return;
		if (!schematicWorld.getBounds()
			.isInside(pos.subtract(schematicWorld.anchor)))
			return;
		BlockState blockState = schematicWorld.getBlockState(pos);
		ItemRequirement requirement = ItemRequirement.of(blockState, schematicWorld.getBlockEntity(pos));
		if (requirement.isInvalid() || requirement.isEmpty())
			return;
		if (AllBlocks.BELT.has(blockState))
			return;

		List<ItemRequirement.StackRequirement> requiredItems = requirement.getRequiredItems();
		ItemStack contextStack = requiredItems.isEmpty() ? ItemStack.EMPTY : requiredItems.get(0).stack;

		if (!context.contraption.hasUniversalCreativeCrate) {
			IItemHandler itemHandler = context.contraption.getSharedInventory();
			for (ItemRequirement.StackRequirement required : requiredItems) {
				ItemStack stack= ItemHelper
					.extract(itemHandler, required::matches, ExtractionCountMode.EXACTLY,
						required.stack.getCount(), true);
				if (stack.isEmpty())
					return;
			}
			for (ItemRequirement.StackRequirement required : requiredItems)
				contextStack = ItemHelper.extract(itemHandler, required::matches,
					ExtractionCountMode.EXACTLY, required.stack.getCount(), false);
		}

		CompoundTag data = BlockHelper.prepareBlockEntityData(blockState, schematicWorld.getBlockEntity(pos));
		BlockSnapshot blocksnapshot = BlockSnapshot.create(world.dimension(), world, pos);
		BlockHelper.placeSchematicBlock(world, blockState, pos, contextStack, data);
		if (ForgeEventFactory.onBlockPlace(player, blocksnapshot, Direction.UP))
			blocksnapshot.restore(true, false);
	}

	@Override
	public void tick(MovementContext context) {
		if (context.world.isClientSide)
			return;
		if (!context.stall)
			return;

		DeployerFakePlayer player = getPlayer(context);
		Mode mode = getMode(context);

		Pair<BlockPos, Float> blockBreakingProgress = player.blockBreakingProgress;
		if (blockBreakingProgress != null) {
			int timer = context.data.getInt("Timer");
			if (timer < 20) {
				timer++;
				context.data.putInt("Timer", timer);
				return;
			}

			context.data.remove("Timer");
			activate(context, blockBreakingProgress.getKey(), player, mode);
			tryDisposeOfExcess(context);
		}

		context.stall = player.blockBreakingProgress != null;
	}

	@Override
	public void cancelStall(MovementContext context) {
		if (context.world.isClientSide)
			return;

		MovementBehaviour.super.cancelStall(context);
		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;
		if (player.blockBreakingProgress == null)
			return;
		context.world.destroyBlockProgress(player.getId(), player.blockBreakingProgress.getKey(), -1);
		player.blockBreakingProgress = null;
	}

	@Override
	public void stopMoving(MovementContext context) {
		if (context.world.isClientSide)
			return;

		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;

		cancelStall(context);
		context.blockEntityData.put("Inventory", player.getInventory()
			.save(new ListTag()));
		player.discard();
	}

	private void tryGrabbingItem(MovementContext context) {
		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;
		if (player.getMainHandItem()
			.isEmpty()) {
			ItemStack filter = getFilter(context);
			if (AllItems.SCHEMATIC.isIn(filter))
				return;
			ItemStack held = ItemHelper.extract(context.contraption.getSharedInventory(),
				stack -> FilterItem.test(context.world, stack, filter), 1, false);
			player.setItemInHand(InteractionHand.MAIN_HAND, held);
		}
	}

	private void tryDisposeOfExcess(MovementContext context) {
		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;
		Inventory inv = player.getInventory();
		ItemStack filter = getFilter(context);

		for (List<ItemStack> list : Arrays.asList(inv.armor, inv.offhand, inv.items)) {
			for (int i = 0; i < list.size(); ++i) {
				ItemStack itemstack = list.get(i);
				if (itemstack.isEmpty())
					continue;

				if (list == inv.items && i == inv.selected && FilterItem.test(context.world, itemstack, filter))
					continue;

				dropItem(context, itemstack);
				list.set(i, ItemStack.EMPTY);
			}
		}
	}

	@Override
	public void writeExtraData(MovementContext context) {
		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;
		context.data.put("HeldItem", player.getMainHandItem()
			.serializeNBT());
	}

	private DeployerFakePlayer getPlayer(MovementContext context) {
		if (!(context.temporaryData instanceof DeployerFakePlayer) && context.world instanceof ServerLevel) {
			UUID owner = context.blockEntityData.contains("Owner") ? context.blockEntityData.getUUID("Owner") : null;
			DeployerFakePlayer deployerFakePlayer = new DeployerFakePlayer((ServerLevel) context.world, owner);
			deployerFakePlayer.onMinecartContraption = context.contraption instanceof MountedContraption;
			deployerFakePlayer.getInventory()
				.load(context.blockEntityData.getList("Inventory", Tag.TAG_COMPOUND));
			if (context.data.contains("HeldItem"))
				deployerFakePlayer.setItemInHand(InteractionHand.MAIN_HAND,
					ItemStack.of(context.data.getCompound("HeldItem")));
			context.blockEntityData.remove("Inventory");
			context.temporaryData = deployerFakePlayer;
		}
		return (DeployerFakePlayer) context.temporaryData;
	}

	private ItemStack getFilter(MovementContext context) {
		return ItemStack.of(context.blockEntityData.getCompound("Filter"));
	}

	private Mode getMode(MovementContext context) {
		return NBTHelper.readEnum(context.blockEntityData, "Mode", Mode.class);
	}

	@Override
	public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffers) {
		if (!ContraptionRenderDispatcher.canInstance())
			DeployerRenderer.renderInContraption(context, renderWorld, matrices, buffers);
	}

	@Override
	public boolean hasSpecialInstancedRendering() {
		return true;
	}

	@Nullable
	@Override
	public ActorInstance createInstance(MaterialManager materialManager, VirtualRenderWorld simulationWorld,
		MovementContext context) {
		return new DeployerActorInstance(materialManager, simulationWorld, context);
	}
}
