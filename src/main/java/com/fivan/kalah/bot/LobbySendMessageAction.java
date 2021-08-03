package com.fivan.kalah.bot;

import lombok.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Consumer;

@Value
public class LobbySendMessageAction {

  SendMessage sendMessage;
  Consumer<Message> messageConsumer;

  public void execute(TelegramBot bot) throws TelegramApiException {
    Message message = bot.execute(sendMessage);
    messageConsumer.accept(message);
  }
}
