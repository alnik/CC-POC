/**
 *
 */
package de.hybris.platform.multicountry.promotions.mappers;

import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapper;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperException;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;

import org.springframework.beans.factory.annotation.Required;


/**
 * @author i307088
 *
 */
public class BaseStoreRuleParameterValueMapper implements RuleParameterValueMapper<BaseStoreModel>
{

	private BaseStoreService baseStoreService;

	@Override
	public String toString(final BaseStoreModel baseStore) throws RuleParameterValueMapperException
	{
		ServicesUtil.validateParameterNotNull(baseStore, "Object cannot be null");
		return baseStore.getUid();
	}

	@Override
	public BaseStoreModel fromString(final String value) throws RuleParameterValueMapperException
	{
		ServicesUtil.validateParameterNotNull(value, "String value cannot be null");
		try
		{
			return getBaseStoreService().getBaseStoreForUid(value);
		}
		catch (AmbiguousIdentifierException | UnknownIdentifierException e)
		{
			throw new RuleParameterValueMapperException("Cannot find BaseStore with the code: " + value);
		}
	}

	@Required
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	protected BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}
}
