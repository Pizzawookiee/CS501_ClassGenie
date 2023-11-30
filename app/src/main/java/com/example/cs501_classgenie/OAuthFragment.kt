package com.example.cs501_classgenie

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.cs501_classgenie.databinding.FragmentOAuthBinding
import kotlinx.coroutines.launch
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

class OAuthFragment : Fragment() {

    companion object {
        fun newInstance() = OAuthFragment()
    }

    val OAuthViewModel: OAuthViewModel by activityViewModels()

    private var _binding: FragmentOAuthBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOAuthBinding.inflate(inflater, container, false)



        val sync_button: Button = binding.syncButton
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sync_button.setOnClickListener {
                    //run OAuth code from ViewModel
                    Log.d("OAuth", "making call to ViewModel")
                    //OAuthViewModel.authorize()
                    /*
                    launch{

                    }
                     */
                }
            }
        }



        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}