package com.hybris.multicountry.adaptivesearch.strategies.impl;

import static com.hybris.multicountry.adaptivesearch.MultiCountrySolrAsSearchProvider.ALL_CATEGORIES_FIELD;
import static com.hybris.multicountry.adaptivesearch.MultiCountrySolrAsSearchProvider.BASE_STORE;

import de.hybris.platform.adaptivesearchsolr.strategies.impl.DefaultSolrAsCategoryPathResolver;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.solrfacetsearch.search.FacetValueField;
import de.hybris.platform.solrfacetsearch.search.QueryField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.store.BaseStoreModel;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;

public class MultiCountryAsSolrAsCategoryPathResolver extends DefaultSolrAsCategoryPathResolver {

	@Resource
	private SessionService sessionService;


	@Override
	protected void resolveCategoryCodesFromFilterQueries(SearchQuery searchQuery, List<String> categoryCodes) {
		final List<QueryField> filterQueries = searchQuery.getFilterQueries();
		if (CollectionUtils.isNotEmpty(filterQueries)) {
			final String mcFacetFilterIndexProperty = buildFieldName();
			searchQuery.getFilterQueries().stream()
					   .filter(qf -> mcFacetFilterIndexProperty.equals(qf.getField()))
					   .map(QueryField::getValues)
					   .filter(CollectionUtils::isNotEmpty)
					   .findFirst()
					   .ifPresent(values -> addCategoryCodes(categoryCodes, values));
		}
	}

	@Override
	protected void resolveCategoryCodesFromFacetValues(SearchQuery searchQuery, List<String> categoryCodes) {
		final List<FacetValueField> facetValues = searchQuery.getFacetValues();
		if (CollectionUtils.isNotEmpty(facetValues)) {
			final String mcFacetFilterIndexProperty = this.resolveFacetFilterIndexProperty();
			searchQuery.getFacetValues().stream()
					   .filter(qf -> mcFacetFilterIndexProperty.equals(qf.getField()))
					   .map(FacetValueField::getValues)
					   .filter(CollectionUtils::isNotEmpty)
					   .findFirst()
					   .ifPresent(values -> addCategoryCodes(categoryCodes, values));
		}
	}

	// TODO externalize and share with MC Provider class
	private String buildFieldName() {
		// TODO replace constant with this.resolveFilterIndexProperty() (see DefaultSolrAsCategoryPathResolver)
		String solrCategoryField = ALL_CATEGORIES_FIELD;
		final BaseStoreModel store = sessionService.getAttribute(BASE_STORE);
		if (store != null) {
			solrCategoryField += "_" + store.getUid() + "_string_mv";
		}
		return solrCategoryField;
	}
}
