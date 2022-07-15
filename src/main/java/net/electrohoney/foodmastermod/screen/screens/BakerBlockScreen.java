package net.electrohoney.foodmastermod.screen.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import mezz.jei.api.gui.drawable.IDrawable;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.entity.custom.BakerBlockEntity;
import net.electrohoney.foodmastermod.block.entity.custom.PotBlockEntity;
import net.electrohoney.foodmastermod.screen.menus.BakerBlockMenu;
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

public class BakerBlockScreen extends AbstractContainerScreen<BakerBlockMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/baker_gui.png");
    //@todo overwrite image width to correct menu(did that, I should delete this todo)
    private static final int imageWidth = 176;
    private static final int imageHeight = 166+32;
    private FluidStackRenderer renderer;
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();

    private final IDrawable overlay = null;
    public BakerBlockScreen(BakerBlockMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init(){
        super.init();
        assignFluidRenderer();
    }

    private void assignFluidRenderer() {
        renderer = new FluidStackRenderer(BakerBlockEntity.BAKER_MAX_FLUID_CAPACITY, true, 12, 52);
    }


    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight)/ 2;
        //render all labels
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
        //render fluid tooltip
        if(isMouseAboveArea(pMouseX, pMouseY, x, y, 10, 30)) {
            renderTooltip(pPoseStack, renderer.getTooltip(menu.getFluid(), TooltipFlag.Default.NORMAL),
                    Optional.empty(),pMouseX - x, pMouseY - y);
        }
        //render temperature tooltip
        Component displayTemperature =
                new TranslatableComponent("foodmaster.tooltip.temperature.out.of.max", nf.format(menu.getTemperature()), nf.format(menu.getMaxTemperature()));
        if(isMouseAboveArea(pMouseX, pMouseY, x, y, 31, 26, 7, 54)) {
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
        blit(pPoseStack, x + 30, y + 29 + 53 - t, 176,  85-t,  7, t);

        int f = this.menu.getScaledBakeTime();
        blit(pPoseStack, x + 63, y+91 - f, 176, 13-f, 14 , f);

        if(menu.isCrafting()){
            //x and y of arrow, offset of arrow progress, vertical offset of progress, width of image, height of image
            blit(pPoseStack, x + 107, y + 48, 176, 14,  menu.getScaledProgress(), 16);
        }
        //32 is an y offset cause by the texture change
        renderer.render(pPoseStack, x+10, y+30, menu.getFluid());
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
