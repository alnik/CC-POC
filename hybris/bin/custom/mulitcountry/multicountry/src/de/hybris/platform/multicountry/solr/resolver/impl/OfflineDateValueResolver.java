package de.hybris.platform.multicountry.solr.resolver.impl;

import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.multicountry.util.TimeZoneHelper;

import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;


/**
 * Gets the offline date for product in a multicountry assignment.
 *
 * For each availability group, the offline date is the earliest date among all the variant and base product offline
 * dates.
 *
 */
public class OfflineDateValueResolver extends AbstractProductAvailabilityValueResolver<Date>
{

	protected static final Logger LOG = Logger.getLogger(OfflineDateValueResolver.class);

	@Override
	protected Date internalGetValue(final Collection<ProductAvailabilityAssignmentModel> availabilityAssignments)
	{
		Date date = null;
		for (final ProductAvailabilityAssignmentModel assignment : availabilityAssignments)
		{
			if (date == null || (assignment.getOfflineDate() != null && date.after(assignment.getOfflineDate())))
			{
				date = assignment.getOfflineDate();
			}
		}
		return TimeZoneHelper.adjustDateForTimeZone(date);
	}
}
