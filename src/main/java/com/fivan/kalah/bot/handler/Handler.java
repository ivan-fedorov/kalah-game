package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.State;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.stereotype.Component;

@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface Handler {
    State value();
}
