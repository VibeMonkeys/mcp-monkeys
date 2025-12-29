package com.monkeys.client.exception

/**
 * Chat Service 전용 예외
 */
class ChatServiceException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
