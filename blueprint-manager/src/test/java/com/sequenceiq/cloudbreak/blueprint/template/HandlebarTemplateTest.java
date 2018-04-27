package com.sequenceiq.cloudbreak.blueprint.template;

import static com.sequenceiq.cloudbreak.util.FileReaderUtils.readFileFromClasspath;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.util.Maps;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.google.common.collect.Sets;
import com.sequenceiq.cloudbreak.TestUtil;
import com.sequenceiq.cloudbreak.api.model.DatabaseVendor;
import com.sequenceiq.cloudbreak.api.model.rds.RdsType;
import com.sequenceiq.cloudbreak.blueprint.nifi.HdfConfigs;
import com.sequenceiq.cloudbreak.blueprint.template.views.BlueprintView;
import com.sequenceiq.cloudbreak.blueprint.template.views.SharedServiceConfigsView;
import com.sequenceiq.cloudbreak.blueprint.templates.GeneralClusterConfigs;
import com.sequenceiq.cloudbreak.common.model.OrchestratorType;
import com.sequenceiq.cloudbreak.domain.RDSConfig;

@RunWith(Parameterized.class)
public class HandlebarTemplateTest {

    private Handlebars handlebars = HandlebarUtils.handlebars();

    private String input;

    private String output;

    private Map<String, Object> model;

    public HandlebarTemplateTest(String input, String output, Map<String, Object> model) {
        this.input = input;
        this.output = output;
        this.model = model;
    }

    @Parameterized.Parameters(name = "{index}: templateTest {0} with handlebar where the expected file is  {1}")
    public static Iterable<Object[]> data() throws JsonProcessingException {
        return Arrays.asList(new Object[][]{
                {"blueprints/configurations/atlas/ldap.handlebars", "configurations/atlas/atlas-with-ldap.json",
                        ldapConfigWhenLdapPresentedThenShouldReturnWithLdapConfig()},
                {"blueprints/configurations/atlas/ldap.handlebars", "configurations/atlas/atlas-without-ldap.json",
                        withoutLdapConfigWhenLdapNotPresentedThenShouldReturnWithoutLdapConfig()},
                {"blueprints/configurations/druid_superset/rds.handlebars", "configurations/druid_superset/druid-with-rds.json",
                        druidSupersetRdsConfigWhenRdsPresentedThenShouldReturnWithRdsConfig()},
                {"blueprints/configurations/druid_superset/rds.handlebars", "configurations/druid_superset/druid-without-rds.json",
                        druidSupersetWithoutRdsConfigWhenRdsNotPresentedThenShouldReturnWithoutRdsConfig()},
                {"blueprints/configurations/superset/rds.handlebars", "configurations/superset/superset-with-rds.json",
                        supersetRdsConfigWhenRdsPresentedThenShouldReturnWithRdsConfig()},
                {"blueprints/configurations/superset/rds.handlebars", "configurations/superset/superset-without-rds.json",
                        supersetWithoutRdsConfigWhenRdsNotPresentedThenShouldReturnWithoutRdsConfig()},
                {"blueprints/configurations/hadoop/ldap.handlebars", "configurations/hadoop/hadoop-with-ldap.json",
                        ldapConfigWhenLdapPresentedThenShouldReturnWithLdapConfig()},
                {"blueprints/configurations/hadoop/ldap.handlebars", "configurations/hadoop/hadoop-without-ldap.json",
                        withoutLdapConfigWhenLdapNotPresentedThenShouldReturnWithoutLdapConfig()},
                {"blueprints/configurations/hadoop/global.handlebars", "configurations/hadoop/global.json",
                        objectWithoutEverything()},
                {"blueprints/configurations/hive/rds.handlebars", "configurations/hive/hive-with-rds.json",
                        hiveRdsConfigWhenRdsPresentedThenShouldReturnWithRdsConfig()},
                {"blueprints/configurations/hive/rds.handlebars", "configurations/hive/hive-without-rds.json",
                        objectWithoutEverything()},
                {"blueprints/configurations/llap/global.handlebars", "configurations/llap/global.json",
                        llapObjectWhenNodeCountPresented()},
                {"blueprints/configurations/nifi/global.handlebars", "configurations/nifi/global-with-hdf-nifitargets.json",
                        nifiConfigWhenHdfPresentedThenShouldReturnWithNifiConfig(false)},
                {"blueprints/configurations/nifi/global.handlebars", "configurations/nifi/global-with-hdf-full.json",
                        nifiConfigWhenHdfPresentedThenShouldReturnWithNifiConfig(true)},
                {"blueprints/configurations/nifi/global.handlebars", "configurations/nifi/global-without-hdf.json",
                        nifiConfigWhenHdfNotPresentedThenShouldReturnWithNotNifiConfig()},
                {"blueprints/configurations/ranger/global.handlebars", "configurations/ranger/global.json",
                        objectWithoutEverything()},
                {"blueprints/configurations/ranger/ldap.handlebars", "configurations/ranger/ranger-with-ldap.json",
                        ldapConfigWhenLdapPresentedThenShouldReturnWithLdapConfig()},
                {"blueprints/configurations/ranger_usersync/ldap.handlebars", "configurations/ranger/ranger-usersync-with-ldap.json",
                        ldapConfigWhenLdapPresentedThenShouldReturnWithLdapConfig()},
                {"blueprints/configurations/ranger/ldap.handlebars", "configurations/ranger/ranger-without-ldap.json",
                        withoutLdapConfigWhenLdapNotPresentedThenShouldReturnWithoutLdapConfig()},
                {"blueprints/configurations/ranger/rds.handlebars", "configurations/ranger/ranger-with-rds.json",
                        rangerRdsConfigWhenRdsPresentedThenShouldReturnWithRdsConfig()},
                {"blueprints/configurations/ranger/rds.handlebars", "configurations/ranger/ranger-with-oracle-rds.json",
                        rangerRdsConfigWhenOracleRdsPresentedThenShouldReturnWithRdsConfig()},
                {"blueprints/configurations/ranger/rds.handlebars", "configurations/ranger/ranger-without-rds.json",
                        objectWithoutEverything()},
                {"blueprints/configurations/beacon/rds.handlebars", "configurations/beacon/beacon-with-rds.json",
                        beaconWhenRdsPresentedThenShouldReturnWithRdsConfigs()},
                {"blueprints/configurations/beacon/rds.handlebars", "configurations/beacon/beacon-without-rds.json",
                        objectWithoutEverything()},
                {"blueprints/configurations/yarn/global.handlebars", "configurations/yarn/global-without-container.json",
                        objectContainerExecutorIsFalseThenShouldReturnWithoutContainerConfigs()},
                {"blueprints/configurations/yarn/global.handlebars", "configurations/yarn/global-with-container.json",
                        objectContainerExecutorIsTrueThenShouldReturnWithContainerConfigs()},
                {"blueprints/configurations/zeppelin/global.handlebars", "configurations/zeppelin/global-with-2_5.json",
                        zeppelinWhenStackVersionIs25ThenShouldReturnWithZeppelinEnvConfigs()},
                {"blueprints/configurations/zeppelin/global.handlebars", "configurations/zeppelin/global-without-2_5.json",
                        zeppelinWhenStackVersionIsNot25ThenShouldReturnWithZeppelinShiroIniConfigs()},
                {"blueprints/configurations/hive/ldap.handlebars", "configurations/hive/hive-with-ldap.json",
                        hiveWhenLdapPresentedThenShouldReturnWithLdapConfigs()},
                {"blueprints/configurations/hive/ldap.handlebars", "configurations/hive/hive-without-ldap.json",
                        hiveWhenLdapNotPresentedThenShouldReturnWithoutLdapConfigs()},
                {"blueprints/configurations/oozie/rds.handlebars", "configurations/oozie/oozie-with-rds.json",
                        oozieWhenRdsPresentedThenShouldReturnWithRdsConfigs()},
                {"blueprints/configurations/oozie/rds.handlebars", "configurations/oozie/oozie-without-rds.json",
                        objectWithoutEverything()},
                {"blueprints/configurations/webhcat/global.handlebars", "configurations/webhcat/webhcat.json",
                        objectWithoutEverything()},
                {"blueprints/configurations/druid/rds.handlebars", "configurations/druid/druid-without-rds.json",
                        druidWithoutRdsConfigWhenRdsNotPresentedThenShouldReturnWithoutRdsConfig()},
                {"blueprints/configurations/druid/rds.handlebars", "configurations/druid/druid-with-rds.json",
                        druidRdsConfigWhenRdsPresentedThenShouldReturnWithRdsConfig()},
                {"blueprints/configurations/ranger/shared_service.handlebars", "configurations/ranger/shared-service-datalake.json",
                        sSConfigWhenSSAndDatalakePresentedThenShouldReturnWithSSDatalakeConfig()},
                {"blueprints/configurations/ranger/shared_service.handlebars", "configurations/ranger/shared-service-no-datalake.json",
                        sSConfigWhenNoSSAndDatalakePresentedThenShouldReturnWithoutSSDatalakeConfig()},
                {"blueprints/configurations/hive/shared_service.handlebars", "configurations/hive/shared-service-datalake.json",
                        sSConfigWhenSSAndDatalakePresentedThenShouldReturnWithSSDatalakeConfig()},
                {"blueprints/configurations/hive/shared_service.handlebars", "configurations/hive/shared-service-attached.json",
                        sSConfigWhenNoSSAndDatalakePresentedThenShouldReturnWithoutSSDatalakeConfig()},
        });
    }

    @Test
    public void test() throws IOException {
        String actual = compileTemplate(input, model);
        String expected = readExpectedTemplate(output);
        String message = String.format("expected: [%s] %nactual: [%s] %n", expected, actual);

        Assert.assertEquals(message, expected, actual);
    }

    public static Map<String, Object> objectWithoutEverything() {
        GeneralClusterConfigs generalClusterConfigs = new GeneralClusterConfigs();
        generalClusterConfigs.setPassword("cloudbreak123!");

        return new BlueprintTemplateModelContextBuilder()
                .withGeneralClusterConfigs(generalClusterConfigs)
                .build();
    }

    public static Map<String, Object> oozieWhenRdsPresentedThenShouldReturnWithRdsConfigs() {
        return new BlueprintTemplateModelContextBuilder()
                .withRdsConfigs(Sets.newHashSet(TestUtil.rdsConfig(RdsType.OOZIE)))
                .build();
    }

    public static Map<String, Object> beaconWhenRdsPresentedThenShouldReturnWithRdsConfigs() {
        return new BlueprintTemplateModelContextBuilder()
                .withRdsConfigs(Sets.newHashSet(TestUtil.rdsConfig(RdsType.BEACON)))
                .build();
    }

    public static Map<String, Object> zeppelinWhenStackVersionIsNot25ThenShouldReturnWithZeppelinShiroIniConfigs() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("blueprints_basics_zeppelin_shiro_ini_content", "testshiroini");

        BlueprintView blueprintView = new BlueprintView("blueprintText", "2.6", "HDP");

        return new BlueprintTemplateModelContextBuilder()
                .withBlueprintView(blueprintView)
                .withCustomProperties(properties)
                .build();
    }

    public static Map<String, Object> zeppelinWhenStackVersionIs25ThenShouldReturnWithZeppelinEnvConfigs() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("blueprints_basics_zeppelin_shiro_ini_content", "testshiroini");

        BlueprintView blueprintView = new BlueprintView("blueprintText", "2.5", "HDP");

        return new BlueprintTemplateModelContextBuilder()
                .withBlueprintView(blueprintView)
                .withCustomProperties(properties)
                .build();
    }

    public static Map<String, Object> objectContainerExecutorIsTrueThenShouldReturnWithContainerConfigs() {
        GeneralClusterConfigs generalClusterConfigs = new GeneralClusterConfigs();
        generalClusterConfigs.setOrchestratorType(OrchestratorType.CONTAINER);

        return new BlueprintTemplateModelContextBuilder()
                .withGeneralClusterConfigs(generalClusterConfigs)
                .build();
    }

    public static Map<String, Object> objectContainerExecutorIsFalseThenShouldReturnWithoutContainerConfigs() {
        GeneralClusterConfigs generalClusterConfigs = new GeneralClusterConfigs();
        generalClusterConfigs.setOrchestratorType(OrchestratorType.HOST);

        return new BlueprintTemplateModelContextBuilder()
                .withGeneralClusterConfigs(generalClusterConfigs)
                .build();
    }

    public static Map<String, Object> llapObjectWhenNodeCountPresented() {
        GeneralClusterConfigs generalClusterConfigs = new GeneralClusterConfigs();
        generalClusterConfigs.setNodeCount(6);

        return new BlueprintTemplateModelContextBuilder()
                .withGeneralClusterConfigs(generalClusterConfigs)
                .build();
    }

    public static Map<String, Object> ldapConfigWhenLdapPresentedThenShouldReturnWithLdapConfig() throws JsonProcessingException {
        return new BlueprintTemplateModelContextBuilder()
                .withLdap(TestUtil.ldapConfig())
                .withGateway(TestUtil.gateway())
                .build();
    }

    public static Map<String, Object> withoutLdapConfigWhenLdapNotPresentedThenShouldReturnWithoutLdapConfig() {
        return new BlueprintTemplateModelContextBuilder()
                .build();
    }

    public static Map<String, Object> druidSupersetRdsConfigWhenRdsPresentedThenShouldReturnWithRdsConfig() {
        GeneralClusterConfigs generalClusterConfigs = new GeneralClusterConfigs();
        generalClusterConfigs.setPassword("adminPassword");
        generalClusterConfigs.setUserName("lastname");
        generalClusterConfigs.setIdentityUserEmail("admin@example.com");

        return new BlueprintTemplateModelContextBuilder()
                .withRdsConfigs(Sets.newHashSet(TestUtil.rdsConfig(RdsType.SUPERSET)))
                .withGeneralClusterConfigs(generalClusterConfigs)
                .build();
    }

    public static Map<String, Object> druidSupersetWithoutRdsConfigWhenRdsNotPresentedThenShouldReturnWithoutRdsConfig() {
        GeneralClusterConfigs generalClusterConfigs = new GeneralClusterConfigs();
        generalClusterConfigs.setPassword("adminPassword");
        generalClusterConfigs.setUserName("lastname");
        generalClusterConfigs.setIdentityUserEmail("admin@example.com");

        return new BlueprintTemplateModelContextBuilder()
                .withGeneralClusterConfigs(generalClusterConfigs)
                .build();
    }

    public static Map<String, Object> druidRdsConfigWhenRdsPresentedThenShouldReturnWithRdsConfig() {
        GeneralClusterConfigs generalClusterConfigs = new GeneralClusterConfigs();
        generalClusterConfigs.setPassword("adminPassword");
        generalClusterConfigs.setUserName("lastname");
        generalClusterConfigs.setIdentityUserEmail("admin@example.com");

        return new BlueprintTemplateModelContextBuilder()
                .withRdsConfigs(Sets.newHashSet(TestUtil.rdsConfig(RdsType.DRUID)))
                .withGeneralClusterConfigs(generalClusterConfigs)
                .build();
    }

    public static Map<String, Object> druidWithoutRdsConfigWhenRdsNotPresentedThenShouldReturnWithoutRdsConfig() {
        GeneralClusterConfigs generalClusterConfigs = new GeneralClusterConfigs();
        generalClusterConfigs.setPassword("adminPassword");
        generalClusterConfigs.setUserName("lastname");
        generalClusterConfigs.setIdentityUserEmail("admin@example.com");

        return new BlueprintTemplateModelContextBuilder()
                .withGeneralClusterConfigs(generalClusterConfigs)
                .build();
    }

    public static Map<String, Object> supersetRdsConfigWhenRdsPresentedThenShouldReturnWithRdsConfig() {
        GeneralClusterConfigs generalClusterConfigs = new GeneralClusterConfigs();
        generalClusterConfigs.setPassword("adminPassword");
        generalClusterConfigs.setUserName("lastname");
        generalClusterConfigs.setIdentityUserEmail("admin@example.com");

        return new BlueprintTemplateModelContextBuilder()
                .withRdsConfigs(Sets.newHashSet(TestUtil.rdsConfig(RdsType.SUPERSET)))
                .withGeneralClusterConfigs(generalClusterConfigs)
                .build();
    }

    public static Map<String, Object> supersetWithoutRdsConfigWhenRdsNotPresentedThenShouldReturnWithoutRdsConfig() {
        GeneralClusterConfigs generalClusterConfigs = new GeneralClusterConfigs();
        generalClusterConfigs.setPassword("adminPassword");
        generalClusterConfigs.setUserName("lastname");
        generalClusterConfigs.setIdentityUserEmail("admin@example.com");

        return new BlueprintTemplateModelContextBuilder()
                .withGeneralClusterConfigs(generalClusterConfigs)
                .build();
    }

    public static Map<String, Object> nifiConfigWhenHdfPresentedThenShouldReturnWithNifiConfig(boolean withProxyHost) {
        GeneralClusterConfigs generalClusterConfigs = new GeneralClusterConfigs();
        generalClusterConfigs.setPassword("adminPassword");
        generalClusterConfigs.setUserName("lastname");
        generalClusterConfigs.setIdentityUserEmail("admin@example.com");

        BlueprintView blueprintView = new BlueprintView("blueprintText", "2.6", "HDF");

        return new BlueprintTemplateModelContextBuilder()
                .withGeneralClusterConfigs(generalClusterConfigs)
                .withHdfConfigs(new HdfConfigs("nifigtargets", "nifigtargets",
                        withProxyHost ? Optional.of("nifiproxyhost") : Optional.empty()))
                .withBlueprintView(blueprintView)
                .build();
    }

    public static Map<String, Object> nifiConfigWhenHdfNotPresentedThenShouldReturnWithNotNifiConfig() {
        GeneralClusterConfigs generalClusterConfigs = new GeneralClusterConfigs();
        generalClusterConfigs.setPassword("adminPassword");
        generalClusterConfigs.setUserName("lastname");
        generalClusterConfigs.setIdentityUserEmail("admin@example.com");

        BlueprintView blueprintView = new BlueprintView("blueprintText", "2.6", "HDP");

        return new BlueprintTemplateModelContextBuilder()
                .withGeneralClusterConfigs(generalClusterConfigs)
                .withHdfConfigs(new HdfConfigs("nifigtargets", "nifigtargets", Optional.empty()))
                .withBlueprintView(blueprintView)
                .build();
    }

    public static Map<String, Object> hiveRdsConfigWhenRdsPresentedThenShouldReturnWithRdsConfig() {
        return new BlueprintTemplateModelContextBuilder()
                .withRdsConfigs(Sets.newHashSet(TestUtil.rdsConfig(RdsType.HIVE)))
                .build();
    }

    private static Object hiveWhenLdapPresentedThenShouldReturnWithLdapConfigs() {
        return new BlueprintTemplateModelContextBuilder()
                .withLdap(TestUtil.ldapConfig())
                .build();
    }

    private static Object hiveWhenLdapNotPresentedThenShouldReturnWithoutLdapConfigs() {
        return new BlueprintTemplateModelContextBuilder()
                .build();
    }

    public static Map<String, Object> rangerRdsConfigWhenRdsPresentedThenShouldReturnWithRdsConfig() throws JsonProcessingException {
        RDSConfig rdsConfig = TestUtil.rdsConfig(RdsType.RANGER);
        return new BlueprintTemplateModelContextBuilder()
                .withRdsConfigs(Sets.newHashSet(rdsConfig))
                .build();
    }

    public static Map<String, Object> sSConfigWhenSSAndDatalakePresentedThenShouldReturnWithSSDatalakeConfig() throws JsonProcessingException {
        return new BlueprintTemplateModelContextBuilder()
                .withSharedServiceConfigs(datalakeSharedServiceConfig().get())
                .build();
    }

    public static Map<String, Object> sSConfigWhenNoSSAndDatalakePresentedThenShouldReturnWithoutSSDatalakeConfig() throws JsonProcessingException {
        return new BlueprintTemplateModelContextBuilder()
                .withSharedServiceConfigs(attachedClusterSharedServiceConfig().get())
                .withCustomProperties(Maps.newHashMap("REMOTE_CLUSTER_NAME", "datalake-1"))
                .build();
    }

    public static Map<String, Object> rangerRdsConfigWhenOracleRdsPresentedThenShouldReturnWithRdsConfig() throws JsonProcessingException {
        RDSConfig rdsConfig = TestUtil.rdsConfig(RdsType.RANGER, DatabaseVendor.ORACLE11);

        // TODO we should somehow handle this
        //Map<String, String> attributes = new HashMap<>();
        //attributes.put("rangerAdminPassword", "rangerAdminPassword");
        //rdsConfig.setAttributes(new Json(attributes));

        return new BlueprintTemplateModelContextBuilder()
                .withRdsConfigs(Sets.newHashSet(rdsConfig))
                .build();
    }

    private String compileTemplate(String sourceFile, Map<String, Object> model) {
        String result;
        try {
            Template template = handlebars.compileInline(readSourceTemplate(sourceFile), "{{{", "}}}");
            result = template.apply(model);
        } catch (IOException e) {
            result = "";
        }
        return result;
    }

    private String readExpectedTemplate(String file) throws IOException {
        return readFileFromClasspath(String.format("handlebar/%s", file));
    }

    private String readSourceTemplate(String file) throws IOException {
        return readFileFromClasspath(String.format("%s", file));
    }

    private static Optional<SharedServiceConfigsView> datalakeSharedServiceConfig() {
        SharedServiceConfigsView sharedServiceConfigsView = new SharedServiceConfigsView();
        sharedServiceConfigsView.setDatalakeCluster(true);
        return Optional.of(sharedServiceConfigsView);
    }

    private static Optional<SharedServiceConfigsView> attachedClusterSharedServiceConfig() {
        SharedServiceConfigsView sharedServiceConfigsView = new SharedServiceConfigsView();
        sharedServiceConfigsView.setAttachedCluster(true);
        sharedServiceConfigsView.setRangerAdminPassword("cloudbreak123!");
        return Optional.of(sharedServiceConfigsView);
    }
}
