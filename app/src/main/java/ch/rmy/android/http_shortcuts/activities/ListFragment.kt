package ch.rmy.android.http_shortcuts.activities

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.adapters.ShortcutAdapter
import ch.rmy.android.http_shortcuts.adapters.ShortcutGridAdapter
import ch.rmy.android.http_shortcuts.adapters.ShortcutListAdapter
import ch.rmy.android.http_shortcuts.dialogs.CurlExportDialog
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.http.ExecutionScheduler
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.GridLayoutManager
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.SelectionMode
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.mapFor
import ch.rmy.android.http_shortcuts.utils.mapIf
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import ch.rmy.curlcommand.CurlCommand
import ch.rmy.curlcommand.CurlConstructor
import com.afollestad.materialdialogs.MaterialDialog
import io.realm.RealmChangeListener
import io.realm.RealmList
import kotterknife.bindView
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class ListFragment : BaseFragment() {

    override val layoutResource = R.layout.fragment_list

    private val shortcutList: RecyclerView by bindView(R.id.shortcut_list)

    var categoryId: Long = 0
        set(categoryId) {
            field = categoryId
            if (isAdded) {
                onCategoryChanged()
            }
        }
    var selectionMode: SelectionMode? = null
    private var category: Category? = null

    private val controller by lazy { Controller() }
    private val categories by lazy { controller.getCategories() }

    private val shortcutChangeListener = RealmChangeListener<RealmList<Shortcut>> { shortcuts ->
        if (isVisible && shortcuts.isValid) {
            onShortcutsChanged(shortcuts)
        }
    }

    override fun onStart() {
        super.onStart()
        onCategoryChanged()
    }

    private fun onCategoryChanged() {
        if (context == null) {
            return
        }
        category?.shortcuts?.removeChangeListener(shortcutChangeListener)
        category = controller.getCategoryById(this.categoryId) ?: return
        category!!.shortcuts.addChangeListener(shortcutChangeListener)

        val manager: RecyclerView.LayoutManager
        val adapter: ShortcutAdapter
        when (category!!.layoutType) {
            Category.LAYOUT_GRID -> {
                adapter = ShortcutGridAdapter(context!!)
                manager = GridLayoutManager(context!!)
            }
            else -> {
                adapter = ShortcutListAdapter(context!!)
                manager = LinearLayoutManager(context)
            }
        }
        adapter.setPendingShortcuts(controller.getShortcutsPendingExecution())
        adapter.clickListener = this::onItemClicked
        adapter.longClickListener = this::onItemLongClicked
        adapter.setItems(category!!.shortcuts)

        shortcutList.layoutManager = manager
        shortcutList.adapter = adapter
        onShortcutsChanged(category!!.shortcuts)
    }

    private fun onItemClicked(shortcut: Shortcut) {
        when (selectionMode) {
            SelectionMode.HOME_SCREEN, SelectionMode.PLUGIN -> tabHost.selectShortcut(shortcut)
            else -> {
                if (controller.isAppLocked()) {
                    executeShortcut(shortcut)
                    return
                }
                val action = Settings(context!!).clickBehavior
                when (action) {
                    Settings.CLICK_BEHAVIOR_RUN -> executeShortcut(shortcut)
                    Settings.CLICK_BEHAVIOR_EDIT -> editShortcut(shortcut)
                    Settings.CLICK_BEHAVIOR_MENU -> showContextMenu(shortcut)
                }
            }
        }
    }

    private fun onItemLongClicked(shortcut: Shortcut): Boolean {
        if (controller.isAppLocked()) {
            return false
        }
        showContextMenu(shortcut)
        return true
    }

    private fun onShortcutsChanged(shortcuts: List<Shortcut>) {
        val categories = controller.getCategories()
        if (categories.isValid) {
            (shortcutList.layoutManager as? GridLayoutManager)?.setEmpty(shortcuts.isEmpty())
            LauncherShortcutManager.updateAppShortcuts(context!!, categories)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        category?.shortcuts?.removeAllChangeListeners()
        controller.destroy()
    }

    override fun setupViews() {
        shortcutList.setHasFixedSize(true)
        onCategoryChanged()
    }

    private fun showContextMenu(shortcut: Shortcut) {
        MenuDialogBuilder(context!!)
                .title(shortcut.name)
                .item(R.string.action_place) {
                    tabHost.placeShortcutOnHomeScreen(shortcut)
                }
                .item(R.string.action_run) {
                    executeShortcut(shortcut)
                }
                .item(R.string.action_edit) {
                    editShortcut(shortcut)
                }
                .mapIf(canMoveShortcut(shortcut)) {
                    it.item(R.string.action_move) {
                        openMoveDialog(shortcut)
                    }
                }
                .item(R.string.action_duplicate) {
                    duplicateShortcut(shortcut)
                }
                .mapIf(isPending(shortcut)) {
                    it.item(R.string.action_cancel_pending) {
                        cancelPendingExecution(shortcut)
                    }
                }
                .item(R.string.action_curl_export) {
                    showCurlExportDialog(shortcut)
                }
                .item(R.string.action_delete) {
                    showDeleteDialog(shortcut)
                }
                .showIfPossible()
    }

    private fun isPending(shortcut: Shortcut) =
            controller.getShortcutPendingExecution(shortcut.id) != null

    private fun executeShortcut(shortcut: Shortcut) {
        val intent = ExecuteActivity.IntentBuilder(context!!, shortcut.id)
                .build()
        startActivity(intent)
    }

    private fun editShortcut(shortcut: Shortcut) {
        val intent = EditorActivity.IntentBuilder(context!!)
                .shortcutId(shortcut.id)
                .build()
        startActivityForResult(intent, REQUEST_EDIT_SHORTCUT)
    }

    private fun canMoveShortcut(shortcut: Shortcut): Boolean =
            canMoveShortcut(shortcut, -1) || canMoveShortcut(shortcut, +1) || categories.size > 1

    private fun canMoveShortcut(shortcut: Shortcut, offset: Int): Boolean {
        if (category == null) {
            return false
        }
        val position = category!!.shortcuts.indexOf(shortcut) + offset
        return position >= 0 && position < category!!.shortcuts.size
    }

    private fun openMoveDialog(shortcut: Shortcut) {
        MenuDialogBuilder(context!!)
                .mapIf(canMoveShortcut(shortcut, -1)) {
                    it.item(R.string.action_move_up) {
                        moveShortcut(shortcut, -1)
                    }
                }
                .mapIf(canMoveShortcut(shortcut, 1)) {
                    it.item(R.string.action_move_down) {
                        moveShortcut(shortcut, 1)
                    }
                }
                .mapIf(categories.size > 1) {
                    it.item(R.string.action_move_to_category) {
                        showMoveToCategoryDialog(shortcut)
                    }
                }
                .showIfPossible()
    }

    private fun moveShortcut(shortcut: Shortcut, offset: Int) {
        category?.let { currentCategory ->
            if (!canMoveShortcut(shortcut, offset)) {
                return
            }
            val position = currentCategory.shortcuts.indexOf(shortcut) + offset
            if (position == currentCategory.shortcuts.size) {
                controller.moveShortcut(shortcut.id, targetCategoryId = currentCategory.id)
            } else {
                controller.moveShortcut(shortcut.id, targetPosition = position)
            }.subscribe()
        }
    }

    private fun showMoveToCategoryDialog(shortcut: Shortcut) {
        category?.let { currentCategory ->
            MenuDialogBuilder(context!!)
                    .title(R.string.title_move_to_category)
                    .mapFor(this.categories.filter { it.id != currentCategory.id }) { builder, category ->
                        builder.item(category.name) {
                            if (category.isValid) {
                                moveShortcut(shortcut, category)
                            }
                        }
                    }
                    .showIfPossible()
        }
    }

    private fun moveShortcut(shortcut: Shortcut, category: Category) {
        controller.moveShortcut(shortcut.id, targetCategoryId = category.id)
                .subscribe {
                    tabHost.showSnackbar(String.format(getString(R.string.shortcut_moved), shortcut.name))
                }
    }

    private fun duplicateShortcut(shortcut: Shortcut) {
        category?.let { currentCategory ->
            val newName = String.format(getString(R.string.copy), shortcut.name)
            controller.persist(shortcut.duplicate(newName))
                    .done { duplicate ->
                        controller.moveShortcut(
                                duplicate.id,
                                targetCategoryId = currentCategory.id,
                                targetPosition = currentCategory
                                        .shortcuts.indexOfFirst { it.id == shortcut.id }
                                        .takeIf { it != -1 }
                                        ?.let { it + 1 }
                        )
                                .subscribe()
                    }
            tabHost.showSnackbar(String.format(getString(R.string.shortcut_duplicated), shortcut.name))
        }
    }

    private fun cancelPendingExecution(shortcut: Shortcut) {
        controller.removePendingExecution(shortcut.id)
                .subscribe {
                    tabHost.showSnackbar(String.format(getString(R.string.pending_shortcut_execution_cancelled), shortcut.name))
                    ExecutionScheduler.schedule(context!!)
                }
    }

    private fun showCurlExportDialog(shortcut: Shortcut) {
        val command = CurlCommand.Builder()
                .url(shortcut.url)
                .username(shortcut.username)
                .password(shortcut.password)
                .method(shortcut.method)
                .timeout(shortcut.timeout)
                .mapFor(shortcut.headers) { builder, header ->
                    builder.header(header.key, header.value)
                }
                .mapFor(shortcut.parameters) { builder, parameter ->
                    try {
                        builder.data(URLEncoder.encode(parameter.key, "UTF-8")
                                + "="
                                + URLEncoder.encode(parameter.value, "UTF-8")
                                + "&"
                        )
                    } catch (e: UnsupportedEncodingException) {
                        builder
                    }
                }
                .data(shortcut.bodyContent)
                .build()

        CurlExportDialog(
                context!!,
                shortcut.getSafeName(context!!),
                CurlConstructor.toCurlCommandString(command)
        ).show()
    }

    private fun showDeleteDialog(shortcut: Shortcut) {
        MaterialDialog.Builder(context!!)
                .content(R.string.confirm_delete_shortcut_message)
                .positiveText(R.string.dialog_delete)
                .onPositive { _, _ -> deleteShortcut(shortcut) }
                .negativeText(R.string.dialog_cancel)
                .showIfPossible()
    }

    private fun deleteShortcut(shortcut: Shortcut) {
        tabHost.showSnackbar(String.format(getString(R.string.shortcut_deleted), shortcut.name))
        tabHost.removeShortcutFromHomeScreen(shortcut)
        controller.deleteShortcut(shortcut.id)
                .subscribe {
                    ExecutionScheduler.schedule(context!!)
                }
    }

    private val tabHost: TabHost
        get() = activity as TabHost

    internal interface TabHost {

        fun selectShortcut(shortcut: Shortcut)

        fun placeShortcutOnHomeScreen(shortcut: Shortcut)

        fun removeShortcutFromHomeScreen(shortcut: Shortcut)

        fun showSnackbar(message: CharSequence)

    }

    companion object {

        private const val REQUEST_EDIT_SHORTCUT = 2

    }

}