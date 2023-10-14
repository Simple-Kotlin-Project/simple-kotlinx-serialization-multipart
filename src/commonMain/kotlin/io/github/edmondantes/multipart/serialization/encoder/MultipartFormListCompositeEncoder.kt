package io.github.edmondantes.multipart.serialization.encoder

import io.github.edmondantes.multipart.builder.multipartPart
import io.github.edmondantes.multipart.serialization.config.MultipartFormEncoderDecoderConfig
import io.github.edmondantes.multipart.serialization.encoder.MultipartFormEncoder.Companion.ID
import io.github.edmondantes.multipart.serialization.encoder.MultipartFormEncoder.Companion.notSupport
import io.github.edmondantes.multipart.serialization.util.MultipartFormDataBuilderWithActions
import io.github.edmondantes.serialization.encoding.UniqueCompositeEncoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

public open class MultipartFormListCompositeEncoder(
    protected open val builder: MultipartFormDataBuilderWithActions,
    protected open val name: String,
    protected open val config: MultipartFormEncoderDecoderConfig,
    override val serializersModule: SerializersModule = config.serializersModule,
) : UniqueCompositeEncoder {
    override val id: String = ID

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        encodeElement(value.toString())
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        encodeElement(value.toString())
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        encodeElement(value.toString())
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        encodeElement(value.toString())
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        encodeElement(value.toString())
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        encodeElement(value.toString())
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        encodeElement(value.toString())
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        encodeElement(value.toString())
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        encodeElement(value)
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        notSupport()
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?,
    ) {
        if (value != null) {
            encodeSerializableElement(descriptor, index, serializer, value)
        }
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T,
    ) {
        when (value) {
            is ByteArray -> builder.add(name, multipartPart { body = value })
            is Boolean -> encodeBooleanElement(descriptor, index, value)
            is Byte -> encodeByteElement(descriptor, index, value)
            is Char -> encodeCharElement(descriptor, index, value)
            is Short -> encodeShortElement(descriptor, index, value)
            is Int -> encodeIntElement(descriptor, index, value)
            is Long -> encodeLongElement(descriptor, index, value)
            is Float -> encodeFloatElement(descriptor, index, value)
            is Double -> encodeDoubleElement(descriptor, index, value)
            is String -> encodeStringElement(descriptor, index, value)
            else -> notSupport()
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {}

    protected open fun encodeElement(value: String) {
        builder.add(
            name,
            multipartPart {
                body = config.stringEncoder(value)
            },
        )
    }
}
