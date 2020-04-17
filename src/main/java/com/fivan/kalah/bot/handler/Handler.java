package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.State;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Handler {
    State value();
}
