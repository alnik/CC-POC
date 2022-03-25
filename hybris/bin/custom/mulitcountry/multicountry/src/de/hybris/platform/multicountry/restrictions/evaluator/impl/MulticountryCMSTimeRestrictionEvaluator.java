/**
 *
 */
package de.hybris.platform.multicountry.restrictions.evaluator.impl;

import de.hybris.platform.cms2.model.restrictions.CMSTimeRestrictionModel;
import de.hybris.platform.cms2.servicelayer.data.RestrictionData;
import de.hybris.platform.cms2.servicelayer.services.evaluator.CMSRestrictionEvaluator;
import de.hybris.platform.cms2.servicelayer.services.evaluator.impl.CMSTimeRestrictionEvaluator;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.time.TimeService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author brendan.dobbs
 *
 */
public class MulticountryCMSTimeRestrictionEvaluator implements CMSRestrictionEvaluator<CMSTimeRestrictionModel>
{
	private static final Logger LOG = Logger.getLogger(CMSTimeRestrictionEvaluator.class);

	private SessionService sessionService;
	private TimeService timeService;


	@Override
	public boolean evaluate(final CMSTimeRestrictionModel timeRestriction, final RestrictionData context)
	{
		Date now = getSessionService().getAttribute("previewTime");
		boolean previewDate = true;
		if (now == null)
		{
			previewDate = false;
			// timeService returns the time with an offset for the time zone
			now = Boolean.TRUE.equals(timeRestriction.getUseStoreTimeZone()) ? getTimeService().getCurrentTime() : new Date();
		}

		final Date from = timeRestriction.getActiveFrom();
		final Date until = timeRestriction.getActiveUntil();
		if (LOG.isDebugEnabled())
		{
			final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			if (previewDate)
			{
				LOG.debug("Using preview time: " + dateformat.format(now));
			}
			LOG.debug("Current time: " + dateformat.format(now));
			LOG.debug("Valid from: " + ((from != null) ? dateformat.format(from) : "null"));
			LOG.debug("Valid until: " + ((until != null) ? dateformat.format(until) : "null"));
		}

		boolean after = true;
		if (from != null)
		{
			after = now.after(from);
		}
		boolean before = true;
		if (until != null)
		{
			before = now.before(until);
		}
		return after && before;
	}

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	public TimeService getTimeService()
	{
		return timeService;
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}
}
