package de.hybris.platform.multicountry.solr.provider.impl;

import static java.util.Locale.ROOT;

import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.provider.impl.DefaultFieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.SolrFieldNameConstants;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.util.Config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.solr.common.StringUtils;


public class MulticountryFieldNameProvider extends DefaultFieldNameProvider
{
	private BaseStoreService baseStoreService;
	private CMSSiteService cmsSiteService;


	private static final String USED_SEPARATOR = Config.getString("solr.indexedproperty.forbidden.char",
			SolrFieldNameConstants.DEFAULT_SEPARATOR);


	public Collection<String> getFieldNames(final IndexedProperty indexedProperty, final String qualifier, final String context)
	{
		final Set<String> fields = new HashSet<String>(FieldType.values().length);

		if (!StringUtils.isEmpty(qualifier) && !StringUtils.isEmpty(context))
		{
			fields.add(getFieldNameForIndexing(indexedProperty, context + "_" + qualifier));
			fields.add(getFieldNameForSorting(indexedProperty, context + "_" + qualifier));
		}
		else if (StringUtils.isEmpty(qualifier) && !StringUtils.isEmpty(context))
		{
			fields.add(getFieldNameForIndexing(indexedProperty, context));
			fields.add(getFieldNameForSorting(indexedProperty, context));
		}
		else
		{
			fields.add(getFieldNameForIndexing(indexedProperty, qualifier));
			fields.add(getFieldNameForSorting(indexedProperty, qualifier));
		}


		if (indexedProperty.isAutoSuggest())
		{
			if (qualifier != null)
			{
				fields.add(SolrFieldNameConstants.AUTOSUGGEST_FIELD + SolrFieldNameConstants.DEFAULT_SEPARATOR
						+ qualifier.toLowerCase(ROOT));
			}
			else
			{
				fields.add(SolrFieldNameConstants.AUTOSUGGEST_FIELD);
			}
		}
		if (indexedProperty.isSpellCheck())
		{
			if (qualifier != null)
			{
				fields.add(SolrFieldNameConstants.SPELLCHECK_FIELD + SolrFieldNameConstants.DEFAULT_SEPARATOR
						+ qualifier.toLowerCase(ROOT));
			}
			else
			{
				fields.add(SolrFieldNameConstants.SPELLCHECK_FIELD);
			}
		}

		return fields;
	}



	@Override
	public String getFieldName(final IndexedProperty indexedProperty, final String qualifier, final FieldType fieldType)
	{
		String fieldQualifier = qualifier;
		if (indexedProperty.isCurrency())
		{
			fieldQualifier = getQualifierForPriceValues(qualifier);
		}

		else if (indexedProperty.isCategoryCatalogDependent())
		{
			fieldQualifier = getQualifierForCategoryDependentFields();
		}

		return super.getFieldName(indexedProperty, fieldQualifier, fieldType);
	}


	@Override
	protected String getFieldName(final IndexedProperty indexedProperty, final String name, final String type,
			final String specifier)
	{
		String rangeType = type;
		final String separator = USED_SEPARATOR;
		if (isRanged(indexedProperty))
		{
			rangeType = SolrFieldNameConstants.RANGE_TYPE;
		}
		rangeType = rangeType.toLowerCase(ROOT);

		final StringBuilder fieldName = new StringBuilder();

		if (specifier == null)
		{
			fieldName.append(name).append(separator).append(rangeType);
		}
		else
		{
			if (rangeType.equals(SolrFieldNameConstants.TEXT_TYPE))
			{
				fieldName.append(name).append(separator).append(specifier.toLowerCase(ROOT)).append(separator)
						.append(SolrFieldNameConstants.TEXT_TYPE);
			}
			else
			{
				fieldName.append(name).append(separator).append(specifier.toLowerCase(ROOT)).append(separator).append(rangeType);
			}
		}

		if (indexedProperty.isMultiValue())
		{
			fieldName.append(separator).append(SolrFieldNameConstants.MV_TYPE);
		}

		return fieldName.toString();
	}

	protected String getQualifierForPriceValues(final String qualifier)
	{
		final BaseStoreModel baseStore = getBaseStoreService().getCurrentBaseStore();
		final String currencyISO = qualifier;
		HybrisEnumValue userPriceGroup = null;
		if (baseStore != null && baseStore.getUserPriceGroup() != null)
		{
			userPriceGroup = baseStore.getUserPriceGroup();
		}

		final StringBuilder sb = new StringBuilder();
		if (null != userPriceGroup)
		{
			sb.append(userPriceGroup.getCode()).append("_");
		}
		sb.append(currencyISO);

		return sb.toString();
	}

	protected String getQualifierForCategoryDependentFields()
	{
		final CMSSiteModel cmsSite = getCmsSiteService().getCurrentSite();
		// Qualifier for category catalog dependent fields is the cms site uid.
		String siteUID = null;
		if (cmsSite != null)
		{
			siteUID = cmsSite.getUid();
		}
		return siteUID;
	}

	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	public CMSSiteService getCmsSiteService()
	{
		return cmsSiteService;
	}

	public void setCmsSiteService(final CMSSiteService cmsSiteService)
	{
		this.cmsSiteService = cmsSiteService;
	}
}
