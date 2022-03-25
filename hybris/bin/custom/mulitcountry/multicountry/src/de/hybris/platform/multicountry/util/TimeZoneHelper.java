/**
 * 
 */
package de.hybris.platform.multicountry.util;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.Utilities;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * @author brendan.dobbs
 * 
 */
public final class TimeZoneHelper
{
	private static final Logger LOG = Logger.getLogger(TimeZoneHelper.class);
    private static final String DATE_GRANULARITY = "multicountry.date.granularity";

    /**
     * Constructor.
     */
    private TimeZoneHelper()
    {
        //no-op
    }

    public static Date getCurrentTime()
    {
        final Object timeOffsetAttr = JaloSession.getCurrentSession().getAttribute(SessionContext.TIMEOFFSET);
        final long timeOffsetMillis = timeOffsetAttr != null ? ((Long) timeOffsetAttr) : 0;
        Date date = new Date(System.currentTimeMillis() + timeOffsetMillis);
        return DateUtils.truncate(date, Calendar.SECOND);
    }

    public static Date getTimeInTimeZone(final String timeZoneCode)
	{
		final TimeZone timeZone = TimeZone.getTimeZone(timeZoneCode);
		// get date in system time zone
		final Calendar cal = Calendar.getInstance();
		final int systemTimeOffset = cal.getTimeZone().getOffset(cal.getTime().getTime());
		// set offset, pass in date to ensure we get day light savings time of applicable
		final Date dateInCurrentTimeZone = Utilities.getDefaultCalendar().getTime();

		final int timeZoneOffsetDifferential = timeZone.getOffset(dateInCurrentTimeZone.getTime()) - systemTimeOffset;

		cal.add(Calendar.MILLISECOND, timeZoneOffsetDifferential);
		final Date currentDateInLocalTimeZone = cal.getTime();

		if (LOG.isDebugEnabled())
		{
			LOG.debug("System timezone [" + TimeZone.getDefault().getID() + "] with offset ["
					+ TimeZone.getDefault().getOffset(Calendar.getInstance().getTime().getTime()) / 1000 / 60 / 60 + "]");
			LOG.debug("Using timezone ["
					+ timeZone.getID()
					+ "] with offset ["
					+ (timeZone.getOffset(Utilities.getDefaultCalendar().getTime().getTime()) / 1000 / 60 / 60 + "] for store ["
							+ timeZoneCode + "]"));
			LOG.debug("Timezone differential [" + (timeZoneOffsetDifferential / 1000 / 60 / 60) + "]");
		}

		return currentDateInLocalTimeZone;
	}

    public static int getTimeZoneOffsetDifferential(final String timeZoneCode)
    {
        final TimeZone timeZone = TimeZone.getTimeZone(timeZoneCode);
        // get date in system time zone
        final Calendar cal = Calendar.getInstance();
        final int systemTimeOffset = cal.getTimeZone().getOffset(cal.getTime().getTime());
        // set offset, pass in date to ensure we get day light savings time of applicable
        final Date dateInCurrentTimeZone = Utilities.getDefaultCalendar().getTime();

        return timeZone.getOffset(dateInCurrentTimeZone.getTime()) - systemTimeOffset;
    }

    public static Date adjustDateForTimeZone(Date date) {
        Date convertedDate = null;
        if (date != null) {
            TimeZone timeZone = TimeZone.getDefault();
            long timezoneOffset = timeZone.getOffset(System.currentTimeMillis());
            convertedDate = new Date(date.getTime() + timezoneOffset);
        }
        return convertedDate;
    }

    /**
     * Truncate the date otherwise we will have performance issues since the queries will never be cached.
     *
     * @param cal calendar to trucate
     */
    public void truncateDate(final Calendar cal) {
        cal.set(Calendar.MILLISECOND, 0);
        if (!isSecondsIncluded()) {
            cal.set(Calendar.SECOND, 0);
        }
        if (!isMinutesIncluded()) {
            cal.set(Calendar.MINUTE, 0);
        }
        if (!isHoursIncluded()) {
            cal.set(Calendar.HOUR, 0);
        }
    }

    private boolean isMinutesIncluded() {
        return Config.getString(DATE_GRANULARITY, "").equals("MINUTES") || isSecondsIncluded();
    }

    private boolean isHoursIncluded() {
        return Config.getString(DATE_GRANULARITY, "").equals("HOURS") || isMinutesIncluded();
    }

    private boolean isSecondsIncluded() {
        return Config.getString(DATE_GRANULARITY, "").equals("SECONDS");
    }

}
