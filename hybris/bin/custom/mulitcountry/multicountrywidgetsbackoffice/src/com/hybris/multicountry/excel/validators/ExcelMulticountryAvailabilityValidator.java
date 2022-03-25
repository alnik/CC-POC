
package com.hybris.multicountry.excel.validators;

import com.hybris.backoffice.excel.validators.ExcelValidator;
import com.hybris.multicountry.excel.translators.ExcelMulticountryAvailabilityTypeTranslator;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.CollectionTypeModel;
import de.hybris.platform.core.model.type.RelationDescriptorModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.europe1.enums.PriceRowChannel;
import de.hybris.platform.europe1.enums.UserPriceGroup;
import de.hybris.platform.europe1.jalo.impex.Europe1PricesTranslator;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.jalo.enumeration.EnumerationManager;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.daos.CurrencyDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.translators.ExcelEurope1PricesTypeTranslator;
import com.hybris.backoffice.excel.util.ExcelDateUtils;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;

import javax.annotation.Resource;


/**
 * Validates productAviialabilityAssignment for {@link ExcelMulticountryAvailabilityTypeTranslator}.
 *
 * <pre>
 *    <b>Format:</b>availabilityGroup:approvalStatus:[dateFrom to dateTo]
 * </pre>
 * <p>
 * Validator checks if:
 * <ul>
 * <li>Given availabilityGroup exists</li>
 * <li>approvalStatus exists</li>
 * <li>dateFrom and dateTo are in correct format {@link ExcelDateUtils#getDateTimeFormat()} and dateFrom is not after
 * dateTo</li
 * </ul>
 */
public class ExcelMulticountryAvailabilityValidator implements ExcelValidator {
    private static final Logger LOG = LoggerFactory.getLogger(ExcelMulticountryAvailabilityValidator.class);
    protected static final String AVAILABILITY_GROUP_KEY = "AvailabilityGroup";
    protected static final String APPROVAL_STATUS_KEY = "ApprovalStatus";
    public static final Pattern PATTERN_DATE_RANGE = Pattern.compile("(.+)\\s*to\\s*(.+)");
    public static final String VALIDATION_AVAILABILITY_GROUP_DOESNT_EXIST = "excel.import.validation.availability.group.doesnt.exist";
    public static final String VALIDATION_NO_SUCH_APPROVAL_STATUS = "excel.import.validation.availability.status.does.not.exist";
    public static final String VALIDATION_INCORRECT_DATE_RANGE = "excel.import.validation.availability.date.incorrect.format";
    public static final String VALIDATION_START_DATE_AFTER_END_DATE = "excel.import.validation.availability.date.start.after.end";
    protected final String EXISTING_AVAILABILITY_GROUPS = "SELECT {"+ProductAvailabilityGroupModel.PK+"} FROM {"+ ProductAvailabilityGroupModel._TYPECODE+"}";

    private EnumerationService enumerationService;



    private FlexibleSearchService flexibleSearchService;
    private ExcelDateUtils excelDateUtils;


    @Override
    public ExcelValidationResult validate(final ImportParameters importParameters,
                                          final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> ctx) {
        if (!ctx.containsKey(AVAILABILITY_GROUP_KEY)) {
            populateContext(ctx);
        }

        final List<ValidationMessage> validationMessages = new ArrayList<>();

        for (final Map<String, String> parameters : importParameters.getMultiValueParameters()) {
            validationMessages.addAll(validateSingleValue(ctx, parameters));
        }
        return validationMessages.isEmpty() ? ExcelValidationResult.SUCCESS : new ExcelValidationResult(validationMessages);
    }

    protected List<ValidationMessage> validateSingleValue(final Map<String, Object> ctx, final Map<String, String> parameters) {
        final List<ValidationMessage> validations = new ArrayList<>();
        validateAvailabilityGroup(ctx, parameters.get(ExcelMulticountryAvailabilityTypeTranslator.AVAILABILITY_GROUP)).ifPresent(validations::add);
        validateAvailabilityStatus(parameters.get(ExcelMulticountryAvailabilityTypeTranslator.AVAILABILITY_STATUS)).ifPresent(validations::add);
        validateDateRange(parameters.get(excelDateUtils.getDateRangeParamKey())).ifPresent(validations::add);
        return validations;

    }


    protected Optional<ValidationMessage> validateDateRange(final String dateRange) {
        if (StringUtils.isEmpty(dateRange)) {
            return Optional.empty();
        }

        final Matcher matcher = PATTERN_DATE_RANGE.matcher(dateRange);

        if (matcher.matches()) {
            final Date from = parseDate(matcher.group(1));
            final Date to = parseDate(matcher.group(2));

            if (from != null && to != null) {
                if (from.after(to)) {
                    return Optional
                            .of(new ValidationMessage(VALIDATION_START_DATE_AFTER_END_DATE, matcher.group(1), matcher.group(2)));
                }
                return Optional.empty();
            }
        }
        return Optional.of(new ValidationMessage(VALIDATION_INCORRECT_DATE_RANGE, dateRange));
    }

    protected Date parseDate(final String date) {
        try {
            return excelDateUtils.convertToImportedDate(date);
        } catch (final DateTimeParseException e) {
            LOG.debug("Wrong date format " + date, e);
            return null;
        }
    }

    protected Optional<ValidationMessage> validateAvailabilityStatus(final String status) {
        if (StringUtils.isNotEmpty(status)) {
            try {
                enumerationService.getEnumerationValue("ArticleApprovalStatus", status);
            } catch (final UnknownIdentifierException nie) {
                return Optional.of(new ValidationMessage(VALIDATION_NO_SUCH_APPROVAL_STATUS, status));
            }       
        }
        return Optional.empty();
    }

    protected Optional<ValidationMessage> validateAvailabilityGroup(final Map<String, Object> ctx, final String availabilityGroup) {
        if (!containsAvailabilityGroup(ctx, availabilityGroup)) {
            return Optional.of(new ValidationMessage(VALIDATION_AVAILABILITY_GROUP_DOESNT_EXIST, availabilityGroup));
        }
        return Optional.empty();
    }


    protected boolean containsAvailabilityGroup(final Map<String, Object> ctx, final String availabilityGroup) {
        return ((Set) ctx.get(AVAILABILITY_GROUP_KEY)).contains(availabilityGroup);
    }

    protected void populateContext(final Map<String, Object> ctx) {
        //final Set<String> availabilityGroups = Set.of("PART-availability", "PARTAPP-availability");// Could be retrived from DB with flexible-search but might impact the performances
        final Set<String> availabilityGroups = getExistingAvailabilityGroup().stream().map(x -> x.getId()).collect(Collectors.toSet());
        ctx.put(AVAILABILITY_GROUP_KEY, availabilityGroups);
    }

    public List<ProductAvailabilityGroupModel> getExistingAvailabilityGroup()
    {
        return getFlexibleSearchService().<ProductAvailabilityGroupModel> search(EXISTING_AVAILABILITY_GROUPS).getResult();
    }


    @Override
    public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor) {
        return importParameters.isCellValueNotBlank() && attributeDescriptor instanceof RelationDescriptorModel
                && ProductAvailabilityAssignmentModel._TYPECODE
                .equals(((RelationDescriptorModel) attributeDescriptor).getRelationType().getTargetType().getCode());

    }


    protected EnumerationService getEnumerationService() {
        return enumerationService;
    }

    @Required
    public void setEnumerationService(final EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }

    protected ExcelDateUtils getExcelDateUtils() {
        return excelDateUtils;
    }

    @Required
    public void setExcelDateUtils(final ExcelDateUtils excelDateUtils) {
        this.excelDateUtils = excelDateUtils;
    }

    protected FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    @Required
    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }
}
