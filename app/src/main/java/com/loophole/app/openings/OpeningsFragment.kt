package com.loophole.app.openings

import android.os.Bundle
import android.view.View
import com.loophole.app.PlaceholderFragment
import com.loophole.app.R
import com.loophole.app.auth.AuthManager

/** Home tab. Issue #5 (Openings Catalog) replaces the placeholder content. */
class OpeningsFragment : PlaceholderFragment(R.string.openings_placeholder) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val name = AuthManager.currentEmail(requireContext()) ?: getString(R.string.guest)
        binding.titleText.text = getString(R.string.welcome_user, name)
    }
}
