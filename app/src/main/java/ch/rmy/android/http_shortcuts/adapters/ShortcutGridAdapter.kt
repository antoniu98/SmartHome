package ch.rmy.android.http_shortcuts.adapters

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import kotterknife.bindView

class ShortcutGridAdapter(context: Context) : ShortcutAdapter(context) {

    override fun createViewHolder(parentView: ViewGroup) = ShortcutViewHolder(parentView)

    inner class ShortcutViewHolder(parent: ViewGroup) : BaseViewHolder<Shortcut>(parent, R.layout.grid_item_shortcut, this@ShortcutGridAdapter) {

        private val name: TextView by bindView(R.id.name)
        private val icon: IconView by bindView(R.id.icon)
        private val layout: RelativeLayout by bindView(R.id.shortcut_item)

        override fun updateViews(item: Shortcut) {
            name.text = item.name
            icon.setImageURI(item.getIconURI(context), item.iconName)
            icon.setAdjustViewBounds(true)
            icon.setScaleType(ImageView.ScaleType.FIT_XY)
        }

    }



}
