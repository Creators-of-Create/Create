package com.simibubi.create.content.contraptions.particle;

import javax.annotation.Nonnull;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.fan.IAirCurrentSource;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.content.contraptions.processing.fan.AbstractFanProcessingType;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class AirFlowParticle extends SimpleAnimatedParticle {

	private final IAirCurrentSource source;

	protected AirFlowParticle(ClientLevel world, IAirCurrentSource source, double x, double y, double z,
							  SpriteSet sprite) {
		super(world, x, y, z, sprite, world.random.nextFloat() * .5f);
		this.source = source;
		this.quadSize *= 0.75F;
		this.lifetime = 40;
		hasPhysics = false;
		selectSprite(7);
		Vec3 offset = VecHelper.offsetRandomly(Vec3.ZERO, Create.RANDOM, .25f);
		this.setPos(x + offset.x, y + offset.y, z + offset.z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
		setAlpha(.25f);
	}

	@Nonnull
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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

			Vec3 directionVec = Vec3.atLowerCornerOf(source.getAirCurrent().direction.getNormal());
			Vec3 motion = directionVec.scale(1 / 8f);
			if (!source.getAirCurrent().pushing)
				motion = motion.scale(-1);

			double distance = new Vec3(x, y, z).subtract(VecHelper.getCenterOf(source.getAirCurrentPos()))
					.multiply(directionVec).length() - .5f;
			if (distance > source.getAirCurrent().maxDistance + 1 || distance < -.25f) {
				dissipate();
				return;
			}
			motion = motion.scale(source.getAirCurrent().maxDistance - (distance - 1f)).scale(.5f);
			selectSprite((int) Mth.clamp((distance / source.getAirCurrent().maxDistance) * 8 + level.random.nextInt(4),
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

	public void setProperties(int color_1, int color_2, float alpha, int sprite_length) {
		setColor(Color.mixColors(color_1, color_2, level.random.nextFloat()));
		setAlpha(alpha);
		selectSprite(level.random.nextInt(sprite_length));
	}

	public void addParticle(ParticleOptions option, float chance, float speed) {
		if (level.random.nextFloat() < chance) {
			level.addParticle(option, x, y, z, xd * speed, yd * speed, zd * speed);
		}
	}

	public void morphType(double distance) {
		if (source.getAirCurrent() == null)
			return;
		AbstractFanProcessingType type = source.getAirCurrent().getSegmentAt((float) distance);
		if (type != null) type.morphType(this);
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
		return this.level.isLoaded(blockpos) ? LevelRenderer.getLightColor(level, blockpos) : 0;
	}

	private void selectSprite(int index) {
		setSprite(sprites.get(index, 8));
	}

	public static class Factory implements ParticleProvider<AirFlowParticleData> {
		private final SpriteSet spriteSet;

		public Factory(SpriteSet animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		public Particle createParticle(AirFlowParticleData data, ClientLevel worldIn, double x, double y, double z,
									   double xSpeed, double ySpeed, double zSpeed) {
			BlockEntity te = worldIn.getBlockEntity(new BlockPos(data.posX, data.posY, data.posZ));
			if (!(te instanceof IAirCurrentSource))
				te = null;
			return new AirFlowParticle(worldIn, (IAirCurrentSource) te, x, y, z, this.spriteSet);
		}
	}

}
