package io.github.edmondantes.multipart.serialization.decoder

import io.github.edmondantes.multipart.MultipartFormData
import io.github.edmondantes.multipart.serialization.config.MultipartFormEncoderDecoderConfig
import io.github.edmondantes.multipart.serialization.decoder.MultipartFormDecoder.Companion.ID
import io.github.edmondantes.multipart.serialization.decoder.MultipartFormDecoder.Companion.notSupport
import io.github.edmondantes.serialization.decoding.UniqueDecoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule

public open class MultipartFormRootDecoder(
    protected open val multipart: MultipartFormData,
    protected open val config: MultipartFormEncoderDecoderConfig,
    override val serializersModule: SerializersModule = config.serializersModule,
) : UniqueDecoder {
    override val id: String = ID

    @OptIn(ExperimentalSerializationApi::class)
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (descriptor.kind is StructureKind.CLASS || descriptor.kind is StructureKind.OBJECT) {
            return MultipartFormCompositeDecoder(multipart, config, serializersModule)
        } else {
            throw SerializationException("Multipart form-data decoder can not decode structures with kind '${descriptor.kind}'")
        }
    }

    override fun decodeBoolean(): Boolean {
        notSupport()
    }

    override fun decodeByte(): Byte {
        notSupport()
    }

    override fun decodeChar(): Char {
        notSupport()
    }

    override fun decodeShort(): Short {
        notSupport()
    }

    override fun decodeInt(): Int {
        notSupport()
    }

    override fun decodeLong(): Long {
        notSupport()
    }

    override fun decodeFloat(): Float {
        notSupport()
    }

    override fun decodeDouble(): Double {
        notSupport()
    }

    override fun decodeString(): String {
        notSupport()
    }

    override fun decodeInline(descriptor: SerialDescriptor): Decoder {
        notSupport()
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        notSupport()
    }

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean {
        notSupport()
    }

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? {
        notSupport()
    }
}
