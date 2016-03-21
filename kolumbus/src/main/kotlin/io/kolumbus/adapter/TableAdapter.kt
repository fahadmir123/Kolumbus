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

package io.kolumbus.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.kolumbus.Kolumbus
import io.kolumbus.R
import io.kolumbus.extension.prettify
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

class TableAdapter(val entries: List<RealmObject>, val fields: List<Field>, val methods: Map<String, Method?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_FIELD = 1

    override fun getItemCount() = this.entries.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_FIELD
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is FieldViewHolder) {
            this.bindFieldRow(this.entries[position - 1], holder)
        } else if ( holder is HeaderViewHolder) {
            this.bindHeaderRow(holder)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        val childLayoutRes = this.getLayoutForViewType(viewType)
        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.kolumbus_adapter_table, parent, false) as ViewGroup

        this.fields.forEach {
            layoutInflater.inflate(childLayoutRes, view, true)
        }

        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(view)
        } else {
            FieldViewHolder(view)
        }
    }

    private fun bindFieldRow(entry: RealmObject, holder: FieldViewHolder) {
        this.processField(holder) { fieldView, field ->
            val result = this.methods[field.name]?.invoke(entry)

            if (result is Boolean) {
                Kolumbus.architect.displayBoolean(fieldView, result)
            } else if (result is Float) {
                Kolumbus.architect.displayFloat(fieldView, result)
            } else if (result is Int) {
                Kolumbus.architect.displayInt(fieldView, result)
            } else if (result is RealmList<*>) {
                val returnType = field.genericType as ParameterizedType
                val type = returnType.actualTypeArguments[0] as Class<RealmObject>

                Kolumbus.architect.displayRealmList(fieldView, result, type)
            } else if (result is RealmObject) {
                Kolumbus.architect.displayRealmObject(fieldView, result)
            } else if (result is String) {
                if (result.isEmpty()) {
                    Kolumbus.architect.displayEmpty(fieldView)
                } else {
                    if (Patterns.WEB_URL.matcher(result).matches()) {
                        Kolumbus.architect.displayUrl(fieldView, result)
                    } else {
                        try {
                            val color = Color.parseColor(result)

                            Kolumbus.architect.displayColor(fieldView, result, color)
                        } catch (exception: IllegalArgumentException) {
                            Kolumbus.architect.displayString(fieldView, result)
                        }
                    }
                }
            } else if (result != null) {
                Kolumbus.architect.displayAny(fieldView, result)
            } else {
                Kolumbus.architect.displayNull(fieldView)
            }
        }
    }

    private fun bindHeaderRow(holder: HeaderViewHolder) {
        this.processField(holder) { fieldView, field ->
            fieldView.text = if (field.isAnnotationPresent(PrimaryKey::class.java)) {
                "#${field.name.prettify()}"
            } else {
                field.name.prettify()
            }
        }
    }

    private fun getLayoutForViewType(viewType: Int): Int {
        if (viewType == VIEW_TYPE_HEADER) {
            return R.layout.kolumbus_adapter_table_header
        }

        return R.layout.kolumbus_adapter_table_text
    }

    private fun processField(holder: RecyclerView.ViewHolder, callback: (fieldView: TextView, field: Field) -> Unit) {
        this.fields.forEachIndexed { index, field ->
            val fieldView = (holder.itemView as ViewGroup).getChildAt(index) as TextView

            callback(fieldView, field)
        }
    }

    class FieldViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)
}