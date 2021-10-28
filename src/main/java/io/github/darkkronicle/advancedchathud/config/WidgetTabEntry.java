/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.config;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.util.ColorUtil;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

/*
   This class is based heavily off of https://github.com/maruohon/minihud/blob/d565d39c68bdcd3ed1e1cf2007491e03d9659f34/src/main/java/fi/dy/masa/minihud/gui/widgets/WidgetShapeEntry.java#L19 which is off the GNU LGPL
*/
@Environment(EnvType.CLIENT)
public class WidgetTabEntry extends WidgetListEntryBase<ChatTab> {

    private final WidgetListTabs parent;
    private final boolean isOdd;
    private final List<String> hoverLines;
    private final int buttonStartX;
    private final ChatTab tab;
    private boolean main = true;

    public WidgetTabEntry(
            int x,
            int y,
            int width,
            int height,
            boolean isOdd,
            ChatTab tab,
            int listIndex,
            WidgetListTabs parent) {
        super(x, y, width, height, tab, listIndex);
        this.parent = parent;
        this.isOdd = isOdd;
        this.hoverLines = tab.getWidgetHoverLines();
        this.tab = tab;

        y += 1;
        int pos = x + width - 2;

        if (listIndex != 0) {
            main = false;
            // If it's 0 it's the main tab
            pos -= addButton(pos, y, ButtonListener.Type.REMOVE);
        }
        pos -= addButton(pos, y, ButtonListener.Type.CONFIGURE);

        buttonStartX = pos;
    }

    protected int addButton(int x, int y, ButtonListener.Type type) {
        ButtonGeneric button = new ButtonGeneric(x, y, -1, true, type.getDisplayName());
        this.addButton(button, new ButtonListener(type, this));

        return button.getWidth() + 1;
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, MatrixStack matrixStack) {
        RenderUtils.color(1f, 1f, 1f, 1f);

        // Draw a lighter background for the hovered and the selected entry
        if (selected || this.isMouseOver(mouseX, mouseY)) {
            RenderUtils.drawRect(
                    this.x,
                    this.y,
                    this.width,
                    this.height,
                    ColorUtil.WHITE.withAlpha(150).color());
        } else if (this.isOdd) {
            RenderUtils.drawRect(
                    this.x, this.y, this.width, this.height, ColorUtil.WHITE.withAlpha(70).color());
        } else {
            RenderUtils.drawRect(
                    this.x, this.y, this.width, this.height, ColorUtil.WHITE.withAlpha(50).color());
        }
        String name = this.tab.getName().config.getStringValue();
        this.drawString(this.x + 4, this.y + 7, ColorUtil.WHITE.color(), name, matrixStack);

        RenderUtils.color(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();

        super.render(mouseX, mouseY, selected, matrixStack);

        RenderUtils.disableDiffuseLighting();
    }

    @Override
    public void postRenderHovered(
            int mouseX, int mouseY, boolean selected, MatrixStack matrixStack) {
        super.postRenderHovered(mouseX, mouseY, selected, matrixStack);

        if (mouseX >= this.x
                && mouseX < this.buttonStartX
                && mouseY >= this.y
                && mouseY <= this.y + this.height) {
            RenderUtils.drawHoverText(mouseX, mouseY, this.hoverLines, matrixStack);
        }
    }

    private static class ButtonListener implements IButtonActionListener {

        private final Type type;
        private final WidgetTabEntry parent;

        public ButtonListener(Type type, WidgetTabEntry parent) {
            this.parent = parent;
            this.type = type;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            if (type == Type.REMOVE) {
                HudConfigStorage.TABS.remove(parent.tab);
                parent.parent.refreshEntries();
                AdvancedChatHud.MAIN_CHAT_TAB.setUpTabs();
            }
            if (type == Type.CONFIGURE) {
                GuiBase.openGui(
                        new GuiTabEditor(parent.parent.getParent(), parent.tab, parent.main));
            }
        }

        public enum Type {
            CONFIGURE("configure"),
            REMOVE("remove");

            private final String translate;

            Type(String name) {
                this.translate = translate(name);
            }

            private static String translate(String key) {
                return "advancedchathud.config.tabmenu." + key;
            }

            public String getDisplayName() {
                return StringUtils.translate(translate);
            }
        }
    }
}
