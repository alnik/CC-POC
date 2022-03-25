package de.hybris.platform.multicountry.services.impl;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.multicountry.constants.MulticountryConstants;
import de.hybris.platform.multicountry.strategies.MulticountryCategoryCatalogSelector;
import de.hybris.platform.product.impl.DefaultProductService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Created by i844957 on 5/6/16.
 */
public class DefaultMulticountryProductService extends DefaultProductService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultMulticountryProductService.class);

	private BaseStoreService baseStoreService;
	private MulticountryCategoryCatalogSelector categoryCatalogSelector;

	@Override
	public ProductModel getProductForCode(final String code)
	{
		//get all products from all catalogs with this code
		final List<ProductModel> products = getProductDao().findProductsByCode(code);
		final CatalogVersionModel catalogVersion = getCategoryCatalogVersion();
		ProductModel selectedProduct = null;
		//more than one found? prefer the selected category catalog version
		if (products.size() > 1)
		{
			final Optional<ProductModel> catalogProduct = products.stream()
					.filter(product -> product.getCatalogVersion().equals(catalogVersion)).findFirst();
			if (catalogProduct.isPresent())
			{
				selectedProduct = catalogProduct.get();
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Selecting product {} from category catalog {}:{}", selectedProduct.getCode(),
							catalogVersion.getCatalog().getName(), catalogVersion.getVersion());
				}
			}
		}
		else if (products.size() > 0)
		{
			selectedProduct = products.get(0);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Multiple products with {} found but none are in the active category catalog {}:{}",
						selectedProduct.getCode(), catalogVersion.getCatalog().getName(), catalogVersion.getVersion());
			}
		}

		if (selectedProduct == null)
		{
			throw new UnknownIdentifierException("No Product with code [" + code + "] found.");
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Returning product {} from {}:{}, current category catalog is {}:{}", selectedProduct.getCode(),
					selectedProduct.getCatalogVersion().getCatalog().getName(), selectedProduct.getCatalogVersion().getVersion(),
					catalogVersion.getCatalog().getName(), catalogVersion.getVersion());
		}

		return selectedProduct;
	}

	/**
	 * Attempts to select a category catalog from the session, or from the default configuration of the site.
	 *
	 * @return A catalog version model, may be null
	 */
	protected CatalogVersionModel getCategoryCatalogVersion()
	{
		//get the category for code from the session-based category catalog
		final CatalogModel currentCatalog = getSessionService().getAttribute(MulticountryConstants.ACTIVE_CATEGORY_CATALOG);
		if (currentCatalog != null)
		{
			return currentCatalog.getActiveCatalogVersion();
		}
		else
		{
			final BaseStoreModel baseStore = getBaseStoreService().getCurrentBaseStore();
			if (baseStore == null)
			{
				return null;
			}
			else
			{
				final CatalogModel catalog = getCategoryCatalogSelector().select(baseStore);
				return catalog == null ? null : catalog.getActiveCatalogVersion();
			}
		}
	}

	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	@Required
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	public MulticountryCategoryCatalogSelector getCategoryCatalogSelector()
	{
		return categoryCatalogSelector;
	}

	@Required
	public void setCategoryCatalogSelector(final MulticountryCategoryCatalogSelector categoryCatalogSelector)
	{
		this.categoryCatalogSelector = categoryCatalogSelector;
	}
}
