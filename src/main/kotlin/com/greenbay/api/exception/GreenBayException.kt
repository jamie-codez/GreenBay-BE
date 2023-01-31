package com.greenbay.api.exception

class GreenBayException(
    message: String? = null, throwable: Throwable? = null
) : BaseException(message, throwable)