package jp.techacademy.madoka.inagaki.mytaskapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_input.*
import java.util.*


class InputActivity : AppCompatActivity() {

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null

    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                date_button.text = dateString
            }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false)
        timePickerDialog.show()
    }

    private val mOnDoneClickListener = View.OnClickListener {
        addTask()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        spnCategory.adapter = CategoryAdapter(this)

            // ActionBarを設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        // EXTRA_TASKからTaskのidを取得して、 idからTaskのインスタンスを取得する
        val intent = intent
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()
        realm.close()

        if (mTask == null) {
            // 新規作成の場合
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合
            title_edit_text.setText(mTask!!.title)
            content_edit_text.setText(mTask!!.contents)
            val adapter = spnCategory.adapter as CategoryAdapter
            Log.d("test", adapter.toString())
            val selectedPosition = adapter.mCategoryList.indexOfFirst { it.categoryName== mTask!!.category1 }
            if (selectedPosition >= 0) {
                spnCategory.setSelection(selectedPosition)
            Log.d("test", selectedPosition.toString())
            spnCategory.setSelection(selectedPosition)
            }

//            category_edit_text.setText(mTask!!.category1)

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }
    }

    private fun addTask() {
        val realm = Realm.getDefaultInstance()

        //追加したりするときに必要なもの
        realm.beginTransaction()

        if (mTask == null) {
            // 新規作成の場合
            mTask = Task()

            val taskRealmResults = realm.where(Task::class.java).findAll()

            val identifier: Int =
                //max(最大)のidがnull→まだ何もない→idは0になる
                //nullじゃない→すでにidがある→一番大きなidに+1して最新のidを生成
                if (taskRealmResults.max("id") != null) {
                    taskRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            //それをidとして代入
            mTask!!.id = identifier
        }

        //editに入力したテキストをタイトル・コンテンツそれぞれに代入
        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()

        //pinnerの押した場所からカテゴリ名取得
        val categoryPosition = spnCategory.selectedItemPosition
        val result = realm.where(Category::class.java).findAll()
        val categories = result.toMutableList()
        val category = categories[categoryPosition].categoryName

        //mTaskに代入
        mTask!!.title = title
        mTask!!.contents = content
        mTask!!.category1 = category

        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        mTask!!.date = date

        //できあがったmTaskをコミット
        realm.copyToRealmOrUpdate(mTask!!)
        realm.commitTransaction()

        realm.close()


        //タスクアラームレシーバーを起動するためのintent
        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
        //通知するときにタスクのタイトルを表示するのに必要になるのでputExtraを用意する
        resultIntent.putExtra(EXTRA_TASK, mTask!!.id)

        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)
    }
}

