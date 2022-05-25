package jp.techacademy.madoka.inagaki.mytaskapp

import io.realm.RealmObject
import java.io.Serializable
import java.util.*
import io.realm.annotations.PrimaryKey

open class Task: RealmObject(), Serializable {
    var title: String =""
    var contents: String = ""
    var date: Date = Date()

    @PrimaryKey
    var id: Int = 0
}