package com.fivan.kalah.bot.handler;

import com.fivan.kalah.bot.State;
import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface Handler {
  State value();
}
