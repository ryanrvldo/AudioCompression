package com.ryanrvldo.audiocompression.util

import kotlin.math.pow

object EliasDeltaCodes {
    const val METHOD_CODE = 111

    fun getCodeList(size: Int): List<String> {
        if (size == 0) return emptyList()

        val codes = mutableListOf<String>()
        for (i in 1..size) {
            codes.add(encoding(i))
        }
        return codes
    }

    fun encoding(num: Int): String {
        var stringBit = Integer.toBinaryString(num)
        var bitCount = stringBit.length
        var code = stringBit.substringAfter("1")
        stringBit = Integer.toBinaryString(bitCount)
        bitCount = stringBit.length - 1
        code = "0".repeat(bitCount) + stringBit + code
        return code
    }

    fun getCodeMap(size: Int): Map<String, Int> {
        if (size == 0) return emptyMap()

        val codeMap = mutableMapOf<String, Int>()
        for (i in 1..size) {
            codeMap[encoding(i)] = i - 1
        }
        return codeMap
    }

    fun decoding(code: String): Int {
        val count = code.substringBefore("1").length
        val leftBits = code.substring(0 until (2 * count + 1)).length
        val num = when (val rightBits = code.substring(leftBits until code.length)) {
            "" -> 1.0
            else -> 2.0.pow(rightBits.length) + Integer.parseInt(rightBits, 2)
        }
        return num.toInt()
    }
}