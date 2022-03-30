package at.spar.ecom.sparocctests


import at.spar.ecom.sparocctests.controllers.SparCartFlowTest
import at.spar.ecom.sparocctests.controllers.SparProductsStockTest
import at.spar.ecom.sparocctests.controllers.SparBaseSitesTest
import at.spar.ecom.sparocctests.setup.SparTestSetupUtils
import de.hybris.bootstrap.annotations.IntegrationTest
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite.class)
@Suite.SuiteClasses([SparBaseSitesTest, SparCartFlowTest, SparProductsStockTest])
@IntegrationTest
class SparAllOCCSpockTests {

	@BeforeClass
	static void setUpClass() {
		SparTestSetupUtils.loadExtensionDataInJunit()
		SparTestSetupUtils.startServer()
	}

	@AfterClass
	static void tearDown() {
		SparTestSetupUtils.stopServer()
		SparTestSetupUtils.cleanData()
	}

	@Test
	static void testing() {
		//dummy test class
	}
}
