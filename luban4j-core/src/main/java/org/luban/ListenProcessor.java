package org.luban;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author 鲁班大叔
 * @date 2023
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("org.luban.Listen")
public class ListenProcessor extends AbstractProcessor {
    private Messager messager;
    private JavacTrees javacTrees;
    private TreeMaker treeMaker;
    private Names names;
    private static final String GET_METHOD_PREFIX = "get";
    private static final String IS_METHOD_PREFIX = "is";
    private static final String SET_METHOD_PREFIX = "set";

    /**
     * @Description: 1. Message 主要是用来在编译时期打log用的
     * 2. JavacTrees 提供了待处理的抽象语法树
     * 3. TreeMaker 封装了创建AST节点的一些方法
     * 4. Names 提供了创建标识符的方法
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.javacTrees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Listen.class);

        elements.forEach(e -> {
            // 追加import

            CompilationUnitTree compilationUnit = javacTrees.getPath(e).getCompilationUnit();

            JCTree.JCCompilationUnit jccu = (JCTree.JCCompilationUnit) compilationUnit;
            java.util.List<JCTree> trees = new ArrayList<>();
            trees.addAll(jccu.defs);
            JCTree.JCImport jcImport = treeMaker.Import(select(ident("org.luban.monitor"), "*"), false);
            trees.add(1, jcImport);
            jccu.defs = List.from(trees);

            final JCTree classTree = javacTrees.getTree(e);

            classTree.accept(new TreeTranslator() {
                @Override
                public void visitMethodDef(JCTree.JCMethodDecl methodTree) {
                    treeMaker.pos = methodTree.pos;
                    ClassTree classDecl = (ClassTree) classTree;
                    // 只能是主类下的方法，勿略内部类
                    String methodName = methodTree.getName().toString();
                    if (isWriteMethod(classDecl, methodTree)) {// set 前缀方法
                        String fileName = methodName.substring(3);
                        fileName = fileName.replaceFirst("^.", fileName.substring(0, 1).toLowerCase());// 首字母转小写
                        String paramName = methodTree.getParameters().get(0).getName().toString();
                        JCTree.JCExpressionStatement exec2 = treeMaker.Exec(
                                treeMaker.Apply(
                                        List.nil(),
                                        select("this." + LISTENER_NAME + ".set"),
                                        List.of(
                                                select("this." + fileName),
                                                ident(paramName),
                                                ident("this"),
                                                treeMaker.Literal(fileName)
                                        ) // 方法中的内容
                                )
                        );
                        // 构建新的代码
                        methodTree.body.stats = methodTree.body.stats.append(exec2);
                    } else if (isReadMethod(classDecl, methodTree)) {// get is 前缀
                        String fileName = methodName.replaceFirst("^(get|is)", "");
                        fileName = fileName.replaceFirst("^.", fileName.substring(0, 1).toLowerCase());// 首字母转小写
                        JCTree.JCExpressionStatement exec2 = treeMaker.Exec(
                                treeMaker.Apply(
                                        List.nil(),
                                        select("this." + LISTENER_NAME + ".get"),
                                        List.of(
                                                select("this." + fileName),
                                                ident("this"),
                                                treeMaker.Literal(fileName)
                                        ) // 方法中的内容
                                )
                        );
                        // 构建新的代码
                        methodTree.body.stats = methodTree.body.stats.prepend(exec2);
                    }
                    super.visitMethodDef(methodTree);
                }

                @Override
                public void visitClassDef(JCTree.JCClassDecl tree) {
                    if (tree != classTree)
                        return;
                    treeMaker.pos = classTree.pos;
                    tree.implementing = tree.implementing.append(ident("PropertyListenerSuport"));
                    tree.defs = tree.defs.prependList(List.from(buildListenerSuport())); //追加项目
                    super.visitClassDef(tree);
                }

            });
        });
        return true;
    }

    static final String LISTENER_NAME = "$p_c_l";

    private JCTree.JCFieldAccess select(JCTree.JCExpression selected, String expressive) {
        return treeMaker.Select(selected, names.fromString(expressive));
    }

    private JCTree.JCFieldAccess select(String expressive) {
        String[] exps = expressive.split("\\.");
        JCTree.JCFieldAccess access = treeMaker.Select(ident(exps[0]), names.fromString(exps[1]));
        int index = 2;
        while (index < exps.length) {
            access = treeMaker.Select(access, names.fromString(exps[index++]));
        }
        return access;
    }

    public java.util.List<JCTree> buildListenerSuport() {
        java.util.List<JCTree> jcTrees = new ArrayList<>();
//
        // 添加变量 private PropertyListener $p_c_l;
        JCTree.JCVariableDecl newFile =
                treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE|Flags.TRANSIENT),
                        names.fromString(LISTENER_NAME),
                        ident("PropertyListener"),
                        select("PropertyListener.EMPTY")
                );
        jcTrees.add(newFile);
        // 添加设置方法
       /* @Override
        public void insertPropertyListener(PropertyListener listener) {
            this.$p_c_l=listener;
        }*/
        {
            ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
            statements.append(treeMaker.Exec(treeMaker.Assign(
                    treeMaker.Select(ident("this"),
                            names.fromString(LISTENER_NAME)),
                    ident("listener"))));
            JCTree.JCBlock body = treeMaker.Block(0, statements.toList());


            JCTree.JCMethodDecl newInsertMethod = treeMaker.MethodDef(
                    treeMaker.Modifiers(Flags.PUBLIC),//修饰符
                    names.fromString("insertPropertyListener"),//方法名
                    treeMaker.Type(new Type.JCVoidType()),//返回类型
                    List.nil(), // 注解
                    buileParams("PropertyListener", "listener"), // 参数声明
                    List.nil(),//throws
                    body,
                    null);
            jcTrees.add(newInsertMethod);
        }
        // 删除方法
        /*@Override
        public void removePropertyListener(PropertyListener listener) {
            this.$p_c_l=null;
        }*/
        {
            ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
            statements.append(treeMaker.Exec(treeMaker.Assign(
                    treeMaker.Select(ident("this"),
                            names.fromString(LISTENER_NAME)),
                    treeMaker.Literal(TypeTag.BOT, null)
            )));
            JCTree.JCBlock body = treeMaker.Block(0, statements.toList());

            JCTree.JCMethodDecl newInsertMethod = treeMaker.MethodDef(
                    treeMaker.Modifiers(Flags.PUBLIC),//修饰符
                    names.fromString("removePropertyListener"),//方法名
                    treeMaker.Type(new Type.JCVoidType()),//返回类型
                    List.nil(), // 注解
                    buileParams("PropertyListener", "listener"), // 参数声明
                    List.nil(),//throws
                    body,
                    null);
            jcTrees.add(newInsertMethod);
        }
      /*读取方法
        public PropertyListener currentPropertyListener()
        {
            return  this.$p_c_l;
        }
        */
        {
            ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
            statements.append(treeMaker.Return(select("this."+LISTENER_NAME)));
            JCTree.JCBlock body = treeMaker.Block(0, statements.toList());

            JCTree.JCMethodDecl newInsertMethod = treeMaker.MethodDef(
                    treeMaker.Modifiers(Flags.PUBLIC),//修饰符
                    names.fromString("currentPropertyListener"),//方法名
                    ident("PropertyListener"),//返回类型
                    List.nil(), // 注解
                    List.nil(), // 参数声明
                    List.nil(),//throws
                    body,
                    null);
            jcTrees.add(newInsertMethod);
        }

        return jcTrees;
    }


    private boolean isReadMethod(ClassTree classTree, MethodTree methodTree) {
        if (!classTree.getMembers().contains(methodTree)) {
            return false;
        } else if (isStatic(methodTree)) {
            return false;
        } else if (methodTree.getParameters().size() > 0) {
            return false;
        }
        String methodName = methodTree.getName().toString();
        if (!methodName.matches("^(get|is).+")) {
            return false;
        }
        //return
        String nameText = methodName.replaceFirst("^(get|is)", "");
        nameText = nameText.replaceFirst("^.", nameText.substring(0, 1).toLowerCase());// 首字母转小写
        String filename = nameText;
        java.util.List<VariableTree> files = getFiles(classTree,
                f -> !isStatic(f),
                f -> f.getName().toString().equals(filename),
                f -> f.getType().toString().equals(methodTree.getReturnType().toString()));
        return files.size() > 0;
    }

    private boolean isWriteMethod(ClassTree classTree, MethodTree methodTree) {
        if (!classTree.getMembers().contains(methodTree)) {
            return false;
        } else if (isStatic(methodTree)) {
            return false;
        } else if (methodTree.getParameters().size() != 1) {
            return false;
        }
        String methodName = methodTree.getName().toString();
        if (!methodName.matches("^set.+")) {
            return false;
        }
        // 截取set之后的字符，并且首字母转小写
        String fileName = methodName.substring(3).replaceFirst("^.", methodName.substring(3, 4).toLowerCase());
        java.util.List<VariableTree> files =
                getFiles(classTree,
                        f -> !isStatic(f),
                        f -> f.getName().toString().equals(fileName),
                        f -> methodTree.getReturnType().toString().equals("void"));
        return files.size() > 0;
    }


    @SafeVarargs
    private final java.util.List<VariableTree> getFiles(ClassTree classTree, Predicate<VariableTree>... filter) {
        Predicate<VariableTree> f = o -> true;// 合并
        for (Predicate<VariableTree> p : filter) {
            f = f.and(p);
        }

        java.util.List<VariableTree> files = classTree.getMembers().stream()
                .filter(s -> s instanceof VariableTree)
                .map(s -> (VariableTree) s)
                .filter(f)
                .collect(Collectors.toList());
        return files;
    }

    private boolean isStatic(VariableTree tree) {
        return tree.getModifiers().getFlags().contains(Modifier.STATIC);
    }

    private boolean isStatic(MethodTree tree) {
        return tree.getModifiers().getFlags().contains(Modifier.STATIC);
    }

    private boolean isStatic(ClassTree tree) {
        return tree.getModifiers().getFlags().contains(Modifier.STATIC);
    }


    private JCTree.JCIdent ident(String name) {
        return treeMaker.Ident(names.fromString(name));
    }


    private List<JCTree.JCVariableDecl> buileParams(String... inputs) {
        if ((inputs.length & 1) != 0) { // implicit nullcheck of input
            throw new InternalError("length is odd");
        }
        ListBuffer<JCTree.JCVariableDecl> listBuffer = new ListBuffer<>();
        int size = inputs.length >> 1;
        String type, name;
        JCTree.JCVariableDecl var;
        for (int i = 0; i < size; i++) {
            type = inputs[i];
            name = inputs[i + 1];
            var = treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), names.fromString(name), treeMaker.Ident(names.fromString(type)), null);
            listBuffer.add(var);
        }
        return listBuffer.toList();
    }
}
