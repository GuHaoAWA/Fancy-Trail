package com.guhao.fancy_trail.register;


import com.guhao.fancy_trail.client.shaderpasses.*;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class FTPostPasses {

    public static PostPassBase blit;
    public static Blur blur;
    public static PostPassBase composite;
    public static SpaceBroken space_broken;
    public static DepthCull depth_cull;
    public static DownSampling downSampler;
    public static UpSampling upSampler;
    public static UnityComposite unity_composite;
    public static UEComposite ue_composite;


    public static MaskComposite mask_composite;

    public static AirDisturbance air_disturbance;
    public static ChromaticAberration chromatic_aberration;
    public static RGBTrail rgb_trail;
    public static StarrySwordTrail starry_sword_trail;
    public static void register(RegisterShadersEvent event) {
        try {
            System.out.println("Load Shader");
            ResourceManager rm = Minecraft.getInstance().getResourceManager();
            composite = new PostPassBase("fancy_trail:composite", rm);
            blit = new PostPassBase("fancy_trail:blit", rm);
            downSampler = new DownSampling("fancy_trail:down_sampling", rm);
            upSampler = new UpSampling("fancy_trail:up_sampling", rm);
            unity_composite = new UnityComposite("fancy_trail:unity_composite", rm);
            ue_composite = new UEComposite("fancy_trail:ue_composite", rm);

            space_broken = new SpaceBroken("fancy_trail:space_broken", rm);
            depth_cull = new DepthCull("fancy_trail:depth_cull", rm);

            mask_composite = new MaskComposite("fancy_trail:mask_composite", rm);

            blur = new Blur(rm);

            air_disturbance = new AirDisturbance("fancy_trail:air_disturbance", rm);
            chromatic_aberration = new ChromaticAberration("fancy_trail:chromatic_aberration", rm);
            rgb_trail = new RGBTrail("fancy_trail:rgb_trail", rm);
            starry_sword_trail = new StarrySwordTrail("fancy_trail:starry_sword_trail", rm);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
