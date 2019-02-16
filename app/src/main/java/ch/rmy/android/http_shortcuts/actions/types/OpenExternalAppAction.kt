package ch.rmy.android.http_shortcuts.actions.types

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import ch.rmy.android.http_shortcuts.activities.EditorActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import com.android.volley.VolleyError
import java.lang.Exception

class OpenExternalAppAction(
        id: String,
        actionType: OpenExternalAppActionType,
        data: Map<String, String>
) : BaseAction(id, actionType, data) {


    override fun getDescription(context: Context): CharSequence =
            "Open External Application"

    override fun performBlocking(context: Context, shortcutId: Long, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int) {
        var name: String = (context as ExecuteActivity).shortcut.url
        var intent = Intent()
        try{
            var params = name.split("  ")
            if(params.size == 2){
                intent.setClassName(params[0], params[1])
                context.startActivity(intent)
            }
            if(params.size == 1){
                intent = context.packageManager.getLaunchIntentForPackage(params[0])
            }
            //intent = context.packageManager.getLaunchIntentForPackage(name)
        }catch (e:Exception){
            Log.e("errortag", "---> NULL intent <---")
            Toast.makeText(context, "We don't have an app to open(NULL)", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            var componentName: ComponentName = ComponentName("package","class")
            //intent.setComponent(componentName)
            context.startActivity(intent)
        }catch (e:Exception){
            Toast.makeText(context, "Incorrect package!", Toast.LENGTH_SHORT).show()
            Log.e("errortag", "---> Incorrect PAACKAGE <---")
        }

    }

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) =
            OpenExternalAppActionEditorView(context)
    }