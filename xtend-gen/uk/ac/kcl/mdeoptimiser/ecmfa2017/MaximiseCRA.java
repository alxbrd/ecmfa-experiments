package uk.ac.kcl.mdeoptimiser.ecmfa2017;

import com.google.common.base.Objects;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import uk.ac.kcl.mdeoptimiser.ecmfa2017.AbstractModelQueryFitnessFunction;

@SuppressWarnings("all")
public class MaximiseCRA extends AbstractModelQueryFitnessFunction {
  @Override
  public double computeFitness(final EObject model) {
    final double cohesion = this.calculateCohesionRatio(model);
    final double coupling = this.calculateCouplingRatio(model);
    InputOutput.<String>println(("Calculated CRA : " + Double.valueOf((cohesion - coupling))));
    return ((cohesion - coupling) * (-1));
  }
  
  public double calculateCohesionRatio(final EObject classModel) {
    double _xblockexpression = (double) 0;
    {
      double cohesionRatio = 0.0;
      Object _feature = this.getFeature(classModel, "classes");
      for (final EObject classObject : ((EList<EObject>) _feature)) {
        {
          final Iterable<EObject> classObjectMethods = this.getClassFeatures(classObject, "Method");
          final Iterable<EObject> classObjectAttributes = this.getClassFeatures(classObject, "Attribute");
          int _size = IterableExtensions.size(classObjectMethods);
          boolean _equals = (_size == 0);
          if (_equals) {
            double _cohesionRatio = cohesionRatio;
            cohesionRatio = (_cohesionRatio + 0.0);
          } else {
            int _size_1 = IterableExtensions.size(classObjectMethods);
            boolean _equals_1 = (_size_1 == 1);
            if (_equals_1) {
              int _size_2 = IterableExtensions.size(classObjectAttributes);
              boolean _equals_2 = (_size_2 == 0);
              if (_equals_2) {
                double _cohesionRatio_1 = cohesionRatio;
                cohesionRatio = (_cohesionRatio_1 + 0.0);
              } else {
                final int mai = this.mai(classObject, classObject);
                double _cohesionRatio_2 = cohesionRatio;
                int _size_3 = IterableExtensions.size(classObjectMethods);
                int _size_4 = IterableExtensions.size(classObjectAttributes);
                int _multiply = (_size_3 * _size_4);
                double _divide = (mai / ((double) _multiply));
                cohesionRatio = (_cohesionRatio_2 + _divide);
              }
            } else {
              int _size_5 = IterableExtensions.size(classObjectAttributes);
              boolean _equals_3 = (_size_5 == 0);
              if (_equals_3) {
                final int mmi = this.mmi(classObject, classObject);
                double _cohesionRatio_3 = cohesionRatio;
                int _size_6 = IterableExtensions.size(classObjectMethods);
                int _size_7 = IterableExtensions.size(classObjectMethods);
                int _minus = (_size_7 - 1);
                int _multiply_1 = (_size_6 * _minus);
                double _divide_1 = (mmi / ((double) _multiply_1));
                cohesionRatio = (_cohesionRatio_3 + _divide_1);
              } else {
                final int mai_1 = this.mai(classObject, classObject);
                final int mmi_1 = this.mmi(classObject, classObject);
                int _size_8 = IterableExtensions.size(classObjectMethods);
                int _size_9 = IterableExtensions.size(classObjectAttributes);
                int _multiply_2 = (_size_8 * _size_9);
                final double maCoupling = ((double) _multiply_2);
                int _size_10 = IterableExtensions.size(classObjectMethods);
                int _size_11 = IterableExtensions.size(classObjectMethods);
                int _minus_1 = (_size_11 - 1);
                int _multiply_3 = (_size_10 * _minus_1);
                final double mmCoupling = ((double) _multiply_3);
                double _cohesionRatio_4 = cohesionRatio;
                cohesionRatio = (_cohesionRatio_4 + ((mai_1 / maCoupling) + (mmi_1 / mmCoupling)));
              }
            }
          }
        }
      }
      _xblockexpression = cohesionRatio;
    }
    return _xblockexpression;
  }
  
  public double calculateCouplingRatio(final EObject classModel) {
    double _xblockexpression = (double) 0;
    {
      double couplingRatio = 0.0;
      Object _feature = this.getFeature(classModel, "classes");
      final EList<EObject> classModelClasses = ((EList<EObject>) _feature);
      for (final EObject classSource : classModelClasses) {
        double _couplingRatio = couplingRatio;
        double _calculateCouplingRatio = this.calculateCouplingRatio(classSource, classModel);
        couplingRatio = (_couplingRatio + _calculateCouplingRatio);
      }
      _xblockexpression = couplingRatio;
    }
    return _xblockexpression;
  }
  
  public double calculateCouplingRatio(final EObject classSource, final EObject classModel) {
    double _xblockexpression = (double) 0;
    {
      double couplingRatio = 0.0;
      final Iterable<EObject> sourceClassMethods = this.getClassFeatures(classSource, "Method");
      Object _feature = this.getFeature(classModel, "classes");
      for (final EObject classTarget : ((EList<EObject>) _feature)) {
        boolean _notEquals = (!Objects.equal(classSource, classTarget));
        if (_notEquals) {
          final Iterable<EObject> targetClassMethods = this.getClassFeatures(classTarget, "Method");
          final Iterable<EObject> targetClassAttributes = this.getClassFeatures(classTarget, "Attribute");
          int _size = IterableExtensions.size(sourceClassMethods);
          boolean _equals = (_size == 0);
          if (_equals) {
            double _couplingRatio = couplingRatio;
            couplingRatio = (_couplingRatio + 0.0);
          } else {
            int _size_1 = IterableExtensions.size(targetClassMethods);
            boolean _lessEqualsThan = (_size_1 <= 1);
            if (_lessEqualsThan) {
              int _size_2 = IterableExtensions.size(targetClassAttributes);
              boolean _equals_1 = (_size_2 == 0);
              if (_equals_1) {
                double _couplingRatio_1 = couplingRatio;
                couplingRatio = (_couplingRatio_1 + 0.0);
              } else {
                double _couplingRatio_2 = couplingRatio;
                int _mai = this.mai(classSource, classTarget);
                int _size_3 = IterableExtensions.size(sourceClassMethods);
                int _size_4 = IterableExtensions.size(targetClassAttributes);
                int _multiply = (_size_3 * _size_4);
                double _divide = (_mai / ((double) _multiply));
                couplingRatio = (_couplingRatio_2 + _divide);
              }
            } else {
              int _size_5 = IterableExtensions.size(targetClassAttributes);
              boolean _equals_2 = (_size_5 == 0);
              if (_equals_2) {
                double _couplingRatio_3 = couplingRatio;
                int _mmi = this.mmi(classSource, classTarget);
                int _size_6 = IterableExtensions.size(sourceClassMethods);
                int _size_7 = IterableExtensions.size(targetClassMethods);
                int _minus = (_size_7 - 1);
                int _multiply_1 = (_size_6 * _minus);
                double _divide_1 = (_mmi / ((double) _multiply_1));
                couplingRatio = (_couplingRatio_3 + _divide_1);
              } else {
                final int mai = this.mai(classSource, classTarget);
                final int mmi = this.mmi(classSource, classTarget);
                int _size_8 = IterableExtensions.size(sourceClassMethods);
                int _size_9 = IterableExtensions.size(targetClassAttributes);
                int _multiply_2 = (_size_8 * _size_9);
                final double maCoupling = ((double) _multiply_2);
                int _size_10 = IterableExtensions.size(sourceClassMethods);
                int _size_11 = IterableExtensions.size(targetClassMethods);
                int _minus_1 = (_size_11 - 1);
                int _multiply_3 = (_size_10 * _minus_1);
                final double mmCoupling = ((double) _multiply_3);
                if (((maCoupling > 0) && (mmCoupling > 0))) {
                  double _couplingRatio_4 = couplingRatio;
                  couplingRatio = (_couplingRatio_4 + ((mai / maCoupling) + (mmi / mmCoupling)));
                }
              }
            }
          }
        }
      }
      _xblockexpression = couplingRatio;
    }
    return _xblockexpression;
  }
  
  public int mai(final EObject classSource, final EObject classTarget) {
    int _xblockexpression = (int) 0;
    {
      int mai = 0;
      Iterable<EObject> _classFeatures = this.getClassFeatures(classSource, "Method");
      for (final EObject method : _classFeatures) {
        Iterable<EObject> _classFeatures_1 = this.getClassFeatures(classTarget, "Attribute");
        for (final EObject attributeTarget : _classFeatures_1) {
          {
            Object _feature = this.getFeature(method, "dataDependency");
            final EList<EObject> dataDependencies = ((EList<EObject>) _feature);
            boolean _contains = dataDependencies.contains(attributeTarget);
            if (_contains) {
              mai++;
            }
          }
        }
      }
      _xblockexpression = mai;
    }
    return _xblockexpression;
  }
  
  public int mmi(final EObject classSource, final EObject classTarget) {
    int _xblockexpression = (int) 0;
    {
      int mmi = 0;
      Iterable<EObject> _classFeatures = this.getClassFeatures(classSource, "Method");
      for (final EObject methodSource : _classFeatures) {
        Iterable<EObject> _classFeatures_1 = this.getClassFeatures(classTarget, "Method");
        for (final EObject methodTarget : _classFeatures_1) {
          {
            Object _feature = this.getFeature(methodSource, "functionalDependency");
            final EList<EObject> functionalDependencies = ((EList<EObject>) _feature);
            boolean _contains = functionalDependencies.contains(methodTarget);
            if (_contains) {
              mmi++;
            }
          }
        }
      }
      _xblockexpression = mmi;
    }
    return _xblockexpression;
  }
  
  @Override
  public String getName() {
    return "Maximise CRA";
  }
}
