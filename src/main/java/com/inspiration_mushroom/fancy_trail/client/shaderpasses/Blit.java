package com.inspiration_mushroom.fancy_trail.client.shaderpasses;

import com.guhao.vix.client.shaderpasses.PostPassBase;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class Blit extends PostPassBase {
    public Blit(ResourceManager rsmgr) throws IOException {
        super(new EffectInstance(rsmgr, "fancy_trail:blit"));
    }

}
