package io.github.edmondantes.multipart.serialization.config

import io.github.edmondantes.multipart.MultipartDecoder
import io.github.edmondantes.multipart.MultipartEncoder
import io.github.edmondantes.multipart.impl.DefaultMultipartEncoderDecoder
import io.github.edmondantes.multipart.impl.DefaultMultipartEncoderDecoderConfiguration
import io.github.edmondantes.multipart.serialization.util.nextAlphanumericString
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.random.Random

public class MultipartFormConfig(
    public val boundary: String = Random.nextAlphanumericString(70),
    multipartEncoder: MultipartEncoder? = null,
    multipartDecoder: MultipartDecoder? = null,
    public val stringEncoder: (String) -> ByteArray = String::encodeToByteArray,
    public val stringDecoder: (ByteArray) -> String = ByteArray::decodeToString,
    public val serializersModule: SerializersModule = EmptySerializersModule(),
) {
    public val multipartEncoder: MultipartEncoder
    public val multipartDecoder: MultipartDecoder

    init {
        if (multipartEncoder == null || multipartDecoder == null) {
            val encoderDecoder = DefaultMultipartEncoderDecoder(
                DefaultMultipartEncoderDecoderConfiguration(
                    shouldCheckBoundary = true,
                    shouldCheckTrailerCounts = true,
                    stringEncoder,
                    stringDecoder,
                    "\n",
                ),
            )

            this.multipartEncoder = multipartEncoder ?: encoderDecoder
            this.multipartDecoder = multipartDecoder ?: encoderDecoder
        } else {
            this.multipartEncoder = multipartEncoder
            this.multipartDecoder = multipartDecoder
        }
    }

    public companion object {
        public val Default: MultipartFormConfig

        init {
            val encoderDecoder = DefaultMultipartEncoderDecoder(
                DefaultMultipartEncoderDecoderConfiguration(
                    shouldCheckBoundary = true,
                    shouldCheckTrailerCounts = true,
                    String::encodeToByteArray,
                    ByteArray::decodeToString,
                    "\n",
                ),
            )

            Default = MultipartFormConfig(
                ('1'..'9').joinToString("") + '0' + ('a'..'z').joinToString(""),
                encoderDecoder,
                encoderDecoder,
                String::encodeToByteArray,
                ByteArray::decodeToString,
                EmptySerializersModule(),
            )
        }
    }
}
