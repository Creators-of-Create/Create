package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import java.util.List;
import java.util.UUID;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.IDisplayAssemblyExceptions;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.content.contraptions.components.tracks.ControllerRailBlock;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CartAssemblerTileEntity extends SmartTileEntity implements IDisplayAssemblyExceptions {
	private static final int assemblyCooldown = 8;

	protected ScrollOptionBehaviour<CartMovementMode> movementMode;
	private int ticksSinceMinecartUpdate;
	protected AssemblyException lastException;
	protected AbstractMinecartEntity cartToAssemble;

	public CartAssemblerTileEntity(TileEntityType<? extends CartAssemblerTileEntity> type) {
		super(type);
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

	public void tryAssemble(AbstractMinecartEntity cart) {
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
			Vector3i accelerationVector =
				ControllerRailBlock.getAccelerationVector(AllBlocks.CONTROLLER_RAIL.getDefaultState()
					.setValue(ControllerRailBlock.SHAPE, state.getValue(CartAssemblerBlock.RAIL_SHAPE))
					.setValue(ControllerRailBlock.BACKWARDS, state.getValue(CartAssemblerBlock.BACKWARDS)));
			float speed = block.getRailMaxSpeed(state, level, worldPosition, cart);
			cart.setDeltaMovement(Vector3d.atLowerCornerOf(accelerationVector)
				.scale(speed));
		}
		if (action == CartAssemblerBlock.CartAssemblerAction.DISASSEMBLE_BRAKE) {
			Vector3d diff = VecHelper.getCenterOf(worldPosition)
				.subtract(cart.position());
			cart.setDeltaMovement(diff.x / 16f, 0, diff.z / 16f);
		}
	}

	protected void assemble(World world, BlockPos pos, AbstractMinecartEntity cart) {
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
			Vector3d diff = contraption.connectedCart.position()
				.subtract(cart.position());
			initialOrientation = Direction.fromYRot(MathHelper.atan2(diff.z, diff.x) * 180 / Math.PI);
		}

		OrientedContraptionEntity entity = OrientedContraptionEntity.create(world, contraption, initialOrientation);
		if (couplingFound)
			entity.setCouplingId(cart.getUUID());
		entity.setPos(pos.getX(), pos.getY(), pos.getZ());
		world.addFreshEntity(entity);
		entity.startRiding(cart);

		if (cart instanceof FurnaceMinecartEntity) {
			CompoundNBT nbt = cart.serializeNBT();
			nbt.putDouble("PushZ", 0);
			nbt.putDouble("PushX", 0);
			cart.deserializeNBT(nbt);
		}
	}

	protected void disassemble(World world, BlockPos pos, AbstractMinecartEntity cart) {
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

	protected void disassembleCart(AbstractMinecartEntity cart) {
		cart.ejectPassengers();
		if (cart instanceof FurnaceMinecartEntity) {
			CompoundNBT nbt = cart.serializeNBT();
			nbt.putDouble("PushZ", cart.getDeltaMovement().x);
			nbt.putDouble("PushX", cart.getDeltaMovement().z);
			cart.deserializeNBT(nbt);
		}
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		movementMode = new ScrollOptionBehaviour<>(CartMovementMode.class,
			Lang.translate("contraptions.cart_movement_mode"), this, getMovementModeSlot());
		movementMode.requiresWrench();
		behaviours.add(movementMode);
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		AssemblyException.write(compound, lastException);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		lastException = AssemblyException.read(compound);
		super.fromTag(state, compound, clientPacket);
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
		protected Vector3d getSouthLocation() {
			return VecHelper.voxelSpace(8, 8, 18);
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

	public void assembleNextTick(AbstractMinecartEntity cart) {
		if (cartToAssemble == null)
			cartToAssemble = cart;
	}

	public boolean isMinecartUpdateValid() {
		return ticksSinceMinecartUpdate >= assemblyCooldown;
	}

}
