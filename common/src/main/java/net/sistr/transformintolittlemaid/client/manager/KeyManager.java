package net.sistr.transformintolittlemaid.client.manager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.LiteralText;
import net.sistr.littlemaidmodelloader.client.screen.ModelSelectScreen;
import net.sistr.transformintolittlemaid.TransformIntoLittleMaidMod;
import net.sistr.transformintolittlemaid.network.TransformLittleMaidPacket;
import net.sistr.transformintolittlemaid.util.LittleMaidTransformable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class KeyManager {
    public static final KeyBinding TRANSFORM = new KeyBinding(
            TransformIntoLittleMaidMod.MODID + ".key.transform",
            GLFW.GLFW_KEY_APOSTROPHE,
            "key.categories." + TransformIntoLittleMaidMod.MODID);
    public static final KeyBinding SELECT_MODEL = new KeyBinding(
            TransformIntoLittleMaidMod.MODID + ".key.select_model",
            GLFW.GLFW_KEY_LEFT_BRACKET,
            "key.categories." + TransformIntoLittleMaidMod.MODID);

    public static void keyTick() {
        if (TRANSFORM.wasPressed()) pressTransform();
        if (SELECT_MODEL.wasPressed()) pressKeySelect();
    }

    private static void pressTransform() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != null && mc.player != null) {
            boolean changed = !((LittleMaidTransformable) mc.player).isTransformedLittleMaid_TLM();
            ((LittleMaidTransformable) mc.player).setTransformedLittleMaid_TLM(changed);
            TransformLittleMaidPacket.sendC2SPacket(changed);
            mc.player.calculateDimensions();
        }

    }

    private static void pressKeySelect() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != null && mc.player != null)
            mc.openScreen(new ModelSelectScreen(new LiteralText(""), mc.world, mc.player));
    }

}
