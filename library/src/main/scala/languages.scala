package pamflet

import collection.immutable.Map

object Language {
  // see http://en.wikipedia.org/wiki/IETF_language_tag
  val languageNames: Map[String, String] = Map(
    "ar" -> "العربية",
    "bn" -> "বাংলা",
    "ca" -> "Català",
    "cs" -> "Čeština",
    "de" -> "Deutsch",
    "en" -> "English",
    "es" -> "Español",
    "fa" -> "فارسی",
    "fi" -> "Suomi",
    "fr" -> "Français",
    "he" -> "עברית",
    "hi" -> "हिन्दी",
    "hu" -> "Magyar",
    "id" -> "Bahasa Indonesia",
    "it" -> "Italiano",
    "ja" -> "日本語",
    "ko" -> "한국어",
    "nl" -> "Nederlands",
    "no" -> "Norsk (Bokmål)",
    "pl" -> "Polski",
    "pt" -> "Português",
    "ru" -> "Русский",
    "sv" -> "Svenska",
    "tr" -> "Türkçe",
    "vi" -> "Tiếng Việt",
    "uk" -> "Українська",
    "zh" -> "中文"
  )

  def languageName(code: String): Option[String] = languageNames get code
}
