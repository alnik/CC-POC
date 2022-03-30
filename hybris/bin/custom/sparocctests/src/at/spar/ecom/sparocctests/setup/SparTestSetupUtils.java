package at.spar.ecom.sparocctests.setup;

import de.hybris.platform.commercewebservicestests.setup.TestSetupUtils;
import de.hybris.platform.core.Registry;


/**
 * Utility class to be used in test suites to manage tests (e.g. start server, load data).
 */
public class SparTestSetupUtils extends TestSetupUtils {

  public static void loadExtensionDataInJunit() throws Exception {
    loadDataInJunit();
    loadExtensionData();
  }

  public static void loadExtensionData() {
    final SparOCCTestSetup occTestSetup = Registry.getApplicationContext().getBean("sparOCCTestSetup", SparOCCTestSetup.class);
    occTestSetup.loadData();
  }

}

