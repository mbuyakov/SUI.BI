package ru.sui.bi.structuredquerytosqlconverter

class ConverterDialectHelperRegistry(private val dialectHelpers: List<ConverterDialectHelper>) {

    // Пока так, потом добавим тип
    fun get(): ConverterDialectHelper {
        return dialectHelpers.first()
    }

}