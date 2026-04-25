package com.jetbrains.spacetutorial.texaswatch.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Summary (list view) ──────────────────────────────────────────────────────

@Serializable
data class OffenderSummary(
    @SerialName("indIdn")       val indIdn: Int,
    @SerialName("dpsNumber")    val dpsNumber: String,
    @SerialName("firstName")    val firstName: String,
    @SerialName("lastName")     val lastName: String,
    @SerialName("fullName")     val fullName: String,
    @SerialName("photoUrl")     val photoUrl: String?,
    @SerialName("address")      val address: String?,
    @SerialName("age")          val age: Int?,
    @SerialName("detailsUrl")   val detailsUrl: String,
    val lat: Double? = null,
    val lon: Double? = null,
)

/** Returns the best available display name — falls back to firstName+lastName if fullName is blank or "Unknown". */
val OffenderSummary.displayName: String get() {
    if (fullName.isNotBlank() && !fullName.equals("Unknown", ignoreCase = true)) return fullName
    val constructed = listOf(firstName, lastName)
        .filter { it.isNotBlank() && !it.equals("Unknown", ignoreCase = true) }
        .joinToString(" ")
    return constructed.ifBlank { fullName }
}

// ── Paginated search response ────────────────────────────────────────────────

@Serializable
data class OffenderSearchResponse(
    @SerialName("content")       val content: List<OffenderSummary>,
    @SerialName("page")          val page: Int = 0,
    @SerialName("size")          val size: Int = 20,
    @SerialName("totalElements") val totalElements: Long,
    @SerialName("totalPages")    val totalPages: Int
)

// ── Detail response ──────────────────────────────────────────────────────────

@Serializable
data class OffenderDetail(
    @SerialName("indIdn")               val indIdn: Int,
    @SerialName("dpsNumber")            val dpsNumber: String,
    @SerialName("physicalDescription")  val physicalDescription: PhysicalDescription?,
    @SerialName("names")                val names: Names?,
    @SerialName("birthInfo")            val birthInfo: BirthInfo?,
    @SerialName("registryInfo")         val registryInfo: RegistryInfo?,
    @SerialName("offenses")             val offenses: List<Offense> = emptyList(),
    @SerialName("photos")               val photos: Photos?,
    @SerialName("addresses")            val addresses: List<OffenderAddress> = emptyList(),
    @SerialName("summary")              val summary: OffenderSummaryDetail?
)

@Serializable
data class PhysicalDescription(
    @SerialName("sex")                  val sex: String?,
    @SerialName("sexDescription")       val sexDescription: String?,
    @SerialName("race")                 val race: String?,
    @SerialName("raceDescription")      val raceDescription: String?,
    @SerialName("heightFormatted")      val heightFormatted: String?,
    @SerialName("weightPounds")         val weightPounds: Int?,
    @SerialName("hairColorDescription") val hairColorDescription: String?,
    @SerialName("eyeColorDescription")  val eyeColorDescription: String?
)

@Serializable
data class Names(
    @SerialName("baseName") val baseName: NameEntry?,
    @SerialName("aliases")  val aliases: List<NameEntry> = emptyList()
)

@Serializable
data class NameEntry(
    @SerialName("firstName")  val firstName: String?,
    @SerialName("lastName")   val lastName: String?,
    @SerialName("middleName") val middleName: String?,
    @SerialName("fullName")   val fullName: String?
)

@Serializable
data class BirthInfo(
    @SerialName("age") val age: Int?
)

@Serializable
data class RegistryInfo(
    @SerialName("riskLevel")            val riskLevel: String?,
    @SerialName("riskLevelDescription") val riskLevelDescription: String?,
    @SerialName("sexuallyViolentPredator") val sexuallyViolentPredator: Boolean = false,
    @SerialName("deregistered")         val deregistered: Boolean = false,
    @SerialName("endingRegistrationDate") val endingRegistrationDate: String?
)

@Serializable
data class Offense(
    @SerialName("offenseId")          val offenseId: Int,
    @SerialName("offenseDescription") val offenseDescription: String?,
    @SerialName("statute")            val statute: String?,
    @SerialName("convictionDate")     val convictionDate: String?,
    @SerialName("ageOfVictim")        val ageOfVictim: Int?,
    @SerialName("sexOfVictimDescription") val sexOfVictimDescription: String?,
    @SerialName("dispositionDescription") val dispositionDescription: String?
)

@Serializable
data class Photos(
    @SerialName("currentPhoto") val currentPhoto: Photo?,
    @SerialName("allPhotos")    val allPhotos: List<Photo> = emptyList()
)

@Serializable
data class Photo(
    @SerialName("photoId")      val photoId: Int,
    @SerialName("photoUrl")     val photoUrl: String?,
    @SerialName("dateReported") val dateReported: String?,
    @SerialName("isCurrent")    val isCurrent: Boolean = false
)

@Serializable
data class OffenderAddress(
    @SerialName("addressId")        val addressId: Int,
    @SerialName("fullAddress")      val fullAddress: String?,
    @SerialName("city")             val city: String?,
    @SerialName("state")            val state: String?,
    @SerialName("zipCode")          val zipCode: String?,
    @SerialName("countyName")       val countyName: String?,
    @SerialName("latitude")         val latitude: Double?,
    @SerialName("longitude")        val longitude: Double?,
    @SerialName("isCurrent")        val isCurrent: Boolean = false,
    @SerialName("lastReportedDate") val lastReportedDate: String?
)

@Serializable
data class OffenderSummaryDetail(
    @SerialName("totalOffenses")            val totalOffenses: Int,
    @SerialName("hasCurrentAddress")        val hasCurrentAddress: Boolean,
    @SerialName("hasCurrentPhoto")          val hasCurrentPhoto: Boolean,
    @SerialName("mostRecentRegistrationDate") val mostRecentRegistrationDate: String?
)

// ── Map / radius ─────────────────────────────────────────────────────────────

@Serializable
data class MapOffenderResponse(
    @SerialName("content")       val content: List<MapOffender>,
    @SerialName("page")          val page: Int = 0,
    @SerialName("size")          val size: Int = 20,
    @SerialName("totalElements") val totalElements: Long,
    @SerialName("totalPages")    val totalPages: Int
)

@Serializable
data class MapOffender(
    @SerialName("indIdn")     val indIdn: Int,
    @SerialName("dpsNumber")  val dpsNumber: String,
    @SerialName("fullName")   val fullName: String,
    @SerialName("photoUrl")   val photoUrl: String?,
    @SerialName("address")    val address: String?,
    @SerialName("latitude")   val latitude: Double?,
    @SerialName("longitude")  val longitude: Double?
)

@Serializable
data class RiskStats(
    @SerialName("lowAndModerateCount") val lowAndModerateCount: Int,
    @SerialName("highRiskCount")       val highRiskCount: Int
)

// ── Contact scan ─────────────────────────────────────────────────────────────

@Serializable
data class ContactMatchResult(
    @SerialName("contactName") val contactName: String,
    @SerialName("matches")     val matches: List<OffenderSummary>,
)

@Serializable
data class ContactScanResponse(
    @SerialName("results")              val results: List<ContactMatchResult>,
    @SerialName("totalMatches")         val totalMatches: Int,
    @SerialName("contactsWithMatches")  val contactsWithMatches: Int,
)
