package uk.ac.kcl.mdeoptimiser.ecmfa2017;

import com.google.common.base.Objects;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import uk.ac.kcl.mdeoptimiser.ecmfa2017.AbstractModelQueryFitnessFunction;

@SuppressWarnings("all")
public class MinimiseClasslessFeatures extends AbstractModelQueryFitnessFunction {
  @Override
  public double computeFitness(final EObject model) {
    Object _feature = this.getFeature(model, "features");
    final Function1<EObject, Boolean> _function = (EObject feature) -> {
      Object _feature_1 = this.getFeature(feature, "isEncapsulatedBy");
      return Boolean.valueOf(Objects.equal(_feature_1, null));
    };
    Iterable<EObject> _filter = IterableExtensions.<EObject>filter(((EList<EObject>) _feature), _function);
    int fitness = IterableExtensions.size(_filter);
    return fitness;
  }
  
  @Override
  public String getName() {
    return "Mimise classless features";
  }
}
