package de.hybris.platform.contextualattributevalues.converters.populator;

import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.converters.populator.DefaultIndexedPropertyPopulator;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;


/**
 * @author i314082
 *
 */
public class ContextualSolrIndexedPopulator extends DefaultIndexedPropertyPopulator
{
	@Override
	public void populate(final SolrIndexedPropertyModel source, final IndexedProperty target) throws ConversionException
	{
		super.populate(source, target);
		target.setContextual(source.isContextual());
	}
}
