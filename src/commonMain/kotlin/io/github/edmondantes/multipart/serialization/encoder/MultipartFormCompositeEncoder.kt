package io.github.edmondantes.multipart.serialization.encoder

import io.github.edmondantes.multipart.builder.MultipartPartBuilder
import io.github.edmondantes.multipart.builder.multipartPart
import io.github.edmondantes.multipart.builder.multipartPartBuilder
import io.github.edmondantes.multipart.serialization.MultipartDynamicHeader
import io.github.edmondantes.multipart.serialization.MultipartDynamicHeaders
import io.github.edmondantes.multipart.serialization.MultipartStaticHeader
import io.github.edmondantes.multipart.serialization.MultipartStaticHeaders
import io.github.edmondantes.multipart.serialization.config.MultipartFormEncoderDecoderConfig
import io.github.edmondantes.multipart.serialization.encoder.MultipartFormEncoder.Companion.ID
import io.github.edmondantes.multipart.serialization.encoder.MultipartFormEncoder.Companion.notSupport
import io.github.edmondantes.multipart.serialization.util.MultipartFormDataBuilderWithActions
import io.github.edmondantes.serialization.encoding.UniqueCompositeEncoder
import io.github.edmondantes.serialization.getElementAllAnnotation
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

public open class MultipartFormCompositeEncoder(
    protected open val multipartBuilder: MultipartFormDataBuilderWithActions,
    protected open val config: MultipartFormEncoderDecoderConfig,
    protected open val name: String? = null,
    override val serializersModule: SerializersModule = config.serializersModule,
) : UniqueCompositeEncoder {
    override val id: String = ID

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        encodeElement(descriptor, index, value.toString())
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        encodeElement(descriptor, index, value.toString())
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        encodeElement(descriptor, index, CharArray(1) { value }.concatToString())
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        encodeElement(descriptor, index, value.toString())
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        encodeElement(descriptor, index, value.toString())
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        encodeElement(descriptor, index, value.toString())
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        encodeElement(descriptor, index, value.toString())
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        encodeElement(descriptor, index, value.toString())
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        notSupport()
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        encodeElement(descriptor, index, value)
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

    @OptIn(ExperimentalSerializationApi::class)
    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T,
    ) {
        when (value) {
            is String -> encodeElement(descriptor, index, value)
            is ByteArray -> encodeElement(descriptor, index, value)

            else -> {
                val part = multipartPartBuilder()
                serializer.serialize(
                    MultipartFormEncoder(
                        multipartBuilder,
                        config,
                        part,
                        name ?: descriptor.getElementName(index),
                    ),
                    value,
                )

                part.body?.also { encodeElement(descriptor, index, it) }
            }
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {}

    protected open fun encodeElement(descriptor: SerialDescriptor, index: Int, value: String) {
        if (!tryToEncodeDynamicHeader(descriptor, index, value)) {
            encodeElement(descriptor, index, config.stringEncoder(value), false)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    protected open fun encodeElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: ByteArray,
        tryEncodeDynamicHeader: Boolean = true,
    ) {
        if (tryEncodeDynamicHeader) {
            tryToEncodeDynamicHeader(descriptor, index, value.decodeToString())
        }

        multipartBuilder.add(
            name ?: descriptor.getElementName(index),
            multipartPart {
                body = value

                val staticHeaders =
                    descriptor
                        .getElementAllAnnotation(index)
                        .filterIsInstance<MultipartStaticHeaders>()
                        .flatMap { it.headers.toList() } +
                        descriptor
                            .getElementAllAnnotation(index)
                            .filterIsInstance<MultipartStaticHeader>()

                staticHeaders.forEach {
                    encodeHeaderValueOrAttribute(it.name, it.headerAttribute, this, it.value)
                }
            },
        )
    }

    protected open fun tryToEncodeDynamicHeader(descriptor: SerialDescriptor, index: Int, value: String): Boolean {
        val dynamicHeader = getDynamicHeaders(descriptor, index)

        dynamicHeader.forEach { header ->
            multipartBuilder.addOnBuild { formDataBuilder ->
                formDataBuilder.edit(header.serialPropertyName) { partBuilder ->
                    encodeHeaderValueOrAttribute(header.headerName, header.headerAttribute, partBuilder, value)
                }
            }
        }

        return dynamicHeader.isNotEmpty()
    }

    protected open fun getDynamicHeaders(descriptor: SerialDescriptor, index: Int): List<MultipartDynamicHeader> =
        descriptor
            .getElementAllAnnotation(index)
            .filterIsInstance<MultipartDynamicHeaders>()
            .flatMap { it.headers.toList() } +
            descriptor
                .getElementAllAnnotation(index)
                .filterIsInstance<MultipartDynamicHeader>()

    protected open fun encodeHeaderValueOrAttribute(
        name: String,
        headerAttribute: String,
        builder: MultipartPartBuilder,
        value: String,
    ) {
        builder.header(name) {
            if (headerAttribute.isNotBlank()) {
                attributes[headerAttribute] = value
            } else {
                this.value = value
            }
        }
    }
}
