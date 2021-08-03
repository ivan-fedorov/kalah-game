package com.fivan.kalah.bot;

import com.fivan.kalah.bot.handler.ActionsAndMethods;
import com.fivan.kalah.bot.handler.DispatcherHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

  private final DispatcherHandler dispatcherHandler;
  @Value("${bot.telegram.name}")
  private String botName;
  @Value("${bot.telegram.token}")
  private String token;

  @Override
  public void onUpdateReceived(Update update) {
    ActionsAndMethods actionsAndMethods = dispatcherHandler.handle(update);
    for (BotApiMethod<?> botApiMethod : actionsAndMethods.getMethods()) {
      try {
        execute(botApiMethod);
      } catch (Exception e) {
        log.warn("Something went wrong with Telegram API for method: {}", botApiMethod.getMethod(), e);
      }
    }
    for (LobbySendMessageAction action : actionsAndMethods.getActions()) {
      try {
        action.execute(this);
      } catch (Exception e) {
        log.warn("Something went wrong with Telegram API for action: {}", action, e);
      }
    }
  }

  @Override
  public String getBotUsername() {
    return botName;
  }

  @Override
  public String getBotToken() {
    return token;
  }
}
