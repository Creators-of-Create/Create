package com.simibubi.create.content.contraptions.components.deployer;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.content.schematics.filtering.SchematicInstances;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.NBTProcessors;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandler;

public class DeployerMovementBehaviour extends MovementBehaviour {

	@Override
	public Vector3d getActiveAreaOffset(MovementContext context) {
		return Vector3d.atLowerCornerOf(context.state.getValue(DeployerBlock.FACING)
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
		World world = context.world;

		ItemStack filter = getFilter(context);
		if (AllItems.SCHEMATIC.isIn(filter))
			activateAsSchematicPrinter(context, pos, player, world, filter);

		Vector3d facingVec = Vector3d.atLowerCornerOf(context.state.getValue(DeployerBlock.FACING)
			.getNormal());
		facingVec = context.rotation.apply(facingVec);
		Vector3d vec = context.position.subtract(facingVec.scale(2));
		player.yRot = AbstractContraptionEntity.yawFromVector(facingVec);
		player.xRot = AbstractContraptionEntity.pitchFromVector(facingVec) - 90;

		DeployerHandler.activate(player, vec, pos, facingVec, mode);
	}

	protected void activateAsSchematicPrinter(MovementContext context, BlockPos pos, DeployerFakePlayer player,
		World world, ItemStack filter) {
		if (!filter.hasTag())
			return;
		if (!world.getBlockState(pos)
			.getMaterial()
			.isReplaceable())
			return;

		CompoundNBT tag = filter.getTag();
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
		ItemStack firstRequired = requiredItems.isEmpty() ? ItemStack.EMPTY : requiredItems.get(0).item;

		if (!context.contraption.hasUniversalCreativeCrate) {
			IItemHandler iItemHandler = context.contraption.inventory;
			for (ItemRequirement.StackRequirement required : requiredItems) {
				int amountFound = ItemHelper
						.extract(iItemHandler, s -> ItemRequirement.validate(required.item, s), ExtractionCountMode.UPTO,
								required.item.getCount(), true)
						.getCount();
				if (amountFound < required.item.getCount())
					return;
			}
			for (ItemRequirement.StackRequirement required : requiredItems)
				ItemHelper.extract(iItemHandler, s -> ItemRequirement.validate(required.item, s), ExtractionCountMode.UPTO,
						required.item.getCount(), false);
		}

		CompoundNBT data = null;
		if (AllBlockTags.SAFE_NBT.matches(blockState)) {
			TileEntity tile = schematicWorld.getBlockEntity(pos);
			if (tile != null) {
				data = tile.save(new CompoundNBT());
				data = NBTProcessors.process(tile, data, true);
			}
		}

		BlockSnapshot blocksnapshot = BlockSnapshot.create(world.dimension(), world, pos);
		BlockHelper.placeSchematicBlock(world, blockState, pos, firstRequired, data);
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
	public void stopMoving(MovementContext context) {
		if (context.world.isClientSide)
			return;

		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;

		context.tileData.put("Inventory", player.inventory.save(new ListNBT()));
		player.remove();
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
			ItemStack held = ItemHelper.extract(context.contraption.inventory,
				stack -> FilterItem.test(context.world, stack, filter), 1, false);
			player.setItemInHand(Hand.MAIN_HAND, held);
		}
	}

	private void tryDisposeOfExcess(MovementContext context) {
		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;
		PlayerInventory inv = player.inventory;
		ItemStack filter = getFilter(context);

		for (List<ItemStack> list : Arrays.asList(inv.armor, inv.offhand, inv.items)) {
			for (int i = 0; i < list.size(); ++i) {
				ItemStack itemstack = list.get(i);
				if (itemstack.isEmpty())
					continue;

				if (list == inv.items && i == inv.selected
					&& FilterItem.test(context.world, itemstack, filter))
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
		if (!(context.temporaryData instanceof DeployerFakePlayer) && context.world instanceof ServerWorld) {
			DeployerFakePlayer deployerFakePlayer = new DeployerFakePlayer((ServerWorld) context.world);
			deployerFakePlayer.inventory.load(context.tileData.getList("Inventory", NBT.TAG_COMPOUND));
			if (context.data.contains("HeldItem"))
				deployerFakePlayer.setItemInHand(Hand.MAIN_HAND, ItemStack.of(context.data.getCompound("HeldItem")));
			context.tileData.remove("Inventory");
			context.temporaryData = deployerFakePlayer;
		}
		return (DeployerFakePlayer) context.temporaryData;
	}

	private ItemStack getFilter(MovementContext context) {
		return ItemStack.of(context.tileData.getCompound("Filter"));
	}

	private Mode getMode(MovementContext context) {
		return NBTHelper.readEnum(context.tileData, "Mode", Mode.class);
	}

	@Override
	public void renderInContraption(MovementContext context, PlacementSimulationWorld renderWorld,
		ContraptionMatrices matrices, IRenderTypeBuffer buffers) {
		if (!Backend.getInstance().canUseInstancing())
			DeployerRenderer.renderInContraption(context, renderWorld, matrices, buffers);
	}

	@Override
	public boolean hasSpecialInstancedRendering() {
		return true;
	}

	@Nullable
	@Override
	public ActorInstance createInstance(MaterialManager<?> materialManager, PlacementSimulationWorld simulationWorld, MovementContext context) {
		return new DeployerActorInstance(materialManager, simulationWorld, context);
	}
}
