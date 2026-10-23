package io.legado.app.ui.book.import.remote

import android.os.Bundle
import android.text.InputType
import android.graphics.Typeface
import android.graphics.Color
import io.legado.app.ui.widget.ModernUi
import io.legado.app.utils.toastOnUi
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.data.entities.Server
import io.legado.app.data.entities.rule.RowUi
import io.legado.app.databinding.DialogWebdavServerBinding
import io.legado.app.databinding.ItemSourceEditBinding
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.utils.GSON
import io.legado.app.utils.applyTint
import io.legado.app.utils.setLayout
import io.legado.app.utils.viewbindingdelegate.viewBinding
import org.json.JSONObject

class ServerConfigDialog() : BaseDialogFragment(R.layout.dialog_webdav_server, true),
    Toolbar.OnMenuItemClickListener {

    constructor(id: Long) : this() {
        arguments = Bundle().apply {
            putLong("id", id)
        }
    }

    private val binding by viewBinding(DialogWebdavServerBinding::bind)
    private val viewModel by viewModels<ServerConfigViewModel>()

    private val webDavServerUi = listOf(
        RowUi("url"),
        RowUi("username"),
        RowUi("password", RowUi.Type.password)
    )

    override fun onStart() {
        super.onStart()
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        binding.vwBg.background = ModernUi.shape(requireContext(), requireContext().bottomBackground, 28)
        binding.toolBar.setBackgroundColor(Color.TRANSPARENT)
        binding.toolBar.setTitleTextColor(ModernUi.ink(requireContext()))
        binding.toolBar.inflateMenu(R.menu.server_config)
        binding.toolBar.menu.applyTint(requireContext())
        binding.toolBar.setOnMenuItemClickListener(this)
        viewModel.init(arguments?.getLong("id")) {
            upConfigView(viewModel.mServer)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> getServer().let {
                if (it.getConfigJsonObject()?.optString("url").isNullOrBlank()) {
                    toastOnUi(getString(R.string.ui_server_address_required))
                    return true
                }
                viewModel.save(it) {
                    dismissAllowingStateLoss()
                }
            }
        }
        return true
    }

    private fun upConfigView(server: Server?) {
        binding.etName.setText(server?.name)
        binding.spType.setSelection(
            when (server?.type) {
                else -> 0
            }
        )
        when (server?.type) {
            else -> upWebDavServerUi(server?.getConfigJsonObject())
        }
    }

    private fun upWebDavServerUi(config: JSONObject?) {
        webDavServerUi.forEachIndexed { index, rowUi ->
            when (rowUi.type) {
                RowUi.Type.text -> ItemSourceEditBinding.inflate(
                    layoutInflater,
                    binding.root,
                    false
                ).let {
                    binding.flexbox.addView(it.root)
                    it.root.id = index + 1000
                    it.textInputLayout.hint = getString(when (rowUi.name) {
                        "url" -> R.string.ui_server_address
                        "username" -> R.string.ui_server_account
                        else -> R.string.ui_server_password
                    })
                    it.editText.minHeight = ModernUi.dp(requireContext(), 56)
                    it.editText.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                    it.editText.setSingleLine(true)
                    it.editText.setText(config?.getString(rowUi.name))
                }
                RowUi.Type.password -> ItemSourceEditBinding.inflate(
                    layoutInflater,
                    binding.root,
                    false
                ).let {
                    binding.flexbox.addView(it.root)
                    it.root.id = index + 1000
                    it.textInputLayout.hint = getString(when (rowUi.name) {
                        "url" -> R.string.ui_server_address
                        "username" -> R.string.ui_server_account
                        else -> R.string.ui_server_password
                    })
                    it.editText.minHeight = ModernUi.dp(requireContext(), 56)
                    it.editText.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                    it.editText.setSingleLine(true)
                    it.editText.inputType =
                        InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
                    it.editText.setText(config?.getString(rowUi.name))
                }
            }
        }
    }

    private fun getServer(): Server {
        val server = viewModel.mServer?.copy() ?: Server()
        server.name = binding.etName.text.toString()
        server.type = when (binding.spType.selectedItemPosition) {
            else -> Server.TYPE.WEBDAV
        }
        server.config = when (server.type) {
            else -> GSON.toJson(getWebDavConfig())
        }
        return server
    }

    private fun getWebDavConfig(): HashMap<String, String> {
        val data = hashMapOf<String, String>()
        webDavServerUi.forEachIndexed { index, rowUi ->
            val rowView = binding.root.findViewById<View>(index + 1000)
            ItemSourceEditBinding.bind(rowView).editText.text?.let {
                data[rowUi.name] = it.toString()
            }
        }
        return data
    }

}