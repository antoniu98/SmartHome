package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import kotterknife.bindView

class OpenExternalAppActionEditorView(
        context: Context
) : BaseActionEditorView(context, R.layout.action_open_ext_app) {

    init {
    }


    override fun compile(): Boolean {
        return true
    }

}