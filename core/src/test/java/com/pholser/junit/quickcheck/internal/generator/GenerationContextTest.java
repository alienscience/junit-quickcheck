package com.pholser.junit.quickcheck.internal.generator;

import static java.util.Arrays.*;

import com.pholser.junit.quickcheck.ForAll;
import com.pholser.junit.quickcheck.SuchThat;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.internal.ParameterContext;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.rules.ExpectedException.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GenerationContextTest {
    @Rule public final ExpectedException thrown = none();

    @Mock private SourceOfRandomness random;
    @Mock private Generator<Integer> generator;
    @Mock private ForAll quantifier;
    @Mock private SuchThat constraint;

    @SuppressWarnings("unchecked")
    @Before public void beforeEach() {
        when(quantifier.sampleSize()).thenReturn(20);
        when(quantifier.discardRatio()).thenReturn(3);
        when(generator.types()).thenReturn(asList(int.class));
        when(generator.generate(same(random), any(GenerationStatus.class))).thenReturn(10).thenReturn(9).thenReturn(8)
            .thenReturn(7).thenReturn(6).thenReturn(5).thenReturn(4).thenReturn(3).thenReturn(2).thenReturn(1)
            .thenReturn(0);
        when(constraint.value()).thenReturn("#x > 0");
    }

    @Test public void whenDiscardRatioExceededEvenWithSomeSuccesses() {
        ParameterContext parameter = new ParameterContext(int.class, "x");
        parameter.addQuantifier(quantifier);
        parameter.addConstraint(constraint);

        GenerationContext generation =
            new GenerationContext(parameter, new GeneratorRepository(random).register(generator));

        thrown.expect(GenerationContext.DiscardRatioExceededException.class);
        thrown.expectMessage(String.format(GenerationContext.DiscardRatioExceededException.MESSAGE_TEMPLATE,
            parameter.parameterName(), parameter.discardRatio(), 30, 10, 3D));

        while (generation.shouldContinue())
            generation.generate(random);
    }
}
