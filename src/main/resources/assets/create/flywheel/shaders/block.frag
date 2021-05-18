#flwbuiltins

#flwinclude <"create:data/blockfragment.glsl">

void FLWMain(BlockFrag r) {
    vec4 tex = FLWBlockTexture(r.texCoords);

    vec4 color = vec4(tex.rgb * FLWLight(r.light).rgb * r.diffuse, tex.a) * r.color;

    FLWFinalizeColor(color);
}
