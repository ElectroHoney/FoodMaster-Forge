package net.electrohoney.foodmastermod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.electrohoney.foodmastermod.FoodMaster;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PotBlockScreen extends AbstractContainerScreen<PotBlockMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/pot_block_gui.png");
    //@todo overwrite image width to correct menu
    private static final int imageWidth = 194;

    public PotBlockScreen(PotBlockMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        this.blit(pPoseStack, x, y, 0, 0, imageWidth, imageHeight);
        int t = this.menu.getScaledTemperature();
        //System.out.print("Temp:" + t + "\n");
        //todo this is good but for some reason the bar is too low
        blit(pPoseStack, x + 35, y + 16 + 53 - t, 195,  70-t,  7, t);


        if(menu.isCrafting()){
            //todo remember to add all progress bars!
//          //x and y of arrow, offset of arrow progress, vertical offset of progress, width of image, height of image
            blit(pPoseStack, x + 110, y + 35, 195, 0,  menu.getScaledProgress(), 16);
        }
    }

    @Override
    public void render(PoseStack pPoseStack, int mouseX, int mouseY, float delta) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, mouseX, mouseY, delta);
        renderTooltip(pPoseStack, mouseX, mouseY);
    }
}
