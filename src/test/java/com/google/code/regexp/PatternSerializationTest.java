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
package com.google.code.regexp;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Verifies that {@link Pattern} is {@link Serializable}
 */
public class PatternSerializationTest {
    static Pattern pattIn;
    static Pattern pattOut;

    @BeforeClass
    static public void beforeClass() throws IOException, ClassNotFoundException {
        pattIn = Pattern.compile("(?<foo>\\w+) (?<bar>\\d+) (.*)\\s+");
        File file = File.createTempFile("pattern", ".ser");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            try {
                oos.writeObject(pattIn);
            } finally {
                oos.close();
            }

            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            try {
                pattOut = (Pattern) ois.readObject();
            } finally {
                ois.close();
            }
        } finally {
            file.delete();
        }
    }

    @Test
    public void deserializedPatternMatchesOriginal() {
        assertThat(pattOut, is(equalTo(pattIn)));
    }

}
