package com.jsonj.persistentwhisper.mixin;

import com.jsonj.persistentwhisper.PersistentWhisperMod;
import com.jsonj.persistentwhisper.GlobalVariables;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
	@Inject(at = @At("HEAD"), method = "sendMessage", cancellable = true)
	private void sendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
		ci.cancel();
		
		GlobalVariables v = PersistentWhisperMod.vars;
		
//		ExampleMod.LOGGER.info("v.targeted: " + (v.targeted ? "true" : "false"));
//		ExampleMod.LOGGER.info("v.name: " + v.name);
//		ExampleMod.LOGGER.info("starting message: " + chatText);
		if (v.targeted) {
			if (chatText.startsWith("/all")) {
				MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("§aYou have stopped whispering."));
//				ExampleMod.LOGGER.info("You have stopped whispering.");
				v.targeted = false;
				return;
			}
//			ExampleMod.LOGGER.info("adding whisper prefix");
			chatText = String.format("/w %s %s", v.name, chatText);
		} else {
			if (chatText.startsWith("/w")) {
				String[] splitted = chatText.split(" ");
				if (splitted.length == 2) {
//					ExampleMod.LOGGER.info("You are now whispering to " + splitted[1]);
					v.targeted = true;
					v.name = splitted[1];
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("§aYou are now whisper to " + v.name));
					return;
				}
			}
		}


//		ExampleMod.LOGGER.info("current message: " + chatText);

		if ((chatText = StringUtils.normalizeSpace(chatText.trim())).isEmpty()) {
			return;
		}
		if (addToHistory) {
			MinecraftClient.getInstance().inGameHud.getChatHud().addToMessageHistory(chatText);
		}
		Text text = ((ChatScreen)(Object)this).getChatPreviewer().tryConsumeResponse(chatText);
		if (chatText.startsWith("/")) {
			MinecraftClient.getInstance().player.sendCommand(chatText.substring(1), text);
		} else {
			MinecraftClient.getInstance().player.sendChatMessage(chatText, text);
		}
	}
}
