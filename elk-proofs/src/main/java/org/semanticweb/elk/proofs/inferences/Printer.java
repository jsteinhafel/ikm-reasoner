/**
 * 
 */
package org.semanticweb.elk.proofs.inferences;

import org.semanticweb.elk.proofs.inferences.classes.ClassInitialization;
import org.semanticweb.elk.proofs.inferences.classes.ClassSubsumption;
import org.semanticweb.elk.proofs.inferences.classes.ConjunctionComposition;
import org.semanticweb.elk.proofs.inferences.classes.ConjunctionDecomposition;
import org.semanticweb.elk.proofs.inferences.classes.DisjointnessContradiction;
import org.semanticweb.elk.proofs.inferences.classes.DisjunctionComposition;
import org.semanticweb.elk.proofs.inferences.classes.ExistentialComposition;
import org.semanticweb.elk.proofs.inferences.classes.ExistentialCompositionViaChain;
import org.semanticweb.elk.proofs.inferences.classes.InconsistentDisjointness;
import org.semanticweb.elk.proofs.inferences.classes.NegationContradiction;
import org.semanticweb.elk.proofs.inferences.classes.ReflexiveExistentialComposition;
import org.semanticweb.elk.proofs.inferences.classes.ThingInitialization;
import org.semanticweb.elk.proofs.inferences.properties.ChainSubsumption;
import org.semanticweb.elk.proofs.inferences.properties.ReflexiveComposition;
import org.semanticweb.elk.proofs.inferences.properties.ReflexivityViaSubsumption;
import org.semanticweb.elk.proofs.inferences.properties.SubsumptionViaReflexivity;
import org.semanticweb.elk.proofs.inferences.properties.ToldReflexivity;

/**
 * Prints inferences
 * 
 * @author Pavel Klinov
 *
 * pavel.klinov@uni-ulm.de
 */
public class Printer {

	public static String print(Inference inference) {
		return inference.accept(new PrintingVisitor(), null);
	}
	
	private static class PrintingVisitor implements InferenceVisitor<Void, String> {

		private String defaultVisit(Inference inf, String rulePrefix) {
			if (inf.getSideCondition() == null) {
				return String.format("%s( %s ) |- %s", rulePrefix, inf.getPremises(), inf.getConclusion());
			}
			else {
				return String.format("%s( %s ) : %s |- %s", rulePrefix, inf.getPremises(), inf.getSideCondition(), inf.getConclusion());
			}
		}
		
		@Override
		public String visit(ClassInitialization inf, Void input) {
			return String.format("Init( %s )", inf.getConclusion());
		}

		@Override
		public String visit(ThingInitialization inf, Void input) {
			return String.format("Init( %s )", inf.getConclusion());
		}

		@Override
		public String visit(ClassSubsumption inf, Void input) {
			return defaultVisit(inf, "R_sub");
		}

		@Override
		public String visit(ConjunctionComposition inf, Void input) {
			
			return defaultVisit(inf, "R_and+");
		}

		@Override
		public String visit(ConjunctionDecomposition inf, Void input) {
			
			return defaultVisit(inf, "R_and-");
		}

		@Override
		public String visit(DisjointnessContradiction inf, Void input) {
			
			return defaultVisit(inf, "R_disj_bot");
		}

		@Override
		public String visit(DisjunctionComposition inf, Void input) {
			
			return defaultVisit(inf, "R_or+");
		}

		@Override
		public String visit(ExistentialComposition inf, Void input) {
			
			return defaultVisit(inf, "R_exists+");
		}

		@Override
		public String visit(ExistentialCompositionViaChain inf, Void input) {
			
			return defaultVisit(inf, "R_chain");
		}

		@Override
		public String visit(InconsistentDisjointness inf, Void input) {
			
			return defaultVisit(inf, "R_disj_inc");
		}

		@Override
		public String visit(NegationContradiction inf, Void input) {
			
			return defaultVisit(inf, "R_neg_bot");
		}

		@Override
		public String visit(ReflexiveExistentialComposition inf, Void input) {
			
			return defaultVisit(inf, "R_exists_reflex");
		}

		@Override
		public String visit(ChainSubsumption inf, Void input) {
			
			return defaultVisit(inf, "R_role_sub");
		}

		@Override
		public String visit(ReflexiveComposition inf, Void input) {
			
			return defaultVisit(inf, "R_reflex_chain");
		}

		@Override
		public String visit(ReflexivityViaSubsumption inf, Void input) {
			
			return defaultVisit(inf, "R_reflex_via_sub");
		}

		@Override
		public String visit(SubsumptionViaReflexivity inf, Void input) {
			
			return defaultVisit(inf, "R_sub_via_reflex");
		}

		@Override
		public String visit(ToldReflexivity inf, Void input) {
			
			return defaultVisit(inf, "R_reflex");
		}
		
	}
}
