package uk.ac.kcl.mdeoptimiser.ecmfa2017

import com.google.inject.Inject
import com.google.inject.Injector
import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedList
import org.eclipse.emf.ecore.EObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors
import uk.ac.kcl.MDEOptimiseStandaloneSetup
import uk.ac.kcl.mdeoptimise.Optimisation
import uk.ac.kcl.interpreter.OptimisationInterpreter
import org.sidiff.common.logging.LogUtil

class RunOptimisation {

	static val Injector injector = new MDEOptimiseStandaloneSetup().createInjectorAndDoEMFRegistration();
	
	def static void main(String[] args) {
		val app = injector.getInstance(RunOptimisation)
		
		LogUtil.logEvents = "MESSAGE,WARNING,ERROR"
		
		if (args.empty) {
			// Run all experiments
			app.run()
		} else {
			// Expect two numerical arguments specifying the index of the spec and the index of the model, resp.
			val specIdx = Integer.parseInt(args.get(0))
			val modelIdx = Integer.parseInt(args.get(1))
			
			val batchStartTime = new SimpleDateFormat("yyMMdd-HHmmss").format(new Date())
		
			app.runBatchForSpecAndModel(optSpecs.get(specIdx), inputModels.get(modelIdx), batchStartTime, 0)
		}
	}

	@Inject
	private ModelLoadHelper modelLoader

	private static class ResultRecord {
		public double timeTaken
		public double maxCRA
		public long bestModelHashCode
		public boolean hasUnassignedFeatures
		public String bestModelPath
	}

	private static class InputModelDesc {
		public String modelName
		public int generations
		public int populationSize

		new(String modelName, int generations, int populationSize) {
			this.modelName = modelName
			this.generations = generations
			this.populationSize = populationSize
		}
	}

	/*
	 * Defining the experiments
	 */
	static val optSpecs = #["cra"]
	static val inputModels = #[
		new InputModelDesc("TTC_InputRDG_A", 100, 50), 
		new InputModelDesc("TTC_InputRDG_B", 100, 50),
		new InputModelDesc("TTC_InputRDG_C", 100, 50),
		new InputModelDesc("TTC_InputRDG_D", 100, 50),
		new InputModelDesc("TTC_InputRDG_E", 100, 50),
		
		new InputModelDesc("TTC_InputRDG_A", 100, 100),
		new InputModelDesc("TTC_InputRDG_B", 100, 100),
 		new InputModelDesc("TTC_InputRDG_C", 100, 100),
		new InputModelDesc("TTC_InputRDG_D", 100, 100),
		new InputModelDesc("TTC_InputRDG_E", 100, 100),
			
		new InputModelDesc("TTC_InputRDG_A", 1000, 100),
		new InputModelDesc("TTC_InputRDG_B", 1000, 100),
		new InputModelDesc("TTC_InputRDG_C", 1000, 100),
		new InputModelDesc("TTC_InputRDG_D", 1000, 100),
		new InputModelDesc("TTC_InputRDG_E", 1000, 100)
		
		]

	/**
	 * Run all experiments
	 */
	def run() {

		val batchStartTime = new SimpleDateFormat("yyMMdd-HHmmss").format(new Date())
		
		optSpecs.forEach [ optSpec |
			inputModels.forEach [ inputDesc, index |
				runBatchForSpecAndModel(optSpec, inputDesc, batchStartTime, index)
			]
		]
	}

	/**
	 * Run a batch of experiments for the given spec and model, recording overall outcomes in a separate file.
	 */
	def runBatchForSpecAndModel(String optSpec, InputModelDesc inputDesc, String batchStartTime, int batchId) {
		val lResults = new LinkedList<ResultRecord>()

		(0 ..< 10).forEach [ idx |
			lResults.add(runOneExperiment(optSpec, inputDesc, batchStartTime, batchId, idx))
		]

		// Write averaged results for this specification and model
		val File f = new File(
			"gen/models/ttc/" + optSpec + "/" + batchStartTime + "/" + inputDesc.modelName + "-configuration-" + batchId + "/overall_results.txt")
		val PrintWriter pw = new PrintWriter(f)
		pw.println("Overall results for this experiment")
		pw.println("===================================")
		pw.println
		pw.printf("Experiment with spec \"%s\" and model \"%s\".\n", optSpec, inputDesc.modelName)
		pw.printf("Running for %01d generations with a population size of %01d.\n", inputDesc.generations,
			inputDesc.populationSize)
		pw.println
		pw.printf("Average time taken: %02f milliseconds.\n",
			lResults.fold(0.0, [acc, r|acc + r.timeTaken]) / lResults.size)
		val bestResult = lResults.maxBy[maxCRA]
		
		if(bestResult.bestModelPath == null){
		
			pw.printf("No valid solutions found for this experiment.")
		
		} else {
			
			pw.printf("Best CRA was %s for model with hash code %08X. This model was %s.\n", bestResult.maxCRA,
				bestResult.bestModelHashCode, (if (bestResult.hasUnassignedFeatures) {
					"invalid"
				} else {
					"valid"
				}))	

			pw.println
			pw.println("Evaluation: CRAIndexCalculator.jar")
			pw.println("===================================")
			pw.println
			pw.printf("Model path: %s\n", bestResult.bestModelPath)
			pw.print(runEvaluationJarAgainstBestModel(bestResult.bestModelPath))

		}
		
		pw.close
		
	}
	
	def String runEvaluationJarAgainstBestModel(String modelPath) {
		
		var evaluatorJar = Runtime.getRuntime().exec("java -jar evaluation/CRAIndexCalculator.jar " + modelPath)
		
		var output = new BufferedReader(new InputStreamReader(evaluatorJar.getInputStream())).lines().parallel().collect(Collectors.joining("\n"))
		
		if(output.length == 0){
			output += new BufferedReader(new InputStreamReader(evaluatorJar.getErrorStream())).lines().parallel().collect(Collectors.joining("\n"))
		}
		
		output
	}
	
	/**
	 * Run a single experiment and record its outcomes
	 */
	def ResultRecord runOneExperiment(String optSpecName, InputModelDesc inputDesc, String batchStartTime, int batchId, int runIdx) {
		System.out.printf("Starting %01dth experiment run for specification \"%s\" with input model \"%s\".\n", runIdx,
			optSpecName, inputDesc.modelName)

		val pathPrefix = "gen/models/ttc/" + optSpecName + "/" + batchStartTime + "/" + inputDesc.modelName + "-configuration-" + batchId + "/" + runIdx
			

		val serializedRulesPrefix = pathPrefix + "/rules/"

		val model = modelLoader.loadModel("src/uk/ac/kcl/mdeoptimiser/ecmfa2017/opt_specs/" + optSpecName +
			".mopt") as Optimisation
		
		//Run modify the model to run with the given experiment configuration
		model.optimisation.algorithmEvolutions = inputDesc.generations * inputDesc.populationSize
		model.optimisation.algorithmPopulation = inputDesc.populationSize

		val modelProvider = injector.getInstance(CRAModelProvider)
		modelProvider.setInputModelName(inputDesc.modelName)

		// Start measuring time
		val startTime = System.nanoTime

		val interpreter = new OptimisationInterpreter(model, modelProvider, serializedRulesPrefix)
		val optimiserOutcome = interpreter.execute()

		// End time measurement
		val endTime = System.nanoTime
		val totalTime = endTime - startTime

		// Store result models
	     optimiserOutcome
				.forEach[m | modelProvider.storeModelAndInfo(m, pathPrefix + "/final")]

		// Output results
		val results = new ResultRecord
		val craComputer = new MaximiseCRA
		val featureCounter = new MinimiseClasslessFeatures

		results.timeTaken = totalTime / 1000000

		val sortedResults = optimiserOutcome.toList().filter [ m | featureCounter.computeFitness(m) == 0].map [ m |
			new Pair<EObject, Double>(m, craComputer.computeFitness(m) * -1)].sortBy[-value]

		if (sortedResults.empty) {
			println("No valid results for this run")
		} else {
			results.bestModelHashCode = sortedResults.head.hashCode
			results.maxCRA = sortedResults.head.value
			results.hasUnassignedFeatures = false
			results.bestModelPath = sortedResults.head.key.eResource.URI.toString
			
			val fResults = new File(pathPrefix + "/final/results.txt")
			val pw = new PrintWriter(fResults)
			
			pw.printf(
				"Experiment using spec \"%s\" and model \"%s\". Running for %01d generations with a population size of %01d.\n\n",
				optSpecName, inputDesc.modelName, inputDesc.generations, inputDesc.populationSize)
			pw.printf("Total time taken for this experiment: %02f milliseconds.\n", results.timeTaken)
			sortedResults.forEach [ p |
				System.out.printf("Result model %08X at CRA %02f.\n", p.key.hashCode, p.value)
				pw.printf("Result model %08X at CRA %02f.\n", p.key.hashCode, p.value)
			]
			pw.close
			
		}

		return results
	}

	def getFeature(EObject o, String feature) {
		o.eGet(o.eClass.getEStructuralFeature(feature))
	}

	def setFeature(EObject o, String feature, Object value) {
		o.eSet(o.eClass.getEStructuralFeature(feature), value)
	}
}
