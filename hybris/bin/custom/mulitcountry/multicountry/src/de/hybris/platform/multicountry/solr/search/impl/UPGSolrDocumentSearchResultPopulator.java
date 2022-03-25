/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2012 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *
 */
package de.hybris.platform.multicountry.solr.search.impl;

import de.hybris.platform.commerceservices.search.solrfacetsearch.populators.SolrDocumentSearchResultValuePopulator;
import de.hybris.platform.europe1.constants.Europe1Constants;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import org.springframework.beans.factory.annotation.Required;


/**
 * Extends DefaultSolrDocumentSearchResultConverter, in order to retrieve the right price for user price group assign to
 * the current session.
 */
public class UPGSolrDocumentSearchResultPopulator extends SolrDocumentSearchResultValuePopulator
{

	private SessionService sessionService;



	/**
	 * Returns the field name to search in Solr. It behaves like the parent's method, except for price fields that must
	 * be aware of the current session's user price group.
	 */
	@Override
	protected String translateFieldName(final SearchQuery searchQuery, final IndexedProperty property)
	{

		final String fieldName;

		if (property.isCurrency())
		{
			//It's a currency field -> let's add the session's UPG.
			final Object upg = this.sessionService.getCurrentSession().getAttribute(Europe1Constants.PARAMS.UPG);
			final String currencyISO = searchQuery.getCurrency();
			final String qualifier = upg == null ? currencyISO : upg + "_" + currencyISO;


			fieldName = this.getFieldNameProvider().getFieldName(property, qualifier, FieldNameProvider.FieldType.INDEX);
		}
		else
		{
			//Any other type, call super.
			fieldName = super.translateFieldName(searchQuery, property);
		}


		return fieldName;


	}

	/**
	 * @param sessionService
	 *           the sessionService to set
	 */
	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}
}