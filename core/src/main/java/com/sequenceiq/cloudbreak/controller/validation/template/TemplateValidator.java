package com.sequenceiq.cloudbreak.controller.validation.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Suppliers;
import com.sequenceiq.cloudbreak.cloud.PlatformParameters;
import com.sequenceiq.cloudbreak.cloud.PlatformParametersConsts;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmTypes;
import com.sequenceiq.cloudbreak.cloud.model.DiskType;
import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.cloud.model.VmType;
import com.sequenceiq.cloudbreak.cloud.model.VmTypeMeta;
import com.sequenceiq.cloudbreak.cloud.model.VolumeParameterConfig;
import com.sequenceiq.cloudbreak.cloud.model.VolumeParameterType;
import com.sequenceiq.cloudbreak.controller.exception.BadRequestException;
import com.sequenceiq.cloudbreak.controller.validation.LocationService;
import com.sequenceiq.cloudbreak.domain.Credential;
import com.sequenceiq.cloudbreak.domain.Template;
import com.sequenceiq.cloudbreak.service.stack.CloudParameterService;

@Component
public class TemplateValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateValidator.class);

    private List<String> convenientLogs = new ArrayList<>();

    @Inject
    private CloudParameterService cloudParameterService;

    @Inject
    private LocationService locationService;

    private final Supplier<Map<Platform, Map<String, VolumeParameterType>>> diskMappings =
            Suppliers.memoize(() -> cloudParameterService.getDiskTypes().getDiskMappings());

    private final Supplier<Map<Platform, PlatformParameters>> platformParameters =
            Suppliers.memoize(() -> cloudParameterService.getPlatformParameters());

    public void validateTemplateRequest(Credential credential, Template template, String region, String availabilityZone, String variant) {

        LOGGER.info("Josh and Toby's version. validateTemplateRequest is invoked");

        String debugMsg = null;
        CloudVmTypes cloudVmTypes = cloudParameterService.getVmTypesV2(credential, region, variant, new HashMap<>());

        if (StringUtils.isEmpty(template.getInstanceType())) {
            validateCustomInstanceType(template);
        } else {
            VmType vmType = null;
            VolumeParameterType volumeParameterType = null;
            Platform platform = Platform.platform(template.cloudPlatform());
            Map<String, Set<VmType>> machines = cloudVmTypes.getCloudVmResponses();
            machines.forEach((k, v) -> {
                String debugMsgLambda = "%s region/az has %d different vmtypes: %s".format(k, v.size(), v.toString());
                LOGGER.info(debugMsgLambda);
                convenientLogs.add(debugMsgLambda);
            });
            String locationString = locationService.location(region, availabilityZone);
            debugMsg = "Current region or az; locationService.location(region=%s, availabilityZone%s)=%s".format(region, availabilityZone, locationString);
            LOGGER.info(debugMsg);
            convenientLogs.add(debugMsg);
            if (machines.containsKey(locationString) && !machines.get(locationString).isEmpty()) {
                for (VmType type : machines.get(locationString)) {
                    debugMsg = "template vmtype: %s available vmtype %s in this az/region".format(type.value(), template.getInstanceType());
                    LOGGER.info(debugMsg);
                    convenientLogs.add(debugMsg);
                    if (type.value().equals(template.getInstanceType())) {
                        vmType = type;
                        break;
                    }
                }
                if (vmType == null) {
                    throw new BadRequestException(
                            String.format("The '%s' instance type isn't supported by '%s' platform", template.getInstanceType(), platform.value()));
                }
            }
            Map<Platform, Map<String, VolumeParameterType>> disks = diskMappings.get();
            if (disks.containsKey(platform) && !disks.get(platform).isEmpty()) {
                Map<String, VolumeParameterType> map = disks.get(platform);
                volumeParameterType = map.get(template.getVolumeType());
                if (volumeParameterType == null) {
                    throw new BadRequestException(
                            String.format("The '%s' volume type isn't supported by '%s' platform%n%s",
                                    template.getVolumeType(), platform.value(), String.join("\n", convenientLogs)));
                }
            }

            validateVolume(template, vmType, platform, volumeParameterType);
        }
    }

    private void validateCustomInstanceType(Template template) {
        Map<String, Object> params = template.getAttributes().getMap();
        Platform platform = Platform.platform(template.cloudPlatform());
        PlatformParameters pps = platformParameters.get().get(platform);
        if (pps != null) {
            Boolean customInstanceType = pps.specialParameters().getSpecialParameters().get(PlatformParametersConsts.CUSTOM_INSTANCETYPE);
            if (BooleanUtils.isTrue(customInstanceType)) {
                if (params.get(PlatformParametersConsts.CUSTOM_INSTANCETYPE_CPUS) == null
                        || params.get(PlatformParametersConsts.CUSTOM_INSTANCETYPE_MEMORY) == null) {
                    throw new BadRequestException(String.format("Missing 'cpus' or 'memory' param for custom instancetype on %s platform",
                            template.cloudPlatform()));
                }
            } else {
                throw new BadRequestException(String.format("Custom instancetype is not supported on %s platform", template.cloudPlatform()));
            }
        }
    }

    private void validateVolume(Template value, VmType vmType, Platform platform, VolumeParameterType volumeParameterType) {
        validateVolumeType(value, platform);
        validateVolumeCount(value, vmType, volumeParameterType);
        validateVolumeSize(value, vmType, volumeParameterType);
        validateMaximumVolumeSize(value, vmType);
    }

    private void validateMaximumVolumeSize(Template value, VmType vmType) {
        if (vmType != null) {
            String maxSize = vmType.getMetaDataValue(VmTypeMeta.MAXIMUM_PERSISTENT_DISKS_SIZE_GB);
            if (maxSize != null) {
                int fullSize = value.getVolumeSize() * value.getVolumeCount();
                if (Integer.parseInt(maxSize) < fullSize) {
                    throw new BadRequestException(
                            String.format("The %s platform does not support %s Gb full volume size. The maximum size of disks could be %s Gb.",
                                    value.cloudPlatform(), fullSize, maxSize));
                }
            }
        }
    }

    private void validateVolumeType(Template value, Platform platform) {
        DiskType diskType = DiskType.diskType(value.getVolumeType());
        Map<Platform, Collection<DiskType>> diskTypes = cloudParameterService.getDiskTypes().getDiskTypes();
        if (diskTypes.containsKey(platform) && !diskTypes.get(platform).isEmpty()) {
            if (!diskTypes.get(platform).contains(diskType)) {
                throw new BadRequestException(String.format("The '%s' platform does not support '%s' volume type", platform.value(), diskType.value()));
            }
        }
    }

    private void validateVolumeCount(Template value, VmType vmType, VolumeParameterType volumeParameterType) {
        if (vmType != null && needToCheckVolume(volumeParameterType, value.getVolumeCount())) {
            VolumeParameterConfig config = vmType.getVolumeParameterbyVolumeParameterType(volumeParameterType);
            if (config != null) {
                if (value.getVolumeCount() > config.maximumNumber()) {
                    throw new BadRequestException(String.format("Max allowed volume count for '%s': %s", vmType.value(), config.maximumNumber()));
                } else if (value.getVolumeCount() < config.minimumNumber()) {
                    throw new BadRequestException(String.format("Min allowed volume count for '%s': %s", vmType.value(), config.minimumNumber()));
                }
            } else {
                throw new BadRequestException(String.format("The '%s' instance type does not support 'Ephemeral' volume type", vmType.value()));
            }
        }
    }

    private void validateVolumeSize(Template value, VmType vmType, VolumeParameterType volumeParameterType) {
        if (vmType != null && needToCheckVolume(volumeParameterType, value.getVolumeCount())) {
            VolumeParameterConfig config = vmType.getVolumeParameterbyVolumeParameterType(volumeParameterType);
            if (config != null) {
                if (value.getVolumeSize() > config.maximumSize()) {
                    throw new BadRequestException(String.format("Max allowed volume size for '%s': %s", vmType.value(), config.maximumSize()));
                } else if (value.getVolumeSize() < config.minimumSize()) {
                    throw new BadRequestException(String.format("Min allowed volume size for '%s': %s", vmType.value(), config.minimumSize()));
                }
            } else {
                throw new BadRequestException(String.format("The '%s' instance type does not support 'Ephemeral' volume type", vmType.value()));
            }
        }
    }

    private boolean needToCheckVolume(VolumeParameterType volumeParameterType, Object value) {
        return volumeParameterType != VolumeParameterType.EPHEMERAL || value != null;
    }
}
