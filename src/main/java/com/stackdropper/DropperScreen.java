package com.stackdropper;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Settings menu, opened with Tab. */
public class DropperScreen extends Screen {
	private ButtonWidget toggleButton;
	private ButtonWidget keyButton;
	private ButtonWidget wholeButton;
	private TextFieldWidget stacksField;
	private TextFieldWidget intervalField;
	private boolean listening = false;

	public DropperScreen() {
		super(Text.literal("Stack Dropper"));
	}

	@Override
	protected void init() {
		Config cfg = Config.get();
		int cx = this.width / 2;
		int y = this.height / 2 - 80;

		toggleButton = addDrawableChild(ButtonWidget.builder(toggleText(), b -> {
			StackDropperClient.enabled = !StackDropperClient.enabled;
			b.setMessage(toggleText());
		}).dimensions(cx - 100, y, 200, 20).build());

		keyButton = addDrawableChild(ButtonWidget.builder(keyText(), b -> {
			listening = true;
			b.setMessage(keyText());
		}).dimensions(cx - 100, y + 26, 200, 20).build());

		stacksField = new TextFieldWidget(this.textRenderer, cx + 30, y + 52, 70, 20, Text.literal("Stacks"));
		stacksField.setTextPredicate(s -> s.matches("\\d{0,2}"));
		stacksField.setText(String.valueOf(cfg.stacksPerDrop));
		stacksField.setEditable(!cfg.wholeInventory);
		stacksField.setChangedListener(s -> {
			try {
				int v = Integer.parseInt(s);
				if (v >= 1 && v <= 36) Config.get().stacksPerDrop = v;
			} catch (NumberFormatException ignored) {
			}
		});
		addDrawableChild(stacksField);

		wholeButton = addDrawableChild(ButtonWidget.builder(wholeText(), b -> {
			Config.get().wholeInventory = !Config.get().wholeInventory;
			stacksField.setEditable(!Config.get().wholeInventory);
			b.setMessage(wholeText());
		}).dimensions(cx - 100, y + 78, 200, 20).build());

		intervalField = new TextFieldWidget(this.textRenderer, cx + 30, y + 104, 70, 20, Text.literal("Interval"));
		intervalField.setTextPredicate(s -> s.matches("\\d{0,3}(\\.\\d{0,3})?"));
		intervalField.setText(trimNumber(cfg.intervalSeconds));
		intervalField.setChangedListener(s -> {
			try {
				double v = Double.parseDouble(s);
				if (v >= 0.01 && v <= 600) Config.get().intervalSeconds = v;
			} catch (NumberFormatException ignored) {
			}
		});
		addDrawableChild(intervalField);

		addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
				.dimensions(cx - 100, y + 136, 200, 20).build());
	}

	private Text toggleText() {
		return Text.literal("Auto-drop: " + (StackDropperClient.enabled ? "ON" : "OFF"));
	}

	private Text keyText() {
		if (listening) {
			return Text.literal("> Press a key (Esc to cancel) <");
		}
		return Text.literal("Change drop keybind: ")
				.append(StackDropperClient.dropKey.getBoundKeyLocalizedText());
	}

	private Text wholeText() {
		return Text.literal("Whole inventory per drop: " + (Config.get().wholeInventory ? "ON" : "OFF"));
	}

	private static String trimNumber(double d) {
		String s = String.format(java.util.Locale.ROOT, "%.3f", d);
		s = s.replaceAll("0+$", "");
		if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
		return s;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		int cx = this.width / 2;
		int y = this.height / 2 - 80;
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, y - 20, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.literal("Stacks per drop"), cx - 100, y + 58, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.literal("Every (seconds)"), cx - 100, y + 110, 0xFFFFFFFF);
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		int key = input.key();
		if (listening) {
			if (key != GLFW.GLFW_KEY_ESCAPE) {
				StackDropperClient.rebind(key);
			}
			listening = false;
			keyButton.setMessage(keyText());
			return true;
		}
		if (key == GLFW.GLFW_KEY_TAB) {
			close();
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	public void removed() {
		Config.save();
	}
}
