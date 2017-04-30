package uk.ac.kcl.mdeoptimiser.gcm2017

import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject

class MinimiseClasslessFeatures extends uk.ac.kcl.mdeoptimiser.gcm2017.AbstractModelQueryFitnessFunction {
	
	override computeFitness(EObject model) {
		var fitness = (model.getFeature("features") as EList<EObject>).filter[feature | feature.getFeature("isEncapsulatedBy") == null].size;
		//println("Classless features: " + fitness)
		return fitness;
	}
	
	override getName() {
		return "Mimise classless features"
	}
	
}
	