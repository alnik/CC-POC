package at.spar.ecom.sparocctests.controllers

import de.hybris.bootstrap.annotations.ManualTest;
import de.hybris.platform.commercewebservicestests.test.groovy.webservicetests.v2.spock.AbstractSpockFlowTest
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator

import static groovyx.net.http.ContentType.*
import static org.apache.http.HttpStatus.SC_OK

@ManualTest
class SparProductsStockTest extends AbstractSpockFlowTest {

	def "Get total number of product's stock levels : #format"() {

		when: "user search for product's stock levels"
		HttpResponseDecorator response = restClient.get(
				path: getBasePathWithSite() + '/products/3429337/stock',
				contentType: format,
				query: ['location': 'tokio'],
				requestContentType: ContentType.URLENC
		) as HttpResponseDecorator

		then: "he gets all the requested fields"
		with(response) {
			status == SC_OK
			response.getFirstHeader(HEADER_TOTAL_COUNT as String).getValue() == '49'
		}

		where:
		format << [JSON]
	}
}
