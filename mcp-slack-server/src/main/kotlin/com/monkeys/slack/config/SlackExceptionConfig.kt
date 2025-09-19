package com.monkeys.slack.config

import com.monkeys.shared.exception.GlobalExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(GlobalExceptionHandler::class)
class SlackExceptionConfig