package uk.ac.kcl.mdeoptimiser.ecmfa2017

import uk.ac.kcl.interpreter.IModelProvider
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EObject
import java.util.Set
import com.google.inject.Inject
import java.util.Iterator

class CRAModelProvider implements IModelProvider {

	@Inject
	private ModelLoadHelper modelLoader

	private String inputModelName
	
	override initialModels(EPackage metamodel) {
		modelLoader.registerPackage(metamodel)

		#[modelLoader.loadModel("src/uk/ac/kcl/mdeoptimiser/ecmfa2017/models/" + inputModelName + ".xmi")].iterator
	}
	
	def storeModels(Iterator<EObject> objects, String path) {
		modelLoader.storeModels (objects, path)
	}
	
	def setInputModelName(String inputModelName) {
		this.inputModelName = inputModelName
	}
	
}