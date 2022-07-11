package ru.sui.bi.structuredquerytosqlconverter.exception

class ConversionException : RuntimeException {

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

}