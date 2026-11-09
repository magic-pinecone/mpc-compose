package org.mpc.domain.session

import org.mpc.domain.model.EeclassAttachment

private val FILE_DOWNLOAD_PATH = Regex("""/filedownload/\d+""")

/** The file an ee-class `/filedownload/{id}` link points to, or null when [url] is not one. */
fun eeclassFileLink(url: String, name: String): EeclassAttachment.File? {
    val parsed = parseHttpsUrl(url) ?: return null
    if (!parsed.host.equals(CampusSites.Eeclass.host, ignoreCase = true)) return null
    val path = parsed.encodedPath.trimEnd('/').takeIf(FILE_DOWNLOAD_PATH::matches) ?: return null
    return EeclassAttachment.File(name, SitePath.of(path))
}
