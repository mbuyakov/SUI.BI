package ru.sui.bi.structuredquerytosqlconverter

class ConverterDialectHelperRegistry(private val dialectHelpers: List<ConverterDialectHelper>) {

    constructor(vararg dialectHelpers: ConverterDialectHelper) : this(dialectHelpers.toList())

    // Пока так, потом добавим тип
    fun get(): ConverterDialectHelper {
        return dialectHelpers.first()
    }

}