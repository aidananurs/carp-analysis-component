package dk.cachet.carp.webservices.analysis_lib.infrastructure

import kotlinx.serialization.Serializable

@Serializable
class URL (val stringRepresentation: String )
{
   init
   {
      require(isValidURL(stringRepresentation)) { "Invalid URL: $stringRepresentation" }
   }

   private fun isValidURL(urlString: String): Boolean
   {
      return try {
         java.net.URL(urlString)
         true
      } catch (e: Exception) {
         false
      }
   }
}