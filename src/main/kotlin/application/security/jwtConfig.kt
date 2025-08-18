import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    private val secret = System.getenv("JWT_SECRET") ?: "dev-secret" // cambia esto en prod
    private val alg = Algorithm.HMAC256(secret)

    const val issuer = "ktor-socios"
    const val audience = "ktor-socios-users"
    const val realm = "ktor-socios-realm"

    fun algorithm(): Algorithm = alg

//    // Overload 1: simple (compat)
//    fun issue(userId: Int, email: String, expiresMs: Long = 1000L * 60 * 60 * 8): String {
//        val now = System.currentTimeMillis()
//        return JWT.create()
//            .withIssuer(issuer)
//            .withAudience(audience)
//            .withClaim("sub", userId)
//            .withClaim("email", email)
//            .withIssuedAt(Date(now))
//            .withExpiresAt(Date(now + expiresMs))
//            .sign(alg)
//    }

    // Overload 2: con roles y permisos (USAR ESTE EN EL LOGIN)
    fun issue(
        userId: Int,
        email: String,
        roles: List<String>,
        perms: List<String>,
        expiresMs: Long = 1000L * 60 * 60 * 8
    ): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("sub", userId)
            .withClaim("email", email)
            .withArrayClaim("roles", roles.toTypedArray())
            .withArrayClaim("perms", perms.toTypedArray())
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + expiresMs))
            .sign(alg)
    }
}
