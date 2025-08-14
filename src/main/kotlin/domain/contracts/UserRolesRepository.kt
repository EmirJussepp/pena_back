package domain.contracts

interface UserRolesRepository {
    fun assign(userId: Int, roleName: String): Boolean
    fun getRolesForUser(userId: Int): List<String>
}