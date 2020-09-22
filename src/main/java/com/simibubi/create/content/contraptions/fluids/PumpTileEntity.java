package com.simibubi.create.content.contraptions.fluids;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class PumpTileEntity extends KineticTileEntity {

	LerpedFloat arrowDirection;
	Couple<FluidNetwork> networks;
	Couple<Map<BlockFace, OpenEndedPipe>> openEnds;
	Couple<MutableBoolean> networksToUpdate;

	boolean reversed;
	FluidStack providedFluid;

	public PumpTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		arrowDirection = LerpedFloat.linear()
			.startWithValue(1);
		networksToUpdate = Couple.create(MutableBoolean::new);
		openEnds = Couple.create(HashMap::new);
		setProvidedFluid(FluidStack.EMPTY);
	}
	
	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.add(new PumpAttachmentBehaviour(this));
	}

	@Override
	public void initialize() {
		super.initialize();
		reversed = getSpeed() < 0;
	}

	@Override
	public void tick() {
		super.tick();
		float speed = getSpeed();

		if (world.isRemote) {
			if (speed == 0)
				return;
			arrowDirection.chase(speed >= 0 ? 1 : -1, .5f, Chaser.EXP);
			arrowDirection.tickChaser();
			return;
		}

		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof PumpBlock))
			return;
		Direction face = blockState.get(PumpBlock.FACING);
		MutableBoolean networkUpdated = new MutableBoolean(false);

		if (networks == null) {
			networks = Couple.create(new FluidNetwork(), new FluidNetwork());
			networks.forEachWithContext((fn, front) -> {
				BlockFace blockFace = new BlockFace(pos, front ? face : face.getOpposite());
				fn.assemble(world, this, blockFace);
				FluidPropagator.showBlockFace(blockFace)
					.lineWidth(1 / 8f);
			});
			networkUpdated.setTrue();
		}

		networksToUpdate.forEachWithContext((update, front) -> {
			if (update.isFalse())
				return;
			FluidNetwork activePipeNetwork = networks.get(front);
			if (activePipeNetwork == null)
				return;
			BlockFace blockFace = new BlockFace(pos, front ? face : face.getOpposite());
			activePipeNetwork.reAssemble(world, this, blockFace);
			FluidPropagator.showBlockFace(blockFace)
				.lineWidth(1 / 8f);
			update.setFalse();
			networkUpdated.setTrue();
		});

		if (networkUpdated.isTrue())
			return;

		networks.forEach(fn -> fn.tick(world, this));

		if (speed == 0)
			return;
		if (speed < 0 != reversed) {
			networks.forEachWithContext((fn, current) -> fn.clearFlows(world, true));
			reversed = speed < 0;
			return;
		}

		boolean pullingSide = isPullingOnSide(true);
		float flowSpeed = Math.abs(speed) / 256f;

		networks.forEachWithContext((fn, front) -> {
			boolean pulling = isPullingOnSide(front);
			fn.tickFlows(world, this, pulling, flowSpeed);
			openEnds.get(front)
				.values()
				.forEach(oep -> oep.tick(world, pulling));
		});

		if (!networks.get(pullingSide)
			.hasEndpoints()) {
			setProvidedFluid(FluidStack.EMPTY);
			return;
		}

		if (networks.getFirst()
			.hasEndpoints()
			&& networks.getSecond()
				.hasEndpoints()) {
			performTransfer();
		}

	}

	@Override
	public void remove() {
		super.remove();
		if (networks != null)
			networks.forEachWithContext((fn, current) -> fn.clearFlows(world, false));
	}

	private void performTransfer() {
		boolean input = isPullingOnSide(true);
		Collection<FluidNetworkEndpoint> inputs = networks.get(input)
			.getEndpoints(true);
		Collection<FluidNetworkEndpoint> outputs = networks.get(!input)
			.getEndpoints(false);

		int flowSpeed = getFluidTransferSpeed();
		FluidStack transfer = FluidStack.EMPTY;
		for (boolean simulate : Iterate.trueAndFalse) {
			FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;

			List<FluidNetworkEndpoint> availableInputs = new ArrayList<>(inputs);
			while (!availableInputs.isEmpty() && transfer.getAmount() < flowSpeed) {
				int diff = flowSpeed - transfer.getAmount();
				int dividedTransfer = diff / availableInputs.size();
				int remainder = diff % availableInputs.size();

				for (Iterator<FluidNetworkEndpoint> iterator = availableInputs.iterator(); iterator.hasNext();) {
					int toTransfer = dividedTransfer;
					if (remainder > 0) {
						toTransfer++;
						remainder--;
					}

					FluidNetworkEndpoint ne = iterator.next();
					IFluidHandler handler = ne.provideHandler()
						.orElse(null);
					if (handler == null) {
						iterator.remove();
						continue;
					}
					FluidStack drained = handler.drain(toTransfer, action);
					if (drained.isEmpty()) {
						iterator.remove();
						continue;
					}
					if (transfer.isFluidEqual(drained) || transfer.isEmpty()) {
						if (drained.getAmount() < toTransfer)
							iterator.remove();
						FluidStack copy = drained.copy();
						copy.setAmount(drained.getAmount() + transfer.getAmount());
						transfer = copy;
						continue;
					}
					iterator.remove();
					continue;
				}

			}

			List<FluidNetworkEndpoint> availableOutputs = new ArrayList<>(outputs);
			while (!availableOutputs.isEmpty() && transfer.getAmount() > 0) {
				int dividedTransfer = transfer.getAmount() / availableOutputs.size();
				int remainder = transfer.getAmount() % availableOutputs.size();

				for (Iterator<FluidNetworkEndpoint> iterator = availableOutputs.iterator(); iterator.hasNext();) {
					FluidNetworkEndpoint ne = iterator.next();
					int toTransfer = dividedTransfer;
					if (remainder > 0) {
						toTransfer++;
						remainder--;
					}

					if (transfer.isEmpty())
						break;
					IFluidHandler handler = ne.provideHandler()
						.orElse(null);
					if (handler == null) {
						iterator.remove();
						continue;
					}

					FluidStack divided = transfer.copy();
					divided.setAmount(toTransfer);
					int fill = handler.fill(divided, action);
					transfer.setAmount(transfer.getAmount() - fill);
					if (fill < toTransfer)
						iterator.remove();
				}

			}

			flowSpeed -= transfer.getAmount();
			transfer = FluidStack.EMPTY;
		}
	}

	public int getFluidTransferSpeed() {
		float rotationSpeed = Math.abs(getSpeed());
		int flowSpeed = (int) (rotationSpeed / 2f);
		if (rotationSpeed != 0 && flowSpeed == 0)
			flowSpeed = 1;
		return flowSpeed;
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putBoolean("Reversed", reversed);
		serializeOpenEnds(compound);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		reversed = compound.getBoolean("Reversed");
		deserializeOpenEnds(compound);
		super.read(compound, clientPacket);
	}

	public void updatePipesOnSide(Direction side) {
		if (!isSideAccessible(side))
			return;
		updatePipeNetwork(isFront(side));
	}

	protected boolean isFront(Direction side) {
		if (networks == null)
			return false;
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof PumpBlock))
			return false;
		Direction front = blockState.get(PumpBlock.FACING);
		boolean isFront = side == front;
		return isFront;
	}

	protected void updatePipeNetwork(boolean front) {
		if (networks != null)
			networks.get(front)
				.clearFlows(world, true);
		networksToUpdate.get(front)
			.setTrue();
		if (getSpeed() == 0 || (isPullingOnSide(front)) && networks != null)
			setProvidedFluid(FluidStack.EMPTY);
	}

	public boolean isSideAccessible(Direction side) {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof PumpBlock))
			return false;
		return blockState.get(PumpBlock.FACING)
			.getAxis() == side.getAxis();
	}

	public boolean isPullingOnSide(boolean front) {
		return front == reversed;
	}

	public Map<BlockFace, OpenEndedPipe> getOpenEnds(Direction side) {
		return openEnds.get(isFront(side));
	}

	private void serializeOpenEnds(CompoundNBT compound) {
		compound.put("OpenEnds", openEnds.serializeEach(m -> {
			CompoundNBT compoundNBT = new CompoundNBT();
			ListNBT entries = new ListNBT();
			m.entrySet()
				.forEach(e -> {
					CompoundNBT innerCompound = new CompoundNBT();
					innerCompound.put("Pos", e.getKey()
						.serializeNBT());
					e.getValue()
						.writeToNBT(innerCompound);
					entries.add(innerCompound);
				});
			compoundNBT.put("Entries", entries);
			return compoundNBT;
		}));
	}

	private void deserializeOpenEnds(CompoundNBT compound) {
		openEnds = Couple.deserializeEach(compound.getList("OpenEnds", NBT.TAG_COMPOUND), c -> {
			Map<BlockFace, OpenEndedPipe> map = new HashMap<>();
			NBTHelper.iterateCompoundList(c.getList("Entries", NBT.TAG_COMPOUND), innerCompound -> {
				BlockFace key = BlockFace.fromNBT(innerCompound.getCompound("Pos"));
				OpenEndedPipe value = new OpenEndedPipe(key);
				value.readNBT(innerCompound);
				map.put(key, value);
			});
			return map;
		});

		compound.put("OpenEnds", openEnds.serializeEach(m -> {
			CompoundNBT compoundNBT = new CompoundNBT();
			ListNBT entries = new ListNBT();
			m.entrySet()
				.forEach(e -> {
					CompoundNBT innerCompound = new CompoundNBT();
					innerCompound.put("Pos", e.getKey()
						.serializeNBT());
					e.getValue()
						.writeToNBT(innerCompound);
					entries.add(innerCompound);
				});
			compoundNBT.put("Entries", entries);
			return compoundNBT;
		}));
	}

	public void setProvidedFluid(FluidStack providedFluid) {
		this.providedFluid = providedFluid;
	}

	class PumpAttachmentBehaviour extends FluidPipeAttachmentBehaviour {

		public PumpAttachmentBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public boolean isPipeConnectedTowards(BlockState state, Direction direction) {
			return isSideAccessible(direction);
		}

		@Override
		public AttachmentTypes getAttachment(IBlockDisplayReader world, BlockPos pos, BlockState state, Direction direction) {
			AttachmentTypes attachment = super.getAttachment(world, pos, state, direction);
			if (attachment == AttachmentTypes.RIM)
				return AttachmentTypes.NONE;
			return attachment;
		}

	}

}
