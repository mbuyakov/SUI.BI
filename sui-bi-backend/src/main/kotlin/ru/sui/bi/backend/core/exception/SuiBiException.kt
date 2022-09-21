package ru.sui.bi.backend.core.exception

class SuiBiException : RuntimeException {

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

}