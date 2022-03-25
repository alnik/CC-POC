package de.hybris.platform.multicountry.solr.resolver.impl;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;
import de.hybris.platform.util.StandardDateRange;
import org.apache.log4j.Logger;

import java.util.*;

/**
 *  * Gets all the prices discriminated by user price group (upg) and currency. Since the PriceService filters the price
 * rows depending on the session user, this implementation gets directly the pricerows from the product itself. This is
 * more performant than asking the service one by one the price for each currency/upg combination (and this is usually
 * performed for two fields, one for the prices and one for the ranges). The drawback is that (a) prices should be
 * defined by product (this class will not index price rows defined, for example, only for a product group); and (b) no
 * gross/net calculation is performed: value is indexed as-is.
 *
 */
public class UPGProductPriceValueResolver extends AbstractValueResolver<ProductModel, Collection<PriceRowModel>, Object> {

    protected static final Logger LOG = Logger.getLogger(UPGProductPriceValueResolver.class);

    protected Collection<PriceRowModel> loadData(final IndexerBatchContext batchContext, final Collection<IndexedProperty> indexedProperties,
            final ProductModel model) throws FieldValueProviderException
    {
        final Collection<PriceRowModel> priceRowModels = model.getEurope1Prices();
        return priceRowModels == null ? Collections.emptySet() : priceRowModels;
    }

    @Override
    protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
            final IndexedProperty indexedProperty, final ProductModel model,
            final ValueResolverContext<Collection<PriceRowModel>, Object> resolverContext)
            throws FieldValueProviderException {

        final Collection<PriceRowModel> priceRowModels = resolverContext.getData();

        for (final PriceRowModel priceRowModel : priceRowModels)
        {

            if (validForIndex(batchContext.getFacetSearchConfig().getIndexConfig(), priceRowModel)) {

                final Double price = priceRowModel.getPrice();

                final String fieldName = this.fieldNameToIndex(priceRowModel);

                document.addField(indexedProperty, price, fieldName);

            }

        }

    }


    protected String fieldNameToIndex(final PriceRowModel priceRowModel)
    {
        //Get currency code & user price group
        final String currencyISO = priceRowModel.getCurrency().getIsocode();
        final HybrisEnumValue userPriceGroup = priceRowModel.getUg();

        //Concat both for the qualifier...
        return userPriceGroup == null ? currencyISO : userPriceGroup.getCode() + "_" + currencyISO;

    }


    /**
     * Determines if the PriceRow is valid for indexing. By now, it just checks the date range and the currency.
     *
     * @param indexConfig
     * @param priceRowModel
     * @return
     */
    protected boolean validForIndex(final IndexConfig indexConfig, final PriceRowModel priceRowModel)
    {
        if (!shouldCurrencyBeIndexed(indexConfig, priceRowModel))
        {
            return false;
        }
        return isPriceRowDateRangeValid(priceRowModel);

    }

    private boolean isPriceRowDateRangeValid(final PriceRowModel priceRowModel) {
        //Get the pricerow validity range
        final StandardDateRange dateRange = priceRowModel.getDateRange();
        if (dateRange == null)
        {
            //nothing to check
            return true;
        }
        final Date validFrom = dateRange.getStart();
        final Date validTo = dateRange.getEnd();
        final Date now = new Date();

        //and check that now the row is valid.
        final boolean valid = (validFrom == null || now.after(validFrom)) && (validTo == null || now.before(validTo));

        return valid;
    }

    private boolean shouldCurrencyBeIndexed(final IndexConfig indexConfig, final PriceRowModel priceRowModel) {
        //Check currency
        final Collection<CurrencyModel> indexedCurrencies = indexConfig.getCurrencies();
        final CurrencyModel priceRowCurrency = priceRowModel.getCurrency();
        if (indexedCurrencies != null && priceRowCurrency != null && !indexedCurrencies.contains(priceRowCurrency))
        {
            //This currency is not indexed
            return false;
        }
        return true;
    }

}
