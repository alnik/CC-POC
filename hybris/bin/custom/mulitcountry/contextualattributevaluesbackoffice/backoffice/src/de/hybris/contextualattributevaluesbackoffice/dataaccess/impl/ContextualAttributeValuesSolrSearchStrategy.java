package de.hybris.contextualattributevaluesbackoffice.dataaccess.impl;

import de.hybris.contextualattributevaluesbackoffice.dataaccess.ContextualFulltextSearchStrategy;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;

import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchStrategy;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualAttributeValuesSolrSearchStrategy extends SolrSearchStrategy implements ContextualFulltextSearchStrategy
{
	@Override
	public boolean isContextual( String typeCode, String fieldName )
	{
		IndexedProperty indexedProperty = getIndexedProperty( typeCode, fieldName );
		return indexedProperty != null && indexedProperty.isContextual();
	}
}
