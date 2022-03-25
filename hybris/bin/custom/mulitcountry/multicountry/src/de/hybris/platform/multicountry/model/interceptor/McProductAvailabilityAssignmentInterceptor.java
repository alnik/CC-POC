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

import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.multicountry.util.CountryManagerUtil;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;



/**
 *
 */
public class McProductAvailabilityAssignmentInterceptor
		implements PrepareInterceptor<ProductAvailabilityAssignmentModel>, RemoveInterceptor<ProductAvailabilityAssignmentModel>
{



	@Override
	public void onPrepare(final ProductAvailabilityAssignmentModel model, final InterceptorContext ctx) throws InterceptorException
	{
		if (!CountryManagerUtil.canEditAvailabilityProduct(model)){
			throw new InterceptorException(CountryManagerUtil.getErrorMessage(model));
		}



	}





	@Override
	public void onRemove(final ProductAvailabilityAssignmentModel model, final InterceptorContext ctx) throws InterceptorException
	{
		if (!CountryManagerUtil.canEditAvailabilityProduct(model))
		{
			throw new InterceptorException(CountryManagerUtil.getErrorMessage(model.getCatalogVersion()));
		}

	}




}
