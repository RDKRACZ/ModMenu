package prospector.modmenu.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.menu.GuiPauseMenu;
import net.minecraft.client.gui.widget.WidgetButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import prospector.modmenu.ModMenu;

@Mixin(Gui.class)
public class GuiMixin {

	@Shadow public int height;

	@Inject(at = @At("HEAD"), method = "addButton(Lnet/minecraft/client/gui/widget/WidgetButton;)Lnet/minecraft/client/gui/widget/WidgetButton;", cancellable = true)
	protected void addButton(WidgetButton var1, CallbackInfoReturnable info) {
		if (((Object) this) instanceof GuiMainMenu) {
			if (ModMenu.replaceRealmsButton) {
				if (var1.id == 14) {
					info.cancel();
				}
			} else {
				if (var1.y <= this.height / 4 + 48 + 24 * 3) {
					var1.y -= 12;
				}
				if (var1.y > this.height / 4 + 48 + 24 * 3) {
					var1.y += 12;
				}
			}
		}
		if (((Object) this) instanceof GuiPauseMenu) {
			if (ModMenu.replaceMojangFeedbackButtons) {
				if (var1.id == 8 || var1.id == 9) {
					info.cancel();
				}
			} else {
				if (var1.y >= this.height / 4 - 16 + 24 * 3 && var1.id != 8 && var1.id != 9) {
					var1.y += 24;
				}
				var1.y -= 12;
			}
		}
	}
}