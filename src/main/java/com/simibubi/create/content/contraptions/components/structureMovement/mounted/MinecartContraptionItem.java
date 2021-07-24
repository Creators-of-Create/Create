package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CKinetics;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity.Type;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class MinecartContraptionItem extends Item {

	private final AbstractMinecartEntity.Type minecartType;

	public static MinecartContraptionItem rideable(Properties builder) {
		return new MinecartContraptionItem(Type.RIDEABLE, builder);
	}

	public static MinecartContraptionItem furnace(Properties builder) {
		return new MinecartContraptionItem(Type.FURNACE, builder);
	}

	public static MinecartContraptionItem chest(Properties builder) {
		return new MinecartContraptionItem(Type.CHEST, builder);
	}

	private MinecartContraptionItem(Type minecartTypeIn, Properties builder) {
		super(builder);
		this.minecartType = minecartTypeIn;
		DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
	}

	// Taken and adjusted from MinecartItem
	private static final IDispenseItemBehavior DISPENSER_BEHAVIOR = new DefaultDispenseItemBehavior() {
		private final DefaultDispenseItemBehavior behaviourDefaultDispenseItem = new DefaultDispenseItemBehavior();

		@Override
		public ItemStack execute(IBlockSource source, ItemStack stack) {
			Direction direction = source.getBlockState()
				.getValue(DispenserBlock.FACING);
			World world = source.getLevel();
			double d0 = source.x() + (double) direction.getStepX() * 1.125D;
			double d1 = Math.floor(source.y()) + (double) direction.getStepY();
			double d2 = source.z() + (double) direction.getStepZ() * 1.125D;
			BlockPos blockpos = source.getPos()
				.relative(direction);
			BlockState blockstate = world.getBlockState(blockpos);
			RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock
				? ((AbstractRailBlock) blockstate.getBlock()).getRailDirection(blockstate, world, blockpos, null)
				: RailShape.NORTH_SOUTH;
			double d3;
			if (blockstate.is(BlockTags.RAILS)) {
				if (railshape.isAscending()) {
					d3 = 0.6D;
				} else {
					d3 = 0.1D;
				}
			} else {
				if (blockstate.getMaterial() != Material.AIR || !world.getBlockState(blockpos.below())
					.is(BlockTags.RAILS)) {
					return this.behaviourDefaultDispenseItem.dispense(source, stack);
				}

				BlockState blockstate1 = world.getBlockState(blockpos.below());
				RailShape railshape1 = blockstate1.getBlock() instanceof AbstractRailBlock
					? ((AbstractRailBlock) blockstate1.getBlock()).getRailDirection(blockstate1, world, blockpos.below(),
						null)
					: RailShape.NORTH_SOUTH;
				if (direction != Direction.DOWN && railshape1.isAscending()) {
					d3 = -0.4D;
				} else {
					d3 = -0.9D;
				}
			}

			AbstractMinecartEntity abstractminecartentity = AbstractMinecartEntity.createMinecart(world, d0, d1 + d3, d2,
				((MinecartContraptionItem) stack.getItem()).minecartType);
			if (stack.hasCustomHoverName())
				abstractminecartentity.setCustomName(stack.getHoverName());
			world.addFreshEntity(abstractminecartentity);
			addContraptionToMinecart(world, stack, abstractminecartentity, direction);

			stack.shrink(1);
			return stack;
		}

		@Override
		protected void playSound(IBlockSource source) {
			source.getLevel()
				.levelEvent(1000, source.getPos(), 0);
		}
	};

	// Taken and adjusted from MinecartItem
	@Override
	public ActionResultType useOn(ItemUseContext context) {
		World world = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		BlockState blockstate = world.getBlockState(blockpos);
		if (!blockstate.is(BlockTags.RAILS)) {
			return ActionResultType.FAIL;
		} else {
			ItemStack itemstack = context.getItemInHand();
			if (!world.isClientSide) {
				RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock
					? ((AbstractRailBlock) blockstate.getBlock()).getRailDirection(blockstate, world, blockpos, null)
					: RailShape.NORTH_SOUTH;
				double d0 = 0.0D;
				if (railshape.isAscending()) {
					d0 = 0.5D;
				}

				AbstractMinecartEntity abstractminecartentity =
					AbstractMinecartEntity.createMinecart(world, (double) blockpos.getX() + 0.5D,
						(double) blockpos.getY() + 0.0625D + d0, (double) blockpos.getZ() + 0.5D, this.minecartType);
				if (itemstack.hasCustomHoverName())
					abstractminecartentity.setCustomName(itemstack.getHoverName());
				PlayerEntity player = context.getPlayer();
				world.addFreshEntity(abstractminecartentity);
				addContraptionToMinecart(world, itemstack, abstractminecartentity,
					player == null ? null : player.getDirection());
			}

			itemstack.shrink(1);
			return ActionResultType.SUCCESS;
		}
	}

	public static void addContraptionToMinecart(World world, ItemStack itemstack, AbstractMinecartEntity cart,
		@Nullable Direction newFacing) {
		CompoundNBT tag = itemstack.getOrCreateTag();
		if (tag.contains("Contraption")) {
			CompoundNBT contraptionTag = tag.getCompound("Contraption");

			Direction intialOrientation = NBTHelper.readEnum(contraptionTag, "InitialOrientation", Direction.class);

			Contraption mountedContraption = Contraption.fromNBT(world, contraptionTag, false);
			OrientedContraptionEntity contraptionEntity =
				newFacing == null ? OrientedContraptionEntity.create(world, mountedContraption, intialOrientation)
					: OrientedContraptionEntity.createAtYaw(world, mountedContraption, intialOrientation,
						newFacing.toYRot());

			contraptionEntity.startRiding(cart);
			contraptionEntity.setPos(cart.getX(), cart.getY(), cart.getZ());
			world.addFreshEntity(contraptionEntity);
		}
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		return "item.create.minecart_contraption";
	}

	@Override
	public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {}

	@SubscribeEvent
	public static void wrenchCanBeUsedToPickUpMinecartContraptions(PlayerInteractEvent.EntityInteract event) {
		Entity entity = event.getTarget();
		PlayerEntity player = event.getPlayer();
		if (player == null || entity == null)
			return;

		ItemStack wrench = player.getItemInHand(event.getHand());
		if (!AllItems.WRENCH.isIn(wrench))
			return;
		if (entity instanceof AbstractContraptionEntity)
			entity = entity.getVehicle();
		if (!(entity instanceof AbstractMinecartEntity))
			return;
		if (!entity.isAlive())
			return;
		AbstractMinecartEntity cart = (AbstractMinecartEntity) entity;
		Type type = cart.getMinecartType();
		if (type != Type.RIDEABLE && type != Type.FURNACE && type != Type.CHEST)
			return;
		List<Entity> passengers = cart.getPassengers();
		if (passengers.isEmpty() || !(passengers.get(0) instanceof OrientedContraptionEntity))
			return;
		OrientedContraptionEntity contraption = (OrientedContraptionEntity) passengers.get(0);

		if (AllConfigs.SERVER.kinetics.spawnerMovement.get() == CKinetics.SpawnerMovementSetting.NO_PICKUP) {
			Contraption blocks = contraption.getContraption();
			if (blocks != null && blocks.getBlocks().values().stream()
					.anyMatch(i -> i.state.getBlock() instanceof SpawnerBlock)) {
				player.displayClientMessage(Lang.translate("contraption.minecart_contraption_illegal_pickup")
						.withStyle(TextFormatting.RED), true);
				return;
			}
		}

		if (event.getWorld().isClientSide) {
			event.setCancellationResult(ActionResultType.SUCCESS);
			event.setCanceled(true);
			return;
		}

		ItemStack generatedStack = create(type, contraption).setHoverName(entity.getCustomName());

		try {
			ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
			CompressedStreamTools.write(generatedStack.serializeNBT(), dataOutput);
			int estimatedPacketSize = dataOutput.toByteArray().length;
			if (estimatedPacketSize > 2_000_000) {
				player.displayClientMessage(Lang.translate("contraption.minecart_contraption_too_big")
					.withStyle(TextFormatting.RED), true);
				return;
			}

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		player.inventory.placeItemBackInInventory(event.getWorld(), generatedStack);
		contraption.remove();
		entity.remove();
		event.setCancellationResult(ActionResultType.SUCCESS);
		event.setCanceled(true);
	}

	public static ItemStack create(Type type, OrientedContraptionEntity entity) {
		ItemStack stack = ItemStack.EMPTY;

		switch (type) {
		case RIDEABLE:
			stack = AllItems.MINECART_CONTRAPTION.asStack();
			break;
		case FURNACE:
			stack = AllItems.FURNACE_MINECART_CONTRAPTION.asStack();
			break;
		case CHEST:
			stack = AllItems.CHEST_MINECART_CONTRAPTION.asStack();
			break;
		default:
			break;
		}

		if (stack.isEmpty())
			return stack;

		CompoundNBT tag = entity.getContraption()
			.writeNBT(false);
		tag.remove("UUID");
		tag.remove("Pos");
		tag.remove("Motion");

		NBTHelper.writeEnum(tag, "InitialOrientation", entity.getInitialOrientation());

		stack.getOrCreateTag()
			.put("Contraption", tag);
		return stack;
	}
}
