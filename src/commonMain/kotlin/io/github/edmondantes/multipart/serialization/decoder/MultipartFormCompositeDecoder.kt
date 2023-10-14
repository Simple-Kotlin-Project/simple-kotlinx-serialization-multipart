package io.github.edmondantes.multipart.serialization.decoder

import io.github.edmondantes.multipart.MultipartFormData
import io.github.edmondantes.multipart.serialization.MultipartDynamicHeader
import io.github.edmondantes.multipart.serialization.MultipartDynamicHeaders
import io.github.edmondantes.multipart.serialization.config.MultipartFormEncoderDecoderConfig
import io.github.edmondantes.multipart.serialization.decoder.MultipartFormDecoder.Companion.notSupport
import io.github.edmondantes.serialization.getElementAllAnnotation
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule

public open class MultipartFormCompositeDecoder(
    protected open val multipartFormData: MultipartFormData,
    protected open val config: MultipartFormEncoderDecoderConfig,
    override val serializersModule: SerializersModule = config.serializersModule,
) : CompositeDecoder {

    protected open var nextElementIndex: Int = 0

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        if (nextElementIndex < descriptor.elementsCount) {
            nextElementIndex++
        } else {
            CompositeDecoder.DECODE_DONE
        }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        decodeElement(descriptor, index).toBoolean()

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        decodeElement(descriptor, index).toByte()

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        decodeElement(descriptor, index).toShort()

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        decodeElement(descriptor, index).getOrNull(0)
            ?: throw SerializationException(
                "Can not find value for property with name '${descriptor.getElementName(index)}'",
            )

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        decodeElement(descriptor, index).toInt()

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        decodeElement(descriptor, index).toLong()

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        decodeElement(descriptor, index).toFloat()

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        decodeElement(descriptor, index).toDouble()

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String =
        decodeElement(descriptor, index)

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        notSupport()
    }

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?,
    ): T? {
        val elementDescriptor = descriptor.getElementDescriptor(index)

        var dynamicHeaderValue: List<String>? = null
        var partValue: List<ByteArray>? = null

        decodeDynamicHeader(descriptor, index, {
            dynamicHeaderValue = it
        }) {
            partValue = decodePartValue(descriptor, index)
        }

        return when (elementDescriptor) {
            BYTE_ARRAY_DESCRIPTOR -> partValue?.get(0) ?: dynamicHeaderValue?.get(0)?.let { config.stringEncoder(it) }
            STRING_DESCRIPTOR -> dynamicHeaderValue?.get(0) ?: partValue?.get(0)?.let { config.stringDecoder(it) }
            else -> if (!dynamicHeaderValue.isNullOrEmpty()) {
                deserializer.deserialize(
                    MultipartFormDecoder(
                        null,
                        dynamicHeaderValue,
                        config,
                        serializersModule,
                    ),
                )
            } else if (!partValue.isNullOrEmpty()) {
                deserializer.deserialize(
                    MultipartFormDecoder(
                        partValue,
                        null,
                        config,
                        serializersModule,
                    ),
                )
            } else {
                null
            }
        } as T?
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?,
    ): T =
        decodeNullableSerializableElement(descriptor, index, deserializer, previousValue)
            ?: throw SerializationException("Can not find value for '${descriptor.getElementName(index)}'")

    override fun endStructure(descriptor: SerialDescriptor) {}

    @OptIn(ExperimentalSerializationApi::class)
    protected open fun decodeElement(descriptor: SerialDescriptor, index: Int): String =
        decodeElementOrNull(descriptor, index).firstOrNull()
            ?: throw SerializationException(
                "Can not find value for property with name '${descriptor.getElementName(index)}'",
            )

    protected open fun decodeElementOrNull(descriptor: SerialDescriptor, index: Int): List<String> =
        decodeDynamicHeader(descriptor, index, { it }) {
            decodePartValue(descriptor, index).map(config.stringDecoder)
        }

    private inline fun <T> decodeDynamicHeader(
        descriptor: SerialDescriptor,
        index: Int,
        dynamicHeaderValueConsumer: (List<String>) -> T,
        notDynamicHeaderAction: () -> T,
    ): T {
        val dynamicHeaders =
            descriptor
                .getElementAllAnnotation(index)
                .filterIsInstance<MultipartDynamicHeaders>()
                .flatMap { it.headers.toList() } +
                descriptor
                    .getElementAllAnnotation(index)
                    .filterIsInstance<MultipartDynamicHeader>()

        if (dynamicHeaders.isNotEmpty()) {
            var headerValue: List<String>? = null
            var i = 0
            while (headerValue == null && i < dynamicHeaders.size) {
                val dynamicHeader = dynamicHeaders[i]
                val header =
                    multipartFormData.namedParts[dynamicHeader.serialPropertyName]?.mapNotNull { it.headers[dynamicHeader.headerName] }
                headerValue =
                    if (dynamicHeader.headerAttribute.isNotEmpty()) {
                        header?.mapNotNull { it.attributes[dynamicHeader.headerAttribute] }
                    } else {
                        header?.mapNotNull { it.value }
                    }
                i++
            }

            return dynamicHeaderValueConsumer(headerValue ?: emptyList())
        }

        return notDynamicHeaderAction()
    }

    @OptIn(ExperimentalSerializationApi::class)
    protected open fun decodePartValue(descriptor: SerialDescriptor, index: Int): List<ByteArray> =
        multipartFormData.namedParts[descriptor.getElementName(index)]?.map { it.body } ?: emptyList()

    internal companion object {
        internal val BYTE_ARRAY_DESCRIPTOR: SerialDescriptor = serialDescriptor<ByteArray>()
        internal val STRING_DESCRIPTOR: SerialDescriptor = serialDescriptor<ByteArray>()
    }
}
