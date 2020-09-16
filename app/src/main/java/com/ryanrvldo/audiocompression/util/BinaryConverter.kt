package com.ryanrvldo.audiocompression.util

object BinaryConverter {

    @ExperimentalUnsignedTypes
    fun hexToBin(bytes: ByteArray): String {
        return buildString {
            for (b in bytes) {
                append(String.format("%08d", Integer.parseInt(b.toUByte().toString(2))))
            }
        }
    }

    fun binToHex(binary: StringBuffer): ByteArray {
        val bytes = ByteArray(binary.length / 8)
        for (i in binary.indices step 8) {
            val num = Integer.parseInt(binary.substring(i, i + 8), 2)
            bytes[i / 8] = num.toByte()
        }
        return bytes
    }
}