package net.electrohoney.foodmastermod.screen.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import net.electrohoney.foodmastermod.FoodMaster;
import net.electrohoney.foodmastermod.block.entity.custom.DistillerBlockEntity;
import net.electrohoney.foodmastermod.block.entity.custom.FermenterBlockEntity;
import net.electrohoney.foodmastermod.screen.menus.DistillerBlockMenu;
import net.electrohoney.foodmastermod.screen.menus.FermenterBlockMenu;
import net.electrohoney.foodmastermod.screen.renderer.FluidStackRenderer;
import net.electrohoney.foodmastermod.util.MouseUtil;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.TooltipFlag;

import java.text.NumberFormat;
import java.util.Optional;

public class FermenterBlockScreen extends AbstractContainerScreen<FermenterBlockMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FoodMaster.MOD_ID, "textures/gui/fermenter_gui.png");
    private static final int imageWidth = 194;
    private static final int imageHeight = 166;
    private FluidStackRenderer rendererInput, rendererOutput;
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();


    private final IDrawable overlay = null;
    public FermenterBlockScreen(FermenterBlockMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init(){
        super.init();
        assignFluidRenderer();
    }

    private void assignFluidRenderer() {
        rendererInput = new FluidStackRenderer(FermenterBlockEntity.FERMENTER_MAX_FLUID_CAPACITY, true, 12, 52);
        rendererOutput = new FluidStackRenderer(FermenterBlockEntity.FERMENTER_RESULT_FLUID_CAPACITY, true, 12, 52);
    }


    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight)/ 2;
        //render all labels
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
        //render input fluid tooltip
        if(isMouseAboveArea(pMouseX, pMouseY, x, y, 32, 17, rendererInput.getWidth(), rendererInput.getHeight())) {
            renderTooltip(pPoseStack, rendererInput.getTooltip(menu.getInputFluid(), TooltipFlag.Default.NORMAL),
                    Optional.empty(),pMouseX - x, pMouseY - y);
        }
        if(isMouseAboveArea(pMouseX, pMouseY, x, y, 141, 17, rendererOutput.getWidth(), rendererOutput.getHeight())) {
            renderTooltip(pPoseStack, rendererOutput.getTooltip(menu.getOutputFluid(), TooltipFlag.Default.NORMAL),
                    Optional.empty(),pMouseX - x, pMouseY - y);
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

        if(menu.isCrafting()){
            //todo remember to add all progress bars! I did them! ha!
//          //x and y of arrow, offset of arrow progress, vertical offset of progress, width of image, height of image
            blit(pPoseStack, x + 110, y + 30, 195, 0,  menu.getScaledProgress(), 21);
        }
        rendererInput.render(pPoseStack, x+32, y+17,menu.getInputFluid());
        rendererOutput.render(pPoseStack, x+141, y+17,menu.getOutputFluid());
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
