package com.payra.calling.data.backend

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.payra.calling.data.FakeChatStore
import org.json.JSONObject

sealed class AuthResult {
    object Ok : AuthResult()
    class Error(val message: String) : AuthResult()
}

/** A link that brought the person back into the app from an e-mail (reset password / confirm Gmail). */
class AuthLink(val type: String, val accessToken: String, val refreshToken: String, val error: String)

object AuthLinks {
    var pending by mutableStateOf<AuthLink?>(null)

    fun handle(data: Uri?) {
        if (data == null || data.scheme != "payra" || data.host != "auth-callback") return
        val raw = data.fragment ?: data.encodedQuery ?: ""
        val params = raw.split("&").mapNotNull {
            val i = it.indexOf('=')
            if (i <= 0) null else Uri.decode(it.substring(0, i)) to Uri.decode(it.substring(i + 1).replace('+', ' '))
        }.toMap()
        pending = AuthLink(
            type = params["type"].orEmpty(),
            accessToken = params["access_token"].orEmpty(),
            refreshToken = params["refresh_token"].orEmpty(),
            error = params["error_description"].orEmpty(),
        )
    }
}

/** Sign up, log in, log out and keep the login alive (the session is saved on the phone). */
object Auth {
    class Session(
        val accessToken: String,
        val refreshToken: String,
        val expiresAtSec: Long,
        val userId: String,
        val phone: String,
        val email: String,
    )

    var session by mutableStateOf<Session?>(null)
        private set

    val isLoggedIn: Boolean get() = session != null

    /** The name typed at sign-up, used to pre-fill the profile setup screen. */
    var signupName: String = ""

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences("payra_auth", Context.MODE_PRIVATE)
        prefs = p
        val token = p.getString("access", null)
        val refresh = p.getString("refresh", null)
        val id = p.getString("uid", null)
        if (!token.isNullOrBlank() && !refresh.isNullOrBlank() && !id.isNullOrBlank()) {
            session = Session(token, refresh, p.getLong("exp", 0L), id, p.getString("phone", "").orEmpty(), p.getString("email", "").orEmpty())
        }
    }

    private fun store(s: Session?) {
        session = s
        val e = prefs?.edit() ?: return
        if (s == null) {
            e.clear()
        } else {
            e.putString("access", s.accessToken).putString("refresh", s.refreshToken).putLong("exp", s.expiresAtSec)
                .putString("uid", s.userId).putString("phone", s.phone).putString("email", s.email)
        }
        e.apply()
    }

    private fun parseSession(j: JSONObject?): Session? {
        if (j == null) return null
        val access = j.optString("access_token")
        val refresh = j.optString("refresh_token")
        val user = j.optJSONObject("user") ?: return null
        if (access.isBlank() || refresh.isBlank()) return null
        val expires = if (j.has("expires_at")) j.optLong("expires_at") else System.currentTimeMillis() / 1000 + j.optLong("expires_in", 3600)
        return Session(access, refresh, expires, user.optString("id"), user.optString("phone"), user.optString("email"))
    }

    /** "+880 1712 345678", "01712345678" and "8801712345678" all become +8801712345678. */
    fun normalizePhone(input: String): String {
        var p = input.replace(Regex("[\\s\\-()]"), "")
        p = when {
            p.startsWith("+") -> p
            p.startsWith("00") -> "+" + p.substring(2)
            p.startsWith("880") -> "+$p"
            p.startsWith("0") -> "+880" + p.substring(1)
            else -> "+$p"
        }
        return p
    }

    private fun looksLikePhone(p: String) = Regex("^\\+[0-9]{8,15}$").matches(p)

    private fun friendly(r: Backend.Response): String {
        if (r.offline) return "No internet connection. Check your network and try again."
        val j = r.json()
        val code = (j?.optString("error_code") ?: "").ifEmpty { j?.optString("code").orEmpty() }
        val msg = (j?.optString("msg") ?: "").ifEmpty { j?.optString("message").orEmpty() }.ifEmpty { j?.optString("error_description").orEmpty() }
        return when {
            code == "invalid_credentials" || msg.contains("Invalid login", true) -> "Incorrect phone number or password."
            code == "user_already_exists" || code == "phone_exists" || msg.contains("already", true) -> "This number already has an account. Log in instead."
            code == "weak_password" -> "That password is too weak. Use 8 or more characters."
            code == "over_request_rate_limit" || code == "over_sms_send_rate_limit" || r.code == 429 -> "Too many tries. Please wait a moment and try again."
            code == "phone_provider_disabled" || code == "sms_send_failed" -> "Phone sign-in is not ready on the server yet."
            code == "same_password" -> "The new password must be different from the old one."
            code == "email_exists" -> "This Gmail is already used by another account."
            msg.isNotBlank() -> msg
            else -> "Something went wrong (${r.code}). Please try again."
        }
    }

    suspend fun signUp(phoneInput: String, password: String, name: String): AuthResult {
        val phone = normalizePhone(phoneInput)
        if (!looksLikePhone(phone)) return AuthResult.Error("Enter a valid phone number, for example +880 1712 345678.")
        val body = JSONObject().put("phone", phone).put("password", password)
            .put("data", JSONObject().put("name", name.trim()))
        val r = Backend.request("POST", "/auth/v1/signup", body)
        if (!r.ok) return AuthResult.Error(friendly(r))
        val s = parseSession(r.json())
            ?: return AuthResult.Error("The server asked for a phone code. Turn off \"Confirm phone\" in Supabase > Authentication > Phone.")
        signupName = name.trim()
        store(s)
        return AuthResult.Ok
    }

    suspend fun signIn(phoneOrEmail: String, password: String): AuthResult {
        val body = JSONObject().put("password", password)
        if (phoneOrEmail.contains("@")) body.put("email", phoneOrEmail.trim()) else {
            val phone = normalizePhone(phoneOrEmail)
            if (!looksLikePhone(phone)) return AuthResult.Error("Enter a valid phone number, for example +880 1712 345678.")
            body.put("phone", phone)
        }
        val r = Backend.request("POST", "/auth/v1/token?grant_type=password", body)
        if (!r.ok) return AuthResult.Error(friendly(r))
        val s = parseSession(r.json()) ?: return AuthResult.Error("Could not log in. Please try again.")
        store(s)
        return AuthResult.Ok
    }

    /** A token that is valid right now (refreshes it first when it is about to expire). Null = logged out. */
    suspend fun validToken(): String? {
        val s = session ?: return null
        val now = System.currentTimeMillis() / 1000
        if (s.expiresAtSec - now > 60) return s.accessToken
        val r = Backend.request("POST", "/auth/v1/token?grant_type=refresh_token", JSONObject().put("refresh_token", s.refreshToken))
        if (r.ok) {
            val fresh = parseSession(r.json())
            if (fresh != null) {
                store(fresh)
                return fresh.accessToken
            }
        }
        // no connection: keep the login and try again later; a refused token means the login is over
        if (r.code in 400..499) store(null)
        return if (r.offline) s.accessToken else null
    }

    suspend fun signOut() {
        val token = session?.accessToken
        store(null)
        FakeChatStore.loadedScreens.clear()
        if (token != null) Backend.request("POST", "/auth/v1/logout", null, token)
    }

    /** Forgot password: the reset link is sent to the Gmail linked to this phone number (the answer never says if it exists). */
    suspend fun requestReset(phoneInput: String): AuthResult {
        val r = Backend.request("POST", "/functions/v1/request-password-reset", JSONObject().put("phone", normalizePhone(phoneInput)))
        return if (r.offline) AuthResult.Error(friendly(r)) else AuthResult.Ok
    }

    suspend fun changePassword(current: String, newPassword: String): AuthResult {
        val s = session ?: return AuthResult.Error("Please log in again.")
        val who = s.phone.ifBlank { s.email }
        val check = Backend.request(
            "POST", "/auth/v1/token?grant_type=password",
            JSONObject().put("password", current).put(if (s.phone.isNotBlank()) "phone" else "email", who),
        )
        if (!check.ok) return AuthResult.Error(if (check.offline) friendly(check) else "Your current password is not correct.")
        val token = validToken() ?: return AuthResult.Error("Please log in again.")
        val r = Backend.request("PUT", "/auth/v1/user", JSONObject().put("password", newPassword), token)
        return if (r.ok) AuthResult.Ok else AuthResult.Error(friendly(r))
    }

    /** Sends a confirmation link to the Gmail. When the person taps it, the Gmail is linked to the account. */
    suspend fun linkEmail(email: String): AuthResult {
        val token = validToken() ?: return AuthResult.Error("Please log in again.")
        val r = Backend.request(
            "PUT", "/auth/v1/user?redirect_to=" + Uri.encode(Backend.APP_LINK),
            JSONObject().put("email", email.trim()), token,
        )
        return if (r.ok) AuthResult.Ok else AuthResult.Error(friendly(r))
    }

    /** Used by the "new password" screen after the person opened the reset link from the e-mail. */
    suspend fun setPasswordWithLink(accessToken: String, newPassword: String): AuthResult {
        val r = Backend.request("PUT", "/auth/v1/user", JSONObject().put("password", newPassword), accessToken)
        return if (r.ok) AuthResult.Ok else AuthResult.Error(
            if (r.code == 401 || r.code == 403) "This link has expired. Ask for a new reset link." else friendly(r),
        )
    }

    /** The person tapped a "confirm Gmail" link: keep them logged in and refresh the account details. */
    suspend fun adoptSession(accessToken: String, refreshToken: String): Boolean {
        val r = Backend.request("GET", "/auth/v1/user", null, accessToken)
        val user = r.json() ?: return false
        if (!r.ok) return false
        store(
            Session(
                accessToken, refreshToken, System.currentTimeMillis() / 1000 + 3000,
                user.optString("id"), user.optString("phone"), user.optString("email"),
            ),
        )
        return true
    }

    suspend fun deleteAccount(): Boolean {
        val token = validToken() ?: return false
        val r = Backend.request("POST", "/functions/v1/delete-account", JSONObject().put("confirm", "DELETE"), token)
        if (r.ok) {
            store(null)
            FakeChatStore.loadedScreens.clear()
        }
        return r.ok
    }
}

/** My profile on the server (name, about). Photos come with the upload step. */
object ProfileRepo {
    suspend fun loadMine() {
        val s = Auth.session ?: return
        val token = Auth.validToken() ?: return
        val r = Backend.request("GET", "/rest/v1/profiles?id=eq.${s.userId}&select=display_name,about,avatar_url,cover_url", null, token)
        val row = r.array()?.optJSONObject(0) ?: return
        FakeChatStore.applyServerProfile(
            name = row.optString("display_name"),
            about = row.optString("about"),
            avatar = if (row.isNull("avatar_url")) "" else row.optString("avatar_url"),
            cover = if (row.isNull("cover_url")) "" else row.optString("cover_url"),
        )
    }

    suspend fun saveMine(name: String, about: String): Boolean {
        val token = Auth.validToken() ?: return false
        val body = JSONObject().put("p_name", name.trim()).put("p_about", about.trim())
        return Backend.request("POST", "/rest/v1/rpc/update_my_profile", body, token).ok
    }
}
