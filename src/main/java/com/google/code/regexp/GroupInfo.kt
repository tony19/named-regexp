/**
 * Copyright (C) 2012-2013 The named-regexp Authors
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

import java.io.Serializable

/**
 * Contains the position and group index of capture groups
 * from a named pattern
 */
open class GroupInfo
/**
 * Constructs a `GroupInfo` with a group index and string
 * position
 *
 * @param groupIndex the group index
 * @param pos the position
 */(private val groupIndex: Int, private val pos: Int) : Serializable {

    /**
     * Gets the string position of the group within a named pattern
     *
     * @return the position
     */
    fun pos() = pos

    /**
     * Gets the group index of the named capture group
     *
     * @return the group index
     */
    fun groupIndex() = groupIndex

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (obj !is GroupInfo) {
            return false
        }
        val other = obj
        return pos == other.pos && groupIndex == other.groupIndex
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    override fun hashCode() = pos xor groupIndex

    companion object {
        /**
         * Determines if a de-serialized file is compatible with this class.
         *
         * Maintainers must change this value if and only if the new version
         * of this class is not compatible with old versions. See Sun docs
         * for [](http://java.sun.com/products/jdk/1.1/docs/guide)
         * /serialization/spec/version.doc.html> details.
         *
         * Not necessary to include in first version of the class, but
         * included here as a reminder of its importance.
         */
        private const val serialVersionUID = 1L
    }

}