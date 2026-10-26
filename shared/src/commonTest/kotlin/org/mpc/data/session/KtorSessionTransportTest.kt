package org.mpc.data.session

import de.infix.testBalloon.framework.core.testSuite
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.mpc.domain.session.CampusSites
import org.mpc.domain.session.Unavailability
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

private const val PORTAL = "https://portal.ncu.edu.tw"
private const val EECLASS = "https://ncueeclass.ncu.edu.tw"
private const val SSO_LOGIN = "$EECLASS/ssologin?systemId=168&loginName=one-time&sn=1&verifyName=MTIz"

private fun MockRequestHandleScope.redirect(location: String, vararg setCookies: String): HttpResponseData = respond(
    content = "",
    status = HttpStatusCode.Found,
    headers = headersOf(
        HttpHeaders.Location to listOf(location),
        HttpHeaders.SetCookie to setCookies.toList(),
    ),
)

private fun transport(
    jar: PlatformCookieJar,
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
): Pair<KtorSessionTransport, MutableList<HttpRequestData>> {
    val requests = mutableListOf<HttpRequestData>()
    val client = HttpClient(
        MockEngine { request ->
            requests += request
            handler(request)
        },
    ) {
        expectSuccess = false
        followRedirects = false
    }
    return KtorSessionTransport(client, jar) to requests
}

val ktorSessionTransportTests by testSuite {
    test("a handoff arms Portal, follows each hop by hand, and stores the site session") {
        runTest {
            val jar = InMemoryCookieJar().apply { cookies["portal.ncu.edu.tw"] = mutableMapOf("SESSION" to "portal") }
            val (transport, requests) = transport(jar) { request ->
                when (request.url.toString()) {
                    "$PORTAL/system/ncueeclass" -> respond("<a href='/system/ncueeclass/go'>OK</a>")
                    "$PORTAL/system/ncueeclass/go" -> redirect(SSO_LOGIN)
                    SSO_LOGIN -> redirect("/dashboard", "PHPSESSID=site; path=/; secure; HttpOnly")
                    "$EECLASS/dashboard" -> respond("dashboard")
                    else -> error("unexpected request")
                }
            }

            assertEquals(HandoffResult.Done, transport.handoff(CampusSites.Eeclass))

            assertEquals(
                listOf("$PORTAL/system/ncueeclass", "$PORTAL/system/ncueeclass/go", SSO_LOGIN, "$EECLASS/dashboard"),
                requests.map { it.url.toString() },
            )
            assertEquals("SESSION=portal", requests[1].headers[HttpHeaders.Cookie])
            assertEquals("PHPSESSID=site", requests[3].headers[HttpHeaders.Cookie])
            assertEquals<Map<String, String>?>(mapOf("PHPSESSID" to "site"), jar.cookies["ncueeclass.ncu.edu.tw"])
        }
    }

    test("a handoff reports Portal sign-in and displacement") {
        runTest {
            val (signedOut, _) = transport(InMemoryCookieJar()) { redirect("$PORTAL/login") }
            val (displaced, _) = transport(InMemoryCookieJar()) { redirect("/expired") }

            assertEquals(HandoffResult.PortalSignedOut, signedOut.handoff(CampusSites.Eeclass))
            assertEquals(HandoffResult.PortalDisplaced, displaced.handoff(CampusSites.Eeclass))
        }
    }

    test("a handoff refuses redirects off Portal and the site, and an unarmed fallback home") {
        runTest {
            val (offHost, offHostRequests) = transport(InMemoryCookieJar()) { request ->
                if (request.url.encodedPath.endsWith("/go")) redirect("https://evil.example/steal") else respond("ok")
            }
            val (unarmed, _) = transport(InMemoryCookieJar()) { request ->
                if (request.url.encodedPath.endsWith("/go")) redirect("$PORTAL/") else respond("ok")
            }

            assertEquals(HandoffResult.Failed(Unavailability.UNEXPECTED_RESPONSE), offHost.handoff(CampusSites.Eeclass))
            assertEquals(2, offHostRequests.size)
            assertEquals(HandoffResult.Failed(Unavailability.UNEXPECTED_RESPONSE), unarmed.handoff(CampusSites.Eeclass))
        }
    }

    test("a WebView cookie change during a request makes the response stale and keeps its cookies out") {
        runTest {
            val jar = InMemoryCookieJar().apply { cookies["ncueeclass.ncu.edu.tw"] = mutableMapOf("PHPSESSID" to "before") }
            val (transport, requests) = transport(jar) {
                jar.cookies.getValue("ncueeclass.ncu.edu.tw")["PHPSESSID"] = "after"
                redirect("/index/login", "PHPSESSID=stale; Path=/")
            }

            val result = transport.get("$EECLASS/dashboard")

            assertEquals("PHPSESSID=before", requests.single().headers[HttpHeaders.Cookie])
            assertEquals(TransportResult.Failed(Unavailability.UNEXPECTED_RESPONSE), result)
            assertEquals("after", jar.cookies.getValue("ncueeclass.ncu.edu.tw")["PHPSESSID"])
        }
    }

    test("a network failure carries only its reason") {
        runTest {
            val (transport, _) = transport(InMemoryCookieJar()) { error("connection reset for $SSO_LOGIN") }

            assertEquals(TransportResult.Failed(Unavailability.NETWORK), transport.get("$EECLASS/dashboard"))
        }
    }

    test("a request without cookies sends no Cookie header") {
        runTest {
            val (transport, requests) = transport(InMemoryCookieJar()) { respond("page") }

            assertIs<TransportResult.Response>(transport.get("$EECLASS/dashboard"))
            assertNull(requests.single().headers[HttpHeaders.Cookie])
        }
    }

    test("overlapping requests do not mistake each other's cookies for a WebView change") {
        runTest {
            val jar = InMemoryCookieJar()
            val (transport, _) = transport(jar) {
                yield()
                redirect("/index/login", "PHPSESSID=fresh; Path=/; Secure; HttpOnly")
            }

            val results = listOf(
                async { transport.get("$EECLASS/dashboard") },
                async { transport.get("$EECLASS/dashboard") },
            ).awaitAll()

            assertEquals(listOf(true, true), results.map { it is TransportResult.Response })
        }
    }
}
