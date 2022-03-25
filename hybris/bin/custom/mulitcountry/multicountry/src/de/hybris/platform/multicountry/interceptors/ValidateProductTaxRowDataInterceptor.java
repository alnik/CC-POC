/**
 *
 */
package de.hybris.platform.multicountry.interceptors;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.europe1.model.TaxRowModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.servicelayer.session.SessionService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Ensures that a tax row is not assigned to a product multiple times.
 */
public class ValidateProductTaxRowDataInterceptor implements ValidateInterceptor<ProductModel>
{
	private static Logger LOG = Logger.getLogger(ValidateProductTaxRowDataInterceptor.class);

	private SessionService sessionService;

	@Override
	public void onValidate(final ProductModel product, final InterceptorContext ctx) throws InterceptorException
	{
		if (isCatalogSyncRunning())
		{
			return;
		}

		if (CollectionUtils.isEmpty(product.getEurope1Taxes()))
		{
			return;
		}

		try
		{
			for (final TaxRowModel taxRow : product.getEurope1Taxes())
			{
				for (final TaxRowModel taxRow1 : product.getEurope1Taxes())
				{
					if (taxRow != taxRow1 && taxRow.getUg() != null && taxRow.getCatalogVersion() != null
							&& taxRow.getUg().equals(taxRow1.getUg()) && taxRow.getCatalogVersion().equals(taxRow1.getCatalogVersion()))
					{
						throw new InterceptorException(
								"Multiple tax rows are specified for the same store for product :" + product.getPk());
					}
				}
			}
		}
		catch (final Exception e)
		{
			LOG.error(e.getMessage(), e);
		}
	}

	protected boolean isCatalogSyncRunning()
	{
		final Boolean running = sessionService.getAttribute("catalog.sync.active");
		return running != null && running.booleanValue();
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
