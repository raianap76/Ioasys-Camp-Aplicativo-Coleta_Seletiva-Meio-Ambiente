package raianapereira.maps.recycle.reciclev.retrofit

import raianapereira.maps.recycle.reciclev.model.User

/**
 * Interface para definir o listener das chamadas da API de usuarios e ponto de coleta.
 */

interface InterfaceRetroFitListener {
    fun onSucess(listaAux: List<User>)

    fun onError()
}