package com.github.mvysny.fakeservlet

import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.security.Principal
import kotlin.test.expect

data class MockPrincipal(private val name: String, val allowedRoles: List<String> = listOf()) : Principal {
    override fun getName(): String = name

    fun isUserInRole(role: String): Boolean = allowedRoles.contains(role)
}

/**
 * Serializes the object to a byte array
 * @return the byte array containing this object serialized form.
 */
fun Serializable?.serializeToBytes(): ByteArray = ByteArrayOutputStream().also { ObjectOutputStream(it).writeObject(this) }.toByteArray()

inline fun <reified T: Serializable> ByteArray.deserialize(): T? = T::class.java.cast(
    ObjectInputStream(inputStream()).readObject())

/**
 * Clones this object by serialization and returns the deserialized clone.
 * @return the clone of this
 */
fun <T : Serializable> T.cloneBySerialization(): T = javaClass.cast(serializeToBytes().deserialize())

/**
 * Expects that [actual] list of objects matches [expected] list of objects. Fails otherwise.
 */
fun <T> expectList(vararg expected: T, actual: ()->List<T>) {
    expect(expected.toList(), actual)
}
