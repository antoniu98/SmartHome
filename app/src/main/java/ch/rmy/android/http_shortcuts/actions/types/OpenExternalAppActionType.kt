package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO

class OpenExternalAppActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = "Open External Application"

    override val isValidBeforeAction = false

    override fun fromDTO(actionDTO: ActionDTO) = OpenExternalAppAction(actionDTO.id, this, actionDTO.data)

    companion object {
        const val TYPE = "external_app"
    }

}