package jp.techacademy.madoka.inagaki.mytaskapp

import io.realm.RealmObject
import java.io.Serializable

open class Category: RealmObject(), Serializable {
    var name: String = ""
    var id: Int = 0
}