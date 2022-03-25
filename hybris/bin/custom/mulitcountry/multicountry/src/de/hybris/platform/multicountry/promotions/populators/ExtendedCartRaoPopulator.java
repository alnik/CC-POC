/**
 *
 */
package de.hybris.platform.multicountry.promotions.populators;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.multicountry.enums.TimezoneEnum;
import de.hybris.platform.multicountry.promotions.rao.BaseStoreRAO;
import de.hybris.platform.multicountry.util.TimeZoneHelper;
import de.hybris.platform.ruleengineservices.converters.populator.CartRaoPopulator;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.store.BaseStoreModel;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author i307088
 *
 */
public class ExtendedCartRaoPopulator extends CartRaoPopulator
{
	private Converter<BaseStoreModel, BaseStoreRAO> baseStoreConverter;
	protected final static Logger LOG = Logger.getLogger(ExtendedCartRaoPopulator.class);

	@Override
	public void populate(final AbstractOrderModel source, final CartRAO target) throws ConversionException
	{
		super.populate(source, target);
		target.setStore(baseStoreConverter.convert(source.getStore()));
		target.setLocalTime(getLocalTimeForBaseStore(source.getStore()));
	}

	@Required
	public void setBaseStoreConverter(final Converter<BaseStoreModel, BaseStoreRAO> baseStoreConverter)
	{
		this.baseStoreConverter = baseStoreConverter;
	}

	protected long getLocalTimeForBaseStore(final BaseStoreModel baseStore)
	{
		final TimezoneEnum timeZoneCode = baseStore.getTimezone();

		Date currentTimeInLocalTimeZone = null;
		if (timeZoneCode != null)
		{
			currentTimeInLocalTimeZone = TimeZoneHelper.getTimeInTimeZone(timeZoneCode.getCode());
		}
		else
		{
			currentTimeInLocalTimeZone = TimeZoneHelper.getCurrentTime();
			LOG.warn(
					"No TimeZone specifided for " + baseStore.getName() + " current server time is used for promotions calculations");
		}


		return currentTimeInLocalTimeZone.getTime();
	}
}
