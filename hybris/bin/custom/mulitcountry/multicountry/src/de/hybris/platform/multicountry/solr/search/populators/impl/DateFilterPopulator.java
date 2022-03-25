package de.hybris.platform.multicountry.solr.search.populators.impl;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.multicountry.services.MulticountryRestrictionService;
import de.hybris.platform.multicountry.util.TimeZoneHelper;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Required;


/**
 * /** Add a filter query that removes all offline products.
 *
 * To manage empty online (or offline) boundaries, the filter uses the forms:
 *
 * -(-onlineDate:[* TO now] AND onlineDate:[* TO *])
 *
 * -(-offlineDate:[now TO *] AND offlineDate:[* TO *])
 *
 */
public class DateFilterPopulator implements Populator<SearchQueryConverterData, SolrQuery>
{

	private static final String AND = " AND ";
	private static final String ONLINE_DATE = "onlineDate";
	private static final String OFFLINE_DATE = "offlineDate";
	private static final String FIELD_FORMAT = "%s_%s_date_mv";
	private static final String ONLINE_DATE_QUERY = "-(-%s:[* TO %s] AND %s:[* TO *])";
	private static final String OFFLINE_DATE_QUERY = "-(-%s:[%s TO *] AND %s:[* TO *])";

	private TimeService timeService;

	private MulticountryRestrictionService multicountryRestrictionService;


	protected String translateField(final String prefix, final ProductAvailabilityGroupModel group)
	{
		return String.format(FIELD_FORMAT, prefix, group.getId().toLowerCase());
	}

	protected String translateDate(final Date date)
	{
		return DateFormatUtils.format(date, "yyyy-MM-dd'T'HH:mm:ss'Z'"); //e.g. "2014-12-31T00:00:00Z";
	}

	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target) throws ConversionException
	{


		final Collection<ProductAvailabilityGroupModel> availabilityGroups = multicountryRestrictionService
				.getCurrentProductAvailabilityGroup();
		final Date now = timeService.getCurrentTime();
		final String nowString = translateDate(TimeZoneHelper.adjustDateForTimeZone(now));

		// Double negation is required
		// e.g. -(-onlineDate_uk-availability_date_mv:[* TO 2014-02-10T00:00:00Z] AND onlineDate_uk-availability_date_mv:[* TO *])
		if (!availabilityGroups.isEmpty())
		{
			final StringBuilder rawQuery = new StringBuilder();



			for (final ProductAvailabilityGroupModel group : availabilityGroups)
			{
				final String onlineDateField = translateField(ONLINE_DATE, group);
				final String offlineDateField = translateField(OFFLINE_DATE, group);

				if (rawQuery.length() > 0) // don't add AND in front of the first clause
				{
					rawQuery.append(AND);
				}
				rawQuery.append(String.format(ONLINE_DATE_QUERY, onlineDateField, nowString, onlineDateField));
				rawQuery.append(AND);
				rawQuery.append(String.format(OFFLINE_DATE_QUERY, offlineDateField, nowString, offlineDateField));
			}
			target.addFilterQuery(rawQuery.toString());

		}

	}



	private String translateFieldsAndAddToQuery(final ProductAvailabilityGroupModel group, final String nowString)
	{

		final StringBuilder rawQuery = new StringBuilder();

		final String onlineDateField = translateField(ONLINE_DATE, group);
		final String offlineDateField = translateField(OFFLINE_DATE, group);

		if (rawQuery.length() > 0) // don't add AND in front of the first clause
		{
			rawQuery.append(AND);
		}
		rawQuery.append(String.format(ONLINE_DATE_QUERY, onlineDateField, nowString, onlineDateField));
		rawQuery.append(AND);
		rawQuery.append(String.format(OFFLINE_DATE_QUERY, offlineDateField, nowString, offlineDateField));

		return rawQuery.toString();
	}

	/**
	 * @param multicountryRestrictionService
	 *           the multicountryRestrictionService to set
	 */
	@Required
	public void setMulticountryRestrictionService(final MulticountryRestrictionService multicountryRestrictionService)
	{
		this.multicountryRestrictionService = multicountryRestrictionService;
	}

	/**
	 * @param timeService
	 *           the timeService to set
	 */
	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}
}
