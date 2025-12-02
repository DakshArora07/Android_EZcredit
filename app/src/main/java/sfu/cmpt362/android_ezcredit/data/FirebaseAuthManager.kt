package sfu.cmpt362.android_ezcredit.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {
    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? = auth.currentUser

    suspend fun createUser(email: String, password: String): FirebaseUser? {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "createUser failed", e)
            null
        }
    }

    suspend fun signIn(email: String, password: String): FirebaseUser? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loginAndSetCompanyContext(email: String, password: String): Long? {
        val user = signIn(email, password) ?: return null

        FirebaseRefs.companiesRef().get().await().children.forEach { companySnap ->
            val companyId = companySnap.key?.toLongOrNull() ?: return@forEach
            FirebaseRefs.usersRef(companyId).get().await().children.forEach { userSnap ->
                val userEmail = userSnap.child("email").getValue(String::class.java)
                if (userEmail == email) {
                    CompanyContext.currentCompanyId = companyId
                    CompanyContext.currentUserId = auth.currentUser?.uid
                    return companyId  // Found!
                }
            }
        }
        return null  // User not found
    }

    fun signOut() {
        auth.signOut()
        CompanyContext.clear()  // Clear company context
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}