/**
 *
 */
package de.hybris.platform.multicountry.services.impl;

import static de.hybris.platform.multicountry.constants.MulticountryConstants.AVAILABILITY_GROUPS;

import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.multicountry.services.MulticountryRestrictionService;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.search.restriction.session.SessionSearchRestriction;
import de.hybris.platform.servicelayer.session.Session;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.type.daos.TypeDao;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author marco.rolando
 *
 */
public class DefaultMulticountryRestrictionService implements MulticountryRestrictionService
{
	private static final String NO_EFFECT_QUERY = "1 = 1";
	private static final String DATE_RESTRICTION = "Frontend_ProductAvailabilityAssignmentOnline";
	private static final String APPROVAL_STATUS_RESTRICTION = "Frontend_ProductAvailabilityAssignmentApproved";
	private static final String EMPTY_AVAILABILITY_GROUP = "1111111111111";

	private SessionService sessionService;
	private SearchRestrictionService searchRestrictionService;
	private TypeDao typeDao;


	@Override
	public void setCurrentProductAvailabilityGroups(final Collection<ProductAvailabilityGroupModel> groups)
	{
		final Session session = getSessionService().getCurrentSession();
		if (CollectionUtils.isEmpty(groups))
		{
			/*
			 * Note: HSQL DB treats 0 as INT and PK as LONG leading to "out of range exception" if the value is 0 There
			 * should be no item having PK 1111111111111 If you need to change the value, adust it in restrictions as well
			 */
			session.setAttribute(AVAILABILITY_GROUPS, EMPTY_AVAILABILITY_GROUP);
		}
		else
		{
			session.setAttribute(AVAILABILITY_GROUPS, Collections.unmodifiableCollection(groups));
		}
	}

	@Override
	public Collection<ProductAvailabilityGroupModel> getCurrentProductAvailabilityGroup()
	{
		final Session session = getSessionService().getCurrentSession();
		if (session.getAttribute(AVAILABILITY_GROUPS) instanceof Collection)
		{
			final Collection<ProductAvailabilityGroupModel> groups = session.getAttribute(AVAILABILITY_GROUPS);
			return CollectionUtils.isEmpty(groups) ? Collections.emptySet() : groups;
		}
		else
		{
			return Collections.emptySet();

		}
	}

	@Override
	public void disableOnlineDateRestriction()
	{
		final ComposedTypeModel productComposedType = getTypeDao().findComposedTypeByCode(
				ProductAvailabilityAssignmentModel._TYPECODE);
		final SessionSearchRestriction disabledRestriction = new SessionSearchRestriction(DATE_RESTRICTION, NO_EFFECT_QUERY,
				productComposedType); // no effect on query
		getSearchRestrictionService().addSessionSearchRestrictions(disabledRestriction);
	}

	@Override
	public void disableApprovalStatusRestriction()
	{
		final ComposedTypeModel productComposedType = getTypeDao().findComposedTypeByCode(
				ProductAvailabilityAssignmentModel._TYPECODE);
		final SessionSearchRestriction disabledRestriction = new SessionSearchRestriction(APPROVAL_STATUS_RESTRICTION,
				NO_EFFECT_QUERY, productComposedType); // no effect on query
		getSearchRestrictionService().addSessionSearchRestrictions(disabledRestriction);
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

	public SearchRestrictionService getSearchRestrictionService()
	{
		return searchRestrictionService;
	}

	@Required
	public void setSearchRestrictionService(final SearchRestrictionService searchRestrictionService)
	{
		this.searchRestrictionService = searchRestrictionService;
	}

	public TypeDao getTypeDao()
	{
		return typeDao;
	}

	@Required
	public void setTypeDao(final TypeDao typeDao)
	{
		this.typeDao = typeDao;
	}
}
