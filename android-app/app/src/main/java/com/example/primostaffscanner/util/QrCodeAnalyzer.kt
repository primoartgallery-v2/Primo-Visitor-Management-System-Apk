package com.example.primostaffscanner.util

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.atomic.AtomicBoolean

class QrCodeAnalyzer(
    private val onQrScanned: (rawText: String, visitorId: String?) -> Unit
) : ImageAnalysis.Analyzer {

    companion object {
        private val VISITOR_ID_REGEX = Regex("""\bPAG-2026-[A-Z0-9]{4,}\b""", RegexOption.IGNORE_CASE)
        private val GENERIC_ID_REGEX = Regex("""\bPAG-[A-Z0-9-]+""", RegexOption.IGNORE_CASE)
        private val URL_PARAM_REGEX = Regex("""[?&]id=([A-Z0-9-]+)""", RegexOption.IGNORE_CASE)

        fun extractVisitorId(text: String): String? {
            val trimmed = text.trim()
            VISITOR_ID_REGEX.find(trimmed)?.value?.let { return it.uppercase() }

            URL_PARAM_REGEX.find(trimmed)?.groupValues?.getOrNull(1)?.let { return it.uppercase() }

            GENERIC_ID_REGEX.find(trimmed)?.value?.let { return it.uppercase() }

            return null
        }
    }

    private val isScanningEnabled = AtomicBoolean(true)

    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                DecodeHintType.TRY_HARDER to java.lang.Boolean.TRUE,
                DecodeHintType.CHARACTER_SET to "UTF-8"
            )
        )
    }

    fun setScanningEnabled(enabled: Boolean) {
        isScanningEnabled.set(enabled)
    }

    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanningEnabled.get()) {
            imageProxy.close()
            return
        }

        try {
            val buffer = imageProxy.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val width = imageProxy.width
            val height = imageProxy.height
            val rotation = imageProxy.imageInfo.rotationDegrees

            // If rotated (portrait typically 90 or 270), rotate luminance data
            val (rotatedData, finalWidth, finalHeight) = if (rotation == 90) {
                Triple(rotate90(data, width, height), height, width)
            } else if (rotation == 270) {
                Triple(rotate270(data, width, height), height, width)
            } else {
                Triple(data, width, height)
            }

            val source = PlanarYUVLuminanceSource(
                rotatedData,
                finalWidth,
                finalHeight,
                0,
                0,
                finalWidth,
                finalHeight,
                false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val result = try {
                reader.decodeWithState(binaryBitmap)
            } catch (_: Exception) {
                null
            } finally {
                reader.reset()
            }

            if (result != null && isScanningEnabled.get()) {
                val rawText = result.text.orEmpty()
                val visitorId = extractVisitorId(rawText)
                if (rawText.isNotBlank()) {
                    onQrScanned(rawText, visitorId)
                }
            }
        } catch (_: Exception) {
            // Ignore frame analysis errors
        } finally {
            imageProxy.close()
        }
    }

    private fun rotate90(src: ByteArray, width: Int, height: Int): ByteArray {
        val dest = ByteArray(src.size)
        var idx = 0
        for (x in 0 until width) {
            for (y in height - 1 downTo 0) {
                dest[idx++] = src[y * width + x]
            }
        }
        return dest
    }

    private fun rotate270(src: ByteArray, width: Int, height: Int): ByteArray {
        val dest = ByteArray(src.size)
        var idx = 0
        for (x in width - 1 downTo 0) {
            for (y in 0 until height) {
                dest[idx++] = src[y * width + x]
            }
        }
        return dest
    }
}
