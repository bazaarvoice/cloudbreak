package com.sequenceiq.cloudbreak.converter;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.sequenceiq.cloudbreak.TestUtil;
import com.sequenceiq.cloudbreak.api.model.RecipeResponse;
import com.sequenceiq.cloudbreak.domain.Recipe;

public class RecipeToJsonConverterTest extends AbstractEntityConverterTest<Recipe> {

    private RecipeToJsonConverter underTest;

    @Before
    public void setUp() {
        underTest = new RecipeToJsonConverter();
    }

    @Test
    public void testConvert() {
        // GIVEN
        // WHEN
        RecipeResponse result = underTest.convert(getSource());
        // THEN
        assertAllFieldsNotNull(result, Collections.singletonList("id"));
    }

    @Override
    public Recipe createSource() {
        return TestUtil.recipes(1).iterator().next();
    }
}
