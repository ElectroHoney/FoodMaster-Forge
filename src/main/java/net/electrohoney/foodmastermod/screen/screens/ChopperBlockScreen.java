package net.electrohoney.foodmastermod.screen.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mezz.jei.api.gui.drawable.IDrawable;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.screen.menus.ChopperBlockMenu;
import net.electrohoney.foodmastermod.screen.renderer.FluidStackRenderer;
import net.electrohoney.foodmastermod.util.MouseUtil;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.text.NumberFormat;

public class ChopperBlockScreen extends AbstractContainerScreen<ChopperBlockMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/chopper_gui.png");
    private FluidStackRenderer renderer;
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();

    private final int imageWidth = 175;

    private final IDrawable overlay = null;
    public ChopperBlockScreen(ChopperBlockMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init(){
        super.init();
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight)/ 2;
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        this.blit(pPoseStack, x, y, 0, 0, imageWidth, imageHeight);

        if(menu.isCrafting()){
           switch (menu.getScaledProgress() % 2){
               case 0 :
                   blit(pPoseStack, x + 91, y + 40, 174, 0,  18, 18);
                   break;
               case 1:
                   blit(pPoseStack, x + 91, y + 40, 192, 0,  18, 18);

           }
        }

    }


    private boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, int areaWidth, int areaHeight) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, areaWidth, areaHeight);
    }

    @Override
    public void render(PoseStack pPoseStack, int mouseX, int mouseY, float delta) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, mouseX, mouseY, delta);
        renderTooltip(pPoseStack, mouseX, mouseY);
    }
}
