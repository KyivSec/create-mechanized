package kyivsec.createmechanized.content.pilot_helmet.client;

import kyivsec.createmechanized.content.pilot_helmet.TrackedContainer;
import kyivsec.createmechanized.network.RegisterContainerPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;
import java.util.UUID;

public class ContainerNameScreen extends Screen {

    private final BlockPos pos;
    private final Optional<UUID> sublevelId;
    private final TrackedContainer.Kind kind;
    private EditBox nameBox;

    public ContainerNameScreen(BlockPos pos, Optional<UUID> sublevelId, TrackedContainer.Kind kind) {
        super(Component.literal("Name " + (kind == TrackedContainer.Kind.FLUID ? "Fluid" : "Energy") + " Container"));
        this.pos = pos;
        this.sublevelId = sublevelId;
        this.kind = kind;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int cy = height / 2;
        nameBox = new EditBox(font, cx - 100, cy - 10, 200, 20, Component.literal("Name"));
        nameBox.setMaxLength(32);
        nameBox.setResponder(s -> {
        });
        addRenderableWidget(nameBox);

        addRenderableWidget(Button.builder(Component.literal("Save"), b -> confirm())
                .bounds(cx - 100, cy + 18, 95, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose())
                .bounds(cx + 5, cy + 18, 95, 20)
                .build());

        setInitialFocus(nameBox);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) {
            confirm();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void confirm() {
        String name = nameBox.getValue().trim();
        if (name.isEmpty()) {
            return;
        }
        PacketDistributor.sendToServer(new RegisterContainerPacket(pos, sublevelId, name, kind));
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int cx = width / 2;
        int cy = height / 2;
        graphics.drawCenteredString(font, this.title, cx, cy - 36, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
