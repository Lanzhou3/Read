package io.legado.app.ui.main.bookshelf

import androidx.core.view.isVisible
import io.legado.app.databinding.ItemBookshelfListBinding

/** Do not leave rows of unlabeled metadata icons on newly imported local books. */
fun ItemBookshelfListBinding.updateMetadataVisibility() {
    tvAuthor.isVisible = tvAuthor.text.isNotBlank()
    ivAuthor.isVisible = tvAuthor.isVisible
    tvRead.isVisible = tvRead.text.isNotBlank()
    ivRead.isVisible = tvRead.isVisible
    tvLast.isVisible = tvLast.text.isNotBlank()
    ivLast.isVisible = tvLast.isVisible
}
