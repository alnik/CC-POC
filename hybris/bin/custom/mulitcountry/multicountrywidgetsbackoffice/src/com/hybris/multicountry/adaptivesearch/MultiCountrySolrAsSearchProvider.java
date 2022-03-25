/**
 *
 */
package com.hybris.multicountry.adaptivesearch;

import de.hybris.platform.adaptivesearch.AsException;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsSearchQueryData;
import de.hybris.platform.adaptivesearchsolr.strategies.impl.SolrAsSearchProvider;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.multicountry.services.MulticountryRestrictionService;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.search.FacetValueField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.store.BaseStoreModel;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author i304602
 *
 */
public class MultiCountrySolrAsSearchProvider extends SolrAsSearchProvider
{
	private final static Logger LOGGER = LoggerFactory.getLogger(MultiCountrySolrAsSearchProvider.class);

	private static final String AVAILABILITY = "availability";

	public static final String BASE_STORE = "baseStore";

	public static final String ALL_CATEGORIES_FIELD = "allCategories";

	private MulticountryRestrictionService multicountryRestrictionService;

	@Override
	protected SearchQuery convertSearchQuery(final AsSearchProfileContext context, final AsSearchQueryData searchQuery)
			throws FacetConfigServiceException, AsException
	{
		if (CollectionUtils.isNotEmpty(context.getCategoryPath())) {
			addCategory(context, searchQuery);

			/*
			We need to clear the category path here otherwise the convert below will
			try to filter on the category field without base store context.
			 */
			context.getCategoryPath().clear();
		}

		final SearchQuery query = super.convertSearchQuery(context, searchQuery);

		addAvailabilityField(query);
		return query;
	}

	private void addCategory(final AsSearchProfileContext context, final AsSearchQueryData searchQuery)
	{
		if (CollectionUtils.isEmpty(context.getCategoryPath()))
		{
			return;
		}

		final Set<String> values = new HashSet<String>();
		final CategoryModel category = context.getCategoryPath().get(context.getCategoryPath().size() - 1);
		values.add(category.getCode());

		if (searchQuery.getFacetValues() == null)
		{
			searchQuery.setFacetValues(new HashMap<>());
		}

		String solrCategoryField = ALL_CATEGORIES_FIELD;
		final BaseStoreModel store = getSessionService().getAttribute(BASE_STORE);
		if (store != null) {
			solrCategoryField += "_" + store.getUid() + "_string_mv";
		}

		searchQuery.getFacetValues().put(solrCategoryField, values);
	}

	private void addAvailabilityField(final SearchQuery query)
	{
		LOGGER.debug("Adding availability to search query if available...");

		final Collection<ProductAvailabilityGroupModel> availabilities = multicountryRestrictionService
				.getCurrentProductAvailabilityGroup();

		if (CollectionUtils.isNotEmpty(availabilities))
		{
			Set<String> availabilityValues = availabilities.stream().map(av -> av.getId()).collect(Collectors.toSet());

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Found availability: " + availabilityValues.stream().collect(Collectors.joining(", ")));
			}

			if (!availabilityValues.isEmpty()) {
				final FacetValueField facetValueField = new FacetValueField(AVAILABILITY, availabilityValues);
				query.getFacetValues().add(facetValueField);
			}
		}

	}

	public void setMulticountryRestrictionService(final MulticountryRestrictionService multicountryRestrictionService)
	{
		this.multicountryRestrictionService = multicountryRestrictionService;
	}
}
