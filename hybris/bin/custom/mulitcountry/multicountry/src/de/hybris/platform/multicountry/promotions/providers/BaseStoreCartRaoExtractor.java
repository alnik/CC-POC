/**
 *
 */
package de.hybris.platform.multicountry.promotions.providers;

import static com.google.common.base.Preconditions.checkArgument;

import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.providers.RAOFactsExtractor;

import java.util.HashSet;
import java.util.Set;


/**
 * @author i307088
 *
 */
public class BaseStoreCartRaoExtractor implements RAOFactsExtractor
{
	public static final String EXPAND_BASESTORE = "EXPAND_BASESTORE";

	@Override
	public Set<?> expandFact(final Object fact)
	{
		checkArgument(fact instanceof CartRAO, "CartRAO type is expected here");
		final Set<Object> facts = new HashSet<>();
		final CartRAO cartRAO = (CartRAO) fact;
		facts.add(cartRAO.getStore());
		return facts;
	}

	@Override
	public String getTriggeringOption()
	{
		return EXPAND_BASESTORE;
	}

	@Override
	public boolean isMinOption()
	{
		return false;
	}

	@Override
	public boolean isDefault()
	{
		return true;
	}



}
