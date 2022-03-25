package de.hybris.platform.contextualattributevalues.provider.impl;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributeValueModel;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.VariantsService;
import de.hybris.platform.servicelayer.exceptions.AttributeNotSupportedException;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;
import de.hybris.platform.variants.model.VariantAttributeDescriptorModel;
import de.hybris.platform.variants.model.VariantProductModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author i304605
 *
 */
public class DefaultLegacyValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider, Serializable
{
	private ContextualValueFieldNameProvider fieldNameProvider;
	private VariantsService variantsService;

	final Logger LOG = LoggerFactory.getLogger(DefaultLegacyValueProvider.class);

	@SuppressWarnings("deprecation")
	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{
		if (model == null)
		{
			throw new IllegalArgumentException("No model given");
		}

		final List<FieldValue> fieldValues = new ArrayList<FieldValue>();
		List<String> rangeNameList = null;

		if (indexedProperty.isContextual() && !indexedProperty.isLocalized())
		{
			for (final ContextualAttributesContextModel localContex : indexConfig.getContexts())
			{
				// read all the contexts to be indexed


				final Object value = getValueWhenContextual(indexedProperty, model, localContex);


				rangeNameList = getRangeNameList(indexedProperty, value);
				final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, null, localContex.getCode());
				addFieldValues(fieldValues, rangeNameList, value, fieldNames);

			}


		}
		else if (indexedProperty.isLocalized() && indexedProperty.isContextual())
		{
			for (final ContextualAttributesContextModel localContex : indexConfig.getContexts())
			{

				final Collection<LanguageModel> languages = indexConfig.getLanguages();
				for (final LanguageModel language : languages)
				{
					Object value = null;
					final Locale locale = i18nService.getCurrentLocale();
					try
					{
						i18nService.setCurrentLocale(localeService.getLocaleByString(language.getIsocode()));
						value = getValueWhenContextual(indexedProperty, model, localContex);
						rangeNameList = getRangeNameList(indexedProperty, value);
					}
					finally
					{
						i18nService.setCurrentLocale(locale);
					}
					final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, language.getIsocode(),
							localContex.getCode());
					addFieldValues(fieldValues, rangeNameList, value, fieldNames);
				}
			}
		}
		else if (indexedProperty.isLocalized() && !indexedProperty.isContextual())
		{
			final Collection<LanguageModel> languages = indexConfig.getLanguages();
			for (final LanguageModel language : languages)
			{
				Object value = null;
				final Locale locale = i18nService.getCurrentLocale();
				try
				{
					i18nService.setCurrentLocale(localeService.getLocaleByString(language.getIsocode()));
					value = getPropertyValue(model, indexedProperty);
					rangeNameList = getRangeNameList(indexedProperty, value);
				}
				finally
				{
					i18nService.setCurrentLocale(locale);
				}
				final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, language.getIsocode());
				addFieldValues(fieldValues, rangeNameList, value, fieldNames);
			}
		}
		else
		{
			final Object value = getPropertyValue(model, indexedProperty);
			rangeNameList = getRangeNameList(indexedProperty, value);
			final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, null);
			addFieldValues(fieldValues, rangeNameList, value, fieldNames);
		}
		return fieldValues;
	}

	private void addFieldValues(final List<FieldValue> fieldValues, final List<String> rangeNameList, final Object value, final Collection<String> fieldNames) {
		for (final String fieldName : fieldNames)
		{
			if (rangeNameList.isEmpty())
			{
				fieldValues.add(new FieldValue(fieldName, value));

			}
			else
			{
				for (final String rangeName : rangeNameList)
				{
					fieldValues.add(new FieldValue(fieldName, rangeName == null ? value : rangeName));
				}
			}
		}
	}

	/**
	 * @param indexedProperty
	 * @param model
	 * @param localContex
	 * @param value
	 * @return
	 */
	private Object getValueWhenContextual(final IndexedProperty indexedProperty, final Object model,
			final ContextualAttributesContextModel localContex)
	{
		Object value = null;
		if (model instanceof ProductModel)
		{
			final ProductModel product = (ProductModel) model;

			for (final ContextualAttributeValueModel contextualAttribute : product.getContextualAttributeValues())
			{

				if (contextualAttribute.getContext().getCode().equals(localContex.getCode()))
				{
					value = getPropertyValue(contextualAttribute, indexedProperty);
				}

			}

			if (value == null)
			{
				value = getPropertyValue(model, indexedProperty);
			}

		}
		else
		{
			value = getPropertyValue(model, indexedProperty);
		}
		return value;
	}

	protected Object getPropertyValue(final Object model, final IndexedProperty indexedProperty)
	{
		String qualifier = indexedProperty.getValueProviderParameter();

		if (qualifier == null || qualifier.trim().isEmpty())
		{
			qualifier = indexedProperty.getName();
		}

		Object result = null;
		try
		{
			result = modelService.getAttributeValue(model, qualifier);
			if ((result == null) && (model instanceof VariantProductModel))
			{
				final ProductModel baseProduct = ((VariantProductModel) model).getBaseProduct();
				result = modelService.getAttributeValue(baseProduct, qualifier);
			}
		}
		catch (final AttributeNotSupportedException ae)
		{
			if (model instanceof VariantProductModel)
			{
				final ProductModel baseProduct = ((VariantProductModel) model).getBaseProduct();
				for (final VariantAttributeDescriptorModel att : baseProduct.getVariantType().getVariantAttributes())
				{
					if (qualifier.equals(att.getQualifier()))
					{
						result = this.variantsService.getVariantAttributeValue((VariantProductModel) model, qualifier);
						break;
					}
				}
			}
			else
			{
				LOG.error(ae.getMessage());
			}
		}
		return result;
	}


	/**
	 * @param fieldNameProvider
	 *           the fieldNameProvider to set
	 */
	@Required
	public void setFieldNameProvider(final ContextualValueFieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}

	@Required
	public void setVariantsService(final VariantsService variantsService)
	{
		this.variantsService = variantsService;
	}
}
