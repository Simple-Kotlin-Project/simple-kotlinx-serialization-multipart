package io.github.edmondantes.multipart.serialization.encoder

import io.github.edmondantes.multipart.builder.MultipartPartBuilder
import io.github.edmondantes.multipart.serialization.config.MultipartFormEncoderDecoderConfig
import io.github.edmondantes.multipart.serialization.util.MultipartFormDataBuilderWithActions
import io.github.edmondantes.serialization.encoding.UniqueEncoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
public open class MultipartFormEncoder(
    protected open val builder: MultipartFormDataBuilderWithActions,
    protected open val config: MultipartFormEncoderDecoderConfig,
    protected open val partBuilder: MultipartPartBuilder? = null,
    protected open val name: String? = null,
    override val serializersModule: SerializersModule = config.serializersModule,
) : UniqueEncoder {
    override val id: String = ID

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder =
        when (descriptor.kind) {
            is StructureKind.LIST -> {
                MultipartFormListCompositeEncoder(
                    builder,
                    name
                        ?: throw SerializationException("Multipart form-data encoder can not encode list without name"),
                    config,
                    serializersModule,
                )
            }

            is StructureKind.CLASS, is StructureKind.OBJECT -> {
                throw SerializationException("Multipart form-data encoder can not encode non-flat structures")
            }

            else -> {
                throw SerializationException("Multipart form-data encoder can not encode structures with kind '${descriptor.kind}'")
            }
        }

    override fun encodeBoolean(value: Boolean) {
        encodeElement(value)
    }

    override fun encodeByte(value: Byte) {
        encodeElement(value)
    }

    override fun encodeChar(value: Char) {
        encodeElement(value)
    }

    override fun encodeShort(value: Short) {
        encodeElement(value)
    }

    override fun encodeInt(value: Int) {
        encodeElement(value)
    }

    override fun encodeLong(value: Long) {
        encodeElement(value)
    }

    override fun encodeFloat(value: Float) {
        encodeElement(value)
    }

    override fun encodeDouble(value: Double) {
        encodeElement(value)
    }

    override fun encodeString(value: String) {
        encodeElement(value)
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeElement(enumDescriptor.getElementName(index))
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        notSupport()
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        (partBuilder ?: notSupport()).body = ByteArray(0)
    }

    protected open fun encodeElement(value: Any) {
        (partBuilder ?: notSupport()).body = config.stringEncoder(value.toString())
    }

    public companion object {
        internal const val ID: String = "io.github.edmondantes.serialization.multipart.encoder"

        public fun notSupport(): Nothing =
            throw SerializationException("Can not encode value. Multipart form-data encoder doesn't support to encode this values")
    }
}
