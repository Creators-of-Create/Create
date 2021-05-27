package com.simibubi.create.content.contraptions.particle;

import javax.annotation.Nonnull;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.fan.IAirCurrentSource;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Blocks;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class AirFlowParticle extends SimpleAnimatedParticle {

	private final IAirCurrentSource source;

	protected AirFlowParticle(ClientWorld world, IAirCurrentSource source, double x, double y, double z,
							  IAnimatedSprite sprite) {
		super(world, x, y, z, sprite, world.rand.nextFloat() * .5f);
		this.source = source;
		this.particleScale *= 0.75F;
		this.maxAge = 40;
		canCollide = false;
		selectSprite(7);
		Vector3d offset = VecHelper.offsetRandomly(Vector3d.ZERO, Create.RANDOM, .25f);
		this.setPosition(posX + offset.x, posY + offset.y, posZ + offset.z);
		this.prevPosX = posX;
		this.prevPosY = posY;
		this.prevPosZ = posZ;
		setAlphaF(.25f);
	}

	@Nonnull
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		if (source == null || source.isSourceRemoved()) {
			dissipate();
			return;
		}
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if (this.age++ >= this.maxAge) {
			this.setExpired();
		} else {
			if (source.getAirCurrent() == null || !source.getAirCurrent().bounds.grow(.25f).contains(posX, posY, posZ)) {
				dissipate();
				return;
			}

			Vector3d directionVec = Vector3d.of(source.getAirCurrent().direction.getDirectionVec());
			Vector3d motion = directionVec.scale(1 / 8f);
			if (!source.getAirCurrent().pushing)
				motion = motion.scale(-1);

			double distance = new Vector3d(posX, posY, posZ).subtract(VecHelper.getCenterOf(source.getAirCurrentPos()))
					.mul(directionVec).length() - .5f;
			if (distance > source.getAirCurrent().maxDistance + 1 || distance < -.25f) {
				dissipate();
				return;
			}
			motion = motion.scale(source.getAirCurrent().maxDistance - (distance - 1f)).scale(.5f);
			selectSprite((int) MathHelper.clamp((distance / source.getAirCurrent().maxDistance) * 8 + world.rand.nextInt(4),
					0, 7));

			morphType(distance);

			motionX = motion.x;
			motionY = motion.y;
			motionZ = motion.z;

			if (this.onGround) {
				this.motionX *= 0.7;
				this.motionZ *= 0.7;
			}
			this.move(this.motionX, this.motionY, this.motionZ);

		}

	}

	public void morphType(double distance) {
		if(source.getAirCurrent() == null)
			return;
		InWorldProcessing.Type type = source.getAirCurrent().getSegmentAt((float) distance);

		if (type == InWorldProcessing.Type.SPLASHING) {
			setColor(ColorHelper.mixColors(0x4499FF, 0x2277FF, world.rand.nextFloat()));
			setAlphaF(1f);
			selectSprite(world.rand.nextInt(3));
			if (world.rand.nextFloat() < 1 / 32f)
				world.addParticle(ParticleTypes.BUBBLE, posX, posY, posZ, motionX * .125f, motionY * .125f,
						motionZ * .125f);
			if (world.rand.nextFloat() < 1 / 32f)
				world.addParticle(ParticleTypes.BUBBLE_POP, posX, posY, posZ, motionX * .125f, motionY * .125f,
						motionZ * .125f);
		}

		if (type == InWorldProcessing.Type.SMOKING) {
			setColor(ColorHelper.mixColors(0x0, 0x555555, world.rand.nextFloat()));
			setAlphaF(1f);
			selectSprite(world.rand.nextInt(3));
			if (world.rand.nextFloat() < 1 / 32f)
				world.addParticle(ParticleTypes.SMOKE, posX, posY, posZ, motionX * .125f, motionY * .125f,
						motionZ * .125f);
			if (world.rand.nextFloat() < 1 / 32f)
				world.addParticle(ParticleTypes.LARGE_SMOKE, posX, posY, posZ, motionX * .125f, motionY * .125f,
						motionZ * .125f);
		}

		if (type == InWorldProcessing.Type.BLASTING) {
			setColor(ColorHelper.mixColors(0xFF4400, 0xFF8855, world.rand.nextFloat()));
			setAlphaF(.5f);
			selectSprite(world.rand.nextInt(3));
			if (world.rand.nextFloat() < 1 / 32f)
				world.addParticle(ParticleTypes.FLAME, posX, posY, posZ, motionX * .25f, motionY * .25f,
						motionZ * .25f);
			if (world.rand.nextFloat() < 1 / 16f)
				world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, Blocks.LAVA.getDefaultState()), posX, posY,
						posZ, motionX * .25f, motionY * .25f, motionZ * .25f);
		}

		if (type == null) {
			setColor(0xEEEEEE);
			setAlphaF(.25f);
			setSize(.2f, .2f);
		}
	}

	private void dissipate() {
		setExpired();
	}

	public int getBrightnessForRender(float partialTick) {
		BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
		return this.world.isBlockPresent(blockpos) ? WorldRenderer.getLightmapCoordinates(world, blockpos) : 0;
	}

	private void selectSprite(int index) {
		setSprite(field_217584_C.get(index, 8));
	}

	public static class Factory implements IParticleFactory<AirFlowParticleData> {
		private final IAnimatedSprite spriteSet;

		public Factory(IAnimatedSprite animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		public Particle makeParticle(AirFlowParticleData data, ClientWorld worldIn, double x, double y, double z,
				double xSpeed, double ySpeed, double zSpeed) {
			TileEntity te = worldIn.getTileEntity(new BlockPos(data.posX, data.posY, data.posZ));
			if (!(te instanceof IAirCurrentSource))
				te = null;
			return new AirFlowParticle(worldIn, (IAirCurrentSource) te, x, y, z, this.spriteSet);
		}
	}

}
