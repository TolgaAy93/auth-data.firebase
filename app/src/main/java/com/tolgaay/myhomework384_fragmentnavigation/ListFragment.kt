package com.tolgaay.myhomework384_fragmentnavigation

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tolgaay.myhomework384_fragmentnavigation.RoomDb.Art
import com.tolgaay.myhomework384_fragmentnavigation.RoomDb.ArtDB
import com.tolgaay.myhomework384_fragmentnavigation.RoomDb.ArtDao
import com.tolgaay.myhomework384_fragmentnavigation.databinding.FragmentListBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    private lateinit var artAdapter: ArtAdapter

    private lateinit var artDao : ArtDao
    private lateinit var artDB: ArtDB

    private val mDisposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        artDB = Room.databaseBuilder(requireContext(),ArtDB::class.java,"Arts").build()
        artDao = artDB.artDao()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        _binding = FragmentListBinding.inflate(inflater,container,false)
        val view = binding!!.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getFromSQL()
    }

    fun getFromSQL() {
        mDisposable.add(artDao.getArtWithNameAndId()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleResponse))
    }

    private fun handleResponse(artList: List<Art>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        artAdapter = ArtAdapter(artList)
        binding.recyclerView.adapter = artAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.art_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_post) {
            val action = ListFragmentDirections.listTODetailFragment(0,"new")
            NavHostFragment.findNavController(this).navigate(action)
        } else if (item.itemId == R.id.logout) {
            auth.signOut()
            val action = ListFragmentDirections.listTOIserLoginFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}