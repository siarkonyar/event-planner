package domainclasses

class Participant (val name: String, val id: Long? = null){

    override fun toString(): String {
        return "$name (id=$id)"
    }
}