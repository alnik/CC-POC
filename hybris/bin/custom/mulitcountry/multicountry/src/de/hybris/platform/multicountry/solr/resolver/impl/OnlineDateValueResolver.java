package de.hybris.platform.multicountry.solr.resolver.impl;

import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.multicountry.util.TimeZoneHelper;

import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;


/**
 * Gets the online date for product in a multicountry assignment.
 *
 * For each availability group, the online date is the latest date among all the variant and base product online dates.
 *
 */
public class OnlineDateValueResolver extends AbstractProductAvailabilityValueResolver<Date>
{

	protected static final Logger LOG = Logger.getLogger(OnlineDateValueResolver.class);

	@Override
	protected Date internalGetValue(final Collection<ProductAvailabilityAssignmentModel> availabilityAssignments)
	{
		Date date = null;
		for (final ProductAvailabilityAssignmentModel assignment : availabilityAssignments)
		{
			if (date == null || date.before(assignment.getOnlineDate()))
			{
				date = assignment.getOnlineDate();
			}
		}
		return TimeZoneHelper.adjustDateForTimeZone(date);
	}
}
