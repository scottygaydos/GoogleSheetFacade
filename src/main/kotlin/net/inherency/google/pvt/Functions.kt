package net.inherency.google.pvt

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

fun capitalize(name: String): String {
    return name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

fun bigDecimalCentsToCurrency(bigDecimalInCents: BigDecimal): String {
    return NumberFormat.getCurrencyInstance().format(bigDecimalInCents.divide(BigDecimal("100")))
}

fun intCentsToCurrency(intInCents: Int?): String {
    if (intInCents == null) {
        return ""
    }
    return bigDecimalCentsToCurrency(intInCents.toBigDecimal())
}