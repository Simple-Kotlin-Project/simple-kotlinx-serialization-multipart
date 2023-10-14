package io.github.edmondantes.multipart.serialization.decoder

import io.github.edmondantes.multipart.serialization.config.MultipartFormEncoderDecoderConfig
import io.github.edmondantes.multipart.serialization.decoder.MultipartFormCompositeDecoder.Companion.BYTE_ARRAY_DESCRIPTOR
import io.github.edmondantes.multipart.serialization.decoder.MultipartFormDecoder.Companion.notSupport
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule

public open class MultipartFormListCompositeDecoder(
    protected open val bytesValue: List<ByteArray>?,
    protected open val stringValue: List<String>?,
    protected open val config: MultipartFormEncoderDecoderConfig,
    override val serializersModule: SerializersModule = config.serializersModule,
) : CompositeDecoder {

    protected open var nextIndex: Int = 0

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (nextIndex < (bytesValue?.size ?: stringValue?.size ?: 0)) {
            nextIndex++
        } else {
            CompositeDecoder.DECODE_DONE
        }
    }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        decodeOneStringElement(index).toBoolean()

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        decodeOneStringElement(index).toByte()

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        decodeOneStringElement(index).let {
            if (it.isEmpty()) {
                notSupport()
            }

            it[0]
        }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        decodeOneStringElement(index).toShort()

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        decodeOneStringElement(index).toInt()

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        decodeOneStringElement(index).toLong()

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        decodeOneStringElement(index).toFloat()

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        decodeOneStringElement(index).toDouble()

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String =
        decodeOneStringElement(index)

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        notSupport()
    }

    @ExperimentalSerializationApi
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?,
    ): T? {
        val elementDescriptor = descriptor.getElementDescriptor(index)

        if (BYTE_ARRAY_DESCRIPTOR == elementDescriptor) {
            return decodeOneBytesElement(index) as T?
        }

        if (elementDescriptor.kind is PrimitiveKind) {
            return when (elementDescriptor.kind) {
                PrimitiveKind.BOOLEAN -> decodeBooleanElement(descriptor, index)
                PrimitiveKind.BYTE -> decodeByteElement(descriptor, index)
                PrimitiveKind.CHAR -> decodeCharElement(descriptor, index)
                PrimitiveKind.SHORT -> decodeShortElement(descriptor, index)
                PrimitiveKind.INT -> decodeIntElement(descriptor, index)
                PrimitiveKind.LONG -> decodeLongElement(descriptor, index)
                PrimitiveKind.DOUBLE -> decodeDoubleElement(descriptor, index)
                PrimitiveKind.STRING -> decodeStringElement(descriptor, index)
                else -> null
            } as T?
        }

        notSupport()
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?,
    ): T =
        decodeNullableSerializableElement(descriptor, index, deserializer, previousValue)
            ?: throw SerializationException("Can not decode")

    override fun endStructure(descriptor: SerialDescriptor) {}

    protected open fun decodeOneStringElement(index: Int): String {
        if (stringValue != null) {
            return stringValue?.getOrNull(index) ?: throw SerializationException("Can not decode element")
        }

        if (bytesValue != null) {
            return bytesValue?.getOrNull(index)?.let(config.stringDecoder)
                ?: throw SerializationException("Can not decode element")
        }

        throw SerializationException("Can not decode element")
    }

    protected open fun decodeOneBytesElement(index: Int): ByteArray {
        if (stringValue != null) {
            stringValue?.getOrNull(index)?.let(config.stringEncoder)
                ?: throw SerializationException("Can not decode element")
        }

        if (bytesValue != null) {
            return bytesValue?.getOrNull(index) ?: throw SerializationException("Can not decode element")
        }

        throw SerializationException("Can not decode element")
    }
}
