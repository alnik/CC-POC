package com.hybris.multicountry.excel.translators;

import com.google.common.base.Joiner;
import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImpexValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.translators.AbstractExcelValueTranslator;
import com.hybris.backoffice.excel.util.ExcelDateUtils;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.CollectionTypeModel;
import de.hybris.platform.core.model.type.RelationDescriptorModel;
import de.hybris.platform.impex.constants.ImpExConstants;
import de.hybris.platform.multicountry.jalo.MulticountryAvailabilityTranslator;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

public class ExcelMulticountryAvailabilityTypeTranslator extends AbstractExcelValueTranslator<Collection<ProductAvailabilityAssignmentModel>> {
    /**
     * availabilityGroups:approvalStatus:dateFrom to dateTo
     */

    private static final Logger LOG = Logger.getLogger(ExcelMulticountryAvailabilityTypeTranslator.class);
    private static final String PATTERN = "%s:%s:%s";
    public static final String AVAILABILITY_GROUP = "PART-availability|PARTAPP-availability";
    public static final String AVAILABILITY_STATUS = "approved|unapproved|check";
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DEFAULT_OFFLINE_DATE = "12/12/2099";

    private ExcelDateUtils excelDateUtils;
    private CommonI18NService commonI18NService;

    @Override
    public boolean canHandle(final AttributeDescriptorModel attributeDescriptor) {
        return attributeDescriptor instanceof RelationDescriptorModel
                && ProductAvailabilityAssignmentModel._TYPECODE
                .equals(((RelationDescriptorModel) attributeDescriptor).getRelationType().getTargetType().getCode());
    }

    @Override
    public Optional<Object> exportData(final Collection<ProductAvailabilityAssignmentModel> objectToExport) {
        return CollectionUtils.emptyIfNull(objectToExport).stream()
                .map(this::exportAvailabilityAssignment)
                .reduce(Joiner.on(',')::join)
                .map(Object.class::cast);
    }

    protected String exportAvailabilityAssignment(final ProductAvailabilityAssignmentModel productAvailabilityAssignment) {
        final String approvalStatus = getValueOrEmpty(productAvailabilityAssignment.getStatus(), ArticleApprovalStatus::getCode);
        final String availabilityGroup = getValueOrEmpty(productAvailabilityAssignment.getAvailabilityGroup(), ProductAvailabilityGroupModel::getId);

        return String.format(PATTERN, availabilityGroup, approvalStatus, getDateRange(productAvailabilityAssignment.getOnlineDate(), productAvailabilityAssignment.getOfflineDate()));
    }

    protected String getDateRange(final Date onlineDate, Date offlineDate) {
        if (offlineDate == null) {
            try {
                offlineDate = new SimpleDateFormat(DEFAULT_DATE_FORMAT).parse(DEFAULT_OFFLINE_DATE);
            } catch (ParseException exception) {
                LOG.error("Error when parsing date :" + exception.getMessage());
            }
        }

        if (onlineDate != null && offlineDate != null) {
            return excelDateUtils.exportDateRange(onlineDate, offlineDate);
        }

        return StringUtils.EMPTY;
    }

    protected <T> String getValueOrEmpty(final T reference, final Function<T, String> valueSupplier) {
        return reference != null ? valueSupplier.apply(reference) : StringUtils.EMPTY;
    }

    @Override
    public String referenceFormat(final AttributeDescriptorModel attributeDescriptor) {
        return String.format("%s:%s:%s", AVAILABILITY_GROUP, AVAILABILITY_STATUS,
                excelDateUtils.getDateRangePattern());
    }

    @Override
    public ImpexValue importValue(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters) {
        final List<String> formattedAvailabilities = new ArrayList<>();
        for (final Map<String, String> params : importParameters.getMultiValueParameters()) {
            formattedAvailabilities.add(buildSingleAvailabilityAssignmentImpexValue(params));
        }
        return new ImpexValue(String.join(", ", formattedAvailabilities),
                new ImpexHeaderValue.Builder(attributeDescriptor.getQualifier()).withDateFormat(excelDateUtils.getDateTimeFormat()).withTranslator(MulticountryAvailabilityTranslator.class.getName()).withQualifier(attributeDescriptor.getQualifier())
                        .build());
    }

    protected String buildSingleAvailabilityAssignmentImpexValue(final Map<String, String> params) {
        final StringBuilder sb = new StringBuilder();
        appendIfPresent(sb, params.get(AVAILABILITY_GROUP));
        appendIfPresent(sb, params.get(AVAILABILITY_STATUS));
        appendIfPresent(sb, getImpexDateRange(params.get(excelDateUtils.getDateRangeParamKey())));
        return sb.toString();
    }

    protected void appendIfPresent(final StringBuilder sb, final String value) {
        if (StringUtils.isNotEmpty(value)) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(value);
        }
    }

    protected String getImpexDateRange(final String dateRange) {
        if (StringUtils.isNotEmpty(dateRange)) {
            final Pair<String, String> range = excelDateUtils.extractDateRange(dateRange);
            if (range != null) {
                return String.format("[%s%s%s]", excelDateUtils.importDate(range.getLeft()),
                        ImpExConstants.Syntax.DATERANGE_DELIMITER, excelDateUtils.importDate(range.getRight()));
            }
        }
        return null;
    }


    protected ExcelDateUtils getExcelDateUtils() {
        return excelDateUtils;
    }

    public void setExcelDateUtils(ExcelDateUtils excelDateUtils) {
        this.excelDateUtils = excelDateUtils;
    }

}
