package raianapereira.maps.recycle.reciclev.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_pontos_view_holder.view.*
import raianapereira.maps.recycle.reciclev.model.User

class PontosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    /**
     * Seta os views do ViewHolder com o que foi recebido da API.
     */

    fun bind(user: User, onClickListener: View.OnClickListener)
    {
        itemView.txtCepAPI.text = user.cep
        itemView.txtEmailAPI.text = user.mail
        if (user.name.length >= 27) {
            if (user.name[25] != ' ') {
                itemView.txtNomeAPI.text = user.name.substring(0, 26) + "..."
            }
            else
            {
                itemView.txtNomeAPI.text = user.name.substring(0, 25) + "..."
            }
        }
        else
        {
            itemView.txtNomeAPI.text = user.name
        }
        if (user.type.equals("E")) {
            itemView.txtTipoAPI.text = "Empresa"
        }
        else if (user.type.equals("C"))
        {
            itemView.txtTipoAPI.text = "Cooperativa"
        }
        else
        {
            itemView.txtTipoAPI.text = user.type
        }
        if (user.mail.equals(""))
        {
            itemView.txtEmail.visibility = View.GONE
            itemView.txtEmailAPI.visibility = View.GONE
        }
        itemView.txtTipoLixoAPI.text = user.discard_type
        itemView.txtRuaAPI.text = user.address.street
        itemView.txtBairroAPI.text = user.address.neighbourhood
        itemView.txtCidadeAPI.text = user.address.city
        itemView.txtUFAPI.text = user.address.uf
        itemView.txtNumeroAPI.text = user.address.number.toString()
        itemView.setOnClickListener(onClickListener)
    }
}
