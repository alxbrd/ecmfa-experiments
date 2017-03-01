package uk.ac.kcl.mdeoptimiser.ecmfa2017;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.Iterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import uk.ac.kcl.interpreter.IModelProvider;
import uk.ac.kcl.mdeoptimiser.ecmfa2017.ModelLoadHelper;

@SuppressWarnings("all")
public class CRAModelProvider implements IModelProvider {
  @Inject
  private ModelLoadHelper modelLoader;
  
  private String inputModelName;
  
  @Override
  public Iterator<EObject> initialModels(final EPackage metamodel) {
    Iterator<EObject> _xblockexpression = null;
    {
      this.modelLoader.registerPackage(metamodel);
      EObject _loadModel = this.modelLoader.loadModel((("src/uk/ac/kcl/mdeoptimiser/ecmfa2017/models/" + this.inputModelName) + ".xmi"));
      _xblockexpression = Collections.<EObject>unmodifiableList(CollectionLiterals.<EObject>newArrayList(_loadModel)).iterator();
    }
    return _xblockexpression;
  }
  
  public void storeModels(final Iterator<EObject> objects, final String path) {
    this.modelLoader.storeModels(objects, path);
  }
  
  public String setInputModelName(final String inputModelName) {
    return this.inputModelName = inputModelName;
  }
}
