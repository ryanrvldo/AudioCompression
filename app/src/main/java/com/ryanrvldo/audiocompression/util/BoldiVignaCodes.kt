package com.ryanrvldo.audiocompression.util

import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow

object BoldiVignaCodes {
    const val METHOD_CODE = 222

    fun getCodeList(size: Int): List<String> {
        if (size == 0) return emptyList()

        val codes = mutableListOf<String>()
        for (i in 1..size) {
            codes.add(encoding(i))
        }
        return codes
    }

    fun encoding(num: Int): String {
        val k = 2
        val h = determineH(num)
        val unaryCode = getUnaryCode(h + 1)
        val x = num - 2.0.pow(h * k)

        val z = (2.0.pow((h + 1) * k) - 2.0.pow(h * k))
        val s = ceil(log2(z))

        val binary = when {
            x < (2.0.pow(s) - z) -> {
                val bin = String.format("%016d", Integer.parseInt(x.toInt().toString(2)))
                bin.substring(bin.length - (s.toInt() - 1), bin.length)
            }
            else -> {
                val bin = String.format(
                    "%016d",
                    Integer.parseInt((2.0.pow(s) + x - z).toInt().toString(2))
                )
                bin.substring(bin.length - s.toInt(), bin.length)
            }
        }
        return unaryCode + binary
    }

    private fun determineH(num: Int): Int {
        val k = 2
        var h = 0
        var i = 0
        var start = 1
        var end = 3
        var isInRange = num in start..end
        while (!isInRange) {
            i++
            start = 2.0.pow((h + i) * k).toInt()
            end = 2.0.pow((h + i + 1) * k).toInt() - 1
            isInRange = num in start..end
            if (isInRange) {
                h = i
            }
        }
        return h
    }

    private fun getUnaryCode(num: Int): String {
        if (num == 1) {
            return "1"
        }
        return "${"0".repeat(num - 1)}1"
    }

    fun getCodeMap(size: Int): Map<String, Int> {
        if (size == 0) return emptyMap()

        val codeMap = mutableMapOf<String, Int>()
        for (i in 1..size) {
            codeMap[encoding(i)] = i - 1
        }
        return codeMap
    }
}