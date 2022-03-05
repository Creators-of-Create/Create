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
import com.simibubi.create.foundation.config.ContraptionMovementSetting;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;
import io.github.fabricators_of_create.porting_lib.util.MinecartAndRailUtil;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecart.Type;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.EntityHitResult;

public class MinecartContraptionItem extends Item {

	private final AbstractMinecart.Type minecartType;

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
	private static final DispenseItemBehavior DISPENSER_BEHAVIOR = new DefaultDispenseItemBehavior() {
		private final DefaultDispenseItemBehavior behaviourDefaultDispenseItem = new DefaultDispenseItemBehavior();

		@Override
		public ItemStack execute(BlockSource source, ItemStack stack) {
			Direction direction = source.getBlockState()
				.getValue(DispenserBlock.FACING);
			Level world = source.getLevel();
			double d0 = source.x() + (double) direction.getStepX() * 1.125D;
			double d1 = Math.floor(source.y()) + (double) direction.getStepY();
			double d2 = source.z() + (double) direction.getStepZ() * 1.125D;
			BlockPos blockpos = source.getPos()
				.relative(direction);
			BlockState blockstate = world.getBlockState(blockpos);
			RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock
				? MinecartAndRailUtil.getDirectionOfRail(blockstate, world, blockpos, null)
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
				RailShape railshape1 = blockstate1.getBlock() instanceof BaseRailBlock
					? MinecartAndRailUtil.getDirectionOfRail(blockstate1, world, blockpos.below(),
						null)
					: RailShape.NORTH_SOUTH;
				if (direction != Direction.DOWN && railshape1.isAscending()) {
					d3 = -0.4D;
				} else {
					d3 = -0.9D;
				}
			}

			AbstractMinecart abstractminecartentity = AbstractMinecart.createMinecart(world, d0, d1 + d3, d2,
				((MinecartContraptionItem) stack.getItem()).minecartType);
			if (stack.hasCustomHoverName())
				abstractminecartentity.setCustomName(stack.getHoverName());
			world.addFreshEntity(abstractminecartentity);
			addContraptionToMinecart(world, stack, abstractminecartentity, direction);

			stack.shrink(1);
			return stack;
		}

		@Override
		protected void playSound(BlockSource source) {
			source.getLevel()
				.levelEvent(1000, source.getPos(), 0);
		}
	};

	// Taken and adjusted from MinecartItem
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level world = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		BlockState blockstate = world.getBlockState(blockpos);
		if (!blockstate.is(BlockTags.RAILS)) {
			return InteractionResult.FAIL;
		} else {
			ItemStack itemstack = context.getItemInHand();
			if (!world.isClientSide) {
				RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock
					? MinecartAndRailUtil.getDirectionOfRail(blockstate, world, blockpos, null)
					: RailShape.NORTH_SOUTH;
				double d0 = 0.0D;
				if (railshape.isAscending()) {
					d0 = 0.5D;
				}

				AbstractMinecart abstractminecartentity =
					AbstractMinecart.createMinecart(world, (double) blockpos.getX() + 0.5D,
						(double) blockpos.getY() + 0.0625D + d0, (double) blockpos.getZ() + 0.5D, this.minecartType);
				if (itemstack.hasCustomHoverName())
					abstractminecartentity.setCustomName(itemstack.getHoverName());
				Player player = context.getPlayer();
				world.addFreshEntity(abstractminecartentity);
				addContraptionToMinecart(world, itemstack, abstractminecartentity,
					player == null ? null : player.getDirection());
			}

			itemstack.shrink(1);
			return InteractionResult.SUCCESS;
		}
	}

	public static void addContraptionToMinecart(Level world, ItemStack itemstack, AbstractMinecart cart,
		@Nullable Direction newFacing) {
		CompoundTag tag = itemstack.getOrCreateTag();
		if (tag.contains("Contraption")) {
			CompoundTag contraptionTag = tag.getCompound("Contraption");

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
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {}

	public static InteractionResult wrenchCanBeUsedToPickUpMinecartContraptions(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		if (player == null || entity == null)
			return InteractionResult.PASS;

		if (player.isSpectator()) // forge checks this, fabric does not
			return InteractionResult.PASS;

		ItemStack wrench = player.getItemInHand(hand);
		if (!AllItems.WRENCH.isIn(wrench))
			return InteractionResult.PASS;
		if (entity instanceof AbstractContraptionEntity)
			entity = entity.getVehicle();
		if (!(entity instanceof AbstractMinecart))
			return InteractionResult.PASS;
		if (!entity.isAlive())
			return InteractionResult.PASS;
		AbstractMinecart cart = (AbstractMinecart) entity;
		Type type = cart.getMinecartType();
		if (type != Type.RIDEABLE && type != Type.FURNACE && type != Type.CHEST)
			return InteractionResult.PASS;
		List<Entity> passengers = cart.getPassengers();
		if (passengers.isEmpty() || !(passengers.get(0) instanceof OrientedContraptionEntity))
			return InteractionResult.PASS;
		OrientedContraptionEntity contraption = (OrientedContraptionEntity) passengers.get(0);

		if(ContraptionMovementSetting.isNoPickup(contraption.getContraption().getBlocks().values())) {
			player.displayClientMessage(Lang.translate("contraption.minecart_contraption_illegal_pickup")
					.withStyle(ChatFormatting.RED), true);
			return InteractionResult.PASS;
		}

		if (world.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		ItemStack generatedStack = create(type, contraption).setHoverName(entity.getCustomName());

		try {
			ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
			NbtIo.write(NBTSerializer.serializeNBT(generatedStack), dataOutput);
			int estimatedPacketSize = dataOutput.toByteArray().length;
			if (estimatedPacketSize > 2_000_000) {
				player.displayClientMessage(Lang.translate("contraption.minecart_contraption_too_big")
					.withStyle(ChatFormatting.RED), true);
				return InteractionResult.PASS;
			}

		} catch (IOException e) {
			e.printStackTrace();
			return InteractionResult.PASS;
		}

		player.getInventory().placeItemBackInInventory(generatedStack);
		contraption.discard();
		entity.discard();
		return InteractionResult.SUCCESS;
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

		CompoundTag tag = entity.getContraption()
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
