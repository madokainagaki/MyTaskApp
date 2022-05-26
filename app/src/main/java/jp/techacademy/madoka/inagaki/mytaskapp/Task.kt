package jp.techacademy.madoka.inagaki.mytaskapp

import android.icu.util.ULocale
import io.realm.RealmObject
import java.io.Serializable
import java.util.*
import io.realm.annotations.PrimaryKey
import java.util.Locale.Category

open class Task: RealmObject(), Serializable {
    var title: String =""
    var contents: String = ""
    var date: Date = Date()

    @PrimaryKey
    var id: Int = 0
    var category1: String = ""
}
