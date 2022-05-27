package jp.techacademy.madoka.inagaki.mytaskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract.Attendees.query
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.realm.*
import kotlinx.android.synthetic.main.activity_category_list.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_input.*
import java.util.*


const val EXTRA_Category = "jp.techacademy.madoka.inagaki.mytaskapp.CATEGORY"

class CategoryList : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private var mCategory: Category? = null

    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mCategoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)

        //realmの設定
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        // 遷移したとき最初に　ListView2へ全項目表示する
        val categoryRealmResults = mRealm.where(Category::class.java).findAll().sort(
            "id",
            Sort.DESCENDING
        )
        mCategoryAdapter = CategoryAdapter(this)
        mCategoryAdapter.mCategoryList = mRealm.copyFromRealm(categoryRealmResults)
        listView2.adapter = mCategoryAdapter

        //追加ボタンを押す
        btnAddCategory.setOnClickListener { view ->
            addCategory()
            reloadListView()
        }
    }

    //全項目表示
    private fun reloadListView() {
        //すべてのタスクを並べて
        val categoryRealmResults = mRealm.where(Category::class.java).findAll().sort(
            "id",
            Sort.DESCENDING
        )

        toListView(categoryRealmResults)
    }


    //リストビュー2アダプターへ
    private fun toListView(categoryRealmResults: RealmResults<Category>) {
        mCategoryAdapter.mCategoryList = mRealm.copyFromRealm(categoryRealmResults)
        listView2.adapter = mCategoryAdapter
        //変わったことを伝える
        mCategoryAdapter.notifyDataSetChanged()
    }

//    ここから追加ーーーーーーーーーーーーーーーーーーー

    private fun addCategory() {
        val realm = Realm.getDefaultInstance()

        //追加したりするときに必要なもの
        realm.beginTransaction()

        mCategory = Category()

        val categoryRealmResults = realm.where(Category::class.java).findAll()

        val maxId = categoryRealmResults.max("id")
        Log.d("test",maxId.toString())

        val identifier: Int =
        //max(最大)のidがnull→まだ何もない→idは0になる
            //nullじゃない→すでにidがある→一番大きなidに+1して最新のidを生成
            if (maxId != null) {
                maxId!!.toInt() + 1
            } else {
                0
            }
        //それをidとして代入
        mCategory!!.id = identifier

        //editに入力したテキストをタイトル・コンテンツそれぞれに代入
        val categoryName = add_category_edit_text.text.toString()

        //mTaskに代入
        mCategory!!.categoryName = categoryName

        add_category_edit_text.text = null

        //できあがったmTaskをコミット
        realm.copyToRealmOrUpdate(mCategory!!)
        realm.commitTransaction()

        realm.close()
    }


    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }
}

