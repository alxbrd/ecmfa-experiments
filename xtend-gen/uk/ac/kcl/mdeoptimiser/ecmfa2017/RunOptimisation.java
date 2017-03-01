package uk.ac.kcl.mdeoptimiser.ecmfa2017;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.ExclusiveRange;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import uk.ac.kcl.MDEOptimiseStandaloneSetup;
import uk.ac.kcl.interpreter.OptimisationInterpreter;
import uk.ac.kcl.mdeoptimise.Optimisation;
import uk.ac.kcl.mdeoptimiser.ecmfa2017.CRAModelProvider;
import uk.ac.kcl.mdeoptimiser.ecmfa2017.MaximiseCRA;
import uk.ac.kcl.mdeoptimiser.ecmfa2017.MinimiseClasslessFeatures;
import uk.ac.kcl.mdeoptimiser.ecmfa2017.ModelLoadHelper;

@SuppressWarnings("all")
public class RunOptimisation {
  private static class ResultRecord {
    public double timeTaken;
    
    public double maxCRA;
    
    public long bestModelHashCode;
    
    public boolean hasUnassignedFeatures;
    
    public String bestModelPath;
  }
  
  private static class InputModelDesc {
    public String modelName;
    
    public int generations;
    
    public int populationSize;
    
    public InputModelDesc(final String modelName, final int generations, final int populationSize) {
      this.modelName = modelName;
      this.generations = generations;
      this.populationSize = populationSize;
    }
  }
  
  private final static Injector injector = new MDEOptimiseStandaloneSetup().createInjectorAndDoEMFRegistration();
  
  public static void main(final String[] args) {
    final RunOptimisation app = RunOptimisation.injector.<RunOptimisation>getInstance(RunOptimisation.class);
    boolean _isEmpty = ((List<String>)Conversions.doWrapArray(args)).isEmpty();
    if (_isEmpty) {
      app.run();
    } else {
      String _get = args[0];
      final int specIdx = Integer.parseInt(_get);
      String _get_1 = args[1];
      final int modelIdx = Integer.parseInt(_get_1);
      String _get_2 = RunOptimisation.optSpecs.get(specIdx);
      RunOptimisation.InputModelDesc _get_3 = RunOptimisation.inputModels.get(modelIdx);
      app.runBatchForSpecAndModel(_get_2, _get_3);
    }
  }
  
  @Inject
  private ModelLoadHelper modelLoader;
  
  /**
   * Defining the experiments
   */
  private final static List<String> optSpecs = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("cra"));
  
  private final static List<RunOptimisation.InputModelDesc> inputModels = Collections.<RunOptimisation.InputModelDesc>unmodifiableList(CollectionLiterals.<RunOptimisation.InputModelDesc>newArrayList(new RunOptimisation.InputModelDesc("TTC_InputRDG_A", 1000, 50), new RunOptimisation.InputModelDesc("TTC_InputRDG_B", 1000, 50), new RunOptimisation.InputModelDesc("TTC_InputRDG_C", 1000, 50), new RunOptimisation.InputModelDesc("TTC_InputRDG_D", 1000, 50), new RunOptimisation.InputModelDesc("TTC_InputRDG_E", 1000, 50), new RunOptimisation.InputModelDesc("TTC_InputRDG_A", 1000, 100), new RunOptimisation.InputModelDesc("TTC_InputRDG_B", 1000, 100), new RunOptimisation.InputModelDesc("TTC_InputRDG_C", 1000, 100), new RunOptimisation.InputModelDesc("TTC_InputRDG_D", 1000, 100), new RunOptimisation.InputModelDesc("TTC_InputRDG_E", 1000, 100), new RunOptimisation.InputModelDesc("TTC_InputRDG_A", 100, 100), new RunOptimisation.InputModelDesc("TTC_InputRDG_B", 100, 100), new RunOptimisation.InputModelDesc("TTC_InputRDG_C", 100, 100), new RunOptimisation.InputModelDesc("TTC_InputRDG_D", 100, 100), new RunOptimisation.InputModelDesc("TTC_InputRDG_E", 100, 100), new RunOptimisation.InputModelDesc("TTC_InputRDG_A", 50, 20), new RunOptimisation.InputModelDesc("TTC_InputRDG_B", 50, 20), new RunOptimisation.InputModelDesc("TTC_InputRDG_C", 50, 20), new RunOptimisation.InputModelDesc("TTC_InputRDG_D", 50, 20), new RunOptimisation.InputModelDesc("TTC_InputRDG_E", 50, 20)));
  
  /**
   * Run all experiments
   */
  public void run() {
    final Consumer<String> _function = (String optSpec) -> {
      final Consumer<RunOptimisation.InputModelDesc> _function_1 = (RunOptimisation.InputModelDesc inputDesc) -> {
        this.runBatchForSpecAndModel(optSpec, inputDesc);
      };
      RunOptimisation.inputModels.forEach(_function_1);
    };
    RunOptimisation.optSpecs.forEach(_function);
  }
  
  /**
   * Run a batch of experiments for the given spec and model, recording overall outcomes in a separate file.
   */
  public void runBatchForSpecAndModel(final String optSpec, final RunOptimisation.InputModelDesc inputDesc) {
    try {
      final LinkedList<RunOptimisation.ResultRecord> lResults = new LinkedList<RunOptimisation.ResultRecord>();
      ExclusiveRange _doubleDotLessThan = new ExclusiveRange(0, 10, true);
      final Consumer<Integer> _function = (Integer idx) -> {
        RunOptimisation.ResultRecord _runOneExperiment = this.runOneExperiment(optSpec, inputDesc, (idx).intValue());
        lResults.add(_runOneExperiment);
      };
      _doubleDotLessThan.forEach(_function);
      SimpleDateFormat _simpleDateFormat = new SimpleDateFormat("yyMMdd-HHmmss");
      Date _date = new Date();
      String _format = _simpleDateFormat.format(_date);
      String _plus = ((((("gen/models/ttc/" + optSpec) + "/") + inputDesc.modelName) + "/overall_results") + _format);
      String _plus_1 = (_plus + ".txt");
      final File f = new File(_plus_1);
      final PrintWriter pw = new PrintWriter(f);
      pw.println("Overall results for this experiment");
      pw.println("===================================");
      pw.println();
      pw.printf("Experiment with spec \"%s\" and model \"%s\".\n", optSpec, inputDesc.modelName);
      pw.printf("Running for %01d generations with a population size of %01d.\n", Integer.valueOf(inputDesc.generations), 
        Integer.valueOf(inputDesc.populationSize));
      pw.println();
      final Function2<Double, RunOptimisation.ResultRecord, Double> _function_1 = (Double acc, RunOptimisation.ResultRecord r) -> {
        return Double.valueOf(((acc).doubleValue() + r.timeTaken));
      };
      Double _fold = IterableExtensions.<RunOptimisation.ResultRecord, Double>fold(lResults, Double.valueOf(0.0), _function_1);
      int _size = lResults.size();
      double _divide = ((_fold).doubleValue() / _size);
      pw.printf("Average time taken: %02f milliseconds.\n", Double.valueOf(_divide));
      final Function1<RunOptimisation.ResultRecord, Double> _function_2 = (RunOptimisation.ResultRecord it) -> {
        return Double.valueOf(it.maxCRA);
      };
      final RunOptimisation.ResultRecord bestResult = IterableExtensions.<RunOptimisation.ResultRecord, Double>maxBy(lResults, _function_2);
      String _xifexpression = null;
      if (bestResult.hasUnassignedFeatures) {
        _xifexpression = "invalid";
      } else {
        _xifexpression = "valid";
      }
      pw.printf("Best CRA was %s for model with hash code %08X. This model was %s.\n", Double.valueOf(bestResult.maxCRA), 
        Long.valueOf(bestResult.bestModelHashCode), _xifexpression);
      pw.println();
      pw.println("Evaluation: CRAIndexCalculator.jar");
      pw.println("===================================");
      pw.println();
      pw.printf("Model path: %s\n", bestResult.bestModelPath);
      String _runEvaluationJarAgainstBestModel = this.runEvaluationJarAgainstBestModel(bestResult.bestModelPath);
      pw.print(_runEvaluationJarAgainstBestModel);
      pw.close();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public String runEvaluationJarAgainstBestModel(final String modelPath) {
    try {
      String _xblockexpression = null;
      {
        Runtime _runtime = Runtime.getRuntime();
        Process evaluatorJar = _runtime.exec(("java -jar evaluation/CRAIndexCalculator.jar " + modelPath));
        InputStream _inputStream = evaluatorJar.getInputStream();
        InputStreamReader _inputStreamReader = new InputStreamReader(_inputStream);
        BufferedReader _bufferedReader = new BufferedReader(_inputStreamReader);
        Stream<String> _lines = _bufferedReader.lines();
        Stream<String> _parallel = _lines.parallel();
        Collector<CharSequence, ?, String> _joining = Collectors.joining("\n");
        String output = _parallel.collect(_joining);
        int _length = output.length();
        boolean _equals = (_length == 0);
        if (_equals) {
          String _output = output;
          InputStream _errorStream = evaluatorJar.getErrorStream();
          InputStreamReader _inputStreamReader_1 = new InputStreamReader(_errorStream);
          BufferedReader _bufferedReader_1 = new BufferedReader(_inputStreamReader_1);
          Stream<String> _lines_1 = _bufferedReader_1.lines();
          Stream<String> _parallel_1 = _lines_1.parallel();
          Collector<CharSequence, ?, String> _joining_1 = Collectors.joining("\n");
          String _collect = _parallel_1.collect(_joining_1);
          output = (_output + _collect);
        }
        _xblockexpression = output;
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  /**
   * Run a single experiment and record its outcomes
   */
  public RunOptimisation.ResultRecord runOneExperiment(final String optSpecName, final RunOptimisation.InputModelDesc inputDesc, final int runIdx) {
    System.out.printf("Starting %01dth experiment run for specification \"%s\" with input model \"%s\".\n", Integer.valueOf(runIdx), optSpecName, inputDesc.modelName);
    SimpleDateFormat _simpleDateFormat = new SimpleDateFormat("yyMMdd-HHmmss");
    Date _date = new Date();
    String _format = _simpleDateFormat.format(_date);
    final String pathPrefix = ((((((("gen/models/ttc/" + optSpecName) + "/") + inputDesc.modelName) + "/") + Integer.valueOf(runIdx)) + "/") + _format);
    EObject _loadModel = this.modelLoader.loadModel((("src/uk/ac/kcl/mdeoptimiser/ecmfa2017/opt_specs/" + optSpecName) + 
      ".mopt"));
    final Optimisation model = ((Optimisation) _loadModel);
    final CRAModelProvider modelProvider = RunOptimisation.injector.<CRAModelProvider>getInstance(CRAModelProvider.class);
    modelProvider.setInputModelName(inputDesc.modelName);
    final long startTime = System.nanoTime();
    final OptimisationInterpreter interpreter = new OptimisationInterpreter(model, modelProvider);
    final Iterator<EObject> optimiserOutcome = interpreter.execute();
    final long endTime = System.nanoTime();
    final long totalTime = (endTime - startTime);
    modelProvider.storeModels(optimiserOutcome, (pathPrefix + "/final"));
    final RunOptimisation.ResultRecord results = new RunOptimisation.ResultRecord();
    final MaximiseCRA craComputer = new MaximiseCRA();
    final MinimiseClasslessFeatures featureCounter = new MinimiseClasslessFeatures();
    results.timeTaken = (totalTime / 1000000);
    boolean _isEmpty = IteratorExtensions.isEmpty(optimiserOutcome);
    if (_isEmpty) {
      InputOutput.<String>println("No valid results for this run");
    } else {
    }
    return results;
  }
  
  public Object getFeature(final EObject o, final String feature) {
    EClass _eClass = o.eClass();
    EStructuralFeature _eStructuralFeature = _eClass.getEStructuralFeature(feature);
    return o.eGet(_eStructuralFeature);
  }
  
  public void setFeature(final EObject o, final String feature, final Object value) {
    EClass _eClass = o.eClass();
    EStructuralFeature _eStructuralFeature = _eClass.getEStructuralFeature(feature);
    o.eSet(_eStructuralFeature, value);
  }
}
