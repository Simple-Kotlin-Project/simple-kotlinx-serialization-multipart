package io.github.edmondantes.multipart.serialization.encoder

import io.github.edmondantes.multipart.serialization.config.MultipartFormEncoderDecoderConfig
import io.github.edmondantes.multipart.serialization.encoder.MultipartFormEncoder.Companion.ID
import io.github.edmondantes.multipart.serialization.encoder.MultipartFormEncoder.Companion.notSupport
import io.github.edmondantes.multipart.serialization.util.MultipartFormDataBuilderWithActions
import io.github.edmondantes.serialization.encoding.UniqueEncoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

public open class MultipartFormRootEncoder(
    protected open val builder: MultipartFormDataBuilderWithActions,
    protected open val config: MultipartFormEncoderDecoderConfig,
    override val serializersModule: SerializersModule = config.serializersModule,
) : UniqueEncoder {
    override val id: String = ID

    @OptIn(ExperimentalSerializationApi::class)
    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        if (descriptor.kind is StructureKind.CLASS || descriptor.kind is StructureKind.OBJECT) {
            return MultipartFormCompositeEncoder(builder, config)
        } else {
            throw SerializationException("Multipart form-data encoder can not encode root structures with kind '${descriptor.kind}'")
        }
    }

    override fun encodeBoolean(value: Boolean) {
        notSupport()
    }

    override fun encodeByte(value: Byte) {
        notSupport()
    }

    override fun encodeChar(value: Char) {
        notSupport()
    }

    override fun encodeShort(value: Short) {
        notSupport()
    }

    override fun encodeInt(value: Int) {
        notSupport()
    }

    override fun encodeLong(value: Long) {
        notSupport()
    }

    override fun encodeFloat(value: Float) {
        notSupport()
    }

    override fun encodeDouble(value: Double) {
        notSupport()
    }

    override fun encodeString(value: String) {
        notSupport()
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        notSupport()
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        notSupport()
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        notSupport()
    }
}
