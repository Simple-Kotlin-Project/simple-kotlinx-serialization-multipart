package io.github.edmondantes.multipart.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@Repeatable
public annotation class MultipartDynamicHeader(
    val headerName: String,
    val serialPropertyName: String,
    val headerAttribute: String = "",
)
