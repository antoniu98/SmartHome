package ch.rmy.android.http_shortcuts.activities

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.View
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.adapters.ChooserAdapter
import ch.rmy.android.http_shortcuts.utils.*
import com.afollestad.materialdialogs.MaterialDialog
import kotterknife.bindView
import java.lang.Exception

class ChooserActivity : BaseActivity(){
    private val backButton: FloatingActionButton by bindView(R.id.back_button_2)

    var listener = object : ChooserAdapter.OnItemClickListener{
         override fun onItemClick(app: ApplicationInfo?) {
            val intent = EditorActivity.IntentBuilder(context)
                    .build()

             //Vars
            var test = packageManager.getPackageInfo(app?.packageName, PackageManager.GET_ACTIVITIES).activities
            var list: ArrayList<String> = ArrayList()

             //Get the list of vars
             for (activityInfo in test) {
                 list.add(activityInfo.name)
             }

            //Create the dialog
             var dialog = MaterialDialog.Builder(context)
                     .title(app?.name.toString())
                     .items(list)
                     .itemsCallback { dialog, itemView, position, text ->
                         var dialog2 = MaterialDialog.Builder(context)
                                 .title("What do you want to launch?")
                                 .items("Activity" , "Application")
                                 .itemsCallback { dialog2, itemView2, position2, text2 ->
                                     if(text2 == "Application"){
                                         intent.putExtra("package_name", app?.packageName)
                                         startActivityForResult(intent, 1)
                                     }else{
                                         intent.putExtra("package_name", app?.packageName + "  " + text)
                                         startActivityForResult(intent, 1)
                                     }
                                 }
                                 .showIfPossible()
                     }
                     .showIfPossible()
            //intent.putExtra("package_name", app?.packageName)
            //startActivityForResult(intent, 1)
        }
    }



//    override fun onItemClick(app: ApplicationInfo?) {
//        val intent = EditorActivity.IntentBuilder(context)
//                .build()
//
//        MaterialDialog.Builder(context)
//                .title("Choose activity:")
//                .showIfPossible()
//        intent.putExtra("package_name", app?.packageName)
//        startActivityForResult(intent, 1)
//    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        //Toast.makeText(this, "onActivityResult called ...", Toast.LENGTH_SHORT).show()
        //MainActivity().onActivityResult(requestCode, resultCode, intent)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }


    private fun init() {
        setContentView(R.layout.chooser_list)

        val listofApps: ArrayList<ApplicationInfo> = packageManager.getInstalledApplications(PackageManager.GET_META_DATA) as ArrayList<ApplicationInfo>
        var test: Array<ActivityInfo>

        for (activity in listofApps){
        try {
            test = packageManager.getPackageInfo(activity.packageName, PackageManager.GET_ACTIVITIES).activities
            if(test == null || activity.name == "null") {
                //listofApps.remove(activity)
                continue
            }
            break
        } catch (e: Exception) {
            Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_SHORT).show()
            break
        }
    }

        try {
            backButton.setOnClickListener { finish() }
            val recycleV: RecyclerView = findViewById(R.id.device_recycle_view)
            recycleV.layoutManager = GridLayoutManager(this, 4)
            recycleV.adapter = ChooserAdapter(this, listofApps, listener)

        }catch (e: Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, CurlImportActivity::class.java)
}
