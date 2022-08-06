package net.electrohoney.foodmastermod.screen.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import mezz.jei.api.gui.drawable.IDrawable;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.entity.custom.FryerBlockEntity;
import net.electrohoney.foodmastermod.block.entity.custom.PotBlockEntity;
import net.electrohoney.foodmastermod.screen.menus.FryerBlockMenu;
import net.electrohoney.foodmastermod.screen.menus.PotBlockMenu;
import net.electrohoney.foodmastermod.screen.renderer.FluidStackRenderer;
import net.electrohoney.foodmastermod.util.MouseUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import java.text.NumberFormat;
import java.util.Optional;

public class FryerBlockScreen extends AbstractContainerScreen<FryerBlockMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/fryer_gui.png");
    //@todo overwrite image width to correct menu(did that, I should delete this todo)
    private static final int imageWidth = 194;
    private static final int imageHeight = 166;
    private FluidStackRenderer renderer;
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();


    private final IDrawable overlay = null;
    public FryerBlockScreen(FryerBlockMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init(){
        super.init();
        assignFluidRenderer();
    }

    private void assignFluidRenderer() {
        renderer = new FluidStackRenderer(FryerBlockEntity.FRYER_MAX_FLUID_CAPACITY, true, 12, 52);
    }


    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight)/ 2;
        //render all labels
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
        //render fluid tooltip
        if(isMouseAboveArea(pMouseX, pMouseY, x, y, 10, 35/2)) {
            renderTooltip(pPoseStack, renderer.getTooltip(menu.getFluid(), TooltipFlag.Default.NORMAL),
                    Optional.empty(),pMouseX - x, pMouseY - y);
        }
        //render temperature tooltip
        Component displayTemperature =
                new TranslatableComponent("foodmaster.tooltip.temperature.out.of.max", nf.format(menu.getTemperature()), nf.format(menu.getMaxTemperature()));
        if(isMouseAboveArea(pMouseX, pMouseY, x, y, 36, 35/2, 7, 54)) {
            renderTooltip(pPoseStack,displayTemperature, pMouseX-x, pMouseY-y);
        }

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
        //todo this is good but for some reason the bar is too low(nope its perfect)(I looked at how the furnace was drawing the flame)
        blit(pPoseStack, x + 34, y + 16 + 53 - t, 195,  70-t,  7, t);


        if(menu.isCrafting()){
            //todo remember to add all progress bars! I did them! ha!
//          //x and y of arrow, offset of arrow progress, vertical offset of progress, width of image, height of image
            blit(pPoseStack, x + 123, y + 20, 195, 0,  menu.getScaledProgress(), 16);
        }
        renderer.render(pPoseStack, x+10, y+17, menu.getFluid());
    }

    private boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, renderer.getWidth(), renderer.getHeight());
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
