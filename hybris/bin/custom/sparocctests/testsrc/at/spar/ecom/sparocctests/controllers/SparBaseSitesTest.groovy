package at.spar.ecom.sparocctests.controllers

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.commercewebservicestests.test.groovy.webservicetests.v2.spock.AbstractSpockFlowTest
import groovyx.net.http.HttpResponseDecorator
import spock.lang.Unroll

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.XML
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_OK

@ManualTest
@Unroll
class SparBaseSitesTest extends AbstractSpockFlowTest {
	def "Client retrieves a base site: #format"() {
		when:
		HttpResponseDecorator response = restClient.get(path: getBasePath() + '/basesites', contentType: format) as HttpResponseDecorator

		then:
		with(response) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_OK
			data.baseSites.any { it.uid == 'wsTest' }
		}

		where:
		format << [JSON]
	}

	def "Client retrieves countries for base sites"() {
		when:
		HttpResponseDecorator response = restClient.get(path: getBasePath() + '/basesites/countries') as HttpResponseDecorator

		then:
		with(response) {
			if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
			status == SC_BAD_REQUEST
			data.errors.get(0).get("type") == "InvalidResourceError"
			data.errors.get(0).get("message") == "Base site basesites doesn't exist"
		}
	}
}
