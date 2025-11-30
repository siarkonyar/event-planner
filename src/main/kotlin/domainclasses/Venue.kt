package domainclasses

class Venue(
    val name: String,
    val address: String,
    val capacity: Int,
    val id: Long? = null
) {
    override fun toString(): String {
        return "name: $name, address: $address, capacity: $capacity"
    }
}
