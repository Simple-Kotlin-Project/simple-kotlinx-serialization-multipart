package io.github.edmondantes.multipart.serialization.util

import io.github.edmondantes.multipart.MultipartFormData
import io.github.edmondantes.multipart.builder.MultipartFormDataBuilder

public class MultipartFormDataBuilderWithActions : MultipartFormDataBuilder() {

    private val onBuildActions: MutableList<(MultipartFormDataBuilder) -> Unit> = mutableListOf()

    public fun addOnBuild(action: (MultipartFormDataBuilder) -> Unit): MultipartFormDataBuilderWithActions = apply {
        onBuildActions.add(action)
    }

    override fun build(): MultipartFormData {
        onBuildActions.forEach { action ->
            action(this)
        }

        return super.build()
    }
}
