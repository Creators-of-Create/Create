package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.particle.AirParticleData;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.INameable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CopperBacktankTileEntity extends KineticTileEntity implements INameable {

	public int airLevel;
	public int airLevelTimer;
	private ITextComponent customName;

	public CopperBacktankTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
	}

	@Override
	public void tick() {
		super.tick();
		if (getSpeed() == 0)
			return;
		if (airLevelTimer > 0) {
			airLevelTimer--;
			return;
		}

		int max = getMaxAir();
		if (world.isRemote) {
			Vector3d centerOf = VecHelper.getCenterOf(pos);
			Vector3d v = VecHelper.offsetRandomly(centerOf, Create.RANDOM, .65f);
			Vector3d m = centerOf.subtract(v);
			if (airLevel != max)
				world.addParticle(new AirParticleData(1, .05f), v.x, v.y, v.z, m.x, m.y, m.z);
			return;
		}

		if (airLevel == max)
			return;

		float abs = Math.abs(getSpeed());
		int increment = MathHelper.clamp(((int) abs - 100) / 20, 1, 5);
		airLevel = Math.min(max, airLevel + increment);
		if (airLevel == max)
			sendData();
		airLevelTimer = MathHelper.clamp((int) (128f - abs / 5f) - 108, 0, 20);
	}

	protected int getMaxAir() {
		return AllConfigs.SERVER.curiosities.maxAirInBacktank.get();
	}

	public int getAirLevel() {
		return airLevel;
	}

	public void setAirLevel(int airLevel) {
		this.airLevel = airLevel;
		sendData();
	}

	public void setCustomName(ITextComponent customName) {
		this.customName = customName;
	}

	public ITextComponent getCustomName() {
		return customName;
	}

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("Air", airLevel);
		compound.putInt("Timer", airLevelTimer);
		if (this.customName != null)
			compound.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		int prev = airLevel;
		airLevel = compound.getInt("Air");
		airLevelTimer = compound.getInt("Timer");
		if (compound.contains("CustomName", 8))
			this.customName = ITextComponent.Serializer.fromJson(compound.getString("CustomName"));
		if (prev != 0 && prev != airLevel && airLevel == getMaxAir() && clientPacket)
			playFilledEffect();
	}

	protected void playFilledEffect() {
		AllSoundEvents.CONFIRM.playAt(world, pos, 0.4f, 1, true);
		Vector3d baseMotion = new Vector3d(.25, 0.1, 0);
		Vector3d baseVec = VecHelper.getCenterOf(pos);
		for (int i = 0; i < 360; i += 10) {
			Vector3d m = VecHelper.rotate(baseMotion, i, Axis.Y);
			Vector3d v = baseVec.add(m.normalize()
				.scale(.25f));

			world.addParticle(ParticleTypes.SPIT, v.x, v.y, v.z, m.x, m.y, m.z);
		}
	}

	@Override
	public ITextComponent getName() {
		return this.customName != null ? this.customName
			: new TranslationTextComponent(AllItems.COPPER_BACKTANK.get()
				.getTranslationKey());
	}

	@Override
	public boolean shouldRenderNormally() {
		return true;
	}

}
