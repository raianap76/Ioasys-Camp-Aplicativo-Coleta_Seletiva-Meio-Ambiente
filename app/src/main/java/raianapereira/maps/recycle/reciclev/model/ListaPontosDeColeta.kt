package raianapereira.maps.recycle.reciclev.model

/**
 * Classe que armazena a lista de pontos de coleta retornados da API.
 */

data class ListaPontosDeColeta(
    val collect_point: List<CollectPoint>
)

/**
 * Classe que armazena um ponto de coleta.
 */

data class CollectPoint(
    val __v: Int,
    val _id: String,
    val cep: String,
    val discard_type: String,
    val name: String,
    val point_address: PointAddress
)

/**
 * Classe que armazena o endereço do ponto de coleta.
 */

data class PointAddress(
    val city: String,
    val neighbourhood: String,
    val number: Int,
    val street: String,
    val uf: String
)