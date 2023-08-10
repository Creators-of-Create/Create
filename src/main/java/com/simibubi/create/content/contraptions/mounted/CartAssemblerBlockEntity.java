package com.simibubi.create.content.contraptions.mounted;

import java.util.List;
import java.util.UUID;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.IDisplayAssemblyExceptions;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.minecart.CouplingHandler;
import com.simibubi.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.minecart.capability.MinecartController;
import com.simibubi.create.content.redstone.rail.ControllerRailBlock;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CartAssemblerBlockEntity extends SmartBlockEntity implements IDisplayAssemblyExceptions {
	private static final int assemblyCooldown = 8;

	protected ScrollOptionBehaviour<CartMovementMode> movementMode;
	private int ticksSinceMinecartUpdate;
	protected AssemblyException lastException;
	protected AbstractMinecart cartToAssemble;

	public CartAssemblerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		ticksSinceMinecartUpdate = assemblyCooldown;
	}

	@Override
	public void tick() {
		super.tick();
		if (ticksSinceMinecartUpdate < assemblyCooldown) {
			ticksSinceMinecartUpdate++;
		}

		tryAssemble(cartToAssemble);
		cartToAssemble = null;
	}

	public void tryAssemble(AbstractMinecart cart) {
		if (cart == null)
			return;

		if (!isMinecartUpdateValid())
			return;
		resetTicksSinceMinecartUpdate();

		BlockState state = level.getBlockState(worldPosition);
		if (!AllBlocks.CART_ASSEMBLER.has(state))
			return;
		CartAssemblerBlock block = (CartAssemblerBlock) state.getBlock();

		CartAssemblerBlock.CartAssemblerAction action = CartAssemblerBlock.getActionForCart(state, cart);
		if (action.shouldAssemble())
			assemble(level, worldPosition, cart);
		if (action.shouldDisassemble())
			disassemble(level, worldPosition, cart);
		if (action == CartAssemblerBlock.CartAssemblerAction.ASSEMBLE_ACCELERATE) {
			if (cart.getDeltaMovement()
				.length() > 1 / 128f) {
				Direction facing = cart.getMotionDirection();
				RailShape railShape = state.getValue(CartAssemblerBlock.RAIL_SHAPE);
				for (Direction d : Iterate.directionsInAxis(railShape == RailShape.EAST_WEST ? Axis.X : Axis.Z))
					if (level.getBlockState(worldPosition.relative(d))
						.isRedstoneConductor(level, worldPosition.relative(d)))
						facing = d.getOpposite();

				float speed = block.getRailMaxSpeed(state, level, worldPosition, cart);
				cart.setDeltaMovement(facing.getStepX() * speed, facing.getStepY() * speed, facing.getStepZ() * speed);
			}
		}
		if (action == CartAssemblerBlock.CartAssemblerAction.ASSEMBLE_ACCELERATE_DIRECTIONAL) {
			Vec3i accelerationVector =
				ControllerRailBlock.getAccelerationVector(AllBlocks.CONTROLLER_RAIL.getDefaultState()
					.setValue(ControllerRailBlock.SHAPE, state.getValue(CartAssemblerBlock.RAIL_SHAPE))
					.setValue(ControllerRailBlock.BACKWARDS, state.getValue(CartAssemblerBlock.BACKWARDS)));
			float speed = block.getRailMaxSpeed(state, level, worldPosition, cart);
			cart.setDeltaMovement(Vec3.atLowerCornerOf(accelerationVector)
				.scale(speed));
		}
		if (action == CartAssemblerBlock.CartAssemblerAction.DISASSEMBLE_BRAKE) {
			Vec3 diff = VecHelper.getCenterOf(worldPosition)
				.subtract(cart.position());
			cart.setDeltaMovement(diff.x / 16f, 0, diff.z / 16f);
		}
	}

	protected void assemble(Level world, BlockPos pos, AbstractMinecart cart) {
		if (!cart.getPassengers()
			.isEmpty())
			return;

		LazyOptional<MinecartController> optional =
			cart.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY);
		if (optional.isPresent() && optional.orElse(null)
			.isCoupledThroughContraption())
			return;

		CartMovementMode mode = CartMovementMode.values()[movementMode.value];

		MountedContraption contraption = new MountedContraption(mode);
		try {
			if (!contraption.assemble(world, pos))
				return;

			lastException = null;
			sendData();
		} catch (AssemblyException e) {
			lastException = e;
			sendData();
			return;
		}

		boolean couplingFound = contraption.connectedCart != null;
		Direction initialOrientation = CartAssemblerBlock.getHorizontalDirection(getBlockState());

		if (couplingFound) {
			cart.setPos(pos.getX() + .5f, pos.getY(), pos.getZ() + .5f);
			if (!CouplingHandler.tryToCoupleCarts(null, world, cart.getId(),
				contraption.connectedCart.getId()))
				return;
		}

		contraption.removeBlocksFromWorld(world, BlockPos.ZERO);
		contraption.startMoving(world);
		contraption.expandBoundsAroundAxis(Axis.Y);

		if (couplingFound) {
			Vec3 diff = contraption.connectedCart.position()
				.subtract(cart.position());
			initialOrientation = Direction.fromYRot(Mth.atan2(diff.z, diff.x) * 180 / Math.PI);
		}

		OrientedContraptionEntity entity = OrientedContraptionEntity.create(world, contraption, initialOrientation);
		if (couplingFound)
			entity.setCouplingId(cart.getUUID());
		entity.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
		world.addFreshEntity(entity);
		entity.startRiding(cart);

		if (cart instanceof MinecartFurnace) {
			CompoundTag nbt = cart.serializeNBT();
			nbt.putDouble("PushZ", 0);
			nbt.putDouble("PushX", 0);
			cart.deserializeNBT(nbt);
		}

		if (contraption.containsBlockBreakers())
			award(AllAdvancements.CONTRAPTION_ACTORS);
	}

	protected void disassemble(Level world, BlockPos pos, AbstractMinecart cart) {
		if (cart.getPassengers()
			.isEmpty())
			return;
		Entity entity = cart.getPassengers()
			.get(0);
		if (!(entity instanceof OrientedContraptionEntity))
			return;
		OrientedContraptionEntity contraption = (OrientedContraptionEntity) entity;
		UUID couplingId = contraption.getCouplingId();

		if (couplingId == null) {
			contraption.yaw = CartAssemblerBlock.getHorizontalDirection(getBlockState())
				.toYRot();
			disassembleCart(cart);
			return;
		}

		Couple<MinecartController> coupledCarts = contraption.getCoupledCartsIfPresent();
		if (coupledCarts == null)
			return;

		// Make sure connected cart is present and being disassembled
		for (boolean current : Iterate.trueAndFalse) {
			MinecartController minecartController = coupledCarts.get(current);
			if (minecartController.cart() == cart)
				continue;
			BlockPos otherPos = minecartController.cart()
				.blockPosition();
			BlockState blockState = world.getBlockState(otherPos);
			if (!AllBlocks.CART_ASSEMBLER.has(blockState))
				return;
			if (!CartAssemblerBlock.getActionForCart(blockState, minecartController.cart())
				.shouldDisassemble())
				return;
		}

		for (boolean current : Iterate.trueAndFalse)
			coupledCarts.get(current)
				.removeConnection(current);
		disassembleCart(cart);
	}

	protected void disassembleCart(AbstractMinecart cart) {
		cart.ejectPassengers();
		if (cart instanceof MinecartFurnace) {
			CompoundTag nbt = cart.serializeNBT();
			nbt.putDouble("PushZ", cart.getDeltaMovement().x);
			nbt.putDouble("PushX", cart.getDeltaMovement().z);
			cart.deserializeNBT(nbt);
		}
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		movementMode = new ScrollOptionBehaviour<>(CartMovementMode.class,
			CreateLang.translateDirect("contraptions.cart_movement_mode"), this, getMovementModeSlot());
		behaviours.add(movementMode);
		registerAwardables(behaviours, AllAdvancements.CONTRAPTION_ACTORS);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		AssemblyException.write(compound, lastException);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		lastException = AssemblyException.read(compound);
		super.read(compound, clientPacket);
	}

	@Override
	public AssemblyException getLastAssemblyException() {
		return lastException;
	}

	protected ValueBoxTransform getMovementModeSlot() {
		return new CartAssemblerValueBoxTransform();
	}

	private class CartAssemblerValueBoxTransform extends CenteredSideValueBoxTransform {

		public CartAssemblerValueBoxTransform() {
			super((state, d) -> {
				if (d.getAxis()
					.isVertical())
					return false;
				if (!state.hasProperty(CartAssemblerBlock.RAIL_SHAPE))
					return false;
				RailShape railShape = state.getValue(CartAssemblerBlock.RAIL_SHAPE);
				return (d.getAxis() == Axis.X) == (railShape == RailShape.NORTH_SOUTH);
			});
		}

		@Override
		protected Vec3 getSouthLocation() {
			return VecHelper.voxelSpace(8, 7, 17.5);
		}

	}

	public enum CartMovementMode implements INamedIconOptions {

		ROTATE(AllIcons.I_CART_ROTATE),
		ROTATE_PAUSED(AllIcons.I_CART_ROTATE_PAUSED),
		ROTATION_LOCKED(AllIcons.I_CART_ROTATE_LOCKED),

		;

		private String translationKey;
		private AllIcons icon;

		CartMovementMode(AllIcons icon) {
			this.icon = icon;
			translationKey = "contraptions.cart_movement_mode." + Lang.asId(name());
		}

		@Override
		public AllIcons getIcon() {
			return icon;
		}

		@Override
		public String getTranslationKey() {
			return translationKey;
		}
	}

	public void resetTicksSinceMinecartUpdate() {
		ticksSinceMinecartUpdate = 0;
	}

	public void assembleNextTick(AbstractMinecart cart) {
		if (cartToAssemble == null)
			cartToAssemble = cart;
	}

	public boolean isMinecartUpdateValid() {
		return ticksSinceMinecartUpdate >= assemblyCooldown;
	}

}
