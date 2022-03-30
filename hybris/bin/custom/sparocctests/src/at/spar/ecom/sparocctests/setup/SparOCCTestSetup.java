package at.spar.ecom.sparocctests.setup;

import de.hybris.platform.commercewebservicestests.setup.CommercewebservicesTestSetup;


public class SparOCCTestSetup extends CommercewebservicesTestSetup {

  public void loadData() {
    getSetupImpexService().importImpexFile("/sparocctests/import/sampledata/essential-data.impex", false);
    getSetupImpexService().importImpexFile("/sparocctests/import/sampledata/base-store-data.impex", false);
    getSetupImpexService().importImpexFile("/sparocctests/import/sampledata/solr-data.impex", false);
    getSetupSolrIndexerService().executeSolrIndexerCronJob(String.format("%sIndex", WS_TEST), true);
  }
}
