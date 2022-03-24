package com.tolgaay.myhomework384_fragmentnavigation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.tolgaay.myhomework384_fragmentnavigation.RoomDb.Art
import com.tolgaay.myhomework384_fragmentnavigation.RoomDb.ArtDB
import com.tolgaay.myhomework384_fragmentnavigation.RoomDb.ArtDao
import com.tolgaay.myhomework384_fragmentnavigation.databinding.FragmentDetailBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


class DetailFragment : Fragment() {

    private var _binding : FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permiisionLauncher: ActivityResultLauncher<String>
    var selectedPicture : Uri? = null
    var selectedBitmap : Bitmap? = null

    private lateinit var artDatabase : ArtDB
    private lateinit var artDao : ArtDao

    private val mDisposable = CompositeDisposable()

    var artFromMain : Art? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var firestore : FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resultLauncher()

        artDatabase = Room.databaseBuilder(requireActivity(),ArtDB::class.java, "Arts").build()
        artDao = artDatabase.artDao()

        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentDetailBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveButton.setOnClickListener { save(view) }
        binding.deleteButton.setOnClickListener { delete(view) }
        binding.imageView.setOnClickListener { selectImage(view) }

        arguments?.let {
            val info = DetailFragmentArgs.fromBundle(it).info
            if (info.equals("new")) {
                binding.artNameText.setText("")
                binding.artistNameText.setText("")
                binding.yearText.setText("")
                binding.saveButton.visibility = View.VISIBLE
                binding.deleteButton.visibility = View.GONE

                val drawableImage = BitmapFactory.decodeResource(context?.resources,R.drawable.selectimage)
                binding.imageView.setImageBitmap(drawableImage)
            } else {
                //old
                binding.saveButton.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE

                val selectedId = DetailFragmentArgs.fromBundle(it).id
                mDisposable.add(artDao.getArtById(selectedId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseWithOldArt))
            }
        }
    }

    private fun handleResponseWithOldArt(art:Art) {
        artFromMain = art
        binding.artNameText.setText(art.artName)
        binding.artistNameText.setText(art.artistName)
        binding.yearText.setText(art.year)
        art.image?.let {
            val bitMap = BitmapFactory.decodeByteArray(it,0,it.size)
            binding.imageView.setImageBitmap(bitMap)
        }
    }

    fun save (view: View) {
        val artName = binding.artNameText.text.toString()
        val artistName = binding.artistNameText.text.toString()
        val year = binding.yearText.text.toString()

        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"
        val reference = storage.reference
        val imagesReference = reference.child("images").child(imageName)

        if (selectedPicture != null) {
            imagesReference.putFile(selectedPicture!!).addOnSuccessListener {
                //download url, firestore
                val upPicRef = storage.reference.child("images").child(imageName)
                upPicRef.downloadUrl.addOnSuccessListener {

                    val downloadUrl = it.toString()

                    val postMap = hashMapOf<String,Any>()
                    postMap.put("downloadUrl",downloadUrl)
                    postMap.put("userEmail",auth.currentUser!!.email.toString())
                    postMap.put("artname", artName).toString()
                    postMap.put("artistname",artistName).toString()
                    postMap.put("year",year).toString()
                    postMap.put("date",com.google.firebase.Timestamp.now())

                    firestore.collection("Arts").add(postMap).addOnCompleteListener { docs->
                        if (docs.isComplete && docs.isSuccessful) {

                            if (selectedBitmap != null) {

                                val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)
                                val outputStream = ByteArrayOutputStream()
                                smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
                                val byteArray = outputStream.toByteArray()
                                val art = Art(artName, artistName, year, byteArray)
                                mDisposable.add(
                                    artDao.insert(art)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(this::handleResponse))
                            }
                        }
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }

    fun delete (view: View) {
        artFromMain.let {
            mDisposable.delete(artDao.delete(it!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse))
        }
    }

    private fun handleResponse() {
        val action = DetailFragmentDirections.detailTOListFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    fun selectImage (view : View) {
        if (ContextCompat.checkSelfPermission(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view,"Permission needed", Snackbar.LENGTH_INDEFINITE).setAction("Give permission") {
                    //request permission
                    permiisionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }.show()
            } else {
                //request permission
                permiisionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            val toGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            //start activity for result
            activityResultLauncher.launch(toGallery)
        }
    }

    fun resultLauncher () {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    selectedPicture = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,selectedPicture!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        } else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedPicture)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        permiisionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {result ->
            if (result) {
                //permission granted
                val toGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(toGallery)
            } else {
                Toast.makeText(requireActivity(),"Permission needed",Toast.LENGTH_LONG).show()
            }
        }
    }

    fun makeSmallerBitmap(image: Bitmap, maximumSize : Int) : Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1) {
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else {
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}