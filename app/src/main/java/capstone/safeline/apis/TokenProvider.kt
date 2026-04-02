package capstone.safeline.apis

object TokenProvider {
    @Volatile
    var token: String? = null
}