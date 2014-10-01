package com.pahimar.ee3.client.renderer.tileentity;

import com.pahimar.ee3.alchemy.Symbol;
import com.pahimar.ee3.tileentity.TileEntityAlchemyArray;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class TileEntityRendererAlchemyArray extends TileEntitySpecialRenderer
{
    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float tick)
    {
        if (tileEntity instanceof TileEntityAlchemyArray)
        {
            TileEntityAlchemyArray tileEntityAlchemyArray = (TileEntityAlchemyArray) tileEntity;

            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glPushMatrix();
            for (Symbol symbol : tileEntityAlchemyArray.getSymbols())
            {
                this.bindTexture(symbol.getTexture());
                renderSymbol(symbol, x, y, z);
            }
            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_CULL_FACE);
        }
    }

    private void renderSymbol(Symbol symbol, double x, double y, double z)
    {
        Tessellator tessellator = Tessellator.instance;

        GL11.glPushMatrix();

        GL11.glTranslatef(0.5f - (symbol.getSize() / 2f), 0f, 0.5f - (symbol.getSize() / 2f));

        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x + symbol.getSize(), y + 0.001d, z + symbol.getSize(), 0, 0);
        tessellator.addVertexWithUV(x + symbol.getSize(), y + 0.001d, z, 0, 1);
        tessellator.addVertexWithUV(x, y + 0.001d, z, 1, 1);
        tessellator.addVertexWithUV(x, y + 0.001d, z + symbol.getSize(), 1, 0);
        tessellator.draw();
        GL11.glPopMatrix();
    }
}