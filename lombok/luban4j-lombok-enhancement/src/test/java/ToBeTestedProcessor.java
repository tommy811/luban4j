import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

@SupportedAnnotationTypes("ToBeTested")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ToBeTestedProcessor extends AbstractProcessor {
    private void note(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        final JavacElements elementUtils = (JavacElements) processingEnv.getElementUtils();
        final TreeMaker treeMaker = TreeMaker.instance(context);
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ToBeTested.class);
        if (elements == null || elements.size() == 0) {
            return true;
        }
        for (Element element : elements) {
            ToBeTested annotation = element.getAnnotation(ToBeTested.class);
            String owner = annotation.owner();
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) elementUtils.getTree(element);
            treeMaker.pos = jcMethodDecl.pos;
            //修改AST的逻辑
            jcMethodDecl.body = treeMaker.Block(0, List.of(
                    treeMaker.Exec(
                            treeMaker.Apply(
                                    List.<JCTree.JCExpression>nil(),
                                    treeMaker.Select(
                                            treeMaker.Select(
                                                    treeMaker.Ident(
                                                            elementUtils.getName("System")
                                                    ),
                                                    elementUtils.getName("out")
                                            ),
                                            elementUtils.getName("println")
                                    ),
                                    List.<JCTree.JCExpression>of(
                                            treeMaker.Literal("Method " + element.getSimpleName() + " invoke by " + owner + ".")
                                    )
                            )
                    ),
                    jcMethodDecl.body));
        }
        return true;
    }
}
