package com.simibubi.create.content.contraptions.fluids.actors;

import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.PASS;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.fluids.FluidFullnessOverlay;
import com.simibubi.create.content.contraptions.fluids.FluidFX;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class SpoutTileEntity extends SmartTileEntity implements IHaveGoggleInformation {

	public static final int FILLING_TIME = 20;

	protected BeltProcessingBehaviour beltProcessing;
	protected int processingTicks;
	protected boolean sendSplash;
	protected int lastRedstoneLevel;

	SmartFluidTankBehaviour tank;

	public SpoutTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		processingTicks = -1;
	}

	protected AxisAlignedBB cachedBoundingBox;
	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if (cachedBoundingBox == null) {
			cachedBoundingBox = super.getRenderBoundingBox().expand(0, -2, 0);
		}
		return cachedBoundingBox;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		tank = SmartFluidTankBehaviour.single(this, 1000);
		behaviours.add(tank);

		beltProcessing = new BeltProcessingBehaviour(this).whenItemEnters(this::onItemReceived)
			.whileItemHeld(this::whenItemHeld);
		behaviours.add(beltProcessing);

	}

	protected ProcessingResult onItemReceived(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler) {
		if (!FillingBySpout.canItemBeFilled(world, transported.stack))
			return PASS;
		if (tank.isEmpty())
			return HOLD;
		if (FillingBySpout.getRequiredAmountForItem(world, transported.stack, getCurrentFluidInTank()) == -1)
			return PASS;
		return HOLD;
	}

	protected ProcessingResult whenItemHeld(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler) {
		if (processingTicks != -1 && processingTicks != 5)
			return HOLD;
		if (!FillingBySpout.canItemBeFilled(world, transported.stack))
			return PASS;
		if (tank.isEmpty())
			return HOLD;
		FluidStack fluid = getCurrentFluidInTank();
		int requiredAmountForItem = FillingBySpout.getRequiredAmountForItem(world, transported.stack, fluid.copy());
		if (requiredAmountForItem == -1)
			return PASS;
		if (requiredAmountForItem > fluid.getAmount())
			return HOLD;

		if (processingTicks == -1) {
			processingTicks = FILLING_TIME;
			notifyUpdate();
			return HOLD;
		}

		// Process finished
		ItemStack out = FillingBySpout.fillItem(world, requiredAmountForItem, transported.stack, fluid);
		if (!out.isEmpty()) {
			List<TransportedItemStack> outList = new ArrayList<>();
			TransportedItemStack held = null;
			TransportedItemStack result = transported.copy();
			result.stack = out;
			if (!transported.stack.isEmpty())
				held = transported.copy();
			outList.add(result);
			handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(outList, held));
		}

		AllTriggers.triggerForNearbyPlayers(AllTriggers.SPOUT, world, pos, 5);
		if (out.getItem() instanceof PotionItem && !PotionUtils.getEffectsFromStack(out).isEmpty())
			AllTriggers.triggerForNearbyPlayers(AllTriggers.SPOUT_POTION, world, pos, 5);
		
		tank.getPrimaryHandler()
			.setFluid(fluid);
		sendSplash = true;
		notifyUpdate();
		return HOLD;
	}

	private FluidStack getCurrentFluidInTank() {
		return tank.getPrimaryHandler()
			.getFluid();
	}

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);

		compound.putInt("ProcessingTicks", processingTicks);
		if (sendSplash && clientPacket) {
			compound.putBoolean("Splash", true);
			sendSplash = false;
		}
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		processingTicks = compound.getInt("ProcessingTicks");
		if (!clientPacket)
			return;
		if (compound.contains("Splash"))
			spawnSplash(tank.getPrimaryTank()
				.getRenderedFluid());
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side != Direction.DOWN)
			return tank.getCapability()
				.cast();
		return super.getCapability(cap, side);
	}

	public void tick() {
		super.tick();
		if (processingTicks >= 0)
			processingTicks--;
		if (processingTicks >= 8 && world.isRemote)
			spawnProcessingParticles(tank.getPrimaryTank()
				.getRenderedFluid());

		if (lastRedstoneLevel != getComparatorOutput()) {
			lastRedstoneLevel = getComparatorOutput();
			if (world != null)
				world.updateComparatorOutputLevel(getPos(), getBlockState().getBlock());
		}
	}

	protected void spawnProcessingParticles(FluidStack fluid) {
		Vec3d vec = VecHelper.getCenterOf(pos);
		vec = vec.subtract(0, 8 / 16f, 0);
		IParticleData particle = FluidFX.getFluidParticle(fluid);
		world.addOptionalParticle(particle, vec.x, vec.y, vec.z, 0, -.1f, 0);
	}

	protected static int SPLASH_PARTICLE_COUNT = 20;

	protected void spawnSplash(FluidStack fluid) {
		Vec3d vec = VecHelper.getCenterOf(pos);
		vec = vec.subtract(0, 2 - 5 / 16f, 0);
		IParticleData particle = FluidFX.getFluidParticle(fluid);
		for (int i = 0; i < SPLASH_PARTICLE_COUNT; i++) {
			Vec3d m = VecHelper.offsetRandomly(Vec3d.ZERO, world.rand, 0.125f);
			m = new Vec3d(m.x, Math.abs(m.y), m.z);
			world.addOptionalParticle(particle, vec.x, vec.y, vec.z, m.x, m.y, m.z);
		}
	}

	public int getComparatorOutput() {
		SpoutTileEntity te = this;
		double fillFraction = (double) te.getCurrentFluidInTank().getAmount() / te.tank.getPrimaryHandler().getCapacity();
		return MathHelper.floor(MathHelper.clamp(fillFraction * 14 + (fillFraction > 0 ? 1  : 0), 0, 15));
	}

	@Override
	public boolean addToGoggleTooltip(List<String> tooltip, boolean isPlayerSneaking) {
		SpoutTileEntity te = this;

		int fluidAmount = te.tank.getPrimaryHandler().getFluidAmount();
		int fluidCapacity = te.tank.getPrimaryHandler().getCapacity();
		double fillFraction = (double) fluidAmount / fluidCapacity;
		FluidStack fluidType = te.tank.getPrimaryHandler().getFluid();

		tooltip.add(spacing + Lang.translate("gui.spout.info_header"));

		if (isPlayerSneaking && AllFluids.POTION.get().getFluid().isEquivalentTo(fluidType.getFluid())) {
			tooltip.add(spacing + TextFormatting.GRAY + Lang.translate("gui.stores_fluid.effectsTitle"));

			ArrayList<ITextComponent> potionTooltip = new ArrayList<>();
			PotionFluidHandler.addPotionTooltip(fluidType, potionTooltip, 1);
			tooltip.addAll(2, potionTooltip.stream()
					.map(c -> spacing + " " + c.getFormattedText())
					.collect(Collectors.toList()));
			return true;
		}

		tooltip.add(spacing + TextFormatting.GRAY + Lang.translate("gui.stores_fluid.title"));

		if (fluidAmount != 0)
			tooltip.add(spacing + " " + FluidFullnessOverlay.getFormattedFluidTypeText(fluidType, fillFraction));

		tooltip.add(spacing + FluidFullnessOverlay.getFormattedFullnessText(fillFraction));
		tooltip.add(spacing + " " + FluidFullnessOverlay.getFormattedCapacityText(fluidAmount, fluidCapacity));

		return true;
	}

}
