/**
 *  Copyright 2011 SECTRA Imtec AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * @author      Markus Berg <markus.berg @ sectra.se>
 * @version     1.6
 * @since       2011-09-21
 */
package com.sectra.jfileshare.objects;

/**
 * User that does not exist in database has been requested
 * @author markus
 */
public class NoSuchUserException extends Exception {

    /**
     * Creates a new instance of <code>NoSuchUserException</code> without detail message.
     */
    public NoSuchUserException() {
    }


    /**
     * Constructs an instance of <code>NoSuchUserException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchUserException(String msg) {
        super(msg);
    }
}
