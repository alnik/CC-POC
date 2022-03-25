/**
 * 
 */
package de.hybris.platform.multicountry.jalo;


import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.user.Customer;
import de.hybris.platform.multicountry.util.TimeZoneHelper;
import de.hybris.platform.util.Config;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;


/**
 * This class replaces the standard Customer Jalo class using spring (see multicountry-spring.xml). If we subclass
 * Customer for a project we should copy this code to the new Jalo class
 * 
 * @author brendan.dobbs
 * 
 */
public class MulticountryCustomer extends Customer
{

	private static final String DATE_GRANULARITY = "multicountry.date.granularity";
	private static final String DATE_TIMEZONE_SUPPORT = "multicountry.searchrestrictions.usetimezones";
	private static final Logger LOG = Logger.getLogger(MulticountryCustomer.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.jalo.user.User#getCurrentDate(de.hybris.platform.jalo.SessionContext)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Date getCurrentDate(final SessionContext ctx)
	{
		// TODO - cache date for a period of time
		if (Config.getBoolean(DATE_TIMEZONE_SUPPORT, true))
		{
			Date date = TimeZoneHelper.getCurrentTime();

			final String granularity = Config.getString(DATE_GRANULARITY, "HOURS");
			if ("DAY".equals(granularity))
			{
				date = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
			}
			else if ("HOURS".equals(granularity))
			{
				date = DateUtils.truncate(date, Calendar.HOUR);
			}
			else if ("MINUTES".equals(granularity))
			{
				date = DateUtils.truncate(date, Calendar.MINUTE);
			}
			else if ("SECONDS".equals(granularity))
			{
				date = DateUtils.truncate(date, Calendar.SECOND);
			}
			else
			{
				LOG.warn("Config parameter multicountry.date.granularity not properly set. Value is " + granularity
						+ " use HOURS has default");
				date = DateUtils.truncate(date, Calendar.HOUR);
			}
			if (LOG.isDebugEnabled())
			{
				LOG.debug("date with truncation [" + date + "]");
			}

			return date;
		}
		return super.getCurrentDate(ctx);
	}
}
