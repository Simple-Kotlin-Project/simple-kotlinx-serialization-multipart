package io.github.edmondantes.serialization.multipart.encoder

import io.github.edmondantes.multipart.Multipart
import io.github.edmondantes.multipart.MultipartPart
import io.github.edmondantes.multipart.builder.MultipartFormDataBuilder
import io.github.edmondantes.multipart.builder.MultipartPartBuilder
import io.github.edmondantes.multipart.builder.multipartFormData
import io.github.edmondantes.multipart.builder.multipartPart
import io.github.edmondantes.multipart.serialization.MultipartDynamicHeader
import io.github.edmondantes.multipart.serialization.MultipartForm
import io.github.edmondantes.multipart.serialization.MultipartStaticHeader
import io.github.edmondantes.multipart.serialization.util.nextAlphanumericString
import io.github.edmondantes.multipart.serialization.util.nextChar
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlin.random.Random
import kotlin.reflect.KProperty0
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MultipartFormDataEncoderTest {

    @Test
    fun encodeEmptyTest() {
        encode(::EmptyTestClass) { Multipart.Empty }
    }

    @Test
    fun decodeEmptyTest() {
        decode(::EmptyTestClass) { Multipart.Empty }
    }

    @Test
    fun encodeNullTest() {
        encode(::NullTestClass, ::NULL_TEST_BYTES)
    }

    @Test
    fun decodeNullTest() {
        decode(::NullTestClass, ::NULL_TEST_BYTES)
    }

    @Test
    fun encodeFlatTest() {
        encode(::FlatTestClass, ::FLAT_TEST_BYTES)
    }

    @Test
    fun decodeFlatTest() {
        decode(::FlatTestClass, ::FLAT_TEST_BYTES)
    }

    /**
     * Because default string encoder is [String.encodeToByteArray], and the method can change some characters
     *
     * **See also** [Kotlin Docs](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/encode-to-byte-array.html)
     */
    @Test
    fun encodeWrongChar() {
        val obj = FlatTestClass(char = Char(56152))
        encode({ obj }, ::FLAT_TEST_BYTES)
    }

    /**
     * Because default string encoder is [String.encodeToByteArray], and the method can change some characters
     *
     * **See also** [Kotlin Docs](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/encode-to-byte-array.html)
     */
    @Test
    fun decodeWrongChar() {
        assertFailsWith<AssertionError> {
            val obj = FlatTestClass(char = Char(56152))
            decode({ obj }, ::FLAT_TEST_BYTES)
        }
    }

    @Test
    fun encodeByteArrayTest() {
        encode(::ByteArrayTestClass, ::BYTE_ARRAY_BYTES)
    }

    @Test
    fun decodeByteArrayTest() {
        decode(::ByteArrayTestClass, ::BYTE_ARRAY_BYTES)
    }

    @Test
    fun encodeIntArrayTest() {
        encode(::IntArrayTestClass, ::INT_ARRAY_BYTES)
    }

    @Test
    fun decodeIntArrayTest() {
        decode(::IntArrayTestClass, ::INT_ARRAY_BYTES)
    }

    @Test
    fun encodeListTest() {
        encode(::ListTestClass, ::LIST_BYTES)
    }

    @Test
    fun decodeListTest() {
        decode(::ListTestClass, ::LIST_BYTES)
    }

    @Test
    fun encodeListByteArrayTest() {
        encode(::ListByteArrayTestClass, ::LIST_BYTE_ARRAY_BYTES)
    }

    @Test
    fun decodeListByteArrayTest() {
        decode(::ListByteArrayTestClass, ::LIST_BYTE_ARRAY_BYTES)
    }

    @Test
    fun encodeListIntArrayTest() {
        assertFailsWith<SerializationException>("Can not encode value. Multipart form-data encoder doesn't support to encode this values") {
            encode(::ListIntArrayTestClass, ::LIST_INT_ARRAY_BYTES)
        }
    }

    @Test
    fun decodeListIntArrayTest() {
        assertFailsWith<SerializationException>("Can not decode value. Multipart form-data decoder doesn't support to decode this values") {
            decode(::ListIntArrayTestClass, ::LIST_INT_ARRAY_BYTES)
        }
    }

    @Test
    fun encodeListListTest() {
        assertFailsWith<SerializationException>("Can not encode value. Multipart form-data encoder doesn't support to encode this values") {
            encode(::ListListTestClass, ::LIST_LIST_BYTES)
        }
    }

    @Test
    fun decodeListListTest() {
        assertFailsWith<SerializationException>("Can not encode value. Multipart form-data encoder doesn't support to encode this values") {
            decode(::ListListTestClass, ::LIST_LIST_BYTES)
        }
    }

    @Test
    fun encodeMapTest() {
        assertFailsWith<SerializationException>("Can not encode value. Multipart form-data encoder doesn't support to encode this values") {
            encode(::MapTestClass) { Multipart.Empty }
        }
    }

    @Test
    fun encodeStaticHeaderTest() {
        encode(::StaticHeaderTestClass, ::STATIC_HEADER_BYTES)
    }

    @Test
    fun decodeStaticHeaderTest() {
        decode(::StaticHeaderTestClass, ::STATIC_HEADER_BYTES)
    }

    @Test
    fun encodeStaticHeaderAttributeTest() {
        encode(::StaticHeaderAttributeTestClass, ::STATIC_HEADER_ATTRIBUTE_BYTES)
    }

    @Test
    fun decodeStaticHeaderAttributeTest() {
        decode(::StaticHeaderAttributeTestClass, ::STATIC_HEADER_ATTRIBUTE_BYTES)
    }

    @Test
    fun encodeDynamicHeaderTest() {
        encode(::DynamicHeaderTestClass, ::DYNAMIC_HEADER_BYTES)
    }

    @Test
    fun decodeDynamicHeaderTest() {
        decode(::DynamicHeaderTestClass, ::DYNAMIC_HEADER_BYTES)
    }

    @Test
    fun encodeDynamicHeaderAttributeTest() {
        encode(::DynamicHeaderAttributeTestClass, ::DYNAMIC_HEADER_ATTRIBUTE_BYTES)
    }

    @Test
    fun decodeDynamicHeaderAttributeTest() {
        decode(::DynamicHeaderAttributeTestClass, ::DYNAMIC_HEADER_ATTRIBUTE_BYTES)
    }

    private inline fun <reified T> encode(factory: () -> T, multipartFactory: (T) -> Multipart) {
        val objForEncode = factory()
        val expected = multipartFactory(objForEncode).let { encoder.encode(boundary, it) }
        val actual = format.encodeToByteArray(objForEncode)

        assertContentEquals(expected, actual)
    }

    private inline fun <reified T> decode(factory: () -> T, multipartFactory: (T) -> Multipart) {
        val expected = factory()
        val objForDecode = multipartFactory(expected).let { encoder.encode(boundary, it) }
        val actual = format.decodeFromByteArray<T>(objForDecode)

        assertEquals(expected, actual)
    }

    private companion object {
        val format = MultipartForm.Default
        val encoder = format.config.multipartEncoder
        val boundary = format.config.boundary

        fun NULL_TEST_BYTES(obj: NullTestClass): Multipart = multipartFormData {
            addNamed(obj::simple)
        }

        fun FLAT_TEST_BYTES(obj: FlatTestClass): Multipart = multipartFormData {
            addNamed(obj::byte)
            addNamed(obj::short)
            addNamed(obj::char)
            addNamed(obj::int)
            addNamed(obj::long)
            addNamed(obj::float)
            addNamed(obj::double)
            addNamed(obj::string)
        }

        fun BYTE_ARRAY_BYTES(obj: ByteArrayTestClass): Multipart = multipartFormData {
            addNamed(obj::simple)
            addNamed(obj::array)
        }

        fun INT_ARRAY_BYTES(obj: IntArrayTestClass): Multipart = multipartFormData {
            addNamed(obj::simple)
            obj.array.forEach { element ->
                add(
                    "array",
                    multipartPart {
                        body = element.toString().encodeToByteArray()
                    },
                )
            }
        }

        fun LIST_BYTES(obj: ListTestClass): Multipart = multipartFormData {
            obj.list.forEach { element ->
                add(
                    "list",
                    multipartPart {
                        body = element.encodeToByteArray()
                    },
                )
            }
        }

        fun LIST_BYTE_ARRAY_BYTES(obj: ListByteArrayTestClass): Multipart = multipartFormData {
            obj.list.forEach { element ->
                add(
                    "list",
                    multipartPart {
                        body = element
                    },
                )
            }
        }

        fun LIST_INT_ARRAY_BYTES(obj: ListIntArrayTestClass): Multipart = multipartFormData {
            obj.list.flatMap { it.toList() }.forEach { element ->
                add(
                    "list",
                    multipartPart {
                        body = element.toString().encodeToByteArray()
                    },
                )
            }
        }

        fun LIST_LIST_BYTES(obj: ListListTestClass): Multipart = multipartFormData {
            obj.list.flatten().forEach { element ->
                add("list", multipartPart { body = element.encodeToByteArray() })
            }
        }

        fun STATIC_HEADER_BYTES(obj: StaticHeaderTestClass): Multipart = multipartFormData {
            addNamed(obj::field) {
                header("Header") {
                    value = "Element1"
                }
            }
        }

        fun STATIC_HEADER_ATTRIBUTE_BYTES(obj: StaticHeaderAttributeTestClass): Multipart = multipartFormData {
            addNamed(obj::field) {
                header("Header") {
                    value = "Element1"
                    attributes["attr"] = "Attribute1"
                }
            }
        }

        fun DYNAMIC_HEADER_BYTES(obj: DynamicHeaderTestClass): Multipart = multipartFormData {
            addNamed(obj::field) {
                header("Header") {
                    value = obj.header
                }
            }
        }

        fun DYNAMIC_HEADER_ATTRIBUTE_BYTES(obj: DynamicHeaderAttributeTestClass): Multipart = multipartFormData {
            addNamed(obj::field) {
                header("Header") {
                    value = obj.header
                    attributes["attr"] = obj.attribute
                }
            }
        }

        @Serializable
        class EmptyTestClass {
            override fun equals(other: Any?): Boolean =
                if (this === other || other is EmptyTestClass) {
                    true
                } else {
                    super.equals(other)
                }

            override fun hashCode(): Int {
                return this::class.hashCode()
            }
        }

        @Serializable
        data class NullTestClass(
            val simple: String = Random.nextAlphanumericString(10),
            val nullable: String? = null,
        )

        @Serializable
        data class FlatTestClass(
            val byte: Byte = Random.nextBytes(1)[0],
            val short: Short = Random.nextInt(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort(),
            val char: Char = Random.nextChar('a', 'z'),
            val int: Int = Random.nextInt(),
            val long: Long = Random.nextLong(),
            val float: Float = Random.nextFloat(),
            val double: Double = Random.nextDouble(),
            val string: String = Random.nextAlphanumericString(100),
        )

        @Serializable
        data class ByteArrayTestClass(
            val simple: String = Random.nextAlphanumericString(10),
            val array: ByteArray = Random.nextAlphanumericString(10).encodeToByteArray(),
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is ByteArrayTestClass) return false

                if (simple != other.simple) return false
                if (!array.contentEquals(other.array)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = simple.hashCode()
                result = 31 * result + array.contentHashCode()
                return result
            }
        }

        @Serializable
        data class IntArrayTestClass(
            val simple: String = Random.nextAlphanumericString(10),
            val array: IntArray = generateSequence { Random.nextInt() }.take(10).toList().toIntArray(),
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is IntArrayTestClass) return false

                if (simple != other.simple) return false
                if (!array.contentEquals(other.array)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = simple.hashCode()
                result = 31 * result + array.contentHashCode()
                return result
            }
        }

        @Serializable
        data class ListTestClass(
            val list: List<String> = generateSequence { Random.nextAlphanumericString(10) }.take(10).toList(),
        )

        @Serializable
        data class ListByteArrayTestClass(
            val list: List<ByteArray> = generateSequence { Random.nextBytes(70) }.take(10).toList(),
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is ListByteArrayTestClass) return false

                if (list.size != other.list.size) return false

                list.forEachIndexed { index, element ->
                    if (!element.contentEquals(other.list[index])) {
                        return false
                    }
                }

                return true
            }

            override fun hashCode(): Int {
                return list.hashCode()
            }
        }

        @Serializable
        data class ListIntArrayTestClass(
            val list: List<IntArray> = generateSequence {
                generateSequence { Random.nextInt() }.take(10).toList().toIntArray()
            }.take(10).toList(),
        )

        @Serializable
        data class ListListTestClass(
            val list: List<List<String>> = generateSequence {
                generateSequence { Random.nextAlphanumericString(10) }.take(10).toList()
            }.take(10).toList(),
        )

        @Serializable
        data class MapTestClass(
            val map: Map<String, String> = generateSequence { Random.nextAlphanumericString(10) }
                .take(10)
                .map { it to it }
                .toMap(),
        )

        @Serializable
        data class StaticHeaderTestClass(
            @MultipartStaticHeader("Header", "Element1")
            val field: String = Random.nextAlphanumericString(10),
        )

        @Serializable
        data class StaticHeaderAttributeTestClass(
            @MultipartStaticHeader("Header", "Element1")
            @MultipartStaticHeader("Header", "Attribute1", "attr")
            val field: String = Random.nextAlphanumericString(10),
        )

        @Serializable
        data class DynamicHeaderTestClass(
            val field: String = Random.nextAlphanumericString(10),
            @MultipartDynamicHeader("Header", "field")
            val header: String = Random.nextAlphanumericString(10),
        )

        @Serializable
        data class DynamicHeaderAttributeTestClass(
            val field: String = Random.nextAlphanumericString(10),
            @MultipartDynamicHeader("Header", "field")
            val header: String = Random.nextAlphanumericString(10),
            @MultipartDynamicHeader("Header", "field", "attr")
            val attribute: String = Random.nextAlphanumericString(10),
        )

        private inline fun Any.part(block: MultipartPartBuilder.() -> Unit = {}): MultipartPart =
            multipartPart {
                body = if (this@part is ByteArray) {
                    this@part
                } else {
                    this@part.toString().encodeToByteArray()
                }
                apply(block)
            }

        private inline fun MultipartFormDataBuilder.addNamed(
            name: String,
            any: Any,
            block: MultipartPartBuilder.() -> Unit = {},
        ) {
            add(name, any.part(block))
        }

        private inline fun MultipartFormDataBuilder.addNamed(
            property: KProperty0<*>,
            block: MultipartPartBuilder.() -> Unit = {},
        ) {
            property.get()?.also { addNamed(property.name, it, block) }
        }
    }
}
