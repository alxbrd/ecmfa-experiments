package uk.ac.kcl.mdeoptimiser.gcm2017

import uk.ac.kcl.interpreter.IModelProvider
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EObject
import com.google.inject.Inject
import org.eclipse.emf.ecore.resource.ResourceSet
import java.util.Collections
import java.io.File
import java.io.PrintWriter
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import java.io.BufferedReader
import java.io.InputStreamReader
import org.eclipse.emf.common.util.URI
import java.util.stream.Collectors
import java.util.List

class CRAModelProvider implements IModelProvider {

	@Inject
	private uk.ac.kcl.mdeoptimiser.gcm2017.ModelLoadHelper modelLoader

	private String inputModelName
	
	val ResourceSet resourceSet = new ResourceSetImpl
		
	override initialModels(EPackage metamodel) {
		modelLoader.registerPackage(metamodel)
		
		if(inputModelName.startsWith("Random")){
					
			var model = modelLoader.loadModelAndRandomAssign("src/uk/ac/kcl/mdeoptimiser/gcm2017/models/" 
				+ inputModelName.subSequence(7, inputModelName.length) + ".xmi"
			)
	
			return #[model].iterator
		} 
		
		var model = modelLoader.loadModel("src/uk/ac/kcl/mdeoptimiser/gcm2017/models/" + inputModelName + ".xmi")
		#[model].iterator

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

	def storeModelAndInfo(EObject model, String pathPrefix) {
		
		storeModel(model, pathPrefix)
		val info = runEvaluationJarAgainstBestModel(pathPrefix + "/" + String.format("%08X", model.hashCode) + ".xmi")
		
		var File f = new File(pathPrefix + "/" + String.format("%08X", model.hashCode) + ".txt")
	
		val PrintWriter pw = new PrintWriter(f)
		pw.println("Initial model: " + this.inputModelName)
		pw.println("Evaluation output: ")
		pw.println(info)
		pw.close
		System.out.println(info)
		
	}
	
	def String runEvaluationJarAgainstBestModel(String modelPath) {
		
		var evaluatorJar = Runtime.getRuntime().exec("java -jar evaluation/CRAIndexCalculator.jar " + modelPath)
		
		var output = new BufferedReader(new InputStreamReader(evaluatorJar.getInputStream())).lines().parallel().collect(Collectors.joining("\n"))
		
		if(output.length == 0){
			output += new BufferedReader(new InputStreamReader(evaluatorJar.getErrorStream())).lines().parallel().collect(Collectors.joining("\n"))
		}
		
		output
	}

	def storeModels(List<EObject> models, String pathPrefix) {
		models.forEach[m|m.storeModel(pathPrefix)]
	}
	
	def setInputModelName(String inputModelName) {
		this.inputModelName = inputModelName
	}
}
