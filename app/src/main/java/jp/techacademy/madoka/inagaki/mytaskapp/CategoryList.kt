package jp.techacademy.madoka.inagaki.mytaskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.nfc.cardemulation.CardEmulation.EXTRA_CATEGORY
import android.os.Bundle
import android.provider.CalendarContract.Attendees.query
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.realm.*
import kotlinx.android.synthetic.main.activity_category_list.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_input.*
import java.util.*


const val EXTRA_CATEGORY = "jp.techacademy.madoka.inagaki.mytaskapp.CATEGORY"

class CategoryList : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private var mCategory: Category? = null

    private var name: String? = null
    private var categoryId: Int = 0

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


        //すでにあるカテゴリをタップしたときの処理
        listView2.setOnItemClickListener { parent, _, position, _ ->
            val category = parent.adapter.getItem(position) as Category
            name = category.categoryName
            categoryId = category.id
            btnAddCategory.setText("上書き")

            Log.d("test", name.toString())
            Log.d("test", categoryId.toString())
            add_category_edit_text.setText(name)
        }

        //すでにあるカテゴリを長押したときの処理
        listView2.setOnItemLongClickListener { parent, _, position, _ ->
            val category = parent.adapter.getItem(position) as Category

            //ダイアログ
            val builder = AlertDialog.Builder(this)
            builder.setTitle("削除")
            builder.setMessage("カテゴリ" + category.categoryName + "を削除します")

            //ok押したらresultsにtaskさがして代入
            builder.setPositiveButton("OK") { _, _ ->
                val results =
                    mRealm.where(Category::class.java).equalTo("id", category.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
            }
            reloadListView()
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

    private fun addCategory() {
        val realm = Realm.getDefaultInstance()

        //追加したりするときに必要なもの
        realm.beginTransaction()

        mCategory = Category()
        btnAddCategory.setText("追加")

        if (categoryId != 0) {
            //カテゴリidが0じゃないならもともとのidを代入して上書きにする
            mCategory!!.id = categoryId
        }else{
            val categoryRealmResults = realm.where(Category::class.java).findAll()

            val maxId = categoryRealmResults.max("id")
            Log.d("test", maxId.toString())

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
        }

        //editに入力したテキストをタイトル・コンテンツそれぞれに代入
        val categoryName = add_category_edit_text.text.toString()

        //mTaskに代入
        mCategory!!.categoryName = categoryName

        add_category_edit_text.text = null

        //できあがったmTaskをコミット
        realm.copyToRealmOrUpdate(mCategory!!)
        realm.commitTransaction()

        realm.close()

        categoryId = 0
        }


    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }
}

