package com.simibubi.create.content.kinetics.base.flwdata;

import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class BeltInstance extends KineticInstance {
    float qX;
    float qY;
    float qZ;
    float qW;
    float sourceU;
    float sourceV;
    float minU;
    float minV;
    float maxU;
    float maxV;
    byte scrollMult;

	protected BeltInstance(InstanceType<? extends KineticInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public BeltInstance setRotation(Quaternionf q) {
        this.qX = q.x();
        this.qY = q.y();
        this.qZ = q.z();
        this.qW = q.w();
        return this;
    }

    public BeltInstance setScrollTexture(SpriteShiftEntry spriteShift) {
        TextureAtlasSprite source = spriteShift.getOriginal();
        TextureAtlasSprite target = spriteShift.getTarget();

        this.sourceU = source.getU0();
        this.sourceV = source.getV0();
        this.minU = target.getU0();
        this.minV = target.getV0();
        this.maxU = target.getU1();
        this.maxV = target.getV1();

		return this;
	}

	public BeltInstance setScrollMult(float scrollMult) {
		this.scrollMult = (byte) (scrollMult * 127);
		return this;
	}
}
