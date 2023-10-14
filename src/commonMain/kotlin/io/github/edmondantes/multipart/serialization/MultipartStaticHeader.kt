package io.github.edmondantes.multipart.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Repeatable
public annotation class MultipartStaticHeader(val name: String, val value: String, val headerAttribute: String = "")
