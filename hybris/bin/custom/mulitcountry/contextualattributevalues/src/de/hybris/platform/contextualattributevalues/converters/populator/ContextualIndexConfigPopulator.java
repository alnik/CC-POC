package de.hybris.platform.contextualattributevalues.converters.populator;

import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.converters.populator.DefaultIndexConfigPopulator;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;

import java.util.Collections;


/**
 * @author i314082
 *
 */
public class ContextualIndexConfigPopulator extends DefaultIndexConfigPopulator
{
	@Override
	public void populate(final SolrFacetSearchConfigModel source, final IndexConfig target) throws ConversionException
	{
		super.populate(source, target);
		target.setContexts(Collections.unmodifiableCollection(source.getContexts()));
	}
}
