package raianapereira.maps.recycle.reciclev.model

import java.io.Serializable

/**
 * Classe que armazena a lista de usuarios retornados da API
 */

data class ListaEmpresaCooperativa(
    val user: List<User>
) : Serializable

/**
 * Classe que armazena um usúario.
 */

data class User(
    val __v: Int,
    val _id: String,
    val address: Address,
    val cep: String,
    val confirmPassword: String,
    val createdAt: String,
    val discard_type: String,
    val mail: String,
    val name: String,
    val password: String,
    val type: String,
    val updatedAt: String
)

/**
 * Classe que armazena o endereço do usuario.
 */

data class Address(
    val city: String,
    val neighbourhood: String,
    val number: Int,
    val street: String,
    val uf: String
)