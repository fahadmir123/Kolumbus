package io.kolumbus.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Patterns
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import io.kolumbus.BuildConfig
import io.kolumbus.R
import io.kolumbus.extension.prettify
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

class TableActivity : AppCompatActivity() {
    companion object {
        private val EXTRA_TABLE_CLASS = BuildConfig.APPLICATION_ID + ".extra.TABLE_CLASS"

        fun start(context: Context, table: Class<out RealmObject>?) {
            val intent = Intent(context, TableActivity::class.java)
            intent.putExtra(EXTRA_TABLE_CLASS, table)

            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(R.layout.kolumbus_activity_table)

        val tableClass = this.intent.getSerializableExtra(EXTRA_TABLE_CLASS) as Class<out RealmObject>

        this.title = tableClass.simpleName.prettify()

        val methods = tableClass.declaredMethods.filter {
            Modifier.isPublic(it.modifiers) && !Modifier.isStatic(it.modifiers) && it.parameterTypes.size == 0
        }
        val table = this.findViewById(R.id.table) as TableLayout?
        var tableRow = this.layoutInflater.inflate(R.layout.kolumbus_table_row, table, false) as TableRow

        table?.addView(tableRow)

        methods.forEach {
            val header = this.layoutInflater.inflate(R.layout.kolumbus_table_row_header, tableRow, false) as TextView
            header.text = it.name.prettify()

            tableRow.addView(header)
        }

        val realm = Realm.getDefaultInstance()
        val entries = realm.where(tableClass).findAll()

        entries.forEach { entry ->
            tableRow = this.layoutInflater.inflate(R.layout.kolumbus_table_row, table, false) as TableRow

            table?.addView(tableRow)

            methods.forEach {
                val value = this.layoutInflater.inflate(R.layout.kolumbus_table_row_text, tableRow, false) as TextView
                val result = it.invoke(entry)

                if (result is Boolean) {
                    value.text = this.getString(if (result) R.string.kolumbus_yes else R.string.kolumbus_no)
                } else if (result is RealmList<*>) {
                    val returnType = it.genericReturnType as ParameterizedType
                    val genericType = returnType.actualTypeArguments[0] as Class<*>

                    // TODO Add click event to display linked data
                    value.text = Html.fromHtml(this.getString(R.string.kolumbus_linked_entries, result.size, genericType.simpleName.prettify()))
                } else if (result is String) {
                    if (result.isEmpty()) {
                        value.text = this.getString(R.string.kolumbus_empty)
                    } else {
                        if (Patterns.WEB_URL.matcher(result).matches()) {
                            value.text = Html.fromHtml(this.getString(R.string.kolumbus_link, result, result.replaceFirst("([^:/])/.*/".toRegex(), "$1/.../")))
                            value.movementMethod = LinkMovementMethod.getInstance()
                        } else {
                            try {
                                val color = Color.parseColor(result)
                                val drawable = this.getDrawable(R.drawable.kolumbus_color_preview) as LayerDrawable
                                drawable.getDrawable(1).setColorFilter(color, PorterDuff.Mode.SRC_IN)

                                value.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                                value.text = result
                            } catch (exception: IllegalArgumentException) {
                                if (result.length > 50) {
                                    // TODO Add click event to display the whole text
                                    value.text = "${result.subSequence(0, 47)}..."
                                } else {
                                    value.text = result
                                }
                            }
                        }
                    }
                } else if (result != null) {
                    value.text = result.toString()
                } else {
                    value.text = this.getString(R.string.kolumbus_null)
                }

                tableRow.addView(value)
            }
        }

        realm.close()
    }
}