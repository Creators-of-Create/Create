package com.simibubi.create.foundation.render.gl.shader;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShaderConstants implements GlShader.PreProcessor {

    private final ArrayList<String> defines;

    public ShaderConstants() {
        defines = new ArrayList<>();
    }

    public static ShaderConstants define(String def) {
        return new ShaderConstants().def(def);
    }

    public ShaderConstants def(String def) {
        defines.add(def);
        return this;
    }

    public ArrayList<String> getDefines() {
        return defines;
    }

    public Stream<String> directives() {
        return defines.stream().map(it -> "#define " + it);
    }

    @Override
    public String process(String source) {
        return new BufferedReader(new StringReader(source)).lines().flatMap(line -> {
            Stream<String> map = Stream.of(line);

            if (line.startsWith("#version")) {
                map = Stream.concat(map, directives());
            }

            return map;
        }).collect(Collectors.joining("\n"));
    }
}
