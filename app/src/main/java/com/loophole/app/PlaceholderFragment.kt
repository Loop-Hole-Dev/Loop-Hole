package com.loophole.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.loophole.app.databinding.FragmentPlaceholderBinding

/**
 * Temporary tab content until each screen's issue is built.
 * Teammates: replace your tab's fragment with a real layout and delete its use of this class.
 */
abstract class PlaceholderFragment(@StringRes private val bodyRes: Int) : Fragment() {

    private var _binding: FragmentPlaceholderBinding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bodyText.setText(bodyRes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
