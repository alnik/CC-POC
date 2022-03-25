/**
 *
 */
package de.hybris.platform.contextualattributevalues.provider.impl;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;
import de.hybris.platform.contextualattributevalues.services.ContextualAttributeValuesSessionService;
import de.hybris.platform.multicountry.solr.provider.impl.MulticountryFieldNameProvider;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;

import javax.annotation.Resource;

import org.apache.solr.common.StringUtils;


/**
 * @author i304605
 *
 */
public class ContextualValueFieldNameProvider extends MulticountryFieldNameProvider
{
	@Resource
	ContextualAttributeValuesSessionService contextualAttributeValuesSessionService;

	@Override
	public String getFieldName(final IndexedProperty indexedProperty, final String qualifier, final FieldType fieldType)
	{
		String fieldQualifier = qualifier;
		if (indexedProperty.isCurrency())
		{
			fieldQualifier = qualifier;
		}

		else if (indexedProperty.isCategoryCatalogDependent())
		{
			fieldQualifier = getQualifierForCategoryDependentFields();
		}
		else if (indexedProperty.isContextual())
		{

			final ContextualAttributesContextModel currentContext = contextualAttributeValuesSessionService.getCurrentContext();
			//final String currentContextCode = currentContext != null ? currentContext.getCode() : "";
			if (currentContext != null)
			{
				if (StringUtils.isEmpty(qualifier))
				{
					fieldQualifier = currentContext.getCode();
				}
				else
				{
					fieldQualifier = currentContext.getCode() + "_" + qualifier;
				}
			}
		}
		return super.getFieldName(indexedProperty, fieldQualifier, fieldType);
	}
}
