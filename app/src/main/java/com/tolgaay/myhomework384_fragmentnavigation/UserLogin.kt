package com.tolgaay.myhomework384_fragmentnavigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tolgaay.myhomework384_fragmentnavigation.databinding.FragmentUserLoginBinding


class UserLogin : Fragment() {

    private var _binding: FragmentUserLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var email : String? = null
    private var password : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserLoginBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonIn.setOnClickListener { signIn(view) }
        binding.buttonUp.setOnClickListener { singUp(view) }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val action = UserLoginDirections.loginTOListFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }

    fun signIn(view: View) {
        email = binding.emailText.text.toString()
        password = binding.passwordText.text.toString()

        if (email.equals("") || password.equals("")) {
            Toast.makeText(requireContext(),"Enter e-mail and Password!",Toast.LENGTH_SHORT).show()
        } else {
            auth.signInWithEmailAndPassword(email!!,password!!).addOnSuccessListener {
                //success singIn
                val action = UserLoginDirections.loginTOListFragment()
                Navigation.findNavController(view).navigate(action)
            }.addOnFailureListener {
                //failure singIn
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun singUp(view: View) {
        email = binding.emailText.text.toString()
        password = binding.passwordText.text.toString()

        if (email.equals("") || password.equals("")) {
            Toast.makeText(requireContext(),"Enter e-mail and Password!",Toast.LENGTH_LONG).show()
        } else {
            auth.createUserWithEmailAndPassword(email!!,password!!).addOnSuccessListener {
                //success singIn
                val action = UserLoginDirections.loginTOListFragment()
                Navigation.findNavController(view).navigate(action)

            }.addOnFailureListener {
                //failure singIn
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}