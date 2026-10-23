package io.legado.app.ui.font

import android.content.DialogInterface
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.constant.AppLog
import io.legado.app.constant.PreferKey
import io.legado.app.databinding.DialogFontSelectBinding
import io.legado.app.help.BuiltInFonts
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.dialogs.SelectItem
import io.legado.app.lib.permission.Permissions
import io.legado.app.lib.permission.PermissionsCompat
import io.legado.app.ui.book.read.config.ReadingPanelStyle
import io.legado.app.ui.file.HandleFileContract
import io.legado.app.ui.widget.dialog.TextDialog
import io.legado.app.utils.FileDoc
import io.legado.app.utils.FileUtils
import io.legado.app.utils.RealPathUtil
import io.legado.app.utils.cnCompare
import io.legado.app.utils.externalFiles
import io.legado.app.utils.getPrefString
import io.legado.app.utils.isContentScheme
import io.legado.app.utils.list
import io.legado.app.utils.listFileDocs
import io.legado.app.utils.putPrefString
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.viewbindingdelegate.viewBinding
import java.io.File
import kotlinx.coroutines.launch

/**
 * 字体选择对话框
 */
class FontSelectDialog : BaseDialogFragment(R.layout.dialog_font_select),
    FontAdapter.CallBack {
    private val fontRegex = Regex("(?i).*\\.[ot]tf")
    private val binding by viewBinding(DialogFontSelectBinding::bind)
    private val adapter by lazy {
        val curFontPath = callBack?.curFontPath ?: ""
        FontAdapter(requireContext(), curFontPath, this)
    }
    private val selectFontDir = registerForActivityResult(HandleFileContract()) {
        it.uri?.let { uri ->
            if (uri.isContentScheme()) {
                putPrefString(PreferKey.fontFolder, uri.toString())
                val doc = DocumentFile.fromTreeUri(requireContext(), uri)
                if (doc != null) {
                    loadFontFiles(FileDoc.fromDocumentFile(doc))
                } else {
                    RealPathUtil.getPath(requireContext(), uri)?.let { path ->
                        loadFontFilesByPermission(path)
                    }
                }
            } else {
                uri.path?.let { path ->
                    putPrefString(PreferKey.fontFolder, path)
                    loadFontFilesByPermission(path)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        parentFragment?.view?.visibility = View.INVISIBLE
        ReadingPanelStyle.show(this, 0.62f)
    }

    override fun onDismiss(dialog: DialogInterface) {
        parentFragment?.view?.visibility = View.VISIBLE
        super.onDismiss(dialog)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        ReadingPanelStyle.prepare(view) { dismissAllowingStateLoss() }
        ReadingPanelStyle.action(binding.importFont, true)
        binding.importFont.setOnClickListener { openFolder() }
        ReadingPanelStyle.action(binding.fontLicense, true)
        binding.fontLicense.setOnClickListener {
            execute {
                requireContext().assets.open(BuiltInFonts.WENKAI_LICENSE).bufferedReader().use { it.readText() }
            }.onSuccess { license ->
                TextDialog(getString(R.string.reading_font_license), license)
                    .show(childFragmentManager, "fontLicense")
            }
        }
        val bundledSample = addFontChoice(
            binding.bundledFonts, getString(R.string.reading_font_wenkai),
            callBack?.curFontPath == BuiltInFonts.WENKAI
        ) {
            callBack?.selectFont(BuiltInFonts.WENKAI)
            dismissAllowingStateLoss()
        }
        val bundledRow = bundledSample.parent as View
        bundledRow.id = R.id.font_wenkai
        bundledRow.isEnabled = false
        execute { requireNotNull(BuiltInFonts.typeface(BuiltInFonts.WENKAI)) }
            .onSuccess {
                bundledSample.typeface = it
                bundledRow.isEnabled = true
            }.onError {
                AppLog.put("加载内置字体失败", it)
                toastOnUi(it.localizedMessage)
            }
        val names = resources.getStringArray(R.array.system_typefaces)
        val families = listOf(Typeface.DEFAULT, Typeface.SERIF, Typeface.MONOSPACE)
        names.forEachIndexed { index, name ->
            addFontChoice(
                binding.systemFonts, name,
                callBack?.curFontPath.isNullOrEmpty() && AppConfig.systemTypefaces == index
            ) {
                AppConfig.systemTypefaces = index
                onDefaultFontChange()
                dismissAllowingStateLoss()
            }.typeface = families[index]
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        val fontPath = getPrefString(PreferKey.fontFolder)
        if (fontPath.isNullOrEmpty()) {
            loadLocalFonts()
        } else if (fontPath.isContentScheme()) {
            val doc = DocumentFile.fromTreeUri(requireContext(), Uri.parse(fontPath))
            if (doc?.canRead() == true) loadFontFiles(FileDoc.fromDocumentFile(doc)) else loadLocalFonts()
        } else {
            loadFontFilesByPermission(fontPath)
        }
    }

    private fun addFontChoice(
        parent: LinearLayout,
        name: String,
        selected: Boolean,
        onSelected: () -> Unit
    ): TextView {
        val density = resources.displayMetrics.density
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = (10 * density).toInt()
            }
            setPadding((16 * density).toInt(), (14 * density).toInt(),
                (16 * density).toInt(), (14 * density).toInt())
            ReadingPanelStyle.action(this, selected)
            isSelected = selected
            contentDescription = name
            addView(TextView(context).apply {
                text = if (selected) "$name  ✓" else name
                textSize = 13f
                setTextColor(if (selected) ReadingPanelStyle.accent(context) else ReadingPanelStyle.ink(context))
            })
            setOnClickListener { onSelected() }
        }
        val sample = TextView(requireContext()).apply {
            setText(R.string.reading_font_sample)
            textSize = 19f
            setTextColor(ReadingPanelStyle.ink(context))
            setPadding(0, (8 * density).toInt(), 0, 0)
        }
        row.addView(sample)
        parent.addView(row)
        return sample
    }

    private fun loadLocalFonts() {
        execute { getLocalFonts().sortedWith { first, second -> first.name.cnCompare(second.name) } }
            .onSuccess {
                adapter.setItems(it)
                binding.emptyFonts.isVisible = it.isEmpty()
            }
    }

    private fun openFolder() {
        lifecycleScope.launch {
            val defaultPath = "SD${File.separator}Fonts"
            selectFontDir.launch {
                otherActions = arrayListOf(SelectItem(defaultPath, -1))
            }
        }
    }

    private fun getLocalFonts(): ArrayList<FileDoc> {
        val path = FileUtils.getPath(requireContext().externalFiles, "font")
        return File(path).listFileDocs {
            it.name.matches(fontRegex)
        }
    }

    private fun loadFontFilesByPermission(path: String) {
        PermissionsCompat.Builder()
            .addPermissions(*Permissions.Group.STORAGE)
            .rationale(R.string.tip_perm_request_storage)
            .onGranted {
                loadFontFiles(
                    FileDoc.fromFile(File(path))
                )
            }
            .request()
    }

    private fun loadFontFiles(fileDoc: FileDoc) {
        execute {
            val fontItems = fileDoc.list {
                it.name.matches(fontRegex)
            } ?: ArrayList()
            mergeFontItems(fontItems, getLocalFonts())
        }.onSuccess {
            adapter.setItems(it)
            binding.emptyFonts.isVisible = it.isEmpty()
        }.onError {
            AppLog.put("加载字体文件失败\n${it.localizedMessage}", it)
            toastOnUi("getFontFiles:${it.localizedMessage}")
        }
    }

    private fun mergeFontItems(
        items1: ArrayList<FileDoc>,
        items2: ArrayList<FileDoc>
    ): List<FileDoc> {
        val items = ArrayList(items1)
        items2.forEach { item2 ->
            var isInFirst = false
            items1.forEach for1@{ item1 ->
                if (item2.name == item1.name) {
                    isInFirst = true
                    return@for1
                }
            }
            if (!isInFirst) {
                items.add(item2)
            }
        }
        return items.sortedWith { o1, o2 ->
            o1.name.cnCompare(o2.name)
        }
    }

    override fun onFontSelect(docItem: FileDoc) {
        execute {
            callBack?.selectFont(docItem.toString())
        }.onSuccess {
            dismissAllowingStateLoss()
        }
    }

    private fun onDefaultFontChange() {
        callBack?.selectFont("")
    }

    private val callBack: CallBack?
        get() = (parentFragment as? CallBack) ?: (activity as? CallBack)

    interface CallBack {
        fun selectFont(path: String)
        val curFontPath: String
    }
}
