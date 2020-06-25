/**
 * Copyright (C) 2012-2014 The named-regexp Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.regexp

import com.google.code.regexp.Pattern.Companion.compile
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import java.io.*

/**
 * Verifies that [Pattern] is [Serializable]
 */
class PatternSerializationTest {
    @Test
    fun deserializedPatternMatchesOriginal() {
        Assert.assertThat(
            pattOut,
            CoreMatchers.`is`(
                CoreMatchers.equalTo(pattIn)
            )
        )
    }

    companion object {
        var pattIn: Pattern? = null
        var pattOut: Pattern? = null

        @BeforeClass
        @Throws(IOException::class, ClassNotFoundException::class)
        fun beforeClass() {
            pattIn =
                compile("(?<foo>\\w+) (?<bar>\\d+) (.*)\\s+")
            val file = File.createTempFile("pattern", ".ser")
            pattOut = try {
                val fos = FileOutputStream(file)
                val oos = ObjectOutputStream(fos)
                try {
                    oos.writeObject(pattIn)
                } finally {
                    oos.close()
                }
                val fis = FileInputStream(file)
                val ois = ObjectInputStream(fis)
                try {
                    ois.readObject() as Pattern
                } finally {
                    ois.close()
                }
            } finally {
                file.delete()
            }
        }
    }
}