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

import java.util.regex.MatchResult

/**
 * The result of a match operation.
 *
 *
 * This interface contains query methods used to determine the results of
 * a match against a regular expression. The match boundaries, groups and
 * group boundaries can be seen but not modified through a MatchResult.
 *
 * @since 0.1.9
 */
interface MatchResult : MatchResult {
    /**
     * Returns the named capture groups in order
     *
     * @return the named capture groups
     */
    fun orderedGroups(): List<String>

    /**
     * Returns the named capture groups
     *
     * @return the named capture groups
     */
    fun namedGroups(): List<Map<String?, String>>

    /**
     * Returns the input subsequence captured by the given group during the
     * previous match operation.
     *
     * @param groupName name of capture group
     * @return the subsequence
     */
    fun group(groupName: String): String

    /**
     * Returns the start index of the subsequence captured by the given group
     * during this match.
     *
     * @param groupName name of capture group
     * @return the index
     */
    fun start(groupName: String?): Int

    /**
     * Returns the offset after the last character of the subsequence captured
     * by the given group during this match.
     *
     * @param groupName name of capture group
     * @return the offset
     */
    fun end(groupName: String?): Int
}