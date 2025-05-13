/*
Privacy Friendly To-Do List
Copyright (C) 2024-2025  Christian Adams

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package android.util

/**
 * Mocking of logger for unit tests.
 */
@Suppress("unused")
object Log {
    @JvmStatic
    fun v(tag: String, msg: String): Int {
        println("VERBOSE: [$tag] $msg")
        return 0
    }

    @JvmStatic
    fun d(tag: String, msg: String): Int {
        println("DEBUG:   [$tag] $msg")
        return 0
    }

    @JvmStatic
    fun i(tag: String, msg: String): Int {
        println("INFO:    [$tag] $msg")
        return 0
    }

    @JvmStatic
    fun w(tag: String, msg: String): Int {
        println("WARN:    [$tag] $msg")
        return 0
    }

    @JvmStatic
    fun e(tag: String, msg: String): Int {
        println("ERROR:   [$tag] $msg")
        return 0
    }
}