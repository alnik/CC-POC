package de.hybris.multicountry.backoffice.widgets.actions;

import com.google.common.collect.Lists;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent.Level;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.core.impl.DefaultWidgetModel;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.util.notifications.NotificationService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.multicountry.services.MulticountryProductLockingService;
import de.hybris.platform.servicelayer.user.UserService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;


/**
 * Abstract action for product locking/unlocking.
 */
public abstract class AbstractProductLockAction extends AbstractComponentWidgetAdapterAware implements CockpitAction<Object, List> {

    protected static final String EVENT_LOCKED = "McProductsLocked";
    protected static final String EVENT_UNLOCKED = "McProductsUnlocked";

    private static final String PARENT_WIDGET_MODEL = "parentWidgetModel";
    private static final String PAGEABLE = "pageable";

    @Resource
    private MulticountryProductLockingService productLockingService;

    @Resource
    private UserService userService;

    @Resource
    private NotificationService notificationService;

    protected abstract Predicate<Object> getPerformCondition();

    protected abstract boolean performAction(final ProductModel product);

    protected abstract String getNotificationEvent();

    @Override
    public boolean canPerform(final ActionContext<Object> ctx) {
        final List<Object> data = getData(ctx);
        return CollectionUtils.isNotEmpty(data) && data.stream().allMatch(getPerformCondition());
    }

    /**
     * Locks or unlocks the object(s) in the context.
     *
     * @param context The context
     * @return An action result with the list of objects from the context that were successfully locked.
     */
    @Override
    public ActionResult<List> perform(final ActionContext<Object> context) {
        final List<Object> data = getData(context);
        if (CollectionUtils.isEmpty(data)) //error out immediately if the context is null or empty
        {
            return new ActionResult<>(ActionResult.ERROR);
        }

        final List<Object> updatedObjects = new ArrayList<>();
        final List<Object> failedObjects = new ArrayList<>();
        for (final Object object : data) {
            if (object instanceof ProductModel && performAction((ProductModel) object)) {
                updatedObjects.add(object);
            } else {
                failedObjects.add(object);
            }
        }

        //notify all the succesfully updated objects, send output
        if (!updatedObjects.isEmpty()) {
            autoRefreshProductList(context);
            showSuccessNotification(context, updatedObjects);
        }

        //notify all the failed objects
        if (!failedObjects.isEmpty()) {
            showFailureNotification(context, failedObjects);
        }

        // return success if all objects were updated
        return new ActionResult<>(failedObjects.isEmpty() ? ActionResult.SUCCESS : ActionResult.ERROR);
    }

    private List<Object> getData(final ActionContext<Object> context) {
        if (context.getData() instanceof Collection) {
            @SuppressWarnings("unchecked") final Collection<Object> data = (Collection<Object>) context.getData();
            return data.stream().filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return context.getData() == null ? Collections.emptyList() : Lists.newArrayList(context.getData());
        }
    }

    public MulticountryProductLockingService getProductLockingService() {
        return productLockingService;
    }

    public EmployeeModel getCurrentEmployee() {
        final UserModel user = userService.getCurrentUser();
        return user instanceof EmployeeModel ? (EmployeeModel) user : null;
    }

    @Override
    public boolean needsConfirmation(final ActionContext<Object> ctx) {
        return false;
    }

    protected void showSuccessNotification(final ActionContext<Object> ctx, final List<Object> lockedObjects) {
        notificationService.notifyUser(getComponentID(), getNotificationEvent(), Level.SUCCESS, lockedObjects);
    }

    protected void showFailureNotification(final ActionContext<Object> ctx, final List<Object> lockedObjects) {
        notificationService.notifyUser(getComponentID(), getNotificationEvent(), Level.FAILURE, lockedObjects);
    }

    private void autoRefreshProductList(final ActionContext<Object> ctx) {
        if (ctx.getData() instanceof ProductModel) {
            sendOutput("selectedItem", ctx.getData());
        } else {
            final Pageable pageable = getPageable(ctx);
            if (pageable != null) {
                pageable.refresh();
                sendOutput("outputRefreshContext", pageable);
            }
        }
    }

    private Pageable getPageable(final ActionContext<Object> ctx) {
        final Object widgetModel = ctx.getParameter(PARENT_WIDGET_MODEL);
        Pageable pageable = null;
        if (widgetModel instanceof DefaultWidgetModel) {
            final DefaultWidgetModel defaultWidgetModel = ((DefaultWidgetModel) ctx.getParameter(PARENT_WIDGET_MODEL));
            final Object page = defaultWidgetModel.get(PAGEABLE);
            pageable = page instanceof Pageable ? (Pageable) page : null;
        }

        return pageable;
    }
}
