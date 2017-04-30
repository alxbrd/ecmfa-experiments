package uk.ac.kcl.mdeoptimiser.gcm2017

import com.google.inject.Inject
import com.google.inject.Provider
import java.util.Collections
import java.util.LinkedList
import java.util.Random
import java.util.Stack
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet

import static org.dynemf.ResourceSetWrapper.rset

class ModelLoadHelper {
	@Inject
	private Provider<ResourceSet> resourceSetProvider

	private ResourceSet resSet
	public EPackage metamodel = null
	public Resource resource = null

	def getResourceSet() {
		if (resSet == null) {
			resSet = resourceSetProvider.get()
		}
		resSet
	}

	def loadModel(String path) {
		
		resource = resourceSet.getResource(URI.createURI(path), true)
		resource.contents.head
	}

	def loadModelAndRandomAssign(String path) {
		
		resource = resSet.getResource(URI.createURI(path), true)
		loadModelAndRandomAssign(resource.contents.head, path)
	}

	def loadModelAndRandomAssign(EObject object, String modelPath){
		
		var rset = rset().register(metamodel.nsURI, metamodel);

		var r = rset.open(modelPath);

		var cra = rset.ePackage(metamodel.nsURI);
		
		var features = r.root().property("features").asList();
		
		for(var int i = 0; i < features.result().size(); i++){
			
			//Add one class for each of the features
			//We ignore adding a name at this time
			r.root(0).add("classes", cra.create("Class"));
		}
		
		//Randomly assign features to classes
	
		var	featuresStack = new Stack<EObject>();
		featuresStack.addAll(features.result());
		
		
		while(!featuresStack.isEmpty()){
			
			var nextFeature = new LinkedList<EObject>();
			nextFeature.add(featuresStack.pop());
			
			r.root().property("classes").asList().at(new Random().nextInt(features.result().size()-1))
				.set("encapsulates", nextFeature);
		}
				
		return r.root.result
		
	}

	def writeModel(EObject model, String path) {
		val resource = resourceSet.createResource(URI.createURI(path))
		if (resource.loaded) {
			resource.contents.clear
		}
		resource.contents.add(model)
		resource.save(Collections.EMPTY_MAP)
	}

	def storeModel(EObject model, String pathPrefix) {
		model.writeModel(pathPrefix + "/" + String.format("%08X", model.hashCode) + ".xmi")
	}

	def registerPackage(EPackage metamodel) {
		
		this.metamodel = metamodel
		
		resourceSet.packageRegistry.put(metamodel.nsURI, metamodel)
	}
}
