/**
 *
 */
package de.hybris.platform.multicountry.restrictions.evaluator.impl;

import de.hybris.platform.cms2.model.restrictions.CMSBaseStoreTimeRestrictionModel;
import de.hybris.platform.cms2.servicelayer.data.RestrictionData;
import de.hybris.platform.cms2.servicelayer.services.evaluator.CMSRestrictionEvaluator;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author brendan.dobbs
 *
 */
public class MulticountryCMSBaseStoreTimeRestrictionEvaluator implements
		CMSRestrictionEvaluator<CMSBaseStoreTimeRestrictionModel>
{
	private MulticountryCMSTimeRestrictionEvaluator cmsTimeRestrictionEvaluator;
	private BaseStoreService baseStoreService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final CMSBaseStoreTimeRestrictionModel cmsStoreTimeRestriction, final RestrictionData context)
	{
		final Collection<BaseStoreModel> baseStores = cmsStoreTimeRestriction.getBaseStores();

		// store doesn't match
		if (CollectionUtils.isEmpty(baseStores) || !baseStores.contains(getBaseStoreService().getCurrentBaseStore()))
		{
			return cmsStoreTimeRestriction.getPassIfStoreDoesntMatch();
		}
		else
		// store matches run the time restriction evaluator
		{
			return getCmsTimeRestrictionEvaluator().evaluate(cmsStoreTimeRestriction, context);
		}
	}

	public MulticountryCMSTimeRestrictionEvaluator getCmsTimeRestrictionEvaluator()
	{
		return cmsTimeRestrictionEvaluator;
	}

	@Required
	public void setCmsTimeRestrictionEvaluator(final MulticountryCMSTimeRestrictionEvaluator cmsTimeRestrictionEvaluator)
	{
		this.cmsTimeRestrictionEvaluator = cmsTimeRestrictionEvaluator;
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
}
