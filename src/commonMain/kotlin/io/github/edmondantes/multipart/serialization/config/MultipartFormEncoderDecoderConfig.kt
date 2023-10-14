package io.github.edmondantes.multipart.serialization.config

import kotlinx.serialization.modules.SerializersModule

public class MultipartFormEncoderDecoderConfig(
    public val stringEncoder: (String) -> ByteArray,
    public val stringDecoder: (ByteArray) -> String,
    public val serializersModule: SerializersModule,
)
