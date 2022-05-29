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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_input.*
import java.util.*


const val EXTRA_TASK = "jp.techacademy.madoka.inagaki.mytaskapp.TASK"

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        spnCategory2.adapter = CategoryAdapter(this)


        fab.setOnClickListener { view ->
            val intent = Intent(this, InputActivity::class.java)
            startActivity(intent)
        }

        btnCategoryList.setOnClickListener { view ->
            val intent = Intent(this, CategoryList::class.java)
            startActivity(intent)
        }

        btnCategory.setOnClickListener{
            categorySearch()
        }

        // Realm設定
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        // ListView設定
        mTaskAdapter = TaskAdapter(this)

        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        //長押しでタスク削除
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            //taskにタップしたタスクを代入している
            val task = parent.adapter.getItem(position) as Task

            //ダイアログ
            val builder = AlertDialog.Builder(this)
            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除します")

            //ok押したらresultsにtaskさがして代入
            builder.setPositiveButton("OK"){ _, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                //アラームを削除する
                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

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
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort(
            "date",
            Sort.DESCENDING
        )

        toListView(taskRealmResults)
    }

    //カテゴリ絞り込みして表示
    private fun categorySearch() {
        val searchWord = sreachEdit.text.toString()

        if(searchWord.isEmpty()) {
            reloadListView()
        }else{
            val query: RealmQuery<Task> = mRealm.where(Task::class.java)
            query.equalTo("category1", searchWord)
            val taskRealmResults: RealmResults<Task> = query.findAll().sort(
                "date",
                Sort.DESCENDING
            )

            toListView(taskRealmResults)
        }
    }

    //リストビュー1アダプターへ
    private fun toListView(taskRealmResults: RealmResults<Task>) {
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)
        listView1.adapter = mTaskAdapter
        //変わったことを伝える
        mTaskAdapter.notifyDataSetChanged()
    }


    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }
}

