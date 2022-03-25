package com.hybris.multicountry.adaptivesearch.strategies.impl;

import com.hybris.multicountry.adaptivesearch.MultiCountrySolrAsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;

import javax.annotation.Resource;

/**
 * Search provider factory that will always return multi country provider.
 */
public class MultiCountryAsSearchProviderFactory implements AsSearchProviderFactory {

    @Resource(name= "multiCountrySolrAsSearchProvider")
    private MultiCountrySolrAsSearchProvider solrAsSearchProvider;

    @Override
    public AsSearchProvider getSearchProvider() {
        return solrAsSearchProvider;
    }

    public void setSolrAsSearchProvider(MultiCountrySolrAsSearchProvider solrAsSearchProvider) {
        this.solrAsSearchProvider = solrAsSearchProvider;
    }
}
