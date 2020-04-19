package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.HandlingResult;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

public interface StateHandler {

    HandlingResult handle(Update update);
    default List<BotApiMethod<?>> getInitialMethods(Update update) {
        return emptyList();
    }
}
