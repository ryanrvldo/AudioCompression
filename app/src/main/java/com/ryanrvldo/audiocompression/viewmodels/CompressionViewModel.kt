package com.ryanrvldo.audiocompression.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanrvldo.audiocompression.util.BinaryConverter
import com.ryanrvldo.audiocompression.util.BoldiVignaCodes
import com.ryanrvldo.audiocompression.util.EliasDeltaCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.properties.Delegates
import kotlin.system.measureNanoTime

class CompressionViewModel : ViewModel() {

    private val _initBytes = MutableLiveData<ByteArray>()
    val initBytes: LiveData<ByteArray>
        get() = _initBytes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _dictionaryCodes = MutableLiveData<ByteArray>()
    val dictionaryCodes: LiveData<ByteArray>
        get() = _dictionaryCodes

    fun setInitBytes(byte: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            _initBytes.postValue(byte)
        }
    }

    suspend fun compressAudio(init: Array<Byte>, methodCode: Int): ByteArray {
        var result: ByteArray by Delegates.notNull()

        withContext(Dispatchers.Default) {
            _isLoading.postValue(true)
            runningTime = measureNanoTime {
                val codes = when (methodCode) {
                    EliasDeltaCodes.METHOD_CODE -> EliasDeltaCodes.getCodeList(256)
                    else -> BoldiVignaCodes.getCodeList(256)
                }

                val byteCodes = init.groupingBy { it }
                    .eachCount().toList()
                    .sortedByDescending { (_, value) ->
                        value
                    }
                    .mapIndexed { index, pair ->
                        pair.first to codes[index]
                    }.toMap()

                _dictionaryCodes.postValue(byteCodes.keys.toByteArray())

                val stringBuffer = StringBuffer()
                init.forEach { byte ->
                    stringBuffer.append(byteCodes[byte])
                }

                val lastBit = stringBuffer.length.rem(8)
                stringBuffer.append(
                    when (lastBit) {
                        0 -> "00000001"
                        else -> "${"0".repeat(7 - lastBit)}1" + String.format(
                            "%08d",
                            Integer.parseInt((9 - lastBit).toString(2))
                        )
                    }
                )
                result = BinaryConverter.binToHex(stringBuffer)
            }.toDouble() / 1_000_000_000
            _isLoading.postValue(false)
        }
        withContext(Dispatchers.Default) {
            calculateCompression(init.size, result.size)
        }
        return result
    }

    private fun calculateCompression(initSize: Int, resultSize: Int) {
        RC = (resultSize.toDouble() / initSize) * 100
        CR = initSize.toDouble() / resultSize
        SS = (1 - (resultSize.toDouble() / initSize)) * 100
        BR = (resultSize.toDouble() * 8) / initSize
    }

    fun saveDictionaryFile(file: File, dictionary: ByteArray) {
        try {
            val outputStream = FileOutputStream(file)
            outputStream.apply {
                write(dictionary)
                close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        var runningTime: Double by Delegates.notNull()
        var RC: Double by Delegates.notNull()
        var CR: Double by Delegates.notNull()
        var SS: Double by Delegates.notNull()
        var BR: Double by Delegates.notNull()
//        private const val TAG = "Compression"
    }
}