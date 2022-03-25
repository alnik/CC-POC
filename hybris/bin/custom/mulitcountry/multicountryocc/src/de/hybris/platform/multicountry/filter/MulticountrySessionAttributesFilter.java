package de.hybris.platform.multicountry.filter;

import static de.hybris.platform.multicountry.constants.MulticountryConstants.ACTIVE_CATEGORY_CATALOG;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;
import de.hybris.platform.contextualattributevalues.services.ContextualAttributeValuesSessionService;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.europe1.constants.Europe1Constants;
import de.hybris.platform.europe1.enums.UserPriceGroup;
import de.hybris.platform.multicountry.enums.TimezoneEnum;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.multicountry.services.MulticountryRestrictionService;
import de.hybris.platform.multicountry.strategies.MulticountryCategoryCatalogSelector;
import de.hybris.platform.multicountry.util.TimeZoneHelper;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.filter.OncePerRequestFilter;
/**
 * Filter sets multicountry session context basing on request parameters:<br>
 * <ul>
 * <li><b>lang</b> - set current {@link LanguageModel}</li>
 * <li><b>curr</b> - set current {@link CurrencyModel}</li>
 * </ul>
 *
 * @author KKW
 *
 */
public class MulticountrySessionAttributesFilter extends OncePerRequestFilter
{
	private static final Logger LOG = Logger.getLogger(MulticountrySessionAttributesFilter.class);

    private BaseStoreService baseStoreService;
    private SessionService sessionService;

    private MulticountryRestrictionService multicountryRestrictionService;
    private TimeService timeService;
    private MulticountryCategoryCatalogSelector categoryCatalogSelector;
    private ContextualAttributeValuesSessionService contextualAttributeValuesSessionService;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException
    {
        //Get the current base store
        final BaseStoreModel currentBaseStore = getBaseStoreService().getCurrentBaseStore();
        LOG.debug("BaseStore is "+ (currentBaseStore != null ? currentBaseStore.getName() : "NULL"));
        if (currentBaseStore != null)
        {
            initializeSessionPriceGroup(currentBaseStore);
            initializeSessionProductAssignment(currentBaseStore);
            // set the timezone and timezone offset for this session

            initializeTimezone(currentBaseStore);
            initializeCategoryCatalog(currentBaseStore);
            // Initialize contextual attributes
            initializeContextualContext(currentBaseStore);
            // Initialize the session with discountgroup
            initializeSessionDiscountGroup(currentBaseStore);
        }

        filterChain.doFilter(request, response);
    }

    protected void initializeSessionProductAssignment(final BaseStoreModel currentBaseStore)
    {
        // Get and save the availability groups for the current base store
        final Collection<ProductAvailabilityGroupModel> availabilityGroups = currentBaseStore.getAvailabilityGroups();
        getMulticountryRestrictionService().setCurrentProductAvailabilityGroups(availabilityGroups);
    }


    protected void initializeCategoryCatalog(final BaseStoreModel currentBaseStore)
    {
        final CatalogModel catalog = getCategoryCatalogSelector().select(currentBaseStore);
        if (catalog == null)
        {
            LOG.info("No category catalog was found for " + currentBaseStore.getName());
        }
        else
        {
            LOG.info("Setting category catalog " + catalog.getName() + " in session for store " + currentBaseStore.getName());
            getSessionService().setAttribute(ACTIVE_CATEGORY_CATALOG, catalog);
        }
    }

    /**
     * TODO put logic into a service so it can be reused
     *
     * @param currentBaseStore
     */
    protected void initializeTimezone(final BaseStoreModel currentBaseStore)
    {
        if (currentBaseStore.getTimezone() != null)
        {
            final TimezoneEnum storeTimeZone = currentBaseStore.getTimezone();
            final TimeZone timeZone = TimeZone.getTimeZone(storeTimeZone.getCode());

            getSessionService().getCurrentSession().setAttribute("timezone", timeZone);

            final int timeZoneOffsetDifferential = TimeZoneHelper.getTimeZoneOffsetDifferential(storeTimeZone.getCode());
            getTimeService().setTimeOffset(timeZoneOffsetDifferential);
        }
    }

    protected void initializeSessionPriceGroup(final BaseStoreModel currentBaseStore)
    {
        //Get the price group assigned to the current base store...
        final UserPriceGroup priceGroup = currentBaseStore.getUserPriceGroup();
        if (priceGroup == null)
        {
            LOG.warn("Store [" + currentBaseStore.getUid() + "] doesn't have a UserPriceGroup");
            return; //no price group?? error?
        }

        //... set price group into the session.
        getSessionService().setAttribute(Europe1Constants.PARAMS.UPG, priceGroup);

    }

    protected void initializeContextualContext(final BaseStoreModel currentBaseStore)
    {
        // Get and save the contextual context for the current base store
        final ContextualAttributesContextModel currentContext = currentBaseStore.getContextualAttributesContext();
        getContextualAttributeValuesSessionService().setCurrentContext(currentContext);
    }

    private void initializeSessionDiscountGroup(BaseStoreModel currentBaseStore) {
        final de.hybris.platform.europe1.enums.UserDiscountGroup userDiscountGroup = currentBaseStore.getUserDiscountGroup();
        if (userDiscountGroup == null)
        {
            LOG.warn("Store [" + currentBaseStore.getUid() + "] doesn't have a DiscountPriceGroup");
            return; //no discount group?? error?
        }
        //... set discount group into the session.
        getSessionService().setAttribute(Europe1Constants.PARAMS.UDG, userDiscountGroup);
    }

    protected BaseStoreService getBaseStoreService() {
        return baseStoreService;
    }

    @Required
    public void setBaseStoreService(final BaseStoreService baseStoreService) {
        this.baseStoreService = baseStoreService;
    }

    protected SessionService getSessionService() {
        return sessionService;
    }

    @Required
    public void setSessionService(final SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public MulticountryRestrictionService getMulticountryRestrictionService()
    {
        return multicountryRestrictionService;
    }

    @Required
    public void setMulticountryRestrictionService(final MulticountryRestrictionService multicountryRestrictionService)
    {
        this.multicountryRestrictionService = multicountryRestrictionService;
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

    public MulticountryCategoryCatalogSelector getCategoryCatalogSelector()
    {
        return categoryCatalogSelector;
    }

    @Required
    public void setCategoryCatalogSelector(final MulticountryCategoryCatalogSelector categoryCatalogSelector)
    {
        this.categoryCatalogSelector = categoryCatalogSelector;
    }

    /**
     * @return the contextualAttributeValuesSessionService
     */
    protected ContextualAttributeValuesSessionService getContextualAttributeValuesSessionService()
    {
        return contextualAttributeValuesSessionService;
    }

    /**
     * @param contextualAttributeValuesSessionService
     *           the contextualAttributeValuesSessionService to set
     */
    @Required
    public void setContextualAttributeValuesSessionService(
            final ContextualAttributeValuesSessionService contextualAttributeValuesSessionService)
    {
        this.contextualAttributeValuesSessionService = contextualAttributeValuesSessionService;
    }
}
