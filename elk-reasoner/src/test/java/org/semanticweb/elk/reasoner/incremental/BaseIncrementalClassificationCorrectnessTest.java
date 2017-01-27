/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2012 Department of Computer Science, University of Oxford
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.semanticweb.elk.reasoner.incremental;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

import org.junit.runner.RunWith;
import org.semanticweb.elk.reasoner.SimpleManifestCreator;
import org.semanticweb.elk.reasoner.TaxonomyTestOutput;
import org.semanticweb.elk.reasoner.taxonomy.TaxonomyPrinter;
import org.semanticweb.elk.reasoner.taxonomy.hashing.TaxonomyHasher;
import org.semanticweb.elk.reasoner.taxonomy.model.Taxonomy;
import org.semanticweb.elk.testing.ConfigurationUtils;
import org.semanticweb.elk.testing.PolySuite;
import org.semanticweb.elk.testing.PolySuite.Config;
import org.semanticweb.elk.testing.PolySuite.Configuration;
import org.semanticweb.elk.testing.TestManifest;
import org.semanticweb.elk.testing.UrlTestInput;

/**
 * Implements the correctness check based on comparing expected and obtained
 * class taxonomies. Subclasses still need to provide methods to load changes
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 * @author Peter Skocovsky
 */
@RunWith(PolySuite.class)
public abstract class BaseIncrementalClassificationCorrectnessTest<A> extends
		IncrementalReasoningCorrectnessTestWithInterrupts<UrlTestInput, A, TaxonomyTestOutput<?>, TaxonomyTestOutput<?>, IncrementalReasoningTestWithInterruptsDelegate<A, TaxonomyTestOutput<?>, TaxonomyTestOutput<?>>> {

	final static String INPUT_DATA_LOCATION = "classification_test_input";

	public BaseIncrementalClassificationCorrectnessTest(
			final TestManifest<UrlTestInput> testManifest,
			final IncrementalReasoningTestWithInterruptsDelegate<A, TaxonomyTestOutput<?>, TaxonomyTestOutput<?>> testDelegate) {
		super(testManifest, testDelegate);
	}

	@Override
	protected void correctnessCheck(final TaxonomyTestOutput<?> actualOutput,
			final TaxonomyTestOutput<?> expectedOutput) throws Exception {

		final Taxonomy<?> expected = expectedOutput.getTaxonomy();

		final Taxonomy<?> incremental = actualOutput.getTaxonomy();

		if (TaxonomyHasher.hash(expected) != TaxonomyHasher.hash(incremental)
				|| !expected.equals(incremental)) {
			StringWriter writer = new StringWriter();

			try {
				writer.write("EXPECTED TAXONOMY:\n");
				TaxonomyPrinter.dumpTaxomomy(expected, writer, false);
				writer.write("INCREMENTAL TAXONOMY:\n");
				TaxonomyPrinter.dumpTaxomomy(incremental, writer, false);
				writer.flush();
			} catch (IOException ioe) {
				// TODO
			}

			fail(writer.getBuffer().toString());
		}

	}

	@Config
	public static Configuration getConfig()
			throws URISyntaxException, IOException {
		return ConfigurationUtils.loadFileBasedTestConfiguration(
				INPUT_DATA_LOCATION,
				IncrementalClassificationCorrectnessTest.class,
				SimpleManifestCreator.INSTANCE, "owl");
	}

}
