package com.simibubi.create.content.contraptions.particle;

import javax.annotation.Nonnull;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.fan.IAirCurrentSource;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.foundation.utility.Color;
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
		super(world, x, y, z, sprite, world.random.nextFloat() * .5f);
		this.source = source;
		this.quadSize *= 0.75F;
		this.lifetime = 40;
		hasPhysics = false;
		selectSprite(7);
		Vector3d offset = VecHelper.offsetRandomly(Vector3d.ZERO, Create.RANDOM, .25f);
		this.setPos(x + offset.x, y + offset.y, z + offset.z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
		setAlpha(.25f);
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
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			if (source.getAirCurrent() == null || !source.getAirCurrent().bounds.inflate(.25f).contains(x, y, z)) {
				dissipate();
				return;
			}

			Vector3d directionVec = Vector3d.atLowerCornerOf(source.getAirCurrent().direction.getNormal());
			Vector3d motion = directionVec.scale(1 / 8f);
			if (!source.getAirCurrent().pushing)
				motion = motion.scale(-1);

			double distance = new Vector3d(x, y, z).subtract(VecHelper.getCenterOf(source.getAirCurrentPos()))
					.multiply(directionVec).length() - .5f;
			if (distance > source.getAirCurrent().maxDistance + 1 || distance < -.25f) {
				dissipate();
				return;
			}
			motion = motion.scale(source.getAirCurrent().maxDistance - (distance - 1f)).scale(.5f);
			selectSprite((int) MathHelper.clamp((distance / source.getAirCurrent().maxDistance) * 8 + level.random.nextInt(4),
					0, 7));

			morphType(distance);

			xd = motion.x;
			yd = motion.y;
			zd = motion.z;

			if (this.onGround) {
				this.xd *= 0.7;
				this.zd *= 0.7;
			}
			this.move(this.xd, this.yd, this.zd);

		}

	}

	public void morphType(double distance) {
		if(source.getAirCurrent() == null)
			return;
		InWorldProcessing.Type type = source.getAirCurrent().getSegmentAt((float) distance);

		if (type == InWorldProcessing.Type.SPLASHING) {
			setColor(Color.mixColors(0x4499FF, 0x2277FF, level.random.nextFloat()));
			setAlpha(1f);
			selectSprite(level.random.nextInt(3));
			if (level.random.nextFloat() < 1 / 32f)
				level.addParticle(ParticleTypes.BUBBLE, x, y, z, xd * .125f, yd * .125f,
						zd * .125f);
			if (level.random.nextFloat() < 1 / 32f)
				level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, xd * .125f, yd * .125f,
						zd * .125f);
		}

		if (type == InWorldProcessing.Type.SMOKING) {
			setColor(Color.mixColors(0x0, 0x555555, level.random.nextFloat()));
			setAlpha(1f);
			selectSprite(level.random.nextInt(3));
			if (level.random.nextFloat() < 1 / 32f)
				level.addParticle(ParticleTypes.SMOKE, x, y, z, xd * .125f, yd * .125f,
						zd * .125f);
			if (level.random.nextFloat() < 1 / 32f)
				level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, xd * .125f, yd * .125f,
						zd * .125f);
		}

		if (type == InWorldProcessing.Type.BLASTING) {
			setColor(Color.mixColors(0xFF4400, 0xFF8855, level.random.nextFloat()));
			setAlpha(.5f);
			selectSprite(level.random.nextInt(3));
			if (level.random.nextFloat() < 1 / 32f)
				level.addParticle(ParticleTypes.FLAME, x, y, z, xd * .25f, yd * .25f,
						zd * .25f);
			if (level.random.nextFloat() < 1 / 16f)
				level.addParticle(new BlockParticleData(ParticleTypes.BLOCK, Blocks.LAVA.defaultBlockState()), x, y,
						z, xd * .25f, yd * .25f, zd * .25f);
		}

		if (type == null) {
			setColor(0xEEEEEE);
			setAlpha(.25f);
			setSize(.2f, .2f);
		}
	}

	private void dissipate() {
		remove();
	}

	public int getLightColor(float partialTick) {
		BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
		return this.level.isLoaded(blockpos) ? WorldRenderer.getLightColor(level, blockpos) : 0;
	}

	private void selectSprite(int index) {
		setSprite(sprites.get(index, 8));
	}

	public static class Factory implements IParticleFactory<AirFlowParticleData> {
		private final IAnimatedSprite spriteSet;

		public Factory(IAnimatedSprite animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		public Particle createParticle(AirFlowParticleData data, ClientWorld worldIn, double x, double y, double z,
				double xSpeed, double ySpeed, double zSpeed) {
			TileEntity te = worldIn.getBlockEntity(new BlockPos(data.posX, data.posY, data.posZ));
			if (!(te instanceof IAirCurrentSource))
				te = null;
			return new AirFlowParticle(worldIn, (IAirCurrentSource) te, x, y, z, this.spriteSet);
		}
	}

}
