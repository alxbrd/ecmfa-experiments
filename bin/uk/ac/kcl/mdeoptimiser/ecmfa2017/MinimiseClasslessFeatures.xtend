package uk.ac.kcl.mdeoptimiser.ecmfa2017

import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject

class MinimiseClasslessFeatures extends AbstractModelQueryFitnessFunction {
	
	override computeFitness(EObject model) {
		var fitness = (model.getFeature("features") as EList<EObject>).filter[feature | feature.getFeature("isEncapsulatedBy") == null].size;
		return fitness;
	}
	
	override getName() {
		return "Mimise classless features"
	}
	
}