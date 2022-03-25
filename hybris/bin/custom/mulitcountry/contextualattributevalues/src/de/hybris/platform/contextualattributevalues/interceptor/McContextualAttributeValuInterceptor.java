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
package de.hybris.platform.contextualattributevalues.interceptor;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributeValueModel;
import de.hybris.platform.contextualattributevalues.util.ContextualAttributeValuesUtil;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;

/**
 *
 */
public class McContextualAttributeValuInterceptor implements PrepareInterceptor<ContextualAttributeValueModel>, RemoveInterceptor<ContextualAttributeValueModel>
{
	@Override
	public void onPrepare( final ContextualAttributeValueModel model, final InterceptorContext ctx ) throws InterceptorException
	{
		if( model.getItemModelContext().isDirty() )
		{
			doInternal( model, ctx );
		}
	}

	@Override
	public void onRemove( final ContextualAttributeValueModel model, final InterceptorContext ctx ) throws InterceptorException
	{
		doInternal( model, ctx );
	}

	private void doInternal( final ContextualAttributeValueModel model, final InterceptorContext ctx ) throws InterceptorException
	{
		if( !ContextualAttributeValuesUtil.canEditContextualAttributeValue( model ) )
		{
			throw new InterceptorException( ContextualAttributeValuesUtil.getErrorMessage( model ) );
		}
	}
}
