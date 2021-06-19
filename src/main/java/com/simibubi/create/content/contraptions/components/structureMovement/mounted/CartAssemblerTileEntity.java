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
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CartAssemblerTileEntity extends SmartTileEntity implements IDisplayAssemblyExceptions {
	private static final int assemblyCooldown = 8;

	protected ScrollOptionBehaviour<CartMovementMode> movementMode;
	private int ticksSinceMinecartUpdate;
	protected AssemblyException lastException;

	protected AbstractMinecartEntity cartToAssemble;
	protected Direction cartInitialOrientation = Direction.NORTH;

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

		BlockState state = world.getBlockState(pos);
		if (!AllBlocks.CART_ASSEMBLER.has(state))
			return;
		CartAssemblerBlock block = (CartAssemblerBlock) state.getBlock();

		CartAssemblerBlock.CartAssemblerAction action = CartAssemblerBlock.getActionForCart(state, cart);
		if (action.shouldAssemble())
			assemble(world, pos, cart);
		if (action.shouldDisassemble())
			disassemble(world, pos, cart);
		if (action == CartAssemblerBlock.CartAssemblerAction.ASSEMBLE_ACCELERATE) {
			Direction facing = cart.getAdjustedHorizontalFacing();

			RailShape railShape = state.get(CartAssemblerBlock.RAIL_SHAPE);
			for (Direction d : Iterate.directionsInAxis(railShape == RailShape.EAST_WEST ? Axis.X : Axis.Z))
				if (world.getBlockState(pos.offset(d))
						.isNormalCube(world, pos.offset(d)))
					facing = d.getOpposite();

			float speed = block.getRailMaxSpeed(state, world, pos, cart);
			cart.setMotion(facing.getXOffset() * speed, facing.getYOffset() * speed, facing.getZOffset() * speed);
		}
		if (action == CartAssemblerBlock.CartAssemblerAction.ASSEMBLE_ACCELERATE_DIRECTIONAL) {
			Vector3i accelerationVector = ControllerRailBlock.getAccelerationVector(
					AllBlocks.CONTROLLER_RAIL.getDefaultState()
							.with(ControllerRailBlock.SHAPE, state.get(CartAssemblerBlock.RAIL_SHAPE))
							.with(ControllerRailBlock.BACKWARDS, state.get(CartAssemblerBlock.RAIL_TYPE) == CartAssembleRailType.CONTROLLER_RAIL_BACKWARDS));
			float speed = block.getRailMaxSpeed(state, world, pos, cart);
			cart.setMotion(Vector3d.of(accelerationVector).scale(speed));
		}
		if (action == CartAssemblerBlock.CartAssemblerAction.DISASSEMBLE_BRAKE) {
			Vector3d diff = VecHelper.getCenterOf(pos)
					.subtract(cart.getPositionVec());
			cart.setMotion(diff.x / 16f, 0, diff.z / 16f);
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
		Direction initialOrientation = cart.getMotion()
				.length() < 1 / 512f ? cartInitialOrientation : cart.getAdjustedHorizontalFacing();

		if (couplingFound) {
			cart.setPosition(pos.getX() + .5f, pos.getY(), pos.getZ() + .5f);
			if (!CouplingHandler.tryToCoupleCarts(null, world, cart.getEntityId(),
					contraption.connectedCart.getEntityId()))
				return;
		}

		contraption.removeBlocksFromWorld(world, BlockPos.ZERO);
		contraption.startMoving(world);
		contraption.expandBoundsAroundAxis(Axis.Y);

		if (couplingFound) {
			Vector3d diff = contraption.connectedCart.getPositionVec().subtract(cart.getPositionVec());
			initialOrientation = Direction.fromAngle(MathHelper.atan2(diff.z, diff.x) * 180 / Math.PI);
		}

		OrientedContraptionEntity entity = OrientedContraptionEntity.create(world, contraption, initialOrientation);
		if (couplingFound)
			entity.setCouplingId(cart.getUniqueID());
		entity.setPosition(pos.getX(), pos.getY(), pos.getZ());
		world.addEntity(entity);
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
					.getBlockPos();
			BlockState blockState = world.getBlockState(otherPos);
			if (!AllBlocks.CART_ASSEMBLER.has(blockState))
				return;
			if (!CartAssemblerBlock.getActionForCart(blockState, minecartController.cart()).shouldDisassemble())
				return;
		}

		for (boolean current : Iterate.trueAndFalse)
			coupledCarts.get(current)
					.removeConnection(current);
		disassembleCart(cart);
	}

	protected void disassembleCart(AbstractMinecartEntity cart) {
		cart.removePassengers();
		if (cart instanceof FurnaceMinecartEntity) {
			CompoundNBT nbt = cart.serializeNBT();
			nbt.putDouble("PushZ", cart.getMotion().x);
			nbt.putDouble("PushX", cart.getMotion().z);
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
		NBTHelper.writeEnum(compound, "CartInitialOrientation", cartInitialOrientation);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		lastException = AssemblyException.read(compound);
		super.fromTag(state, compound, clientPacket);
		cartInitialOrientation = NBTHelper.readEnum(compound, "CartInitialOrientation", Direction.class);
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
				if (!state.contains(CartAssemblerBlock.RAIL_SHAPE))
					return false;
				RailShape railShape = state.get(CartAssemblerBlock.RAIL_SHAPE);
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

	// TODO: Remove these methods once we give Cart Assemblers directionality
	protected void setCartInitialOrientation(Direction direction) {
		cartInitialOrientation = direction;
	}
	@SubscribeEvent
	public static void getOrientationOfStationaryCart(PlayerInteractEvent.RightClickBlock event) {
		PlayerEntity player = event.getPlayer();
		if (player == null)
			return;

		Item item = event.getItemStack().getItem();
		if (item != Items.MINECART && item != Items.CHEST_MINECART && item != Items.FURNACE_MINECART)
			return;
		Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
		if (!(block instanceof CartAssemblerBlock))
			return;
		CartAssemblerTileEntity te = ((CartAssemblerBlock) block).getTileEntity(event.getWorld(), event.getPos());
		if (te == null)
			return;

		te.setCartInitialOrientation(player.getHorizontalFacing());
	}
}
