package raianapereira.maps.recycle.reciclev.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import raianapereira.maps.recycle.reciclev.R
import raianapereira.maps.recycle.reciclev.model.User

class PontosAdapter(val users:ArrayList<User>, val onClickItem: (view: View, user: User)-> Unit) : RecyclerView.Adapter<PontosViewHolder>() {

    /**
     * Retorna a quantidade de itens inseridos na lista.
     */

    override fun getItemCount(): Int
    {
        return (users.size)
    }

    /**
     * Seta o item da posição (position) da lista.
     */

    override fun onBindViewHolder(holder: PontosViewHolder, position: Int)
    {
        val user: User = users[position]
        holder.bind(user,createOnClickListener(user))
    }

    fun createOnClickListener(user: User): View.OnClickListener
    {
        return (View.OnClickListener { view -> onClickItem(view, user) })
    }

    /**
     * Infla o ViewHolder do item.
     */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PontosViewHolder
    {
        return (PontosViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.activity_pontos_view_holder,parent,false)))
    }
}