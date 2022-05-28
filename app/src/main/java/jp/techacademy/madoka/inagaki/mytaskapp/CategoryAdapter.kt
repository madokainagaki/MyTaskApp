package jp.techacademy.madoka.inagaki.mytaskapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import io.realm.Realm

class CategoryAdapter(context: Context): BaseAdapter() {
    val realm = Realm.getDefaultInstance()
    val result = realm.where(Category::class.java).findAll()
    val categories = result.toMutableList()

    private val mLayoutInflater: LayoutInflater
    var mCategoryList= mutableListOf<Category>()

    init {
        this.mLayoutInflater = LayoutInflater.from(context)
        mCategoryList = categories
    }

    override fun getCount(): Int {
        return mCategoryList.size
    }

    override fun getItem(position: Int): Any {
        return mCategoryList[position]
    }

    override fun getItemId(position: Int): Long {
        return mCategoryList[position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: mLayoutInflater.inflate(android.R.layout.simple_list_item_1, null)

        val textView1 = view.findViewById<TextView>(android.R.id.text1)

      //  if (categoryAdp == 0) {
            textView1.text = mCategoryList[position].categoryName
       // }else{
      //      textView1.text = mCategoryList[position].id.toString()
     //   }
        return view
    }
}

