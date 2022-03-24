package com.tolgaay.myhomework384_fragmentnavigation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.tolgaay.myhomework384_fragmentnavigation.RoomDb.Art
import com.tolgaay.myhomework384_fragmentnavigation.databinding.RecyclerRowBinding

class ArtAdapter(val artList : List<Art>) : RecyclerView.Adapter<ArtAdapter.ArtHolder>() {

    class ArtHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.artNameText.text = artList[position].artName
        holder.itemView.setOnClickListener {
            val action = ListFragmentDirections.listTODetailFragment(artList[position].id,"old")
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return artList.size
    }
}