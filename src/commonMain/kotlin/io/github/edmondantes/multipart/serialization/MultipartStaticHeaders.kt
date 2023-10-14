package io.github.edmondantes.multipart.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
public annotation class MultipartStaticHeaders(val headers: Array<MultipartStaticHeader>)
