package io.github.edmondantes.multipart.serialization.decoder

import io.github.edmondantes.multipart.serialization.config.MultipartFormEncoderDecoderConfig
import io.github.edmondantes.serialization.decoding.UniqueDecoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule

public open class MultipartFormDecoder(
    protected open val bytesValue: List<ByteArray>?,
    protected open val stringValue: List<String>?,
    protected open val config: MultipartFormEncoderDecoderConfig,
    override val serializersModule: SerializersModule = config.serializersModule
) : UniqueDecoder {
    override val id: String = ID

    @OptIn(ExperimentalSerializationApi::class)
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (descriptor.kind is StructureKind.LIST) {
            return MultipartFormListCompositeDecoder(bytesValue, stringValue, config)
        }
        throw SerializationException("Can not decode not-flat objects")
    }

    override fun decodeBoolean(): Boolean =
        decodeOneStringElement().toBoolean()

    override fun decodeByte(): Byte =
        decodeOneStringElement().toByte()

    override fun decodeShort(): Short =
        decodeOneStringElement().toShort()

    override fun decodeChar(): Char =
        decodeOneStringElement()[0]

    override fun decodeInt(): Int =
        decodeOneStringElement().toInt()

    override fun decodeLong(): Long =
        decodeOneStringElement().toLong()

    override fun decodeFloat(): Float =
        decodeOneStringElement().toFloat()

    override fun decodeDouble(): Double =
        decodeOneStringElement().toDouble()

    override fun decodeString(): String =
        decodeOneStringElement()

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val decodedElementName = decodeOneStringElement()
        val realElementName = enumDescriptor.elementNames.find {
            it.lowercase() == decodedElementName
        }
            ?: throw SerializationException("Can not find element with name '$decodedElementName' in enum '${enumDescriptor.serialName}'")

        return enumDescriptor.getElementIndex(realElementName)
    }

    override fun decodeInline(descriptor: SerialDescriptor): Decoder {
        throw SerializationException("Not support")
    }

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean =
        stringValue?.firstOrNull()?.isNotEmpty() == true || bytesValue?.firstOrNull()?.isNotEmpty() == true

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? = null

    protected open fun decodeOneStringElement(): String {
        if (stringValue != null) {
            return stringValue?.firstOrNull() ?: throw SerializationException("Can not decode value")
        }

        if (bytesValue != null) {
            return config.stringDecoder(
                bytesValue?.firstOrNull() ?: throw SerializationException("Can not decode value"),
            )
        }

        throw SerializationException("Can not decode value")
    }

    protected open fun decodeOneBytesElement(): ByteArray {
        if (stringValue != null) {
            config.stringEncoder(stringValue?.firstOrNull() ?: throw SerializationException("Can not decode value"))
        }

        if (bytesValue != null) {
            return bytesValue?.firstOrNull() ?: throw SerializationException("Can not decode value")
        }

        throw SerializationException("Can not decode value")
    }

    public companion object {
        internal const val ID: String = "io.github.edmondantes.serialization.multipart.decoder"

        public fun notSupport(): Nothing =
            throw SerializationException("Can not decode value. Multipart form-data decoder doesn't support to decode this values")
    }
}
