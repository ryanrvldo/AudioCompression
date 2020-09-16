package com.ryanrvldo.audiocompression.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryanrvldo.audiocompression.util.BinaryConverter
import com.ryanrvldo.audiocompression.util.BoldiVignaCodes
import com.ryanrvldo.audiocompression.util.EliasDeltaCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates
import kotlin.system.measureNanoTime

class DecompressionViewModel : ViewModel() {

    private val _initBytes = MutableLiveData<ByteArray>()
    val initBytes: LiveData<ByteArray>
        get() = _initBytes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    fun setInitBytes(byteArray: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            _initBytes.postValue(byteArray)
        }
    }

    @ExperimentalUnsignedTypes
    suspend fun decompressAudio(init: ByteArray, dict: ByteArray, methodCode: Int): ByteArray {
        var result: ByteArray by Delegates.notNull()
        _isLoading.postValue(true)
        runningTime = measureNanoTime {
            val codesDeferred = withContext(Dispatchers.Default) {
                async {
                    when (methodCode) {
                        EliasDeltaCodes.METHOD_CODE -> EliasDeltaCodes.getCodeMap(256)
                        else -> BoldiVignaCodes.getCodeMap(256)
                    }
                }
            }

            val compressedBin = withContext(Dispatchers.Default) {
                BinaryConverter.hexToBin(init).run {
                    val paddingBit = Integer.parseInt(
                        substring(length - 8 until length),
                        2
                    )
                    substring(0 until length - (7 + paddingBit))
                }
            }

            withContext(Dispatchers.Default) {
                var stringBit = String()
                val resultList = mutableListOf<Byte>()
                val codes = codesDeferred.await()
                for (bit in compressedBin) {
                    stringBit += bit
                    if (codes.containsKey(stringBit)) {
                        val idxDict = codes[stringBit] ?: error("")
                        resultList.add(dict[idxDict])
                        stringBit = String()
                    }
                }
                result = resultList.toByteArray()
            }
        }.toDouble() / 1_000_000_000
        _isLoading.postValue(false)
        return result
    }

    companion object {
        var runningTime: Double by Delegates.notNull()
//        const val TAG = "Decompression"
    }
}