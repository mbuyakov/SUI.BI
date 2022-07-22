package ru.sui.bi.structuredquerytosqlconverter.exception

class ParseException : RuntimeException {

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

}