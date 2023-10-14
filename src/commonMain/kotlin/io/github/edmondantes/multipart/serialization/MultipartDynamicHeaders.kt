package io.github.edmondantes.multipart.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class MultipartDynamicHeaders(val headers: Array<MultipartDynamicHeader>)
