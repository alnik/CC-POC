/**
 *
 */
package de.hybris.platform.multicountry.promotions.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.multicountry.promotions.rao.BaseStoreRAO;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.store.BaseStoreModel;


/**
 * @author i307088
 *
 */
public class BaseStoreRaoPopulator implements Populator<BaseStoreModel, BaseStoreRAO>
{

	@Override
	public void populate(final BaseStoreModel source, final BaseStoreRAO target) throws ConversionException
	{
		target.setCode(source.getUid());
		target.setTimeZone(source.getTimezone() != null ? source.getTimezone().getCode() : null);
	}
}
