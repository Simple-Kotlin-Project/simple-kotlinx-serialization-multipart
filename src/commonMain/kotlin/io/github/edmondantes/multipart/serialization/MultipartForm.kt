package io.github.edmondantes.multipart.serialization

import io.github.edmondantes.multipart.MultipartFormData
import io.github.edmondantes.multipart.serialization.config.MultipartFormConfig
import io.github.edmondantes.multipart.serialization.config.MultipartFormEncoderDecoderConfig
import io.github.edmondantes.multipart.serialization.decoder.MultipartFormRootDecoder
import io.github.edmondantes.multipart.serialization.encoder.MultipartFormRootEncoder
import io.github.edmondantes.multipart.serialization.util.MultipartFormDataBuilderWithActions
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule

/**
 * Support classes which have properties by only these types:
 * * Byte
 * * Char
 * * Short
 * * Int
 * * Long
 * * Double
 * * String
 * * ByteArray
 * * or List of one of above
 */
public class MultipartForm(
    internal val config: MultipartFormConfig,
    override val serializersModule: SerializersModule = config.serializersModule,
) : BinaryFormat {

    public val multipartFormEncoderDecoderConfig: MultipartFormEncoderDecoderConfig =
        MultipartFormEncoderDecoderConfig(config.stringEncoder, config.stringDecoder, serializersModule)

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        return deserializer.deserialize(
            MultipartFormRootDecoder(
                MultipartFormData.constructFromMultipart(config.multipartDecoder.decode(config.boundary, bytes)),
                multipartFormEncoderDecoderConfig,
            ),
        )
    }

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val builder = MultipartFormDataBuilderWithActions()
        val formEncoder = MultipartFormRootEncoder(builder, multipartFormEncoderDecoderConfig)
        serializer.serialize(formEncoder, value)

        return config.multipartEncoder.encode(config.boundary, builder.build())
    }

    public companion object {
        public val Default: MultipartForm = MultipartForm(MultipartFormConfig.Default)
    }
}
