/**
 *
 */
package de.hybris.platform.multicountry.as;

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.impl.DefaultAsSearchProfileActivationStrategy;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @author i304602
 *
 */
public class MultiCountryAsSearchProfileActivationStrategy extends DefaultAsSearchProfileActivationStrategy
{

	private BaseStoreService baseStoreService;



	@Override
	public List<AbstractAsSearchProfileModel> getActiveSearchProfiles(final AsSearchProfileContext context)
	{
		final List<AbstractAsSearchProfileModel> profiles = super.getActiveSearchProfiles(context);

		final BaseStoreModel store = baseStoreService.getCurrentBaseStore();
		if (store == null)
		{
			return profiles;
		}

		return profiles.stream().filter(profile -> hasStore(profile, store)).collect(Collectors.toList());
	}


	private boolean hasStore(final AbstractAsSearchProfileModel profile, final BaseStoreModel store)
	{
		if (profile.getBaseStore() == null)
		{
			return false;
		}

		return profile.getBaseStore().getPk().equals(store.getPk());
	}


	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

}
