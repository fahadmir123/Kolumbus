package io.kolumbus.demo.model

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey

open class Category : RealmModel {
    open var color = ""
    @PrimaryKey
    open var id = 0
    open var name = ""
}
