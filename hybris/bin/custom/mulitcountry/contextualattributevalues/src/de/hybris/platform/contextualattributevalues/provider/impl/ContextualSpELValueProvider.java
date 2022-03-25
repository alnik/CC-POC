/**
 *
 */
package de.hybris.platform.contextualattributevalues.provider.impl;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributeValueModel;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.impl.SpELValueProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;


/**
 * @author i304605
 *
 */
public class ContextualSpELValueProvider extends SpELValueProvider
{
	private ExpressionParser parser;
	private ApplicationContext applicationContext;
	private ContextualValueFieldNameProvider fieldNameProvider;
	private CommonI18NService commonI18NService;
	private I18NService i18NService;
	private ModelService modelService;
	private TypeService typeService;

	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{
		final String exprValue = getSpringExpression(indexedProperty);

		final Expression parsedExpression = parser.parseExpression(exprValue);
		final StandardEvaluationContext context = new StandardEvaluationContext(model);
		context.setBeanResolver(new BeanFactoryResolver(applicationContext));
		context.setVariable("item", model);

		List<FieldValue> resolvedFieldValues = new ArrayList<FieldValue>();

		if (indexedProperty.isContextual() && !indexedProperty.isLocalized())
		{
			for (final ContextualAttributesContextModel localContex : indexConfig.getContexts())
			{
				final Object value = getValueWhenContextual(indexedProperty, model, localContex, parsedExpression, context);
				resolvedFieldValues.addAll(resolve(indexedProperty, value, null, localContex.getCode()));
			}
		}
		else if (indexedProperty.isLocalized() && indexedProperty.isContextual())
		{
			for (final ContextualAttributesContextModel localContex : indexConfig.getContexts())
			{
				for (final LanguageModel language : indexConfig.getLanguages())
				{
					final Locale locale = commonI18NService.getLocaleForLanguage(language);
					context.setVariable("lang", locale);
					i18NService.setCurrentLocale(locale);
					final Object value = getValueWhenContextual(indexedProperty, model, localContex, parsedExpression, context);
					resolvedFieldValues.addAll(resolve(indexedProperty, value, language.getIsocode(), localContex.getCode()));
				}
			}
		}
		else if (indexedProperty.isLocalized()) {
			for (final LanguageModel language : indexConfig.getLanguages())
			{
				final Locale locale = commonI18NService.getLocaleForLanguage(language);
				context.setVariable("lang", locale);
				i18NService.setCurrentLocale(locale);
				final Object value = parsedExpression.getValue(context);
				resolvedFieldValues.addAll(resolve(indexedProperty, value, language.getIsocode()));

			}
		}
		else if (indexedProperty.isCurrency())
		{
			for (final CurrencyModel currency : indexConfig.getCurrencies())
			{
				final CurrencyModel sessionCurrency = commonI18NService.getCurrentCurrency();
				try
				{
					commonI18NService.setCurrentCurrency(currency);
					context.setVariable("currency", currency);
					final Object value = parsedExpression.getValue(context);
					resolvedFieldValues.addAll(resolve(indexedProperty, value, currency.getIsocode()));
				}
				finally
				{
					commonI18NService.setCurrentCurrency(sessionCurrency);
				}
			}
		}
		else
		{
			final Object value = parsedExpression.getValue(context);
			resolvedFieldValues.addAll(resolve(indexedProperty, value, null, null));
		}

		return resolvedFieldValues;
	}


	protected Collection resolve(final IndexedProperty indexedProperty, final Object value, final String qualifier,
			final String context)
	{
		final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, qualifier, context);

		if (value instanceof Collection)
		{
			return resolveValuesForCollection((Collection) value, fieldNames);

		}
		else if (value == null)
		{
			return Collections.EMPTY_LIST;
		}
		else
		{
			return getFieldValuesForFieldNames(fieldNames, value);
		}
	}

	private Object getValueWhenContextual(final IndexedProperty indexedProperty, final Object model,
			final ContextualAttributesContextModel localContex, final Expression parsedExpression,
			final StandardEvaluationContext context)
	{
		Object value = null;
		if (model instanceof ProductModel)
		{
			final ProductModel product = (ProductModel) model;
			final ComposedTypeModel ct = getTypeService().getComposedTypeForClass(ContextualAttributeValueModel.class);

			for (final ContextualAttributeValueModel contextualAttribute : product.getContextualAttributeValues())
			{

				if (contextualAttribute.getContext().getCode().equals(localContex.getCode()))
				{
					try
					{
						context.setVariable("item", contextualAttribute);
						context.setVariable("originalitem", model);
						value = getPropertyValue(contextualAttribute, indexedProperty, ct);
					}
					catch (final Exception e)
					{
						// silent
					}
				}

			}

			if (value == null)
			{
				context.setVariable("originalitem", model);
				context.setVariable("item", model);
				value = parsedExpression.getValue(context);
			}

		}
		else
		{
			value = parsedExpression.getValue(context);
		}
		return value;
	}

	@Override
	protected Collection resolve(IndexedProperty indexedProperty, Object value, String qualifier) {
		final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, qualifier);

		if (value instanceof Collection)
		{
			return resolveValuesForCollection((Collection) value, fieldNames);

		}
		else if (value == null)
		{
			return Collections.emptyList();
		}
		else
		{
			return getFieldValuesForFieldNames(fieldNames, value);
		}
	}

	protected Object getPropertyValue(final Object contextualModel, final IndexedProperty indexedProperty,
									  final ComposedTypeModel ct)
	{

		final String qualifier = indexedProperty.getName();


		if (getTypeService().hasAttribute(ct, qualifier))
		{
			return modelService.getAttributeValue(contextualModel, qualifier);
		}
		else
		{

			return null;
		}

	}


	@Required
	public void setFieldNameProvider(final ContextualValueFieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException
	{
		this.applicationContext = applicationContext;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Override
	@Required
	public void setParser(final ExpressionParser parser)
	{
		this.parser = parser;
	}

	@Override
	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	@Required
	public void setI18NService(final I18NService i18NService)
	{
		this.i18NService = i18NService;
	}

	/**
	 * @return the typeService
	 */
	public TypeService getTypeService()
	{
		return typeService;
	}

	/**
	 * @param typeService
	 *           the typeService to set
	 */
	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}
}
