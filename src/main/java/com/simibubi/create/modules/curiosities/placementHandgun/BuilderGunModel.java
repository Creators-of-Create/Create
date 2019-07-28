package com.simibubi.create.modules.curiosities.placementHandgun;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.block.CustomRenderItemBakedModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraftforge.client.event.ModelBakeEvent;

@SuppressWarnings("deprecation")
public class BuilderGunModel extends CustomRenderItemBakedModel {

	public IBakedModel rod;
	public IBakedModel body;
	public boolean showBlock;
	
	public IBakedModel goldBody;
	public IBakedModel goldScope;
	public IBakedModel goldAmp;
	public IBakedModel goldAmpCore;
	public IBakedModel goldRetriever;
	public IBakedModel goldAcc;
	public IBakedModel goldAccCore;
	
	public IBakedModel chorusBody;
	public IBakedModel chorusScope;
	public IBakedModel chorusAmp;
	public IBakedModel chorusAmpCore;
	public IBakedModel chorusRetriever;
	public IBakedModel chorusAcc;
	public IBakedModel chorusAccCore;
	
	public BuilderGunModel(IBakedModel template) {
		super(template);
	}
	
	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
		showBlock = cameraTransformType == TransformType.GUI;
		return super.handlePerspective(cameraTransformType);
	}

	@Override
	public CustomRenderItemBakedModel loadPartials(ModelBakeEvent event) {
		String p = "placement_handgun/";
		
		this.rod = loadCustomModel(event, p + "core");
		this.body = loadCustomModel(event, p + "body");
		
		this.goldBody = loadCustomModel(event, p + "gold_body");
		this.goldScope = loadCustomModel(event, p + "gold_scope");
		this.goldAmp = loadCustomModel(event, p + "gold_amplifier");
		this.goldAmpCore = loadCustomModel(event, p + "gold_amplifier_core");
		this.goldRetriever = loadCustomModel(event, p + "gold_retriever");
		this.goldAcc = loadCustomModel(event, p + "gold_accelerator");
		this.goldAccCore = loadCustomModel(event, p + "gold_accelerator_core");
		
		this.chorusBody = loadCustomModel(event, p + "chorus_body");
		this.chorusScope = loadCustomModel(event, p + "chorus_scope");
		this.chorusAmp = loadCustomModel(event, p + "chorus_amplifier");
		this.chorusAmpCore = loadCustomModel(event, p + "chorus_amplifier_core");
		this.chorusRetriever = loadCustomModel(event, p + "chorus_retriever");
		this.chorusAcc = loadCustomModel(event, p + "chorus_accelerator");
		this.chorusAccCore = loadCustomModel(event, p + "chorus_accelerator_core");
		
		return this;
	}

}
