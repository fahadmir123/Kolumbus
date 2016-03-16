/*
 * Copyright (C) 2016 MGaetan89
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kolumbus

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*

object Analyzer {
    fun getAccessors(table: Class<out RealmObject>?, fields: List<Field>): Map<String, Method?> {
        val methods = table?.declaredMethods?.map { it.name } ?: emptyList()

        return fields.associate {
            it.name to if (methods.contains("get${it.name.capitalize()}")) {
                table?.getMethod("get${it.name.capitalize()}")
            } else if (methods.contains("is${it.name.capitalize()}")) {
                table?.getMethod("is${it.name.capitalize()}")
            } else {
                null
            }
        }.filterValues { it != null }
    }

    fun getRealmFields(table: Class<out RealmObject>?): List<Field> {
        return table?.declaredFields?.filter {
            !Modifier.isStatic(it.modifiers) && !it.isAnnotationPresent(Ignore::class.java)
        }?.sortedWith(Comparator { first, second ->
            if (first.isAnnotationPresent(PrimaryKey::class.java) && !second.isAnnotationPresent(PrimaryKey::class.java)) {
                -1
            } else if (!first.isAnnotationPresent(PrimaryKey::class.java) && second.isAnnotationPresent(PrimaryKey::class.java)) {
                1
            } else {
                first.name.compareTo(second.name)
            }
        }) ?: emptyList()
    }
}