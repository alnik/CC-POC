/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2017 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package de.hybris.platform.multicountry.model.interceptor;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.multicountry.util.CountryManagerUtil;
import de.hybris.platform.product.daos.ProductDao;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;



/**
 *
 */
public class McPriceRowInterceptor implements PrepareInterceptor<PriceRowModel>, RemoveInterceptor<PriceRowModel>
{

	private ProductDao productDao;

	@Override
	public void onPrepare(final PriceRowModel model, final InterceptorContext ctx) throws InterceptorException
	{
		model.getUg();
		doInternal(model, ctx);
	}



	@Override
	public void onRemove(final PriceRowModel model, final InterceptorContext ctx) throws InterceptorException {
		doInternal(model, ctx);
	}


	private void doInternal(final PriceRowModel model, final InterceptorContext ctx) throws InterceptorException
	{
		if (!CountryManagerUtil.canEditAPriceRow(model))
		{
			throw new InterceptorException(CountryManagerUtil.getErrorMessage(model));
		}


		if (model.getProduct() != null)
		{
			checkProduct(model.getProduct());
			return;
		}

		if (StringUtils.isEmpty(model.getProductId()))
		{
			return;
		}

		final List<ProductModel> products = productDao.findProductsByCode(model.getProductId());
		if (CollectionUtils.isEmpty(products))
		{
			return;
		}

		for (final ProductModel product : products)
		{
			checkProduct(product);
		}

	}

	private void checkProduct(final ProductModel model) throws InterceptorException
	{
		if (!CountryManagerUtil.canEditProduct(model))
		{
			throw new InterceptorException(CountryManagerUtil.getErrorMessage(model.getCatalogVersion()));
		}

	}



	public void setProductDao(final ProductDao productDao)
	{
		this.productDao = productDao;
	}


}
